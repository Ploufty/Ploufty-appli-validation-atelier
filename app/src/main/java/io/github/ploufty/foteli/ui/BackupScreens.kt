package io.github.ploufty.foteli.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ploufty.foteli.data.BackupContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

private const val REMIND_DAYS = 14
private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.FRENCH)

/** « jamais », « aujourd'hui », « il y a 12 jours ». */
private fun since(time: Long): String {
    if (time == 0L) return "jamais"
    val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - time)
    return when (days) {
        0L -> "aujourd’hui"
        1L -> "hier"
        else -> "il y a $days jours"
    }
}

@Composable
private fun PasswordField(value: String, label: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.width(320.dp),
    )
}

/** Rappel dans l'espace enseignant après 14 jours sans sauvegarde (l'appli ne sauvegarde jamais seule). */
@Composable
fun BackupReminder(vm: AppViewModel) {
    val students by vm.students.collectAsStateWithLifecycle()
    val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - vm.lastBackup)
    if (students.isEmpty() || (vm.lastBackup != 0L && days < REMIND_DAYS)) return
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF6DC))
            .border(1.dp, Yellow, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Dernière sauvegarde : ${since(vm.lastBackup)}. Si la tablette se perd ou se casse, une sauvegarde récente évite de tout perdre.",
            color = Navy,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )
        SecondaryButton("Sauvegarder", { vm.go(Screen.Teacher(TeacherTab.SETTINGS)) })
    }
}

/** Réglages › Sauvegarde : mot de passe (une fois), sauvegarder, restaurer. */
@Composable
fun BackupPanel(vm: AppViewModel) {
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) vm.backupTo(uri)
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.go(Screen.Restore(uri.toString(), firstLaunch = false))
    }
    var changing by remember { mutableStateOf(false) }
    WhitePanel {
        Text("Sauvegarde", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Hint("Dernière sauvegarde : ${since(vm.lastBackup)}${if (vm.lastBackup != 0L) " (${dateFormat.format(Date(vm.lastBackup))})" else ""}.")
        Hint("Un seul fichier chiffré (élèves, ateliers, photos, avatars), à ranger sur une clé USB ou l’ordinateur de l’école. Il se restaure dans Foteli. Le code PIN n’y est jamais.")
        if (!vm.hasBackupPassword || changing) {
            PasswordSetup(onDone = { vm.setBackupPassword(it); changing = false })
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton("Sauvegarder ma classe", { save.launch(vm.backupFileName()) }, enabled = !vm.backupBusy)
                SecondaryButton("Restaurer une sauvegarde", { open.launch(arrayOf("*/*")) })
                SecondaryButton("Changer le mot de passe", { changing = true })
            }
        }
        vm.backupMessage?.let { Text(it, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
    }
}

@Composable
private fun PasswordSetup(onDone: (String) -> Unit) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    Text("Choisissez le mot de passe de sauvegarde (8 caractères minimum)", color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    Hint("Notez-le à côté de votre code de secours, hors de la classe : sans lui, les sauvegardes sont illisibles. Foteli le retient pour les prochaines fois.")
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PasswordField(first, "Mot de passe") { first = it }
        PasswordField(second, "Le même, encore une fois") { second = it }
    }
    if (second.isNotEmpty() && first != second) Text("Les deux mots de passe sont différents.", color = Red, fontWeight = FontWeight.Bold)
    PrimaryButton("Enregistrer le mot de passe", { onDone(first) }, enabled = first.length >= 8 && first == second)
}

/** Restaurer : mot de passe, résumé de ce qui sera remplacé, confirmation rouge. */
@Composable
fun RestoreScreen(state: Screen.Restore, vm: AppViewModel) {
    val scope = rememberCoroutineScope()
    var password by remember(state) { mutableStateOf("") }
    var error by remember(state) { mutableStateOf<String?>(null) }
    var content by remember(state) { mutableStateOf<BackupContent?>(null) }
    val students by vm.students.collectAsStateWithLifecycle()
    val traces by vm.traces.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F8))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("← Retour", { vm.onBack() })
            Heading("Restaurer une sauvegarde")
        }
        val c = content
        if (c == null) {
            WhitePanel {
                Text("Mot de passe de cette sauvegarde", color = Navy, fontWeight = FontWeight.Bold)
                PasswordField(password, "Mot de passe de sauvegarde") { password = it; error = null }
                error?.let { Text(it, color = Red, fontWeight = FontWeight.Bold) }
                PrimaryButton(if (vm.backupBusy) "Lecture…" else "Ouvrir la sauvegarde", {
                    scope.launch { vm.readBackup(state, password).onSuccess { content = it }.onFailure { error = it.message } }
                }, enabled = password.isNotEmpty() && !vm.backupBusy)
            }
        } else {
            WhitePanel {
                Text("« ${c.className} », sauvegarde du ${dateFormat.format(Date(c.date))}", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Hint("${c.students.size} élèves · ${c.workshops.size} ateliers · ${c.traces.size} photos")
            }
            DangerZone {
                Text(
                    if (state.firstLaunch) {
                        "Foteli va être rempli avec cette sauvegarde. Vous choisirez ensuite un nouveau code PIN."
                    } else {
                        "Remplacer toutes les données actuelles (${students.size} élèves, ${traces.size} photos) par cette sauvegarde ? Ce qui n’est pas dans la sauvegarde sera perdu."
                    },
                    color = Navy,
                    fontSize = 16.sp,
                )
                DangerButton(if (state.firstLaunch) "Restaurer" else "Remplacer par la sauvegarde", { vm.applyRestore(state, password) }, enabled = !vm.backupBusy)
            }
        }
    }
}
