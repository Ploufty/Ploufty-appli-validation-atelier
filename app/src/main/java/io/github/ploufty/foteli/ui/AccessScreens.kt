package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Settings
import io.github.ploufty.foteli.security.PinRules
import io.github.ploufty.foteli.security.RescueCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Écrans réservés à l'adulte, centrés. Défilent si l'écran est petit (téléphone). */
@Composable
private fun AdultScreen(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val minHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = minHeight)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        ) { content() }
    }
}

@Composable
private fun Title(text: String) =
    Text(text, color = Palette.text, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

@Composable
private fun Body(text: String, color: Color = Palette.muted) =
    Text(text, color = color, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 620.dp))

@Composable
private fun Link(text: String, onClick: () -> Unit) =
    TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(text, color = Palette.link, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }

/** Pastille ronde avec un symbole, en tête des écrans adulte. */
@Composable
private fun Badge(symbol: String) =
    Box(
        Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Palette.primarySoft),
        contentAlignment = Alignment.Center,
    ) { Text(symbol, fontSize = 34.sp) }

/**
 * Clavier 0-9 pour le code PIN (4 chiffres) et le code de secours (8 chiffres).
 * Les touches s'adaptent à la hauteur de l'écran : sur un téléphone, le clavier se place
 * à côté du titre pour que toutes les touches, 0 compris, restent visibles.
 */
@Composable
fun PinPad(
    title: String,
    error: String?,
    length: Int = PinRules.LENGTH,
    footer: @Composable () -> Unit = {},
    onComplete: (String) -> Unit,
) {
    var code by remember { mutableStateOf("") }
    val press: (String) -> Unit = { key ->
        if (key == "⌫") {
            code = code.dropLast(1)
        } else if (code.length < length) {
            code += key
            if (code.length == length) {
                val entered = code
                code = ""
                onComplete(entered)
            }
        }
    }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        val sideBySide = maxWidth > maxHeight && maxHeight < 560.dp
        val gap = 10.dp
        val keyHeight = (if (sideBySide) (maxHeight - gap * 3) / 4 else (maxHeight * 0.55f - gap * 3) / 4)
            .coerceIn(44.dp, 76.dp)
        val header: @Composable () -> Unit = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (!sideBySide) Badge("🔒")
                Text(title, color = Palette.text, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 460.dp))
                Dots(filled = code.length, length = length)
                if (error != null) Text(error, color = Palette.dangerText, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 460.dp))
                footer()
            }
        }
        if (sideBySide) {
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
                header()
                Keys(keyHeight, gap, press)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                header()
                Keys(keyHeight, gap, press)
            }
        }
    }
}

@Composable
private fun Dots(filled: Int, length: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(length) { i ->
            if (i == 4) Spacer(Modifier.size(10.dp)) // 8 chiffres : deux groupes de 4
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (i < filled) Palette.primary else Color.Transparent)
                    .border(2.dp, if (i < filled) Palette.primary else Palette.muted, CircleShape),
            )
        }
    }
}

@Composable
private fun Keys(keyHeight: Dp, gap: Dp, onKey: (String) -> Unit) {
    val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "⌫"))
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { key ->
                    val size = Modifier.size(width = keyHeight * 1.35f, height = keyHeight)
                    if (key.isEmpty()) {
                        Spacer(size)
                    } else {
                        Box(
                            modifier = size
                                .pressable({ onKey(key) })
                                .panel(18.dp, color = if (key == "⌫") Palette.cardAlt else Palette.card)
                                .semantics { contentDescription = if (key == "⌫") "Effacer" else key },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(key, color = Palette.text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/** Affiche un compte à rebours à la place du clavier tant que l'accès est bloqué. */
@Composable
private fun LockedOrPad(settings: Settings?, title: String, content: @Composable () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val lockedUntil = settings?.pinLockedUntil ?: 0L
    if (lockedUntil > now) {
        AdultScreen {
            Title(title)
            Body("Trop d’essais. Réessayez dans ${(lockedUntil - now + 999) / 1000} s.", Palette.dangerText)
        }
    } else {
        content()
    }
}

// ---------- Premier lancement ----------

@Composable
fun WelcomeScreen(onStart: () -> Unit) = AdultScreen {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0, 13, 26).forEach { RobotAvatar(it, Modifier.size(96.dp)) }
    }
    Title("Bienvenue dans Foteli")
    Body("Les photos et les données restent sur cette tablette. Rien n’est envoyé sur Internet.")
    PrimaryButton("Créer ma classe", onStart)
    Body("Restaurer une sauvegarde : disponible dans une prochaine version (0.7).")
}

@Composable
fun ChoosePinScreen(state: Screen.ChoosePin, vm: AppViewModel) =
    PinPad(
        title = if (state.reset) "Choisissez votre nouveau code à 4 chiffres" else "Choisissez votre code enseignant à 4 chiffres",
        error = state.error,
    ) { vm.choosePin(it, state.reset) }

@Composable
fun ConfirmPinScreen(state: Screen.ConfirmPin, vm: AppViewModel) =
    PinPad(title = "Confirmez votre code", error = null) { vm.confirmPin(it, state) }

@Composable
fun ShowRescueScreen(state: Screen.ShowRescue, vm: AppViewModel) = AdultScreen {
    Badge("🔑")
    Title(if (state.reset) "Votre nouveau code de secours" else "Votre code de secours")
    Body("Il sert uniquement si vous oubliez votre code PIN. Notez-le sur papier et rangez-le hors de la classe.")
    Text(
        state.code,
        color = Palette.text,
        fontSize = 44.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        letterSpacing = 6.sp,
        modifier = Modifier
            .panel(18.dp, border = Palette.primary)
            .padding(horizontal = 28.dp, vertical = 12.dp),
    )
    PrimaryButton("J’ai noté mon code", { vm.rescueNoted(state) })
}

@Composable
fun ClassNameScreen(vm: AppViewModel) = AdultScreen {
    var name by remember { mutableStateOf("") }
    Badge("🏫")
    Title("Nom de la classe")
    Body("Il servira d’en-tête pour l’extraction des photos et dans le nom des sauvegardes. Exemple : MS-GS Mme Martin 2026-2027.")
    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Nom de la classe") },
        singleLine = true,
        modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
    )
    PrimaryButton("Continuer", { vm.saveClassName(name) }, enabled = name.isNotBlank())
}

// ---------- Accès enseignant ----------

@Composable
fun PinEntryScreen(settings: Settings?, vm: AppViewModel) = LockedOrPad(settings, "Code enseignant") {
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    PinPad(
        title = "Code enseignant",
        error = error,
        footer = {
            Row {
                Link("Annuler") { vm.go(Screen.Home) }
                Link("Code oublié ?") { vm.go(Screen.RescueEntry) }
            }
        },
    ) { pin -> scope.launch { error = vm.tryPin(pin) } }
}

@Composable
fun RescueEntryScreen(settings: Settings?, vm: AppViewModel) = LockedOrPad(settings, "Code de secours") {
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    PinPad(
        title = "Code de secours (8 chiffres)",
        error = error,
        length = RescueCode.LENGTH,
        footer = { Link("Annuler") { vm.go(Screen.Home) } },
    ) { code -> scope.launch { error = vm.tryRescue(code) } }
}
