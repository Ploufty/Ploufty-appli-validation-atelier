package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Competency
import io.github.ploufty.foteli.data.ImageKeys
import io.github.ploufty.foteli.data.Referentiel
import io.github.ploufty.foteli.data.Trace
import io.github.ploufty.foteli.data.Workshop

// ---------- Domaines : nom court et couleur ----------

private val domainStyles = mapOf(
    "LAN" to ("Langage" to Color(0xFF2C5FD6)),
    "EPS" to ("Activités physiques" to Color(0xFF1E7D45)),
    "ART" to ("Arts" to Color(0xFFA3307F)),
    "MAT" to ("Maths" to Color(0xFFB4410C)),
    "TES" to ("Temps et espace" to Color(0xFF0F6E66)),
    "VIV" to ("Vivant, matière, objets" to Color(0xFF6D28D9)),
)

private fun domainLabel(code: String) = domainStyles[code]?.first ?: code
private fun domainColor(code: String) = domainStyles[code]?.second ?: Navy

/** Étiquette courte et colorée d'un domaine (« Maths », « Langage »…). */
@Composable
fun DomainTag(code: String) {
    val color = domainColor(code)
    Text(
        domainLabel(code),
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
private fun LevelBadge(level: String) {
    Text(
        level,
        color = Navy,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE8ECF2))
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

private enum class PickerTab(val label: String) { SEARCH("🔍 Rechercher"), BROWSE("📚 Parcourir"), USED("⭐ Déjà utilisées") }

/** Exemples proposés quand la recherche est vide : un toucher lance la recherche. */
private val searchExamples = listOf("puzzle", "kapla", "perles", "peinture", "compter", "prénom", "parcours", "comptines")

// ---------- Créer / modifier un atelier ----------

/**
 * Atelier en une minute : à gauche l'atelier (titre, image, caméra, compétences choisies, enregistrer),
 * à droite l'ajout des compétences (recherche, programme, déjà utilisées). Sur un petit écran, tout s'empile.
 */
@Composable
fun EditWorkshopScreen(workshop: Workshop?, workshops: List<Workshop>, traces: List<Trace>, vm: AppViewModel) {
    var title by remember(workshop) { mutableStateOf(workshop?.title.orEmpty()) }
    var image by remember(workshop) { mutableStateOf(workshop?.image ?: WorkshopImage.PUZZLE.key) }
    // Photo du modèle (enregistrée ou tout juste prise), gardée même si une illustration est choisie entre-temps.
    var model by remember(workshop) { mutableStateOf(workshop?.image?.takeIf { ImageKeys.isPhoto(it) }) }
    var selected by remember(workshop) { mutableStateOf(workshop?.competencies.orEmpty()) }
    var front by remember(workshop) { mutableStateOf(workshop?.frontCamera ?: false) }
    val toggle: (String) -> Unit = { id -> selected = if (id in selected) selected - id else selected + id }

    val details: @Composable ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("← Ateliers", { vm.go(Screen.Teacher(TeacherTab.WORKSHOPS)) })
            Heading(if (workshop == null) "Nouvel atelier" else "Modifier l’atelier")
        }
        WorkshopDetails(
            title = title,
            onTitle = { title = it },
            image = image,
            model = model,
            onImage = { image = it },
            onPhoto = {
                vm.openTeacherCamera { file ->
                    model = ImageKeys.PENDING + file.name
                    image = model!!
                }
            },
            front = front,
            onFront = { front = it },
            selected = selected,
            onRemove = toggle,
            ref = vm.referentiel,
        )
    }
    val save: @Composable () -> Unit = { SaveBar(workshop, title, image, selected, front, vm) }
    val danger: @Composable () -> Unit = { if (workshop != null) DeleteWorkshopZone(workshop, traces, vm) }
    val others = workshops.filter { it.id != workshop?.id }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Deux colonnes sur tablette ; sur un écran bas (téléphone), tout s'empile.
        if (maxWidth >= 900.dp && maxHeight >= 480.dp) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(0.44f).fillMaxHeight()) {
                    Column(
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        details()
                        danger()
                    }
                    save()
                }
                CompetencyPicker(selected, toggle, others, vm, Modifier.weight(0.56f).fillMaxHeight())
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                details()
                CompetencyPicker(selected, toggle, others, vm, Modifier.fillMaxWidth().height(560.dp))
                save()
                danger()
            }
        }
    }
}

@Composable
private fun WorkshopDetails(
    title: String,
    onTitle: (String) -> Unit,
    image: String,
    model: String?,
    onImage: (String) -> Unit,
    onPhoto: () -> Unit,
    front: Boolean,
    onFront: (Boolean) -> Unit,
    selected: List<String>,
    onRemove: (String) -> Unit,
    ref: Referentiel,
) {
    val focus = LocalFocusManager.current
    WhitePanel {
        OutlinedTextField(
            value = title,
            onValueChange = onTitle,
            label = { Text("Titre (ex. Puzzle 12 pièces)") },
            singleLine = true,
            // « OK » sur le clavier le referme.
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            modifier = Modifier.fillMaxWidth(),
        )

    }
    WhitePanel {
        Text("Compétences choisies (${selected.size})", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        if (selected.isEmpty()) Hint("Aucune pour l’instant : cherchez un mot à droite (ex. « puzzle »), puis touchez la compétence.")
        selected.forEach { id ->
            val c = ref[id]
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF3F5F8))
                    .padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(c?.text ?: id, color = Navy, fontSize = 15.sp)
                    if (c != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DomainTag(c.domainCode)
                            LevelBadge(c.level)
                        }
                    }
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onRemove(id) }
                        .semantics { contentDescription = "Retirer cette compétence" },
                    contentAlignment = Alignment.Center,
                ) { Text("✕", color = Muted, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
    WhitePanel {
        Text("Image pour les enfants", color = Navy, fontWeight = FontWeight.Bold)
        val tiles = listOfNotNull(model?.let { it to "Photo du modèle" }) + WorkshopImage.entries.map { it.key to it.label }
        tiles.chunked(4).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                line.forEach { (key, label) ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        WorkshopArt(
                            key,
                            Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(3.dp, if (image == key) Blue else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { onImage(key) },
                        )
                        Text(label, color = Muted, fontSize = 13.sp, maxLines = 1)
                    }
                }
                repeat(4 - line.size) { Box(Modifier.weight(1f)) }
            }
        }
        SecondaryButton(if (model == null) "📷 Photographier le modèle" else "📷 Reprendre la photo du modèle", onPhoto)

        Text("Caméra des enfants", color = Navy, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Arrière · production", !front) { onFront(false) }
            Chip("Avant · posture, mime", front) { onFront(true) }
        }
    }
}

@Composable
private fun SaveBar(workshop: Workshop?, title: String, image: String, selected: List<String>, front: Boolean, vm: AppViewModel) {
    val ready = title.isNotBlank() && selected.isNotEmpty()
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFDDE3EC), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!ready) {
            Hint(
                when {
                    title.isBlank() && selected.isEmpty() -> "Il manque un titre et au moins une compétence."
                    title.isBlank() -> "Il manque le titre."
                    else -> "Il manque au moins une compétence."
                },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (workshop?.active == true) {
                PrimaryButton("Enregistrer", { vm.saveWorkshop(workshop.id, title, image, selected, front, activate = false) }, enabled = ready)
            } else {
                PrimaryButton("Enregistrer et activer", { vm.saveWorkshop(workshop?.id, title, image, selected, front, activate = true) }, enabled = ready)
                SecondaryButton("Sans activer", { if (ready) vm.saveWorkshop(workshop?.id, title, image, selected, front, activate = false) })
            }
        }
    }
}

@Composable
private fun DeleteWorkshopZone(workshop: Workshop, traces: List<Trace>, vm: AppViewModel) {
    var confirm by remember(workshop) { mutableStateOf("") }
    val own = traces.filter { it.workshopId == workshop.id }
    val kids = own.map { it.studentId }.distinct().size
    val focus = LocalFocusManager.current
    DangerZone {
        Text(
            if (own.isEmpty()) {
                "Supprimer « ${workshop.title} » : aucune photo dans cet atelier pour l’instant."
            } else {
                "Supprimer « ${workshop.title} » efface aussi ${if (own.size > 1) "ses" else "sa"} ${photos(own.size)} " +
                    "($kids élève${if (kids > 1) "s" else ""}). Pour garder les photos, désactivez plutôt l’atelier."
            },
            color = Navy,
            fontSize = 16.sp,
        )
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("Pour confirmer, tapez le titre « ${workshop.title} »") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            modifier = Modifier.fillMaxWidth(),
        )
        DangerButton("Supprimer l’atelier", { vm.deleteWorkshop(workshop.id) }, enabled = confirm.trim() == workshop.title)
    }
}

// ---------- Ajouter des compétences ----------

@Composable
private fun CompetencyPicker(selected: List<String>, onToggle: (String) -> Unit, others: List<Workshop>, vm: AppViewModel, modifier: Modifier) {
    val ref = vm.referentiel
    val focus = LocalFocusManager.current
    var tab by remember { mutableStateOf(PickerTab.SEARCH) }
    var query by remember { mutableStateOf(vm.demoSearch) }
    var domain by remember { mutableStateOf<String?>(null) }
    var subdomain by remember { mutableStateOf<String?>(null) }
    val levels = vm.searchLevels

    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFDDE3EC), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ajouter des compétences", color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickerTab.entries.forEach { t -> Chip(t.label, tab == t) { tab = t } }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Hint("Niveaux :")
            listOf("PS", "MS", "GS").forEach { lv ->
                Chip(lv, lv in levels) { vm.chooseSearchLevels(if (lv in levels) levels - lv else levels + lv) }
            }
        }

        when (tab) {
            PickerTab.SEARCH -> {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Un mot : puzzle, kapla, peinture, compter…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { query = "" }
                                    .semantics { contentDescription = "Effacer la recherche" },
                                contentAlignment = Alignment.Center,
                            ) { Text("✕", color = Muted, fontSize = 18.sp) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (Referentiel.searchTokens(query).isEmpty()) {
                    Hint("Les mots de la classe marchent aussi : « kapla » trouve les assemblages, « perles » les motifs.")
                    searchExamples.chunked(4).forEach { line ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            line.forEach { ex -> Chip(ex, false) { query = ex } }
                        }
                    }
                } else {
                    val results = remember(query, levels) { ref.search(query, levels) }
                    val terms = remember(query) { Referentiel.highlightTerms(query) }
                    if (results.isEmpty()) {
                        Hint("Aucune compétence trouvée${if (levels.size < 3) " pour ${levels.sorted().joinToString(", ")}" else ""}. Essayez un autre mot, ou l’onglet « Parcourir ».")
                    } else {
                        Hint("${results.size} résultat${if (results.size > 1) "s" else ""}")
                        CompetencyList(results, selected, onToggle, terms, Modifier.weight(1f))
                    }
                }
            }
            PickerTab.BROWSE -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ref.domains.take(3).forEach { (code, _) -> Chip(domainLabel(code), domain == code) { domain = code; subdomain = null } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ref.domains.drop(3).forEach { (code, _) -> Chip(domainLabel(code), domain == code) { domain = code; subdomain = null } }
                }
                val d = domain
                if (d == null) {
                    Hint("Touchez un domaine du programme.")
                } else {
                    val sd = subdomain
                    if (sd == null) {
                        Hint(ref.domainName(d))
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ref.subdomains(d)) { (code, name) ->
                                Text(
                                    "$name  ›",
                                    color = Navy,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF3F5F8))
                                        .clickable { subdomain = code }
                                        .padding(14.dp),
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SecondaryButton("‹ Sous-domaines", { subdomain = null })
                            Text(
                                ref.subdomains(d).firstOrNull { it.first == sd }?.second.orEmpty(),
                                color = Navy,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        val list = ref.list(d, sd, levels)
                        if (list.isEmpty()) Hint("Rien à ces niveaux dans ce sous-domaine.")
                        CompetencyList(list, selected, onToggle, emptyList(), Modifier.weight(1f))
                    }
                }
            }
            PickerTab.USED -> {
                // Les compétences des autres ateliers, les plus fréquentes d'abord.
                val used = remember(others, levels) {
                    others.flatMap { it.competencies }.groupingBy { it }.eachCount()
                        .entries.sortedByDescending { it.value }
                        .mapNotNull { ref[it.key] }
                        .filter { it.level in levels }
                }
                if (used.isEmpty()) {
                    Hint("Les compétences de vos autres ateliers apparaîtront ici, les plus utilisées d’abord.")
                } else {
                    CompetencyList(used, selected, onToggle, emptyList(), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompetencyList(list: List<Competency>, selected: List<String>, onToggle: (String) -> Unit, terms: List<String>, modifier: Modifier) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(list, key = { it.id }) { c ->
            val on = c.id in selected
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) Color(0xFFE6F4EC) else Color(0xFFF3F5F8))
                    .clickable { onToggle(c.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (on) Green else Color.White)
                        .border(2.dp, if (on) Green else Color(0xFFC6CFDC), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(if (on) "✓" else "+", color = if (on) Color.White else Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(highlight(c.text, terms), color = Navy, fontSize = 15.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        DomainTag(c.domainCode)
                        LevelBadge(c.level)
                        Text(c.subdomain, color = Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

/** Met en gras les mots qui commencent par un terme cherché (mot tapé ou synonyme). */
private fun highlight(text: String, terms: List<String>): AnnotatedString {
    if (terms.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        Regex("[\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+").findAll(text).forEach { m ->
            val part = m.value
            val norm = Referentiel.normalize(part)
            if (part.first().isLetterOrDigit() && terms.any { norm.startsWith(it) }) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, background = Color(0xFFFFF1B8))) { append(part) }
            } else {
                append(part)
            }
        }
    }
}
