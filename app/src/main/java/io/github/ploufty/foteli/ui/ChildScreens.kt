package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook

private const val LONG_PRESS_MS = 3_000L

/** Accueil enfant : la grille des élèves, qui tient toujours sur un seul écran. */
@Composable
fun HomeScreen(students: List<Student>, vm: AppViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        if (students.isEmpty()) {
            Text(
                "Aucun élève pour l’instant.\nEnseignant : appui long de 3 secondes en haut à droite.",
                color = Muted,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            StudentGrid(students) { vm.go(Screen.Child(it.id)) }
        }
        // Accès enseignant : appui long de 3 s dans le coin haut droit, invisible pour les enfants.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(96.dp)
                .semantics { contentDescription = "Espace enseignant : appui long de 3 secondes" }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        val released = withTimeoutOrNull(LONG_PRESS_MS) { waitForUpOrCancellation() }
                        if (released == null) vm.go(Screen.PinEntry)
                    }
                },
        )
    }
}

@Composable
private fun StudentGrid(students: List<Student>, onPick: (Student) -> Unit) {
    val n = students.size
    val columns = when {
        n <= 6 -> n
        n <= 12 -> 6
        n <= 21 -> 7
        else -> 8
    }
    val rows = (n + columns - 1) / columns
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        for (r in 0 until rows) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                for (c in 0 until columns) {
                    val index = r * columns + c
                    if (index < n) {
                        StudentCard(students[index], Modifier.weight(1f).fillMaxHeight()) { onPick(students[index]) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCard(student: Student, modifier: Modifier, onClick: () -> Unit) {
    BoxWithConstraints(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .semantics { contentDescription = student.firstName }
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        val face = min(maxWidth, maxHeight * 0.72f)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StudentFace(student, face)
            if (student.look == StudentLook.ROBOT) {
                Text(student.firstName, color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

/** Version 0.2 : l'élève est reconnu ; le choix de l'atelier arrive en version 0.3. */
@Composable
fun ChildScreen(student: Student?, vm: AppViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            BigBackButton { vm.go(Screen.Home) }
        }
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            if (student != null) StudentFace(student, 220.dp)
            Text(
                "Les ateliers arriveront dans la version 0.3.",
                color = Muted,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
