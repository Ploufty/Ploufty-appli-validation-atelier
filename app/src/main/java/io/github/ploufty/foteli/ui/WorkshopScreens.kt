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
import io.github.ploufty.foteli.data.ImageKeys
import io.github.ploufty.foteli.data.Trace
import io.github.ploufty.foteli.data.Workshop

private fun domainsOf(w: Workshop): List<String> = w.competencies.map { it.substringBefore('-') }.distinct()

// ---------- Liste des ateliers ----------

@Composable
fun WorkshopsTab(workshops: List<Workshop>, traces: List<Trace>, freeMode: Boolean, vm: AppViewModel) {
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
            row.forEach { w -> WorkshopAdminCard(w, traces.count { it.workshopId == w.id }, vm, Modifier.weight(1f)) }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun WorkshopAdminCard(w: Workshop, photoCount: Int, vm: AppViewModel, modifier: Modifier) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { domainsOf(w).forEach { DomainTag(it) } }
            Hint("${w.competencies.size} compétence${if (w.competencies.size > 1) "s" else ""} · caméra ${if (w.frontCamera) "avant" else "arrière"}")
            Text(photos(photoCount), color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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

