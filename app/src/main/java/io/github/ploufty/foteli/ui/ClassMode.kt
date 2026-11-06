package io.github.ploufty.foteli.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * « Mode classe » (docs/v0/technique.md § 2) : l'épinglage d'écran d'Android bloque Accueil, Applis récentes
 * et volet de notifications. Pour en sortir sans passer par Foteli, Android demande le code de la tablette.
 */
class ClassMode(val active: Boolean, val start: () -> Unit, val stop: () -> Unit, val openSecuritySettings: () -> Unit)

val LocalClassMode = staticCompositionLocalOf { ClassMode(false, {}, {}, {}) }

@Composable
fun rememberClassMode(vm: AppViewModel): ClassMode {
    val context = LocalContext.current
    val activity = context as? Activity
    val manager = remember { context.getSystemService(ActivityManager::class.java) }
    var active by remember { mutableStateOf(false) }
    // L'état change quand l'enseignant confirme la fenêtre d'Android : relu chaque seconde.
    LaunchedEffect(Unit) {
        while (true) {
            active = manager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
            delay(1_000)
        }
    }
    // Écran toujours allumé pendant le mode classe (décision T2, désactivable).
    val view = LocalView.current
    SideEffect { view.keepScreenOn = active && vm.keepScreenOn }
    return ClassMode(
        active = active,
        start = {
            vm.classModeHelpSeen()
            activity?.startLockTask()
            vm.go(Screen.Home)
        },
        stop = { activity?.stopLockTask() },
        openSecuritySettings = {
            context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        },
    )
}

/** Panneau « Mode classe » des Réglages : la marche à suivre une fois, puis lancer / quitter. */
@Composable
fun ClassModePanel(vm: AppViewModel) {
    val mode = LocalClassMode.current
    WhitePanel {
        Text("Mode classe", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        if (mode.active) {
            Hint("Actif : les enfants ne peuvent pas sortir de Foteli.")
            SecondaryButton("🔓 Quitter le mode classe", mode.stop)
        } else {
            Hint("Empêche les enfants de sortir de Foteli (boutons Accueil, Applis récentes et volet bloqués).")
            Text("À faire une seule fois sur la tablette :", color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Hint("1. Mettre un code de verrouillage sur la tablette (Paramètres › Sécurité › Verrouillage de l’écran).")
                Hint("2. Activer « Épinglage d’application » et « Demander le code avant de retirer l’épinglage » (Paramètres › Sécurité › Paramètres avancés ; le chemin varie selon la marque).")
                Hint("3. Ensuite : « Lancer le mode classe », puis confirmer la fenêtre d’Android.")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton("🔒 Lancer le mode classe", mode.start)
                SecondaryButton("Ouvrir les paramètres de sécurité", mode.openSecuritySettings)
            }
            Hint("Pour en sortir : appui long de 3 s en haut à droite, code PIN, puis « Quitter le mode classe ».")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Écran toujours allumé pendant le mode classe", color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Hint("Pensez à brancher la tablette pour une longue séance.")
            }
            Switch(checked = vm.keepScreenOn, onCheckedChange = { vm.chooseKeepScreenOn(it) })
        }
    }
}
