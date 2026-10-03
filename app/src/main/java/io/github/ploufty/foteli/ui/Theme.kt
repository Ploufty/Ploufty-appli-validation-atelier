package io.github.ploufty.foteli.ui

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook
import io.github.ploufty.foteli.data.ThemeMode

/**
 * Palette de l'appli, en version Jour et Nuit.
 * Nuit : bleu nuit profond, cartes « verre » légèrement plus claires, accents vifs.
 */
@Immutable
data class FoteliColors(
    val dark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val card: Color,
    val cardAlt: Color,
    val stroke: Color,
    val text: Color,
    val muted: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val link: Color,
    val accent: Color,
    val onAccent: Color,
    val danger: Color,
    val dangerText: Color,
    val dangerSoft: Color,
)

val LightColors = FoteliColors(
    dark = false,
    bgTop = Color(0xFFF4F7FF),
    bgBottom = Color(0xFFE2E8FA),
    card = Color(0xFFFFFFFF),
    cardAlt = Color(0xFFEEF2FC),
    stroke = Color(0xFFD9E0F0),
    text = Color(0xFF1F2A44),
    muted = Color(0xFF586378),
    primary = Color(0xFF3B5BDB),
    onPrimary = Color.White,
    primarySoft = Color(0xFFE2E9FE),
    link = Color(0xFF3B5BDB),
    accent = Color(0xFFFFC53D),
    onAccent = Color(0xFF1F2A44),
    danger = Color(0xFFC23B32),
    dangerText = Color(0xFFB0322A),
    dangerSoft = Color(0xFFFFF1F0),
)

val DarkColors = FoteliColors(
    dark = true,
    bgTop = Color(0xFF2E3B78),
    bgBottom = Color(0xFF191B44),
    card = Color(0xFF34427F),
    cardAlt = Color(0xFF3E4D8F),
    stroke = Color(0xFF4F5FA6),
    text = Color(0xFFF3F5FF),
    muted = Color(0xFFB4BEE4),
    primary = Color(0xFF3D73E0),
    onPrimary = Color.White,
    primarySoft = Color(0xFF2A3C86),
    link = Color(0xFF9DBBFF),
    accent = Color(0xFFFFC53D),
    onAccent = Color(0xFF1F2A44),
    danger = Color(0xFFD9473F),
    dangerText = Color(0xFFFFA39C),
    dangerSoft = Color(0xFF45244A),
)

private val LocalColors = staticCompositionLocalOf { LightColors }

/** Couleurs du thème en cours (Jour ou Nuit). */
val Palette: FoteliColors
    @Composable @ReadOnlyComposable
    get() = LocalColors.current

@Composable
fun FoteliTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val c = if (dark) DarkColors else LightColors
    val scheme = remember(c) {
        (if (c.dark) darkColorScheme() else lightColorScheme()).copy(
            primary = c.primary, onPrimary = c.onPrimary,
            primaryContainer = c.primarySoft, onPrimaryContainer = c.text,
            background = c.bgTop, onBackground = c.text,
            surface = c.card, onSurface = c.text,
            surfaceVariant = c.cardAlt, onSurfaceVariant = c.muted,
            surfaceContainerHighest = c.cardAlt,
            outline = c.muted, outlineVariant = c.stroke,
            error = c.danger, onError = Color.White,
        )
    }
    // Icônes de la barre d'état lisibles sur le fond du thème choisi.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalColors provides c) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

/** Fond dégradé commun à tous les écrans. */
@Composable
fun Modifier.screenBackground(): Modifier =
    background(Brush.verticalGradient(listOf(Palette.bgTop, Palette.bgBottom)))

/** Carte arrondie « verre » : fond, liseré fin. */
@Composable
fun Modifier.panel(radius: Dp = 20.dp, color: Color = Palette.card, border: Color = Palette.stroke): Modifier {
    val shape = RoundedCornerShape(radius)
    return clip(shape).background(color).border(1.dp, border, shape)
}

/** Léger enfoncement au toucher : retour visuel immédiat, utile aux plus petits. */
@Composable
fun Modifier.pressable(onClick: () -> Unit, enabled: Boolean = true, role: Role = Role.Button): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val factor by animateFloatAsState(if (pressed) 0.95f else 1f, label = "press")
    return this.scale(factor).clickable(interactionSource = interaction, indication = null, enabled = enabled, role = role, onClick = onClick)
}

/** Couleur de halo derrière un élève : celle de son robot, très adoucie. */
@Composable
fun haloColor(student: Student): Color =
    if (student.look == StudentLook.ROBOT) Robots.spec(student.robot).color.copy(alpha = if (Palette.dark) 0.30f else 0.22f)
    else Palette.primarySoft

/** Représentation d'un élève : son robot, ou son prénom en grand. */
@Composable
fun StudentFace(student: Student, size: Dp, modifier: Modifier = Modifier) {
    when (student.look) {
        StudentLook.ROBOT -> RobotAvatar(student.robot, modifier.size(size))
        StudentLook.NAME -> Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Text(
                student.firstName,
                color = if (Palette.dark) Palette.link else Palette.primary,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value / maxOf(student.firstName.length, 4) * 1.5f).coerceAtMost(size.value / 2.2f).sp,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Élève dans son halo coloré (accueil, écran enfant, liste de la classe). */
@Composable
fun HaloFace(student: Student, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(haloColor(student)),
        contentAlignment = Alignment.Center,
    ) { StudentFace(student, size * 0.82f) }
}

private val ButtonShape = RoundedCornerShape(14.dp)

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, shape = ButtonShape, modifier = modifier.heightIn(min = 48.dp)) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        shape = ButtonShape,
        border = BorderStroke(1.5.dp, Palette.stroke),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Palette.card, contentColor = Palette.text),
        modifier = modifier.heightIn(min = 48.dp),
    ) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(containerColor = Palette.danger, contentColor = Color.White),
        modifier = Modifier.heightIn(min = 48.dp),
    ) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
}

/** Encadré rouge des suppressions importantes (docs/v0/donnees.md § 4). */
@Composable
fun DangerZone(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .panel(color = Palette.dangerSoft, border = Palette.danger)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("⚠  Zone dangereuse", color = Palette.dangerText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        content()
    }
}

/** Gros bouton rond (retour de l'espace élève) : pastille bleue, symbole blanc. */
@Composable
fun RoundButton(symbol: String, description: String, size: Dp = 88.dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .pressable(onClick)
            .clip(CircleShape)
            .background(Palette.primary)
            .border(3.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Palette.onPrimary, fontSize = (size.value * 0.45f).sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BigBackButton(onClick: () -> Unit) = RoundButton("←", "Retour", onClick = onClick)

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Palette.primary else Palette.cardAlt)
            .border(1.dp, if (selected) Palette.primary else Palette.stroke, RoundedCornerShape(50))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            (if (selected) "✓  " else "") + text,
            color = if (selected) Palette.onPrimary else Palette.text,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

/**
 * Sélecteur en pastille (onglets, choix exclusif) : l'élément choisi ressort en bleu plein.
 * [enabled] permet de griser les entrées pas encore disponibles.
 */
@Composable
fun Segmented(
    items: List<String>,
    selected: Int,
    modifier: Modifier = Modifier,
    enabled: (Int) -> Boolean = { true },
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Palette.cardAlt)
            .border(1.dp, Palette.stroke, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEachIndexed { i, label ->
            val on = i == selected
            val active = enabled(i)
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (on) Palette.primary else Color.Transparent)
                    .clickable(enabled = active, role = Role.Tab) { onSelect(i) }
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = when {
                        on -> Palette.onPrimary
                        active -> Palette.text
                        else -> Palette.muted.copy(alpha = 0.6f)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                )
            }
        }
    }
}
