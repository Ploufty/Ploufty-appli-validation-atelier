package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Fond sombre commun aux écrans réservés à l'adulte (premier lancement, code). */
@Composable
private fun AdultScreen(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) { content() }
}

@Composable
private fun Title(text: String) =
    Text(text, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

@Composable
private fun Body(text: String, color: Color = Color(0xFFD5DEEF)) =
    Text(text, color = color, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.width(620.dp))

@Composable
private fun lightFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White, unfocusedBorderColor = Color(0xFF8FA0C2),
    focusedLabelColor = Color.White, unfocusedLabelColor = Color(0xFFBFD0F5), cursorColor = Color.White,
)

/** Clavier à 4 chiffres, utilisé pour choisir, confirmer et saisir le code PIN. */
@Composable
fun PinPad(title: String, error: String?, enabled: Boolean = true, onComplete: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        repeat(4) { i ->
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (i < code.length) Color.White else Color.Transparent)
                    .border(2.dp, Color.White, CircleShape),
            )
        }
    }
    if (error != null) Text(error, color = Color(0xFFFFC9C9), fontSize = 17.sp, textAlign = TextAlign.Center)
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(width = 96.dp, height = 72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (key.isEmpty()) Color.Transparent else Color.White.copy(alpha = 0.12f))
                            .clickable(enabled = enabled && key.isNotEmpty()) {
                                if (key == "⌫") {
                                    code = code.dropLast(1)
                                } else if (code.length < 4) {
                                    code += key
                                    if (code.length == 4) {
                                        val entered = code
                                        code = ""
                                        onComplete(entered)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(key, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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
fun ChoosePinScreen(state: Screen.ChoosePin, vm: AppViewModel) = AdultScreen {
    PinPad(
        title = if (state.reset) "Choisissez votre nouveau code à 4 chiffres" else "Choisissez votre code enseignant à 4 chiffres",
        error = state.error,
    ) { vm.choosePin(it, state.reset) }
}

@Composable
fun ConfirmPinScreen(state: Screen.ConfirmPin, vm: AppViewModel) = AdultScreen {
    PinPad(title = "Confirmez votre code", error = null) { vm.confirmPin(it, state) }
}

@Composable
fun ShowRescueScreen(state: Screen.ShowRescue, vm: AppViewModel) = AdultScreen {
    var lastFour by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Title(if (state.reset) "Votre nouveau code de secours" else "Votre code de secours")
    Body("Il permet de changer le code PIN si vous l’oubliez. Recopiez-le sur papier et rangez-le hors de la classe. Il ne sera plus jamais affiché.")
    Text(
        state.code,
        color = Navy,
        fontSize = 40.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        letterSpacing = 4.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 28.dp, vertical = 14.dp),
    )
    OutlinedTextField(
        value = lastFour,
        onValueChange = { lastFour = it.take(4) },
        label = { Text("Tapez ses 4 derniers caractères") },
        singleLine = true,
        colors = lightFieldColors(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
    )
    if (error != null) Text(error!!, color = Color(0xFFFFC9C9), fontSize = 17.sp)
    PrimaryButton("Continuer", {
        if (!vm.rescueNoted(state, lastFour)) error = "Ce ne sont pas les 4 derniers caractères."
    })
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
        modifier = Modifier.width(520.dp),
    )
    PrimaryButton("Continuer", { vm.saveClassName(name) }, enabled = name.isNotBlank())
}

// ---------- Accès enseignant ----------

@Composable
fun PinEntryScreen(settings: Settings?, vm: AppViewModel) = AdultScreen {
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val lockedUntil = settings?.pinLockedUntil ?: 0L
    if (lockedUntil > now) {
        val seconds = (lockedUntil - now + 999) / 1000
        Title("Code enseignant")
        Body("Trop d’essais. Réessayez dans $seconds s.", Color(0xFFFFC9C9))
    } else {
        PinPad(title = "Code enseignant", error = error) { pin ->
            scope.launch { error = vm.tryPin(pin) }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        TextButton(onClick = { vm.go(Screen.Home) }) { Text("Annuler", color = Color(0xFFBFD0F5), fontSize = 17.sp) }
        TextButton(onClick = { vm.go(Screen.RescueEntry) }) { Text("Code oublié ?", color = Color(0xFFBFD0F5), fontSize = 17.sp) }
    }
}

@Composable
fun RescueEntryScreen(vm: AppViewModel) = AdultScreen {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Title("Code de secours")
    Body("Tapez le code de 12 caractères noté au premier lancement.")
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text("XXXX-XXXX-XXXX") },
        singleLine = true,
        colors = lightFieldColors(),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = Modifier.width(420.dp),
    )
    if (error != null) Text(error!!, color = Color(0xFFFFC9C9), fontSize = 17.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = { vm.go(Screen.Home) }) { Text("Annuler", color = Color(0xFFBFD0F5), fontSize = 17.sp) }
        PrimaryButton("Valider", {
            scope.launch { if (!vm.tryRescue(input)) error = "Code de secours incorrect." }
        })
    }
}
