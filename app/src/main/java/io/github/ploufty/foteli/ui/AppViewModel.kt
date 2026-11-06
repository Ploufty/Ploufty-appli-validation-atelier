package io.github.ploufty.foteli.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.ploufty.foteli.data.FoteliDatabase
import io.github.ploufty.foteli.data.PhotoStore
import io.github.ploufty.foteli.data.Referentiel
import io.github.ploufty.foteli.data.Settings
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook
import io.github.ploufty.foteli.data.Trace
import io.github.ploufty.foteli.data.Workshop
import io.github.ploufty.foteli.security.PinRules
import io.github.ploufty.foteli.security.RescueCode
import io.github.ploufty.foteli.security.Secrets
import java.io.File
import java.io.FileOutputStream
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Écrans de l'appli (parcours : docs/v0/parcours.md). */
sealed interface Screen {
    data object Loading : Screen

    // Premier lancement, et changement de PIN après « Code oublié ? » (reset = true)
    data object Welcome : Screen
    data class ChoosePin(val reset: Boolean, val error: String? = null) : Screen
    data class ConfirmPin(val first: String, val reset: Boolean) : Screen
    data class ShowRescue(val code: String, val reset: Boolean) : Screen
    data object ClassName : Screen

    // Espace élève
    data object Home : Screen
    /** L'enfant a touché son robot : il choisit son atelier (mode B). */
    data class ChooseWorkshop(val studentId: Long) : Screen
    /** Appareil photo pour l'atelier choisi (workshopId = null : photo libre, rangée dans Souvenirs). */
    data class Camera(val studentId: Long, val workshopId: Long?) : Screen
    /** Photo prise, pas encore validée : ✅ l'enregistre, ↻ l'efface. */
    data class Review(val studentId: Long, val workshopId: Long?, val file: String) : Screen
    /** Coche de réussite, puis retour à l'accueil. */
    data class Bravo(val studentId: Long) : Screen

    // Accès enseignant
    data object PinEntry : Screen
    data object RescueEntry : Screen

    // Espace enseignant
    data class Teacher(val tab: TeacherTab = TeacherTab.CLASS) : Screen
    data class EditStudent(val studentId: Long?) : Screen
    data object BulkAdd : Screen
    data class EditWorkshop(val workshopId: Long?) : Screen
    /** Qui est passé à cet atelier (workshopId = null : Souvenirs). */
    data class WorkshopPhotos(val workshopId: Long?) : Screen
    data class StudentPhotos(val studentId: Long) : Screen
    /** Une photo en grand ; [back] : l'écran d'où l'on vient. */
    data class PhotoView(val traceId: Long, val back: Screen) : Screen
}

enum class TeacherTab { CLASS, WORKSHOPS, PHOTOS, SETTINGS }

/** Onglet Photos : tableau de suivi, par atelier, ou par élève. */
enum class PhotosView(val label: String) { BOARD("Suivi"), WORKSHOP("Par atelier"), STUDENT("Par élève") }

/** Appareil photo de l'enseignant (photo du modèle), affiché par-dessus l'écran en cours. */
class TeacherCameraRequest(val onPhoto: (File) -> Unit)

/** Au-delà, les cartes deviennent trop petites pour les enfants (docs/v0/verification.md E4). */
const val MAX_COMFORTABLE_CARDS = 6

private const val CHILD_IDLE_MS = 60_000L
private const val TEACHER_IDLE_MS = 5 * 60_000L

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = FoteliDatabase.get(app).dao()
    val photos = PhotoStore(app.filesDir)
    private val collator = Collator.getInstance(Locale.FRENCH)

    var screen by mutableStateOf<Screen>(Screen.Loading)
        private set

    val settings: StateFlow<Settings?> =
        dao.settings().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val students: StateFlow<List<Student>> =
        dao.students()
            .map { list -> list.sortedWith(compareBy(collator) { it.firstName }) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val workshops: StateFlow<List<Workshop>> =
        dao.workshops().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val traces: StateFlow<List<Trace>> =
        dao.traces().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val referentiel: Referentiel by lazy { Referentiel.get(app) }

    var teacherCamera by mutableStateOf<TeacherCameraRequest?>(null)
        private set

    private var lastInteraction = System.currentTimeMillis()

    // Réglage de confort de l'enseignant (aucune donnée d'élève) : niveaux retenus pour chercher les compétences.
    private val prefs = app.getSharedPreferences("foteli-preferences", Context.MODE_PRIVATE)
    var searchLevels by mutableStateOf(prefs.getStringSet("niveaux", null)?.toSet() ?: setOf("PS", "MS", "GS"))
        private set

    /** Mode classe : écran allumé (décision T2, activé par défaut) et marche à suivre déjà lue. */
    var keepScreenOn by mutableStateOf(prefs.getBoolean("ecran_allume", true))
        private set
    var classModeKnown by mutableStateOf(prefs.getBoolean("mode_classe_explique", false))
        private set

    fun chooseKeepScreenOn(on: Boolean) {
        keepScreenOn = on
        prefs.edit().putBoolean("ecran_allume", on).apply()
    }

    fun classModeHelpSeen() {
        classModeKnown = true
        prefs.edit().putBoolean("mode_classe_explique", true).apply()
    }

    /** Captures automatiques uniquement : recherche déjà tapée à l'ouverture de l'éditeur. */
    var demoSearch: String = ""
        private set

    fun chooseSearchLevels(levels: Set<String>) {
        if (levels.isEmpty()) return
        searchLevels = levels
        prefs.edit().putStringSet("niveaux", levels).apply()
    }
    private var saving = false

    init {
        viewModelScope.launch {
            // Une photo jamais validée (appli fermée en cours de route) ne survit pas.
            withContext(Dispatchers.IO) { photos.clearPending() }
            val s = dao.settingsNow()
            if (screen == Screen.Loading) {
                screen = if (s?.setupDone == true) Screen.Home else Screen.Welcome
            }
        }
        // Retours automatiques : 1 min côté enfant, 5 min côté enseignant (désactivable).
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                val idle = System.currentTimeMillis() - lastInteraction
                when (screen) {
                    is Screen.ChooseWorkshop, is Screen.Camera, is Screen.Review, is Screen.Bravo, Screen.PinEntry, Screen.RescueEntry ->
                        if (idle > CHILD_IDLE_MS) go(Screen.Home)
                    is Screen.Teacher, is Screen.EditStudent, Screen.BulkAdd, is Screen.EditWorkshop,
                    is Screen.WorkshopPhotos, is Screen.StudentPhotos, is Screen.PhotoView ->
                        if (settings.value?.autoCloseTeacher != false && idle > TEACHER_IDLE_MS) go(Screen.Home)
                    else -> Unit
                }
            }
        }
    }

    fun touch() {
        lastInteraction = System.currentTimeMillis()
    }

    fun go(target: Screen) {
        touch()
        // Une photo non validée est effacée immédiatement (↻, retour, inactivité) : docs/v0/donnees.md § 7.
        val previous = screen
        if (previous is Screen.Review && target !is Screen.Bravo) File(previous.file).delete()
        if (target == Screen.Home) photos.clearPending()
        teacherCamera = null
        screen = target
    }

    /** Bouton retour d'Android : jamais de sortie de l'appli depuis l'espace élève. */
    fun onBack() {
        if (teacherCamera != null) {
            closeTeacherCamera()
            return
        }
        when (val s = screen) {
            is Screen.ChooseWorkshop, Screen.PinEntry, Screen.RescueEntry, is Screen.Bravo -> go(Screen.Home)
            is Screen.Camera -> go(Screen.ChooseWorkshop(s.studentId))
            is Screen.Review -> retake(s)
            is Screen.EditStudent, Screen.BulkAdd -> go(Screen.Teacher(TeacherTab.CLASS))
            is Screen.EditWorkshop -> go(Screen.Teacher(TeacherTab.WORKSHOPS))
            is Screen.WorkshopPhotos, is Screen.StudentPhotos -> go(Screen.Teacher(TeacherTab.PHOTOS))
            is Screen.PhotoView -> go(s.back)
            is Screen.Teacher -> go(Screen.Home)
            else -> Unit
        }
    }

    // ---------- Premier lancement et nouveau PIN ----------

    fun choosePin(pin: String, reset: Boolean) {
        screen = when {
            !PinRules.isWellFormed(pin) -> Screen.ChoosePin(reset, "Le code doit avoir 4 chiffres.")
            PinRules.isWeak(pin) -> Screen.ChoosePin(reset, "Code trop facile à deviner. Choisissez-en un autre.")
            else -> Screen.ConfirmPin(pin, reset)
        }
    }

    fun confirmPin(pin: String, state: Screen.ConfirmPin) {
        if (pin != state.first) {
            screen = Screen.ChoosePin(state.reset, "Les deux codes ne correspondent pas. Recommencez.")
            return
        }
        viewModelScope.launch {
            val code = RescueCode.generate()
            val (pinHash, rescueHash) = withContext(Dispatchers.Default) {
                Secrets.hash(pin) to Secrets.hash(RescueCode.normalize(code))
            }
            val current = dao.settingsNow() ?: Settings()
            dao.saveSettings(
                current.copy(pinHash = pinHash, rescueHash = rescueHash, failedPinAttempts = 0, pinLockedUntil = 0),
            )
            go(Screen.ShowRescue(code, state.reset))
        }
    }

    /** L'enseignant confirme avoir recopié son code de secours. */
    fun rescueNoted(state: Screen.ShowRescue) {
        go(if (state.reset) Screen.Teacher() else Screen.ClassName)
    }

    fun saveClassName(name: String) {
        viewModelScope.launch {
            val current = dao.settingsNow() ?: Settings()
            dao.saveSettings(current.copy(className = name.trim(), setupDone = true))
            go(Screen.Teacher(TeacherTab.CLASS))
        }
    }

    // ---------- Photos des enfants ----------

    /** La photo est prise : l'enfant la regarde avant de la valider. */
    fun photoTaken(from: Screen.Camera, file: File) {
        if (screen != from) {
            // L'enfant est déjà reparti (retour, inactivité) : la photo n'est pas gardée.
            file.delete()
            return
        }
        go(Screen.Review(from.studentId, from.workshopId, file.path))
    }

    fun retake(review: Screen.Review) {
        go(Screen.Camera(review.studentId, review.workshopId))
    }

    /** ✅ : la trace est enregistrée avec une copie des compétences de l'atelier (Q4). */
    fun validate(review: Screen.Review) {
        if (saving || screen != review) return
        saving = true
        viewModelScope.launch {
            try {
                val name = withContext(Dispatchers.IO) { photos.keepPhoto(File(review.file)) }
                if (name != null) {
                    val workshop = workshops.value.firstOrNull { it.id == review.workshopId }
                    dao.insertTrace(
                        Trace(studentId = review.studentId, workshopId = workshop?.id, file = name, competencies = workshop?.competencies.orEmpty()),
                    )
                    go(Screen.Bravo(review.studentId))
                } else {
                    go(Screen.Camera(review.studentId, review.workshopId))
                }
            } finally {
                saving = false
            }
        }
    }

    fun bravoDone(bravo: Screen.Bravo) {
        if (screen == bravo) go(Screen.Home)
    }

    /** Vue choisie dans l'onglet Photos, gardée pendant la séance. */
    var photosView by mutableStateOf(PhotosView.BOARD)

    /** Tri par l'enseignant (B7) : la photo est effacée de la tablette. */
    fun deleteTrace(trace: Trace, back: Screen) {
        viewModelScope.launch {
            dao.deleteTrace(trace.id)
            withContext(Dispatchers.IO) { photos.deletePhotos(listOf(trace.file)) }
            go(back)
        }
    }

    fun openTeacherCamera(onPhoto: (File) -> Unit) {
        touch()
        teacherCamera = TeacherCameraRequest(onPhoto)
    }

    fun closeTeacherCamera() {
        touch()
        teacherCamera = null
    }

    // ---------- Accès enseignant ----------

    /** Vérifie le PIN. Renvoie un message d'erreur, ou null si le code est juste. */
    suspend fun tryPin(pin: String): String? {
        val s = dao.settingsNow() ?: return "Réglages introuvables."
        if (s.pinLockedUntil > System.currentTimeMillis()) return null
        val ok = withContext(Dispatchers.Default) { Secrets.verify(pin, s.pinHash) }
        if (ok) {
            dao.saveSettings(s.copy(failedPinAttempts = 0, pinLockedUntil = 0))
            go(Screen.Teacher())
            return null
        }
        recordFailure(s)
        return "Code incorrect."
    }

    /** Code de secours : mêmes règles de blocage que le PIN (essais faux cumulés). */
    suspend fun tryRescue(input: String): String? {
        val s = dao.settingsNow() ?: return "Réglages introuvables."
        if (s.pinLockedUntil > System.currentTimeMillis()) return null
        val ok = withContext(Dispatchers.Default) { Secrets.verify(RescueCode.normalize(input), s.rescueHash) }
        if (ok) {
            dao.saveSettings(s.copy(failedPinAttempts = 0, pinLockedUntil = 0))
            go(Screen.ChoosePin(reset = true))
            return null
        }
        recordFailure(s)
        return "Code de secours incorrect."
    }

    private suspend fun recordFailure(s: Settings) {
        val failed = s.failedPinAttempts + 1
        val lock = PinRules.lockDurationMillis(failed)
        dao.saveSettings(
            s.copy(
                failedPinAttempts = failed,
                pinLockedUntil = if (lock > 0) System.currentTimeMillis() + lock else s.pinLockedUntil,
            ),
        )
    }

    suspend fun checkPin(pin: String): Boolean {
        val s = dao.settingsNow() ?: return false
        return withContext(Dispatchers.Default) { Secrets.verify(pin, s.pinHash) }
    }

    // ---------- Classe ----------

    fun freeRobots(exceptStudentId: Long? = null): List<Int> {
        val used = students.value.filter { it.look == StudentLook.ROBOT && it.id != exceptStudentId }.map { it.robot }.toSet()
        return (0 until Robots.COUNT).filter { it !in used }
    }

    fun saveStudent(id: Long?, firstName: String, look: StudentLook, robot: Int) {
        val name = firstName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            val existing = students.value.firstOrNull { it.id == id }
            if (existing != null) {
                dao.updateStudent(existing.copy(firstName = name, look = look, robot = robot))
            } else {
                dao.insertStudents(listOf(Student(firstName = name, look = look, robot = robot)))
            }
            go(Screen.Teacher(TeacherTab.CLASS))
        }
    }

    /** Ajout groupé : un prénom par ligne, un robot libre attribué à chacun. */
    fun addStudents(text: String) {
        val names = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (names.isEmpty()) return
        viewModelScope.launch {
            val free = freeRobots().toMutableList()
            val list = names.map { name ->
                val robot = free.removeFirstOrNull()
                if (robot != null) {
                    Student(firstName = name, look = StudentLook.ROBOT, robot = robot)
                } else {
                    Student(firstName = name, look = StudentLook.NAME, robot = 0)
                }
            }
            dao.insertStudents(list)
            go(Screen.Teacher(TeacherTab.CLASS))
        }
    }

    fun deleteStudent(id: Long) {
        viewModelScope.launch {
            val files = dao.traceFilesOfStudent(id)
            dao.deleteTracesOfStudent(id)
            withContext(Dispatchers.IO) { photos.deletePhotos(files) }
            dao.deleteStudent(id)
            go(Screen.Teacher(TeacherTab.CLASS))
        }
    }

    // ---------- Réglages ----------

    fun renameClass(name: String) {
        viewModelScope.launch {
            val current = dao.settingsNow() ?: return@launch
            dao.saveSettings(current.copy(className = name.trim()))
        }
    }

    fun setAutoClose(enabled: Boolean) {
        viewModelScope.launch {
            val current = dao.settingsNow() ?: return@launch
            dao.saveSettings(current.copy(autoCloseTeacher = enabled))
        }
    }

    fun setFreeMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = dao.settingsNow() ?: return@launch
            dao.saveSettings(current.copy(freeMode = enabled))
        }
    }

    fun setFreeModeFrontCamera(front: Boolean) {
        viewModelScope.launch {
            val current = dao.settingsNow() ?: return@launch
            dao.saveSettings(current.copy(freeModeFrontCamera = front))
        }
    }

    /** « Tout effacer » : élèves, ateliers et photos sont supprimés ; PIN, code de secours et réglages restent. */
    fun wipeClass() {
        viewModelScope.launch {
            dao.deleteAllTraces()
            dao.deleteAllStudents()
            dao.deleteAllWorkshops()
            withContext(Dispatchers.IO) { photos.deleteAll() }
            go(Screen.Teacher(TeacherTab.CLASS))
        }
    }

    // ---------- Ateliers ----------

    /** Enregistre l'atelier. [activate] : « Enregistrer et activer ». */
    fun saveWorkshop(id: Long?, title: String, image: String, competencies: List<String>, frontCamera: Boolean, activate: Boolean) {
        val name = title.trim()
        if (name.isEmpty() || competencies.isEmpty()) return
        viewModelScope.launch {
            val existing = workshops.value.firstOrNull { it.id == id }
            val finalImage = withContext(Dispatchers.IO) { photos.keepImage(image) }
            if (existing != null) {
                dao.updateWorkshop(
                    existing.copy(title = name, image = finalImage, competencies = competencies, frontCamera = frontCamera, active = existing.active || activate),
                )
                if (existing.image != finalImage) withContext(Dispatchers.IO) { photos.deleteImage(existing.image) }
            } else {
                dao.insertWorkshop(Workshop(title = name, image = finalImage, competencies = competencies, frontCamera = frontCamera, active = activate))
            }
            go(Screen.Teacher(TeacherTab.WORKSHOPS))
        }
    }

    fun setWorkshopActive(id: Long, active: Boolean) {
        viewModelScope.launch { dao.setWorkshopActive(id, active) }
    }

    fun deactivateAllWorkshops() {
        viewModelScope.launch { dao.deactivateAllWorkshops() }
    }

    /** Puzzle 6 → Puzzle 12 : copie inactive, ouverte aussitôt pour la modifier. */
    fun duplicateWorkshop(id: Long) {
        val source = workshops.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            val image = withContext(Dispatchers.IO) { photos.copyImage(source.image) }
            val newId = dao.insertWorkshop(
                source.copy(id = 0, title = "${source.title} (copie)", image = image, active = false, createdAt = System.currentTimeMillis()),
            )
            go(Screen.EditWorkshop(newId))
        }
    }

    fun deleteWorkshop(id: Long) {
        val workshop = workshops.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            val files = dao.traceFilesOfWorkshop(id)
            dao.deleteTracesOfWorkshop(id)
            withContext(Dispatchers.IO) {
                photos.deletePhotos(files)
                photos.deleteImage(workshop.image)
            }
            dao.deleteWorkshop(id)
            go(Screen.Teacher(TeacherTab.WORKSHOPS))
        }
    }

    // ---------- Démonstration (versions de test uniquement, pour les captures automatiques) ----------

    fun seedDemo(target: String?) {
        viewModelScope.launch {
            if (dao.settingsNow()?.setupDone != true) {
                val pin = withContext(Dispatchers.Default) { Secrets.hash("1234") }
                dao.saveSettings(Settings(className = "MS-GS Démo 2026-2027", pinHash = pin, rescueHash = pin, setupDone = true))
                val names = listOf("Adem", "Amir", "Assia", "Ava", "Eliott", "Inès", "Jade", "Lina", "Malo", "Nelia", "Noah", "Sacha")
                dao.insertStudents(
                    names.mapIndexed { i, n ->
                        if (n == "Jade") Student(firstName = n, look = StudentLook.NAME, robot = 0)
                        else Student(firstName = n, look = StudentLook.ROBOT, robot = (i * 7) % Robots.COUNT)
                    },
                )
                dao.saveSettings((dao.settingsNow() ?: Settings()).copy(freeMode = true))
                listOf(
                    Workshop(title = "Puzzle 12 pièces", image = "puzzle", competencies = listOf("MAT-FOR-MS-04"), active = true),
                    Workshop(title = "Construction Kapla", image = "kapla", competencies = listOf("MAT-FOR-MS-03", "TES-ESP-GS-01"), active = true),
                    Workshop(title = "Collier de perles", image = "perles", competencies = listOf("MAT-MOT-MS-04"), active = true),
                    Workshop(title = "Spirales", image = "graphisme", competencies = listOf("ART-GRA-MS-02"), active = false),
                ).forEach { dao.insertWorkshop(it) }
                // Quelques photos factices (dessins) pour montrer les compteurs.
                val kids = dao.studentsNow().sortedWith(compareBy(collator) { it.firstName })
                val ws = dao.workshopsNow()
                listOf(0 to 0, 1 to 0, 2 to 0, 3 to 1, 4 to 1, 0 to 2, 5 to null).forEachIndexed { i, (k, w) ->
                    val file = photos.newPending()
                    withContext(Dispatchers.IO) { drawDemoPhoto(file, i) }
                    val workshop = w?.let { ws[it] }
                    photos.keepPhoto(file)?.let { name ->
                        dao.insertTrace(Trace(studentId = kids[k].id, workshopId = workshop?.id, file = name, competencies = workshop?.competencies.orEmpty()))
                    }
                }
            }
            val firstStudent = dao.studentsNow().minWith(compareBy(collator) { it.firstName }).id
            val ws = dao.workshopsNow()
            go(
                when (target) {
                    "teacher" -> Screen.Teacher(TeacherTab.CLASS)
                    "edit" -> Screen.EditStudent(null)
                    "workshops" -> Screen.Teacher(TeacherTab.WORKSHOPS)
                    "edit-workshop" -> Screen.EditWorkshop(ws.getOrNull(1)?.id)
                    "search" -> {
                        demoSearch = "kapla"
                        Screen.EditWorkshop(null)
                    }
                    "choose" -> Screen.ChooseWorkshop(firstStudent)
                    "camera" -> Screen.Camera(firstStudent, ws.first().id)
                    "review" -> {
                        val file = photos.newPending()
                        withContext(Dispatchers.IO) { drawDemoPhoto(file, 1) }
                        Screen.Review(firstStudent, ws.first().id, file.path)
                    }
                    "bravo" -> Screen.Bravo(firstStudent)
                    "photos" -> Screen.Teacher(TeacherTab.PHOTOS)
                    "photos-board" -> Screen.Teacher(TeacherTab.PHOTOS)
                    "photos-workshop" -> Screen.WorkshopPhotos(ws.first().id)
                    "photo" -> Screen.PhotoView(dao.tracesNow().first().id, Screen.WorkshopPhotos(ws.first().id))
                    "pin" -> Screen.PinEntry
                    else -> Screen.Home
                },
            )
        }
    }

    /** Dessin coloré qui tient lieu de photo dans la classe de démonstration. */
    private fun drawDemoPhoto(file: File, seed: Int) {
        val bitmap = Bitmap.createBitmap(1600, 1200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFC9A77C.toInt())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colors = listOf(0xFF4DABF7, 0xFFFFD43B, 0xFFFF6B6B, 0xFF69DB7C, 0xFFDA77F2).map { it.toInt() }
        val random = java.util.Random(seed.toLong())
        repeat(9) { k ->
            paint.color = colors[(k + seed) % colors.size]
            val x = 250f + random.nextInt(1100)
            val y = 200f + random.nextInt(800)
            if (k % 2 == 0) canvas.drawCircle(x, y, 90f + random.nextInt(60), paint)
            else canvas.drawRoundRect(x - 120, y - 80, x + 120, y + 80, 24f, 24f, paint)
        }
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        bitmap.recycle()
    }
}
