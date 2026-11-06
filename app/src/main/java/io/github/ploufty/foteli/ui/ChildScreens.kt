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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Dp
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
        // Première arrivée sur l'accueil après la création de la classe : l'enseignant apprend le geste caché.
        if (!vm.teacherAccessKnown) TeacherAccessTip { vm.teacherAccessHelpSeen() }
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
                        if (released == null) {
                            vm.teacherAccessHelpSeen()
                            vm.go(Screen.PinEntry)
                        }
                    }
                },
        )
    }
}

/**
 * Explication unique de l'accès enseignant, avec un cercle qui pulse sur le coin à presser.
 * Le coin reste actif par-dessus : l'enseignant peut essayer le geste tout de suite.
 */
@Composable
private fun TeacherAccessTip(onDone: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "coin").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "coin",
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(Navy.copy(alpha = 0.55f))
            .pointerInput(Unit) { detectTapGestures { } },
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(80.dp)
                .alpha(pulse)
                .border(6.dp, Yellow, CircleShape),
        )
        Column(
            Modifier
                .align(Alignment.Center)
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Pour revenir dans l’espace enseignant", color = Navy, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("1. Appuyez 3 secondes sur le coin en haut à droite (le cercle jaune).", color = Navy, fontSize = 18.sp)
            Text("2. Tapez votre code PIN.", color = Navy, fontSize = 18.sp)
            Text(
                "Ce coin n’est jamais signalé aux enfants. Ce message ne s’affichera plus : notez le geste à côté de votre code de secours si besoin.",
                color = Muted,
                fontSize = 15.sp,
            )
            PrimaryButton("J’ai compris", onDone)
        }
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
            if (student != null) StudentBubble(student, 96.dp)
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
                                WorkshopCard(w, m) { vm.go(Screen.Camera(sid, w.id)) }
                            } else {
                                FreeCard(m) { vm.go(Screen.Camera(sid, null)) }
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

/** « C'est moi » : le robot (ou le prénom) de l'enfant dans une bulle blanche. */
@Composable
internal fun StudentBubble(student: Student, size: Dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) { StudentFace(student, size * 0.83f) }
}
