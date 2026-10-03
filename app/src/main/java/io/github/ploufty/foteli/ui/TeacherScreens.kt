package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Settings
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook
import io.github.ploufty.foteli.data.ThemeMode
import kotlinx.coroutines.launch

private val tabs = listOf(
    "👧 Classe" to TeacherTab.CLASS,
    "🎨 Ateliers · 0.3" to null,
    "📷 Photos · 0.4" to null,
    "⚙️ Réglages" to TeacherTab.SETTINGS,
)

/** Cadre de l'espace enseignant : en-tête, onglets en pastille, bouton « Mode élève ». */
@Composable
fun TeacherFrame(settings: Settings?, tab: TeacherTab?, vm: AppViewModel, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp)) {
            val title: @Composable (Modifier) -> Unit = { m ->
                Column(m) {
                    Text("Espace enseignant", color = Palette.muted, fontSize = 14.sp)
                    Text(
                        settings?.className.orEmpty(),
                        color = Palette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            val tabBar: @Composable (Modifier) -> Unit = { m ->
                Segmented(
                    items = tabs.map { it.first },
                    selected = if (tab == null) -1 else tabs.indexOfFirst { it.second == tab },
                    modifier = m,
                    enabled = { tabs[it].second != null },
                ) { i -> tabs[i].second?.let { vm.go(Screen.Teacher(it)) } }
            }
            val studentMode: @Composable () -> Unit = {
                Box(
                    Modifier
                        .pressable({ vm.go(Screen.Home) })
                        .clip(RoundedCornerShape(50))
                        .background(Palette.accent)
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("🧒 Mode élève", color = Palette.onAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
            if (maxWidth > 1100.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    title(Modifier.weight(1f))
                    tabBar(Modifier)
                    studentMode()
                }
            } else {
                // Écran plus étroit (téléphone) : les onglets passent sur une seconde ligne.
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        title(Modifier.weight(1f))
                        studentMode()
                    }
                    tabBar(Modifier.horizontalScroll(rememberScrollState()))
                }
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                Modifier
                    .widthIn(max = 1100.dp)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) { content() }
        }
    }
}

@Composable
private fun Heading(text: String) = Text(text, color = Palette.text, fontWeight = FontWeight.Bold, fontSize = 24.sp)

@Composable
private fun Label(text: String) = Text(text, color = Palette.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)

@Composable
private fun Hint(text: String) = Text(text, color = Palette.muted, fontSize = 15.sp)

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .panel()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

@Composable
private fun BackRow(title: String, vm: AppViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        RoundButton("←", "Retour à la classe", size = 52.dp) { vm.go(Screen.Teacher(TeacherTab.CLASS)) }
        Heading(title)
    }
}

// ---------- Classe ----------

@Composable
fun ClassTab(students: List<Student>, vm: AppViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading("Élèves")
        Text(
            "${students.size}",
            color = Palette.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Palette.primary)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
        Spacer(Modifier.weight(1f))
        SecondaryButton("+ Plusieurs élèves", { vm.go(Screen.BulkAdd) })
        PrimaryButton("+ Ajouter un élève", { vm.go(Screen.EditStudent(null)) })
    }
    if (students.isEmpty()) {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                RobotAvatar(13, Modifier.size(88.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Label("Votre classe est vide")
                    Hint("Le plus rapide : « Plusieurs élèves », un prénom par ligne. Un robot est attribué automatiquement à chacun.")
                }
                PrimaryButton("Commencer", { vm.go(Screen.BulkAdd) })
            }
        }
    }
    students.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { s ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .pressable({ vm.go(Screen.EditStudent(s.id)) })
                        .panel(18.dp)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HaloFace(s, 60.dp)
                    Column(Modifier.weight(1f)) {
                        Text(s.firstName, color = Palette.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Hint(if (s.look == StudentLook.ROBOT) "robot" else "prénom en grand")
                    }
                    Text("Modifier ›", color = Palette.link, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
fun EditStudentScreen(student: Student?, vm: AppViewModel) {
    val initialRobot = remember(student) { student?.robot ?: vm.freeRobots().firstOrNull() ?: 0 }
    var name by remember(student) { mutableStateOf(student?.firstName.orEmpty()) }
    var look by remember(student) { mutableStateOf(student?.look ?: StudentLook.ROBOT) }
    var robot by remember(student) { mutableIntStateOf(initialRobot) }
    val free = remember(student) { vm.freeRobots(exceptStudentId = student?.id).toSet() }

    BackRow(if (student == null) "Ajouter un élève" else "Modifier ${student.firstName}", vm)
    Panel {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Prénom") },
                    singleLine = true,
                    modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                )
                Label("Sur l’accueil, l’élève est représenté par")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("Un robot", look == StudentLook.ROBOT) { look = StudentLook.ROBOT }
                    Chip("Son prénom en grand", look == StudentLook.NAME) { look = StudentLook.NAME }
                }
                Hint("Sa photo : disponible avec l’appareil photo (version 0.4).")
            }
            // Aperçu en direct, tel qu'il apparaîtra sur l'accueil.
            if (name.isNotBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Hint("Aperçu")
                    HaloFace(Student(firstName = name.trim(), look = look, robot = robot), 120.dp)
                }
            }
        }
        if (look == StudentLook.ROBOT) {
            Label("Choisir un robot (ceux déjà donnés sont grisés)")
            (0 until Robots.COUNT).chunked(12).forEach { line ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    line.forEach { i ->
                        val available = i in free
                        val chosen = robot == i
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (chosen) Palette.primarySoft else Palette.cardAlt)
                                .border(if (chosen) 3.dp else 0.dp, if (chosen) Palette.primary else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable(enabled = available, role = Role.RadioButton) { robot = i }
                                .padding(4.dp),
                        ) {
                            RobotAvatar(i, Modifier.fillMaxSize().alpha(if (available) 1f else 0.2f))
                        }
                    }
                }
            }
        }
        PrimaryButton("Enregistrer", { vm.saveStudent(student?.id, name, look, robot) }, enabled = name.isNotBlank())
    }
    if (student != null) {
        var confirm by remember(student) { mutableStateOf("") }
        DangerZone {
            Text(
                "Supprimer ${student.firstName} efface aussi toutes ses photos. C’est définitif.",
                color = Palette.text,
                fontSize = 16.sp,
            )
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Pour confirmer, tapez le prénom « ${student.firstName} »") },
                singleLine = true,
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            )
            DangerButton("Supprimer ${student.firstName}", { vm.deleteStudent(student.id) }, enabled = confirm.trim() == student.firstName)
        }
    }
}

@Composable
fun BulkAddScreen(vm: AppViewModel) {
    var text by remember { mutableStateOf("") }
    val count = text.lines().count { it.isNotBlank() }
    BackRow("Ajouter plusieurs élèves", vm)
    Panel {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Un prénom par ligne") },
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .height(260.dp),
        )
        Hint("Chaque élève reçoit automatiquement un robot différent. Vous pourrez ensuite le changer ou choisir le prénom en grand.")
        PrimaryButton(if (count > 1) "Ajouter ces $count élèves" else "Ajouter", { vm.addStudents(text) }, enabled = count > 0)
    }
}

// ---------- Réglages ----------

@Composable
fun SettingsTab(settings: Settings?, students: List<Student>, vm: AppViewModel) {
    val scope = rememberCoroutineScope()
    var className by remember { mutableStateOf(settings?.className.orEmpty()) }
    LaunchedEffect(settings?.className) { className = settings?.className.orEmpty() }

    Heading("Réglages")
    Panel {
        Label("Apparence")
        Hint("S’applique à toute l’appli, côté élèves comme côté enseignant. « Système » suit le réglage de la tablette.")
        val mode = settings?.themeMode ?: ThemeMode.SYSTEM
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeOption("📱", "Système", "Comme la tablette", null, mode == ThemeMode.SYSTEM, Modifier.weight(1f)) { vm.setThemeMode(ThemeMode.SYSTEM) }
            ThemeOption("☀️", "Jour", "Toujours clair", LightColors, mode == ThemeMode.LIGHT, Modifier.weight(1f)) { vm.setThemeMode(ThemeMode.LIGHT) }
            ThemeOption("🌙", "Nuit", "Toujours sombre", DarkColors, mode == ThemeMode.DARK, Modifier.weight(1f)) { vm.setThemeMode(ThemeMode.DARK) }
        }
    }
    Panel {
        Label("Nom de la classe")
        Hint("En-tête des extractions de photos et nom des sauvegardes.")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                singleLine = true,
                modifier = Modifier.widthIn(max = 420.dp).weight(1f, fill = false),
            )
            SecondaryButton("Enregistrer", { vm.renameClass(className) })
        }
    }
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Label("Fermeture automatique de l’espace enseignant")
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
            "Tout effacer supprime toute la classe : ${students.size} élève${if (students.size > 1) "s" else ""} et toutes leurs photos. " +
                "Le code PIN et les réglages sont conservés. Rien ne pourra être récupéré sans sauvegarde.",
            color = Palette.text,
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
        if (error != null) Text(error!!, color = Palette.dangerText, fontWeight = FontWeight.Bold)
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

/**
 * Carte de choix d'apparence, avec une mini-vignette du thème.
 * Le choix actif est signalé par la couleur ET par une coche (jamais la couleur seule).
 */
@Composable
private fun ThemeOption(
    emoji: String,
    title: String,
    subtitle: String,
    preview: FoteliColors?,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .pressable(onClick, role = Role.RadioButton)
            .panel(18.dp, color = if (selected) Palette.primarySoft else Palette.cardAlt, border = if (selected) Palette.primary else Palette.stroke)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            // Système : moitié Jour, moitié Nuit.
            val halves = if (preview == null) listOf(LightColors, DarkColors) else listOf(preview)
            halves.forEach { c -> ThemeSwatch(c, Modifier.weight(1f).fillMaxSize()) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(emoji, fontSize = 22.sp)
            Column(Modifier.weight(1f)) {
                Text(title, color = Palette.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(subtitle, color = Palette.muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (selected) Palette.primary else Color.Transparent)
                    .border(2.dp, if (selected) Palette.primary else Palette.muted, CircleShape),
                contentAlignment = Alignment.Center,
            ) { if (selected) Text("✓", color = Palette.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
        }
    }
}

/** Vignette : fond du thème, une carte et une pastille d'accent. */
@Composable
private fun ThemeSwatch(c: FoteliColors, modifier: Modifier) {
    Box(modifier.background(c.bgTop).padding(8.dp)) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(c.card)
                .border(1.dp, c.stroke, RoundedCornerShape(8.dp))
                .padding(6.dp),
        ) {
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(c.primary),
            )
        }
    }
}
