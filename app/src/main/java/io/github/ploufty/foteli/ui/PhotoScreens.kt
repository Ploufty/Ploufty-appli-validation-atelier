package io.github.ploufty.foteli.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import android.view.Surface
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.Workshop
import java.io.File
import kotlinx.coroutines.delay

private val Night = Color(0xFF111827)
private val CheckGreen = Color(0xFF2FB463)

/** Délai avant que ✅ et ↻ répondent : un double appui sur le déclencheur ne valide pas la photo par erreur. */
private const val REVIEW_GUARD_MS = 700L

// ---------- Espace élève ----------

/**
 * Appareil photo de l'enfant (toucher 3) : plein écran, sans texte, deux gros déclencheurs identiques
 * à gauche et à droite (décision D3, à confirmer en classe).
 */
@Composable
fun ChildCameraScreen(
    state: Screen.Camera,
    student: Student?,
    workshop: Workshop?,
    front: Boolean,
    cameraAllowed: Boolean,
    vm: AppViewModel,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val capture = rememberImageCapture()
    var busy by remember { mutableStateOf(false) }
    var unavailable by remember { mutableStateOf(false) }
    val ready = cameraAllowed && !unavailable

    val shoot = {
        if (!busy) {
            busy = true
            val file = vm.photos.newPending()
            takePhoto(context, capture, view.display?.rotation ?: Surface.ROTATION_90, file) { ok ->
                busy = false
                if (ok) vm.photoTaken(state, file) else file.delete()
            }
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Night),
    ) {
        val shutter = min(150.dp, maxHeight * 0.3f)
        if (ready) {
            CameraPreview(front, capture, Modifier.fillMaxSize()) { unavailable = true }
        } else {
            NoCameraIcon(Modifier.align(Alignment.Center).size(min(260.dp, maxHeight * 0.45f)))
        }
        ChildTopBar(student, workshop, state.workshopId == null) { vm.go(Screen.ChooseWorkshop(state.studentId)) }
        if (ready) {
            ShutterButton(shutter, !busy, Modifier.align(Alignment.CenterStart).padding(start = 28.dp), shoot)
            ShutterButton(shutter, !busy, Modifier.align(Alignment.CenterEnd).padding(end = 28.dp), shoot)
        }
        // Éclair blanc : l'enfant voit que la photo est prise.
        if (busy) Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.55f)))
    }
}

/** L'enfant regarde sa photo (toucher 4) : ↻ à gauche pour reprendre, ✅ à droite pour garder. */
@Composable
fun ReviewScreen(state: Screen.Review, student: Student?, workshop: Workshop?, vm: AppViewModel) {
    var ready by remember(state) { mutableStateOf(false) }
    LaunchedEffect(state) {
        delay(REVIEW_GUARD_MS)
        ready = true
    }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Night),
    ) {
        val button = min(150.dp, maxHeight * 0.3f)
        PhotoImage(
            File(state.file),
            Modifier
                .fillMaxSize()
                .padding(horizontal = button + 56.dp, vertical = 24.dp),
            maxSide = 2048,
            contentScale = ContentScale.Fit,
        )
        ChildTopBar(student, workshop, state.workshopId == null) { vm.retake(state) }
        RoundAction(button, Yellow, ready, "Reprendre la photo", Modifier.align(Alignment.CenterStart).padding(start = 28.dp), { vm.retake(state) }) {
            RetakeIcon(Navy, Modifier.fillMaxSize(0.55f))
        }
        RoundAction(button, CheckGreen, ready, "Garder la photo", Modifier.align(Alignment.CenterEnd).padding(end = 28.dp), { vm.validate(state) }) {
            CheckIcon(Color.White, Modifier.fillMaxSize(0.55f))
        }
    }
}

/** Bravo : grande coche verte à côté du robot de l'enfant, puis retour à l'accueil. */
@Composable
fun BravoScreen(state: Screen.Bravo, student: Student?, vm: AppViewModel) {
    val grow = remember { Animatable(0.6f) }
    LaunchedEffect(state) {
        grow.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        grow.snapTo(1f)
        delay(1_800)
        vm.bravoDone(state)
    }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .clickable { vm.bravoDone(state) },
        contentAlignment = Alignment.Center,
    ) {
        val big = min(260.dp, maxHeight * 0.5f)
        Row(horizontalArrangement = Arrangement.spacedBy(48.dp), verticalAlignment = Alignment.CenterVertically) {
            if (student != null) StudentFace(student, big * 0.8f)
            Box(
                Modifier
                    .size(big)
                    .scale(grow.value)
                    .clip(CircleShape)
                    .background(CheckGreen)
                    .semantics { contentDescription = "Bravo, photo enregistrée" },
                contentAlignment = Alignment.Center,
            ) { CheckIcon(Color.White, Modifier.fillMaxSize(0.6f)) }
        }
    }
}

/** En haut à gauche : retour, et rappel « c'est moi » + l'atelier choisi. */
@Composable
private fun ChildTopBar(student: Student?, workshop: Workshop?, free: Boolean, onBack: () -> Unit) {
    Row(
        Modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BigBackButton(onBack)
        if (student != null) StudentBubble(student, 88.dp)
        Box(
            Modifier
                .size(width = 112.dp, height = 84.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (free) Color(0xFF8B5CF6) else Color.White)
                .border(3.dp, Color.White, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (workshop != null) WorkshopArt(workshop.image, Modifier.fillMaxSize()) else if (free) FreePhotoIcon(Modifier.fillMaxSize(0.7f))
        }
    }
}

@Composable
private fun ShutterButton(size: Dp, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            // Anneau sombre + disque blanc : visible sur une scène claire comme sur une scène sombre.
            .background(Color.Black.copy(alpha = 0.45f))
            .border(3.dp, Color.White, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = "Prendre la photo" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun RoundAction(
    size: Dp,
    color: Color,
    enabled: Boolean,
    description: String,
    modifier: Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Box(
        modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.6f)
            .clip(CircleShape)
            .background(color)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { icon() }
}

@Composable
private fun CheckIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val path = Path().apply {
            moveTo(w * 0.12f, w * 0.52f)
            lineTo(w * 0.40f, w * 0.78f)
            lineTo(w * 0.88f, w * 0.24f)
        }
        drawPath(path, color, style = Stroke(w * 0.13f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun RetakeIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val stroke = w * 0.12f
        drawArc(color, 300f, 290f, false, Offset(w * 0.12f, w * 0.12f), Size(w * 0.76f, w * 0.76f), style = Stroke(stroke, cap = StrokeCap.Round))
        val tip = Path().apply {
            moveTo(w * 0.62f, w * 0.02f)
            lineTo(w * 0.92f, w * 0.20f)
            lineTo(w * 0.60f, w * 0.38f)
            close()
        }
        drawPath(tip, color)
    }
}

/** Pas d'appareil photo (non autorisé ou absent) : l'enseignant voit l'explication dans son espace. */
@Composable
private fun NoCameraIcon(modifier: Modifier) {
    Canvas(modifier.semantics { contentDescription = "Appareil photo indisponible" }) {
        val w = size.width
        val grey = Color(0xFF6B7280)
        drawRoundRect(grey, Offset(w * 0.1f, w * 0.3f), Size(w * 0.8f, w * 0.52f), androidx.compose.ui.geometry.CornerRadius(w * 0.08f))
        drawRoundRect(grey, Offset(w * 0.34f, w * 0.2f), Size(w * 0.32f, w * 0.14f), androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        drawCircle(Night, w * 0.16f, Offset(w * 0.5f, w * 0.56f))
        drawLine(Color(0xFFFF6B6B), Offset(w * 0.12f, w * 0.12f), Offset(w * 0.88f, w * 0.92f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    }
}

// ---------- Espace enseignant ----------

/** Photo du modèle d'un atelier, prise par l'enseignant, par-dessus l'écran en cours (rien n'est perdu). */
@Composable
fun TeacherCameraOverlay(request: TeacherCameraRequest, cameraAllowed: Boolean, vm: AppViewModel) {
    val context = LocalContext.current
    val view = LocalView.current
    val capture = rememberImageCapture()
    var busy by remember { mutableStateOf(false) }
    var unavailable by remember { mutableStateOf(false) }
    var taken by remember { mutableStateOf<File?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Night)
            // Bloque les touchers vers l'écran du dessous.
            .pointerInput(Unit) { detectTapGestures { } },
    ) {
        val photo = taken
        when {
            photo != null -> PhotoImage(photo, Modifier.fillMaxSize().padding(24.dp), maxSide = 2048, contentScale = ContentScale.Fit)
            cameraAllowed && !unavailable -> CameraPreview(false, capture, Modifier.fillMaxSize()) { unavailable = true }
            else -> Text(
                "Appareil photo indisponible. Autorisez-le depuis le bandeau jaune de l’espace enseignant.",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center).padding(40.dp),
            )
        }
        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (photo == null) {
                Text("Photo du modèle", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                PrimaryButton("Prendre la photo", {
                    if (!busy) {
                        busy = true
                        val file = vm.photos.newPending()
                        takePhoto(context, capture, view.display?.rotation ?: Surface.ROTATION_90, file) { ok ->
                            busy = false
                            if (ok) taken = file else file.delete()
                        }
                    }
                }, enabled = cameraAllowed && !unavailable && !busy)
                SecondaryButton("Annuler", { vm.closeTeacherCamera() })
            } else {
                Text("Cette photo vous convient ?", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                PrimaryButton("Utiliser cette photo", {
                    request.onPhoto(photo)
                    vm.closeTeacherCamera()
                })
                SecondaryButton("Reprendre", {
                    photo.delete()
                    taken = null
                })
            }
        }
        if (busy) Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.55f)))
    }
}
