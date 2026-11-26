package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import io.github.ploufty.foteli.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import io.github.ploufty.foteli.data.PhotoStore
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook

// Palette « Lagon sobre » (maquette de référence, jetons --f-*). Contrastes AA vérifiés.
val Paper = Color(0xFFEEF5F4)       // fond
val Field = Color(0xFFF2F7F6)       // champ, rangées
val Line = Color(0xFFD3E6E3)        // trait
val Edge = Color(0xFFC2DBD8)        // bord
val Navy = Color(0xFF16323B)        // encre
val Muted = Color(0xFF4A6670)       // encre secondaire
val Ink3 = Color(0xFF5A7775)        // légendes
val High = Color(0xFFF7FBFA)        // haut
val Night = Color(0xFF0B1416)       // nuit (appareil photo)
val OnBar = Color(0xFFBCD6D3)       // texte sur la barre enseignant
val Blue = Color(0xFF0F766E)        // action
val ActionSoft = Color(0xFFDCEFED)
val ActionDark = Color(0xFF0B5C56)
val Green = Color(0xFF1B7A47)       // garder
val Yellow = Color(0xFFF6C453)      // refaire
val YellowInk = Color(0xFF2A2100)
val Free = Color(0xFF6D4FC7)        // mode libre
val FreeSoft = Color(0xFFEEE9FB)
val Red = Color(0xFFB3261E)         // danger (espace enseignant seulement)
val RedSoft = Color(0xFFFFF1F0)
val Note = Color(0xFFFDF3DC)
val NoteLine = Color(0xFFF1D98A)
val NoteInk = Color(0xFF5B4A12)

// Domaines du référentiel = jetons --f-dom-* (texte sur teinte 12 % ≥ 4,8:1, blanc sur plein ≥ 5,7:1).
val DomLan = Color(0xFF2557C7)
val DomEps = Color(0xFF4D6B0F)
val DomArt = Color(0xFFA12D7C)
val DomMat = Color(0xFFA84A00)
val DomTes = Color(0xFF044E6B)
val DomViv = Color(0xFF5C11A0)

/** Fredoka pour les titres et les prénoms, Atkinson Hyperlegible (conçue pour la lisibilité) pour le texte. */
val TitleFont = FontFamily(Font(R.font.fredoka_semibold, FontWeight.SemiBold))
val BodyFont = FontFamily(Font(R.font.atkinson_regular, FontWeight.Normal), Font(R.font.atkinson_bold, FontWeight.Bold))

@Composable
fun FoteliTheme(content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue, onPrimary = Color.White,
            primaryContainer = ActionSoft, onPrimaryContainer = ActionDark,
            secondary = Free, onSecondary = Color.White,
            secondaryContainer = FreeSoft, onSecondaryContainer = Navy,
            tertiary = Green, onTertiary = Color.White,
            background = Paper, onBackground = Navy,
            surface = Color.White, onSurface = Navy,
            surfaceVariant = Field, onSurfaceVariant = Muted,
            surfaceContainer = Color.White, surfaceContainerHigh = High, surfaceContainerHighest = Field,
            surfaceContainerLow = High, surfaceContainerLowest = Color.White,
            outline = Edge, outlineVariant = Line,
            error = Red, onError = Color.White, errorContainer = RedSoft, onErrorContainer = Red,
        ),
        // Tous les textes en Atkinson Hyperlegible ; les titres (Heading, prénoms) passent en Fredoka.
        typography = Typography(
            displayLarge = base.displayLarge.copy(fontFamily = TitleFont),
            headlineMedium = base.headlineMedium.copy(fontFamily = TitleFont),
            titleLarge = base.titleLarge.copy(fontFamily = TitleFont),
            bodyLarge = base.bodyLarge.copy(fontFamily = BodyFont),
            bodyMedium = base.bodyMedium.copy(fontFamily = BodyFont),
            bodySmall = base.bodySmall.copy(fontFamily = BodyFont),
            labelLarge = base.labelLarge.copy(fontFamily = BodyFont),
            labelMedium = base.labelMedium.copy(fontFamily = BodyFont),
            labelSmall = base.labelSmall.copy(fontFamily = BodyFont),
            titleMedium = base.titleMedium.copy(fontFamily = BodyFont),
            titleSmall = base.titleSmall.copy(fontFamily = BodyFont),
        ),
        content = content,
    )
}

/** Représentation d'un élève : son robot, ou son prénom en grand. */
@Composable
fun StudentFace(student: Student, size: Dp, modifier: Modifier = Modifier) {
    when (student.look) {
        StudentLook.ROBOT -> RobotAvatar(student.robot, modifier.size(size))
        StudentLook.PHOTO -> {
            val file = student.photo?.let { PhotoStore(LocalContext.current.filesDir).faceFile(it) }
            if (file != null) {
                PhotoImage(file, modifier.size(size).clip(RoundedCornerShape(size * 0.22f)), maxSide = 512)
            } else {
                RobotAvatar(student.robot, modifier.size(size))
            }
        }
        StudentLook.NAME -> Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Text(
                student.firstName,
                color = Blue,
                fontFamily = TitleFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value / maxOf(student.firstName.length, 4) * 1.5f).coerceAtMost(size.value / 2.2f).sp,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier) { Text(text, fontSize = 16.sp) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier) { Text(text, fontSize = 16.sp) }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = Color.White),
    ) { Text(text, fontSize = 16.sp) }
}

/** Encadré rouge des suppressions importantes (docs/v0/donnees.md § 4). */
@Composable
fun DangerZone(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RedSoft)
            .border(2.dp, Red, RoundedCornerShape(14.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("⚠  Zone dangereuse", color = Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        content()
    }
}

/** Gros bouton rond « retour » de l'espace élève. */
@Composable
fun BigBackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Retour" },
        contentAlignment = Alignment.Center,
    ) {
        Text("‹", color = Navy, fontSize = 64.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Navy else Color.White)
            .border(1.dp, if (selected) Navy else Edge, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = if (selected) Color.White else Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
