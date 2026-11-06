package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Settings
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook
import io.github.ploufty.foteli.data.Trace
import kotlinx.coroutines.launch

/** Cadre de l'espace enseignant : barre du haut, onglets, bouton « Mode élève ». */
@Composable
fun TeacherFrame(settings: Settings?, tab: TeacherTab?, vm: AppViewModel, scroll: Boolean = true, content: @Composable () -> Unit) {
    val focus = LocalFocusManager.current
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F8))
            // Toucher à côté d'un champ referme le clavier.
            .pointerInput(Unit) { detectTapGestures { focus.clearFocus() } },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                settings?.className.orEmpty(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            TabButton("Classe", tab == TeacherTab.CLASS) { vm.go(Screen.Teacher(TeacherTab.CLASS)) }
            TabButton("Ateliers", tab == TeacherTab.WORKSHOPS) { vm.go(Screen.Teacher(TeacherTab.WORKSHOPS)) }
            TabButton("Photos · 0.5", selected = false, enabled = false) {}
            TabButton("Réglages", tab == TeacherTab.SETTINGS) { vm.go(Screen.Teacher(TeacherTab.SETTINGS)) }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Yellow)
                    .clickable { vm.go(Screen.Home) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) { Text("Mode élève", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
        if (scroll) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CameraBanner()
                content()
            }
        } else {
            // L'écran gère lui-même ses zones qui défilent (éditeur d'atelier en deux colonnes).
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CameraBanner()
                Box(Modifier.weight(1f).fillMaxWidth()) { content() }
            }
        }
    }
}

/** Sans l'appareil photo, les enfants ne peuvent rien faire : l'enseignant le voit et le règle en un geste. */
@Composable
private fun CameraBanner() {
    val access = LocalCameraAccess.current
    if (access.allowed) return
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF6DC))
            .border(1.dp, Yellow, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("L’appareil photo n’est pas autorisé", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Hint("Sans lui, les enfants ne peuvent pas prendre de photos (l’accueil affiche la lune). Foteli n’utilise l’appareil photo que dans l’appli : les photos ne vont jamais dans la galerie.")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Autoriser l’appareil photo", access.request)
            SecondaryButton("Rien ne s’affiche ? Ouvrir les réglages Android", access.openSystemSettings)
        }
    }
}

/** « 1 photo », « 3 photos ». */
internal fun photos(n: Int) = if (n > 1) "$n photos" else "$n photo"

@Composable
private fun TabButton(text: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text,
            color = when {
                selected -> Navy
                enabled -> Color(0xFFC9D4EA)
                else -> Color(0xFF6E7C99)
            },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

@Composable
internal fun Heading(text: String) = Text(text, color = Navy, fontWeight = FontWeight.Bold, fontSize = 22.sp)

@Composable
internal fun Hint(text: String) = Text(text, color = Muted, fontSize = 15.sp)

@Composable
internal fun WhitePanel(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFDDE3EC), RoundedCornerShape(14.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

// ---------- Classe ----------

@Composable
fun ClassTab(students: List<Student>, traces: List<Trace>, vm: AppViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading("Élèves (${students.size})")
        Spacer(Modifier.weight(1f))
        PrimaryButton("+ Ajouter un élève", { vm.go(Screen.EditStudent(null)) })
        SecondaryButton("+ Ajouter plusieurs élèves", { vm.go(Screen.BulkAdd) })
    }
    if (students.isEmpty()) {
        Hint("Commencez par « Ajouter plusieurs élèves » : un prénom par ligne, un robot est attribué automatiquement à chacun.")
    }
    students.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { s ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFDDE3EC), RoundedCornerShape(14.dp))
                        .clickable { vm.go(Screen.EditStudent(s.id)) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StudentFace(s, 56.dp)
                    Column(Modifier.weight(1f)) {
                        Text(s.firstName, color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Hint("${if (s.look == StudentLook.ROBOT) "robot" else "prénom en grand"} · ${photos(traces.count { it.studentId == s.id })}")
                    }
                    Text("Modifier", color = Blue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
fun EditStudentScreen(student: Student?, traces: List<Trace>, vm: AppViewModel) {
    val initialRobot = remember(student) { student?.robot ?: vm.freeRobots().firstOrNull() ?: 0 }
    var name by remember(student) { mutableStateOf(student?.firstName.orEmpty()) }
    var look by remember(student) { mutableStateOf(student?.look ?: StudentLook.ROBOT) }
    var robot by remember(student) { mutableIntStateOf(initialRobot) }
    val free = remember(student) { vm.freeRobots(exceptStudentId = student?.id).toSet() }
    val focus = LocalFocusManager.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton("← Classe", { vm.go(Screen.Teacher(TeacherTab.CLASS)) })
        Heading(if (student == null) "Ajouter un élève" else "Modifier ${student.firstName}")
    }
    WhitePanel {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Prénom") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            modifier = Modifier.width(420.dp),
        )
        Text("Sur l’accueil, l’élève est représenté par", color = Navy, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Un robot", look == StudentLook.ROBOT) { look = StudentLook.ROBOT }
            Chip("Son prénom en grand", look == StudentLook.NAME) { look = StudentLook.NAME }
        }
        if (look == StudentLook.ROBOT) {
            Text("Choisir un robot (ceux déjà donnés sont grisés)", color = Navy, fontWeight = FontWeight.Bold)
            (0 until Robots.COUNT).chunked(12).forEach { line ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    line.forEach { i ->
                        val available = i in free
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (robot == i) Color(0xFFE3EBFB) else Color(0xFFF3F5F8))
                                .border(2.dp, if (robot == i) Blue else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable(enabled = available) { robot = i }
                                .padding(4.dp),
                        ) {
                            RobotAvatar(i, Modifier.fillMaxSize().alpha(if (available) 1f else 0.2f))
                        }
                    }
                }
            }
        } else if (name.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Hint("Aperçu :")
                StudentFace(Student(firstName = name.trim(), look = StudentLook.NAME, robot = 0), 120.dp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Enregistrer", { vm.saveStudent(student?.id, name, look, robot) }, enabled = name.isNotBlank())
        }
    }
    if (student != null) {
        var confirm by remember(student) { mutableStateOf("") }
        val own = traces.filter { it.studentId == student.id }
        val inWorkshops = own.mapNotNull { it.workshopId }.distinct().size
        DangerZone {
            Text(
                if (own.isEmpty()) {
                    "Supprimer ${student.firstName} : aucune photo de cet élève pour l’instant. C’est définitif."
                } else {
                    "Supprimer ${student.firstName} efface aussi ${if (own.size > 1) "ses" else "sa"} ${photos(own.size)}" +
                        (if (inWorkshops > 0) " (dans $inWorkshops atelier${if (inWorkshops > 1) "s" else ""})" else "") +
                        ". C’est définitif."
                },
                color = Navy,
                fontSize = 16.sp,
            )
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Pour confirmer, tapez le prénom « ${student.firstName} »") },
                singleLine = true,
                modifier = Modifier.width(480.dp),
            )
            DangerButton("Supprimer ${student.firstName}", { vm.deleteStudent(student.id) }, enabled = confirm.trim() == student.firstName)
        }
    }
}

@Composable
fun BulkAddScreen(vm: AppViewModel) {
    var text by remember { mutableStateOf("") }
    val count = text.lines().count { it.isNotBlank() }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton("← Classe", { vm.go(Screen.Teacher(TeacherTab.CLASS)) })
        Heading("Ajouter plusieurs élèves")
    }
    WhitePanel {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Un prénom par ligne") },
            modifier = Modifier
                .width(420.dp)
                .height(260.dp),
        )
        Hint("Chaque élève reçoit automatiquement un robot différent. Vous pourrez ensuite le changer ou choisir le prénom en grand.")
        PrimaryButton(if (count > 1) "Ajouter ces $count élèves" else "Ajouter", { vm.addStudents(text) }, enabled = count > 0)
    }
}

// ---------- Réglages ----------

@Composable
fun SettingsTab(settings: Settings?, students: List<Student>, workshopCount: Int, traces: List<Trace>, vm: AppViewModel) {
    val scope = rememberCoroutineScope()
    var className by remember { mutableStateOf(settings?.className.orEmpty()) }
    LaunchedEffect(settings?.className) { className = settings?.className.orEmpty() }

    Heading("Réglages")
    WhitePanel {
        Text("Nom de la classe", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Hint("En-tête des extractions de photos et nom des sauvegardes.")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = className, onValueChange = { className = it }, singleLine = true, modifier = Modifier.width(420.dp))
            SecondaryButton("Enregistrer", { vm.renameClass(className) })
        }
    }
    WhitePanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Mode libre (Souvenirs)", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Hint("Ajoute une carte « Photo libre » pour les enfants : ils photographient ce qu’ils veulent, sans compétence. Pensez à le désactiver pendant les séances d’ateliers.")
                Hint("Souvenirs déjà pris : ${photos(traces.count { it.workshopId == null })}.")
            }
            Switch(checked = settings?.freeMode == true, onCheckedChange = { vm.setFreeMode(it) })
        }
        if (settings?.freeMode == true) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Hint("Caméra :")
                Chip("Arrière", settings?.freeModeFrontCamera != true) { vm.setFreeModeFrontCamera(false) }
                Chip("Avant", settings?.freeModeFrontCamera == true) { vm.setFreeModeFrontCamera(true) }
            }
        }
    }
    WhitePanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Fermeture automatique de l’espace enseignant", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Hint("Retour au mode élève après 5 minutes sans action.")
            }
            Switch(checked = settings?.autoCloseTeacher != false, onCheckedChange = { vm.setAutoClose(it) })
        }
    }

    var word by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    DangerZone {
        Text(
            "Tout effacer supprime toute la classe : ${students.size} élève${if (students.size > 1) "s" else ""}, " +
                "$workshopCount atelier${if (workshopCount > 1) "s" else ""} et ${photos(traces.size)}. " +
                "Le code PIN et les réglages sont conservés. Rien ne pourra être récupéré sans sauvegarde.",
            color = Navy,
            fontSize = 16.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = word, onValueChange = { word = it }, label = { Text("Tapez EFFACER") }, singleLine = true, modifier = Modifier.width(240.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(4) },
                label = { Text("Code PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.width(180.dp),
            )
        }
        if (error != null) Text(error!!, color = Red, fontWeight = FontWeight.Bold)
        DangerButton("Tout effacer", {
            scope.launch {
                if (vm.checkPin(pin)) {
                    word = ""
                    pin = ""
                    error = null
                    vm.wipeClass()
                } else {
                    error = "Code PIN incorrect."
                }
            }
        }, enabled = word.trim() == "EFFACER" && pin.length == 4)
    }
}
