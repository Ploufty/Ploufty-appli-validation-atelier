package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

private val SoftWhite = Color(0xFFD5DEEF)
private val LinkBlue = Color(0xFFBFD0F5)
private val ErrorPink = Color(0xFFFFC9C9)

/** Fond sombre des écrans réservés à l'adulte. Défile si l'écran est petit (téléphone). */
@Composable
private fun AdultScreen(content: @Composable () -> Unit) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Navy),
    ) {
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
    Text(text, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

@Composable
private fun Body(text: String, color: Color = SoftWhite) =
    Text(text, color = color, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 620.dp))

@Composable
private fun Link(text: String, onClick: () -> Unit) =
    TextButton(onClick = onClick) { Text(text, color = LinkBlue, fontSize = 17.sp) }

@Composable
private fun lightFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White, unfocusedBorderColor = Color(0xFF8FA0C2),
    focusedLabelColor = Color.White, unfocusedLabelColor = LinkBlue, cursorColor = Color.White,
)

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
            .background(Navy)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        val sideBySide = maxWidth > maxHeight && maxHeight < 560.dp
        val gap = 10.dp
        val keyHeight = (if (sideBySide) (maxHeight - gap * 3) / 4 else (maxHeight * 0.55f - gap * 3) / 4)
            .coerceIn(44.dp, 76.dp)
        val header: @Composable () -> Unit = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 460.dp))
                Dots(filled = code.length, length = length)
                if (error != null) Text(error, color = ErrorPink, fontSize = 17.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 460.dp))
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
                    .background(if (i < filled) Color.White else Color.Transparent)
                    .border(2.dp, Color.White, CircleShape),
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
                    Box(
                        modifier = Modifier
                            .size(width = keyHeight * 1.35f, height = keyHeight)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (key.isEmpty()) Color.Transparent else Color.White.copy(alpha = 0.14f))
                            .clickable(enabled = key.isNotEmpty()) { onKey(key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(key, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
            Body("Trop d’essais. Réessayez dans ${(lockedUntil - now + 999) / 1000} s.", ErrorPink)
        }
    } else {
        content()
    }
}

// ---------- Premier lancement ----------

@Composable
fun WelcomeScreen(onStart: () -> Unit) = AdultScreen {
    Title("Bienvenue dans Foteli")
    Body("Les photos et les données restent sur cette tablette. Rien n’est envoyé sur Internet.")
    PrimaryButton("Créer ma classe", onStart)
    Body("Restaurer une sauvegarde : disponible dans une prochaine version (0.7).", Color(0xFF8FA0C2))
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
    Title(if (state.reset) "Votre nouveau code de secours" else "Votre code de secours")
    Body("Il sert uniquement si vous oubliez votre code PIN. Notez-le sur papier et rangez-le hors de la classe.")
    Text(
        state.code,
        color = Navy,
        fontSize = 44.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        letterSpacing = 6.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 28.dp, vertical = 12.dp),
    )
    PrimaryButton("J’ai noté mon code", { vm.rescueNoted(state) })
}

@Composable
fun ClassNameScreen(vm: AppViewModel) = AdultScreen {
    var name by remember { mutableStateOf("") }
    Title("Nom de la classe")
    Body("Il servira d’en-tête pour l’extraction des photos et dans le nom des sauvegardes. Exemple : MS-GS Mme Martin 2026-2027.")
    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Nom de la classe") },
        singleLine = true,
        colors = lightFieldColors(),
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
