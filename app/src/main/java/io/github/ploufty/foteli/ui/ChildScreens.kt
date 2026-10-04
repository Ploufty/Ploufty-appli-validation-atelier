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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.StudentLook
import io.github.ploufty.foteli.data.Workshop

private const val LONG_PRESS_MS = 3_000L

/** Accueil enfant : la grille des élèves, qui tient toujours sur un seul écran. */
@Composable
fun HomeScreen(students: List<Student>, canPlay: Boolean, vm: AppViewModel) {
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
            // Aucun atelier actif et mode libre désactivé : la lune dort, les élèves ne répondent pas.
            Box(Modifier.fillMaxSize().alpha(if (canPlay) 1f else 0.35f)) {
                StudentGrid(students, enabled = canPlay) { vm.go(Screen.ChooseWorkshop(it.id)) }
            }
            if (!canPlay) SleepingMoon(Modifier.align(Alignment.Center).size(220.dp))
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
private fun StudentGrid(students: List<Student>, enabled: Boolean, onPick: (Student) -> Unit) {
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
                        StudentCard(students[index], enabled, Modifier.weight(1f).fillMaxHeight()) { onPick(students[index]) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCard(student: Student, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    BoxWithConstraints(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable(enabled = enabled, onClick = onClick)
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

@Composable
private fun SleepingMoon(modifier: Modifier) {
    Canvas(modifier.semantics { contentDescription = "Pas d’atelier pour le moment" }) {
        val r = size.minDimension / 2.2f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(Color(0xFFFFE8A3), r, c)
        drawCircle(Paper, r * 0.9f, Offset(c.x + r * 0.45f, c.y - r * 0.3f))
        drawCircle(Color(0xFF5C7CFA), r * 0.09f, Offset(c.x + r * 0.55f, c.y - r * 1.05f))
        drawCircle(Color(0xFF5C7CFA), r * 0.06f, Offset(c.x + r * 0.85f, c.y - r * 1.3f))
    }
}

/** L'enfant reconnaît son atelier grâce à l'image (mode B, docs/v0/parcours.md A). */
@Composable
fun ChooseWorkshopScreen(student: Student?, workshops: List<Workshop>, freeMode: Boolean, vm: AppViewModel) {
    val active = workshops.filter { it.active }
    val count = active.size + if (freeMode) 1 else 0
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            BigBackButton { vm.go(Screen.Home) }
            if (student != null) {
                Box(
                    Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(48.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) { StudentFace(student, 80.dp) }
            }
        }
        val columns = if (count <= 3) count.coerceAtLeast(1) else 3
        val rows = (count + columns - 1) / columns
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            // null = carte « Photo libre »
            val cards: List<Workshop?> = active + if (freeMode) listOf(null) else emptyList()
            val sid = student?.id ?: 0L
            for (r in 0 until rows) {
                Row(
                    Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                ) {
                    for (c in 0 until columns) {
                        val index = r * columns + c
                        if (index < cards.size) {
                            val m = Modifier.weight(1f, fill = false).fillMaxHeight().aspectRatio(1.05f)
                            val w = cards[index]
                            if (w != null) {
                                WorkshopCard(w, m) { vm.go(Screen.CameraSoon(sid, w.id)) }
                            } else {
                                FreeCard(m) { vm.go(Screen.CameraSoon(sid, null)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkshopCard(workshop: Workshop, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .semantics { contentDescription = workshop.title }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WorkshopArt(
            workshop.image,
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp)),
        )
        Text(workshop.title, color = Color(0xFF8A94A6), fontSize = 15.sp, maxLines = 1)
    }
}

@Composable
private fun FreeCard(modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF3EBFF))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Photo libre" }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF8B5CF6)),
            contentAlignment = Alignment.Center,
        ) { FreePhotoIcon(Modifier.fillMaxSize(0.6f)) }
        Text("Photo libre · Souvenirs", color = Color(0xFF8A94A6), fontSize = 15.sp, maxLines = 1)
    }
}

/** Version 0.3 : l'atelier est choisi ; l'appareil photo arrive en version 0.4. */
@Composable
fun CameraSoonScreen(student: Student?, workshop: Workshop?, vm: AppViewModel, studentId: Long) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(28.dp),
    ) {
        BigBackButton { vm.go(Screen.ChooseWorkshop(studentId)) }
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (student != null) StudentFace(student, 180.dp)
            Box(
                Modifier
                    .size(width = 260.dp, height = 200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (workshop == null) Color(0xFF8B5CF6) else Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (workshop != null) WorkshopArt(workshop.image, Modifier.fillMaxSize()) else FreePhotoIcon(Modifier.fillMaxSize(0.6f))
            }
            Text(
                "L’appareil photo arrivera\ndans la version 0.4.",
                color = Muted,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
