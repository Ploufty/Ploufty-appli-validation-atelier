package io.github.ploufty.foteli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ploufty.foteli.data.Workshop

private fun domainsOf(w: Workshop): List<String> = w.competencies.map { it.substringBefore('-') }.distinct()

// ---------- Liste des ateliers ----------

@Composable
fun WorkshopsTab(workshops: List<Workshop>, freeMode: Boolean, vm: AppViewModel) {
    val activeCount = workshops.count { it.active }
    val cards = activeCount + if (freeMode) 1 else 0
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading("Ateliers")
        Hint("$activeCount actif${if (activeCount > 1) "s" else ""} côté enfants")
        Spacer(Modifier.weight(1f))
        PrimaryButton("+ Nouvel atelier", { vm.go(Screen.EditWorkshop(null)) })
        if (activeCount > 0) SecondaryButton("Tout désactiver", { vm.deactivateAllWorkshops() })
    }
    if (cards > MAX_COMFORTABLE_CARDS) {
        Text(
            "Plus de $MAX_COMFORTABLE_CARDS cartes côté enfants${if (freeMode) " (Photo libre comprise)" else ""} : elles deviennent petites. Pensez à désactiver les ateliers terminés.",
            color = Color(0xFF5B4A12),
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF6DC))
                .padding(12.dp),
        )
    }
    if (workshops.isEmpty()) {
        Hint("Aucun atelier. Touchez « + Nouvel atelier » : titre, image, compétences, et c’est prêt.")
    }
    workshops.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            row.forEach { w -> WorkshopAdminCard(w, vm, Modifier.weight(1f)) }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun WorkshopAdminCard(w: Workshop, vm: AppViewModel, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFDDE3EC), RoundedCornerShape(14.dp)),
    ) {
        WorkshopArt(
            w.image,
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .alpha(if (w.active) 1f else 0.45f),
        )
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(w.title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 2)
            domainsOf(w).forEach { code ->
                Text(
                    vm.referentiel.domainName(code),
                    color = Color(0xFF1F3F8F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE3EBFB))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
            Hint("${w.competencies.size} compétence${if (w.competencies.size > 1) "s" else ""} · caméra ${if (w.frontCamera) "avant" else "arrière"}")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = w.active, onCheckedChange = { vm.setWorkshopActive(w.id, it) })
                Text(if (w.active) "Actif" else "Inactif", color = Navy, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("Modifier", { vm.go(Screen.EditWorkshop(w.id)) })
                SecondaryButton("Dupliquer", { vm.duplicateWorkshop(w.id) })
            }
        }
    }
}

// ---------- Créer / modifier un atelier ----------

@Composable
fun EditWorkshopScreen(workshop: Workshop?, vm: AppViewModel) {
    val ref = vm.referentiel
    var title by remember(workshop) { mutableStateOf(workshop?.title.orEmpty()) }
    var image by remember(workshop) { mutableStateOf(workshop?.image ?: WorkshopImage.PUZZLE.key) }
    var selected by remember(workshop) { mutableStateOf(workshop?.competencies.orEmpty()) }
    var front by remember(workshop) { mutableStateOf(workshop?.frontCamera ?: false) }
    val first = workshop?.competencies?.firstOrNull()?.let { ref[it] }
    var domain by remember(workshop) { mutableStateOf(first?.domainCode) }
    var subdomain by remember(workshop) { mutableStateOf(first?.subdomainCode) }
    var level by remember(workshop) { mutableStateOf(first?.level) }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton("← Ateliers", { vm.go(Screen.Teacher(TeacherTab.WORKSHOPS)) })
        Heading(if (workshop == null) "Nouvel atelier" else "Modifier l’atelier")
    }
    WhitePanel {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre (ex. Puzzle 12 pièces)") },
            singleLine = true,
            modifier = Modifier.width(460.dp),
        )

        Text("Image de l’atelier", color = Navy, fontWeight = FontWeight.Bold)
        Hint("Les enfants reconnaîtront l’atelier grâce à elle. Avec la version 0.4, vous pourrez la remplacer par une photo du modèle.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WorkshopImage.entries.forEach { img ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WorkshopArt(
                        img.key,
                        Modifier
                            .size(width = 128.dp, height = 96.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(3.dp, if (image == img.key) Blue else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { image = img.key },
                    )
                    Hint(img.label)
                }
            }
        }

        Text("Compétences", color = Navy, fontWeight = FontWeight.Bold)
        if (selected.isEmpty()) Hint("Aucune compétence choisie : touchez un domaine, puis un sous-domaine et un niveau, et cochez.")
        selected.forEach { id ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF3F5F8))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(id, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Muted)
                Text(ref[id]?.text ?: id, color = Navy, fontSize = 15.sp, modifier = Modifier.weight(1f))
                SecondaryButton("Retirer", { selected = selected - id })
            }
        }
        ChipRows(ref.domains, domain) { domain = it; subdomain = null; level = null }
        domain?.let { d -> ChipRows(ref.subdomains(d), subdomain) { subdomain = it; if (level == null) level = "MS" } }
        if (subdomain != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PS", "MS", "GS").forEach { n -> Chip(n, level == n) { level = n } }
            }
        }
        val d = domain
        val sd = subdomain
        val lv = level
        if (d != null && sd != null && lv != null) {
            val list = ref.list(d, sd, lv)
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFDDE3EC), RoundedCornerShape(10.dp))
                    .padding(6.dp),
            ) {
                if (list.isEmpty()) Hint("Aucune compétence à ce niveau dans ce sous-domaine.")
                list.forEach { c ->
                    val checked = c.id in selected
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selected = if (checked) selected - c.id else selected + c.id },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { selected = if (it) selected + c.id else selected - c.id })
                        Text(c.text, color = Navy, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
            Hint("Choix multiple possible, y compris dans plusieurs domaines.")
        }

        Text("Caméra utilisée par les enfants", color = Navy, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Arrière · production posée", !front) { front = false }
            Chip("Avant · posture, mime", front) { front = true }
        }
        Hint("Les enfants ne peuvent pas la changer.")

        val ready = title.isNotBlank() && selected.isNotEmpty()
        if (!ready) Hint("Pour enregistrer : un titre et au moins une compétence.")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (workshop?.active == true) {
                PrimaryButton("Enregistrer", { vm.saveWorkshop(workshop.id, title, image, selected, front, activate = false) }, enabled = ready)
            } else {
                PrimaryButton("Enregistrer et activer", { vm.saveWorkshop(workshop?.id, title, image, selected, front, activate = true) }, enabled = ready)
                SecondaryButton("Enregistrer sans activer", { if (ready) vm.saveWorkshop(workshop?.id, title, image, selected, front, activate = false) })
            }
        }
    }
    if (workshop != null) {
        var confirm by remember(workshop) { mutableStateOf("") }
        DangerZone {
            Text(
                "Supprimer « ${workshop.title} » efface aussi toutes ses photos. Pour garder les photos, désactivez plutôt l’atelier.",
                color = Navy,
                fontSize = 16.sp,
            )
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Pour confirmer, tapez le titre « ${workshop.title} »") },
                singleLine = true,
                modifier = Modifier.width(520.dp),
            )
            DangerButton("Supprimer l’atelier", { vm.deleteWorkshop(workshop.id) }, enabled = confirm.trim() == workshop.title)
        }
    }
}

/** Puces sur plusieurs lignes (domaines, sous-domaines), sans dépendre d'une mise en page expérimentale. */
@Composable
private fun ChipRows(items: List<Pair<String, String>>, selected: String?, onPick: (String) -> Unit) {
    val perRow = 2
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(perRow).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                line.forEach { (code, name) -> Chip(name, selected == code) { onPick(code) } }
            }
        }
    }
}
