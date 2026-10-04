package io.github.ploufty.foteli.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.ploufty.foteli.data.FoteliDatabase
import io.github.ploufty.foteli.data.Referentiel
import io.github.ploufty.foteli.data.Settings
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook
import io.github.ploufty.foteli.data.Workshop
import io.github.ploufty.foteli.security.PinRules
import io.github.ploufty.foteli.security.RescueCode
import io.github.ploufty.foteli.security.Secrets
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
    /** Atelier choisi (workshopId = null : photo libre). L'appareil photo arrive en 0.4. */
    data class CameraSoon(val studentId: Long, val workshopId: Long?) : Screen

    // Accès enseignant
    data object PinEntry : Screen
    data object RescueEntry : Screen

    // Espace enseignant
    data class Teacher(val tab: TeacherTab = TeacherTab.CLASS) : Screen
    data class EditStudent(val studentId: Long?) : Screen
    data object BulkAdd : Screen
    data class EditWorkshop(val workshopId: Long?) : Screen
}

enum class TeacherTab { CLASS, WORKSHOPS, SETTINGS }

/** Au-delà, les cartes deviennent trop petites pour les enfants (docs/v0/verification.md E4). */
const val MAX_COMFORTABLE_CARDS = 6

private const val CHILD_IDLE_MS = 60_000L
private const val TEACHER_IDLE_MS = 5 * 60_000L

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = FoteliDatabase.get(app).dao()
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

    val referentiel: Referentiel by lazy { Referentiel.get(app) }

    private var lastInteraction = System.currentTimeMillis()

    init {
        viewModelScope.launch {
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
                    is Screen.ChooseWorkshop, is Screen.CameraSoon, Screen.PinEntry, Screen.RescueEntry ->
                        if (idle > CHILD_IDLE_MS) go(Screen.Home)
                    is Screen.Teacher, is Screen.EditStudent, Screen.BulkAdd, is Screen.EditWorkshop ->
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
        screen = target
    }

    /** Bouton retour d'Android : jamais de sortie de l'appli depuis l'espace élève. */
    fun onBack() {
        when (val s = screen) {
            is Screen.ChooseWorkshop, Screen.PinEntry, Screen.RescueEntry -> go(Screen.Home)
            is Screen.CameraSoon -> go(Screen.ChooseWorkshop(s.studentId))
            is Screen.EditStudent, Screen.BulkAdd -> go(Screen.Teacher(TeacherTab.CLASS))
            is Screen.EditWorkshop -> go(Screen.Teacher(TeacherTab.WORKSHOPS))
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

    /** « Tout effacer » : élèves et ateliers sont supprimés ; PIN, code de secours et réglages restent. */
    fun wipeClass() {
        viewModelScope.launch {
            dao.deleteAllStudents()
            dao.deleteAllWorkshops()
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
            if (existing != null) {
                dao.updateWorkshop(
                    existing.copy(title = name, image = image, competencies = competencies, frontCamera = frontCamera, active = existing.active || activate),
                )
            } else {
                dao.insertWorkshop(Workshop(title = name, image = image, competencies = competencies, frontCamera = frontCamera, active = activate))
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
            val newId = dao.insertWorkshop(source.copy(id = 0, title = "${source.title} (copie)", active = false, createdAt = System.currentTimeMillis()))
            go(Screen.EditWorkshop(newId))
        }
    }

    fun deleteWorkshop(id: Long) {
        viewModelScope.launch {
            dao.deleteWorkshop(id)
            go(Screen.Teacher(TeacherTab.WORKSHOPS))
        }
    }

    private suspend fun workshopIdForDemo(): Long? {
        var list = workshops.value
        var tries = 0
        while (list.size < 2 && tries < 20) { delay(100); list = workshops.value; tries++ }
        return list.getOrNull(1)?.id
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
            }
            go(
                when (target) {
                    "teacher" -> Screen.Teacher(TeacherTab.CLASS)
                    "edit" -> Screen.EditStudent(null)
                    "workshops" -> Screen.Teacher(TeacherTab.WORKSHOPS)
                    "edit-workshop" -> Screen.EditWorkshop(workshopIdForDemo())
                    "choose" -> Screen.ChooseWorkshop(dao.studentsNow().first().id)
                    "pin" -> Screen.PinEntry
                    else -> Screen.Home
                },
            )
        }
    }
}
