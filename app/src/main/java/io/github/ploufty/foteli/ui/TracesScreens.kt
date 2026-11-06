package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Student
import io.github.ploufty.foteli.data.Trace
import io.github.ploufty.foteli.data.Workshop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dayFormat = SimpleDateFormat("d MMM", Locale.FRENCH)
private val fullFormat = SimpleDateFormat("EEEE d MMMM yyyy 'à' HH'h'mm", Locale.FRENCH)

private const val SOUVENIRS = "Souvenirs"

@Composable
private fun Thumb(trace: Trace, size: Dp, vm: AppViewModel, onClick: () -> Unit) {
    PhotoImage(
        vm.photos.photo(trace.file),
        Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        maxSide = 400,
    )
}

@Composable
private fun CardBox(modifier: Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFDDE3EC), RoundedCornerShape(14.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(12.dp),
    ) { content() }
}

// ---------- Onglet Photos ----------

@Composable
fun PhotosTab(students: List<Student>, workshops: List<Workshop>, traces: List<Trace>, vm: AppViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading("Photos (${traces.size})")
        Spacer(Modifier.weight(1f))
        PhotosView.entries.forEach { v -> Chip(v.label, vm.photosView == v) { vm.photosView = v } }
    }
    if (traces.isEmpty()) Hint("Aucune photo pour l’instant. Les photos validées par les enfants apparaîtront ici.")

    if (vm.photosView == PhotosView.BOARD) {
        Board(students, workshops.filter { it.active }, traces, vm)
    } else if (vm.photosView == PhotosView.WORKSHOP) {
        // Ateliers actifs d'abord, puis les autres, puis Souvenirs.
        val rows: List<Workshop?> = workshops.sortedByDescending { it.active } + listOf(null)
        rows.chunked(3).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                line.forEach { w ->
                    val own = traces.filter { it.workshopId == w?.id }
                    val kids = own.map { it.studentId }.distinct().size
                    CardBox(Modifier.weight(1f), onClick = { vm.go(Screen.WorkshopPhotos(w?.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier
                                    .size(width = 96.dp, height = 72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF8B5CF6)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (w != null) WorkshopArt(w.image, Modifier.fillMaxSize()) else FreePhotoIcon(Modifier.fillMaxSize(0.7f))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(w?.title ?: SOUVENIRS, color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 2)
                                Text(
                                    "$kids/${students.size} élève${if (students.size > 1) "s" else ""} · ${photos(own.size)}",
                                    color = if (w != null && kids < students.size) Navy else Green,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                                if (w != null && !w.active) Hint("inactif")
                            }
                        }
                    }
                }
                repeat(3 - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    } else {
        students.chunked(3).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                line.forEach { s ->
                    val own = traces.filter { it.studentId == s.id }
                    CardBox(Modifier.weight(1f), onClick = { vm.go(Screen.StudentPhotos(s.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StudentFace(s, 56.dp)
                            Column(Modifier.weight(1f)) {
                                Text(s.firstName, color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Hint(photos(own.size))
                            }
                            own.firstOrNull()?.let { Thumb(it, 64.dp, vm) { vm.go(Screen.StudentPhotos(s.id)) } }
                        }
                    }
                }
                repeat(3 - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private enum class PassFilter(val label: String) { ALL("Tous"), NOT_YET("Pas encore passés"), DONE("Déjà passés") }

// ---------- Tableau de suivi des ateliers en cours ----------

/** Élèves en lignes, ateliers actifs en colonnes : ✓ (nombre de photos) ou — . Un toucher ouvre l'atelier. */
@Composable
private fun Board(students: List<Student>, active: List<Workshop>, traces: List<Trace>, vm: AppViewModel) {
    if (active.isEmpty()) {
        Hint("Aucun atelier actif. Le tableau montre les ateliers actifs : activez-les dans l’onglet Ateliers.")
        return
    }
    var onlyTodo by remember { mutableStateOf(false) }
    val done = active.associate { w -> w.id to traces.filter { it.workshopId == w.id }.groupingBy { it.studentId }.eachCount() }
    val shown = if (onlyTodo) students.filter { s -> active.any { (done[it.id]?.get(s.id) ?: 0) == 0 } } else students
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Chip("Tous les élèves", !onlyTodo) { onlyTodo = false }
        Chip("Seulement ceux qui ont un atelier à faire", onlyTodo) { onlyTodo = true }
    }
    WhitePanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 200.dp, height = 1.dp))
            active.forEach { w ->
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { vm.go(Screen.WorkshopPhotos(w.id)) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    WorkshopArt(w.image, Modifier.size(width = 72.dp, height = 54.dp).clip(RoundedCornerShape(8.dp)))
                    Text(w.title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                    Hint("${done[w.id]?.size ?: 0}/${students.size}")
                }
            }
        }
        if (shown.isEmpty()) Hint("Tous les élèves ont fait tous les ateliers actifs.")
        shown.forEach { s ->
            val count = active.count { (done[it.id]?.get(s.id) ?: 0) > 0 }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF7F9FB)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier
                        .size(width = 200.dp, height = 52.dp)
                        .clickable { vm.go(Screen.StudentPhotos(s.id)) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StudentFace(s, 36.dp)
                    Column {
                        Text(s.firstName, color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                        Hint("$count/${active.size}")
                    }
                }
                active.forEach { w ->
                    val n = done[w.id]?.get(s.id) ?: 0
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable { vm.go(Screen.WorkshopPhotos(w.id)) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (n > 0) {
                            Text(if (n > 1) "✓ $n" else "✓", color = Green, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        } else {
                            Text("—", color = Color(0xFFB4BCC8), fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------- Qui est passé à un atelier (B5) ----------

@Composable
fun WorkshopPhotosScreen(state: Screen.WorkshopPhotos, students: List<Student>, workshop: Workshop?, traces: List<Trace>, vm: AppViewModel) {
    val own = traces.filter { it.workshopId == state.workshopId }
    val kids = own.map { it.studentId }.distinct().size
    var filter by remember(state) { mutableStateOf(PassFilter.ALL) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton("← Photos", { vm.go(Screen.Teacher(TeacherTab.PHOTOS)) })
        Heading(workshop?.title ?: SOUVENIRS)
        Spacer(Modifier.weight(1f))
        Text(
            "$kids/${students.size} élève${if (students.size > 1) "s" else ""} ${if (kids > 1) "sont passés" else "est passé"}",
            color = Navy,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PassFilter.entries.forEach { f -> Chip(f.label, filter == f) { filter = f } }
    }
    val passed = own.map { it.studentId }.toSet()
    val shown = when (filter) {
        PassFilter.ALL -> students
        PassFilter.NOT_YET -> students.filter { it.id !in passed }
        PassFilter.DONE -> students.filter { it.id in passed }
    }
    if (shown.isEmpty()) Hint(if (filter == PassFilter.NOT_YET) "Tous les élèves sont passés." else "Personne pour l’instant.")
    shown.chunked(2).forEach { line ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            line.forEach { s ->
                val mine = own.filter { it.studentId == s.id }
                CardBox(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StudentFace(s, 48.dp)
                        Text(s.firstName, color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.size(width = 110.dp, height = 24.dp))
                        if (mine.isEmpty()) {
                            Text("— pas encore", color = Muted, fontSize = 15.sp)
                        } else {
                            mine.take(4).forEach { t -> Thumb(t, 72.dp, vm) { vm.go(Screen.PhotoView(t.id, state)) } }
                            if (mine.size > 4) Hint("+${mine.size - 4}")
                        }
                    }
                }
            }
            repeat(2 - line.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

// ---------- Les traces d'un élève (B6) ----------

@Composable
fun StudentPhotosScreen(state: Screen.StudentPhotos, student: Student?, workshops: List<Workshop>, traces: List<Trace>, vm: AppViewModel) {
    val own = traces.filter { it.studentId == state.studentId }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton("← Photos", { vm.go(Screen.Teacher(TeacherTab.PHOTOS)) })
        if (student != null) StudentFace(student, 48.dp)
        Heading("${student?.firstName.orEmpty()} · ${photos(own.size)}")
    }
    if (own.isEmpty()) Hint("Pas encore de photo.")
    // Groupé par atelier, l'atelier de la photo la plus récente d'abord.
    own.groupBy { it.workshopId }.forEach { (wid, list) ->
        WhitePanel {
            Text(workshops.firstOrNull { it.id == wid }?.title ?: SOUVENIRS, color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            list.chunked(6).forEach { line ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    line.forEach { t ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Thumb(t, 140.dp, vm) { vm.go(Screen.PhotoView(t.id, state)) }
                            Hint(dayFormat.format(Date(t.takenAt)))
                        }
                    }
                }
            }
        }
    }
}

// ---------- Une photo en grand, trier (B7) ----------

@Composable
fun PhotoViewScreen(state: Screen.PhotoView, trace: Trace?, student: Student?, workshop: Workshop?, vm: AppViewModel) {
    if (trace == null) {
        SecondaryButton("← Retour", { vm.go(state.back) })
        Hint("Cette photo n’existe plus.")
        return
    }
    var confirm by remember(trace.id) { mutableStateOf(false) }
    val title = workshop?.title ?: if (trace.workshopId == null) SOUVENIRS else "Atelier supprimé"
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        PhotoImage(
            vm.photos.photo(trace.file),
            Modifier
                .weight(0.64f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF111827)),
            maxSide = 2048,
            contentScale = ContentScale.Fit,
        )
        Column(
            Modifier
                .weight(0.36f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SecondaryButton("← Retour", { vm.go(state.back) })
            WhitePanel {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (student != null) StudentFace(student, 56.dp)
                    Text(student?.firstName.orEmpty(), color = Navy, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Text(title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Hint(fullFormat.format(Date(trace.takenAt)))
                // Compétences au moment de la photo (Q4), même si l'atelier a changé depuis.
                trace.competencies.forEach { id ->
                    val c = vm.referentiel[id]
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(c?.text ?: id, color = Navy, fontSize = 15.sp)
                        if (c != null) DomainTag(c.domainCode)
                    }
                }
            }
            DangerZone {
                if (!confirm) {
                    DangerButton("Supprimer cette photo", { confirm = true }, enabled = true)
                } else {
                    Text(
                        "Supprimer cette photo de ${student?.firstName.orEmpty()} ($title, ${dayFormat.format(Date(trace.takenAt))}) ? Elle sera effacée de la tablette.",
                        color = Navy,
                        fontSize = 16.sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DangerButton("Supprimer", { vm.deleteTrace(trace, state.back) }, enabled = true)
                        SecondaryButton("Annuler", { confirm = false })
                    }
                }
            }
        }
    }
}
