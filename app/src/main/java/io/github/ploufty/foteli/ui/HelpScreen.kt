package io.github.ploufty.foteli.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Aide intégrée : affiche docs/guide-enseignant.md (embarqué dans l'appli à la compilation).
 * Mise en forme minimale : « ## » = rubrique, « - » = point.
 */
@Composable
fun HelpScreen() {
    val context = LocalContext.current
    val sections = remember {
        val text = runCatching { context.assets.open("guide-enseignant.md").bufferedReader().readText() }.getOrDefault("")
        text.split("\n## ").drop(1).map { block ->
            val lines = block.lines()
            lines.first().trim() to lines.drop(1).filter { it.isNotBlank() }
        }
    }
    Heading("Aide")
    sections.forEach { (title, lines) ->
        WhitePanel {
            Text(title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                lines.forEach { line ->
                    Text(if (line.startsWith("- ")) "•  " + line.removePrefix("- ") else line, color = Navy, fontSize = 16.sp)
                }
            }
        }
    }
}
