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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook

val Navy = Color(0xFF1F2A44)
val Blue = Color(0xFF2C5FD6)
val Paper = Color(0xFFEEF3F9)
val Green = Color(0xFF1E8E4E)
val Red = Color(0xFFC23B32)
val RedSoft = Color(0xFFFFF1F0)
val Yellow = Color(0xFFFFC53D)
val Muted = Color(0xFF5A6576)

@Composable
fun FoteliTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Blue, onPrimary = Color.White, background = Paper, error = Red),
        content = content,
    )
}

/** Représentation d'un élève : son robot, ou son prénom en grand. */
@Composable
fun StudentFace(student: Student, size: Dp, modifier: Modifier = Modifier) {
    when (student.look) {
        StudentLook.ROBOT -> RobotAvatar(student.robot, modifier.size(size))
        StudentLook.NAME -> Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Text(
                student.firstName,
                color = Blue,
                fontWeight = FontWeight.Bold,
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
            .clickable(onClick = onClick),
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
            .border(1.dp, if (selected) Navy else Color(0xFFC6CFDC), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text, color = if (selected) Color.White else Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
