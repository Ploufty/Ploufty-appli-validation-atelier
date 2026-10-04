package io.github.ploufty.foteli.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/** Illustrations provisoires des ateliers, en attendant la photo du modèle (version 0.4). */
enum class WorkshopImage(val key: String, val label: String) {
    PUZZLE("puzzle", "Puzzle"),
    KAPLA("kapla", "Construction"),
    PERLES("perles", "Perles"),
    GRAPHISME("graphisme", "Graphisme"),
    FORMES("formes", "Formes"),
    PEINTURE("peinture", "Peinture"),
    ;

    companion object {
        fun of(key: String) = entries.firstOrNull { it.key == key } ?: PUZZLE
    }
}

private val Wood = Color(0xFFC9A77C)
private val WoodLine = Color(0x2E6B4A2A)
private val Plank = Color(0xFFEFC98F)
private val PlankEdge = Color(0xFFA9793F)

@Composable
fun WorkshopArt(key: String, modifier: Modifier = Modifier) {
    val image = WorkshopImage.of(key)
    Canvas(modifier) {
        drawRect(Wood)
        listOf(0.18f, 0.43f, 0.69f, 0.9f).forEach { y ->
            drawLine(WoodLine, Offset(0f, size.height * y), Offset(size.width, size.height * y), strokeWidth = size.height * 0.01f)
        }
        // Repère 160 × 120, centré
        val u = minOf(size.width / 160f, size.height / 120f)
        val ox = (size.width - 160f * u) / 2f
        val oy = (size.height - 120f * u) / 2f
        val draw = ArtScope(this, u, ox, oy)
        when (image) {
            WorkshopImage.PUZZLE -> draw.puzzle()
            WorkshopImage.KAPLA -> draw.kapla()
            WorkshopImage.PERLES -> draw.perles()
            WorkshopImage.GRAPHISME -> draw.graphisme()
            WorkshopImage.FORMES -> draw.formes()
            WorkshopImage.PEINTURE -> draw.peinture()
        }
    }
}

private class ArtScope(val d: DrawScope, val u: Float, val ox: Float, val oy: Float) {
    fun p(x: Float, y: Float) = Offset(ox + x * u, oy + y * u)
    fun s(w: Float, h: Float) = Size(w * u, h * u)

    fun puzzle() {
        d.drawRoundRect(Color(0xFFF8F9FA), p(36f, 14f), s(88f, 88f), CornerRadius(6f * u))
        val colors = listOf(Color(0xFF4DABF7), Color(0xFFFFD43B), Color(0xFFFF6B6B), Color(0xFF69DB7C))
        listOf(40f to 18f, 80f to 18f, 40f to 58f, 80f to 58f).forEachIndexed { i, (x, y) ->
            d.drawRoundRect(colors[i], p(x, y), s(38f, 38f), CornerRadius(4f * u))
        }
        d.drawCircle(colors[0], 6f * u, p(79f, 37f))
        d.drawCircle(colors[0], 6f * u, p(59f, 57f))
        d.drawCircle(colors[1], 6f * u, p(99f, 57f))
        d.drawCircle(colors[2], 6f * u, p(79f, 77f))
    }

    fun kapla() {
        for (k in 0 until 5) {
            val y = 96f - k * 13f
            if (k % 2 == 0) {
                d.drawRect(Plank, p(44f, y), s(72f, 10f))
                d.drawRect(PlankEdge, p(44f, y), s(72f, 10f), style = Stroke(1f * u))
            } else {
                listOf(48f, 102f).forEach { x ->
                    d.drawRect(Plank, p(x, y), s(10f, 10f))
                    d.drawRect(PlankEdge, p(x, y), s(10f, 10f), style = Stroke(1f * u))
                }
            }
        }
    }

    fun perles() {
        val string = Path().apply {
            moveTo(p(20f, 40f).x, p(20f, 40f).y)
            quadraticTo(p(80f, 110f).x, p(80f, 110f).y, p(140f, 40f).x, p(140f, 40f).y)
        }
        d.drawPath(string, Color(0xFF343A40), style = Stroke(1.5f * u))
        for (k in 0..10) {
            val t = k / 10f
            val x = (1 - t) * (1 - t) * 20f + 2 * (1 - t) * t * 80f + t * t * 140f
            val y = (1 - t) * (1 - t) * 40f + 2 * (1 - t) * t * 110f + t * t * 40f
            d.drawCircle(if (k % 3 == 2) Color(0xFF4DABF7) else Color(0xFFFF6B6B), 6.5f * u, p(x, y))
            d.drawCircle(Color.White, 6.5f * u, p(x, y), style = Stroke(1.5f * u))
        }
    }

    fun graphisme() {
        d.drawRect(Color(0xFFFDFDFD), p(28f, 14f), s(104f, 92f))
        listOf(56f to 40f, 104f to 40f, 56f to 82f, 104f to 82f).forEach { (cx, cy) ->
            val spiral = Path()
            for (step in 0..60) {
                val a = step * 0.35f
                val r = 1.5f + step * 0.28f
                val pt = p(cx + r * kotlin.math.cos(a), cy + r * kotlin.math.sin(a))
                if (step == 0) spiral.moveTo(pt.x, pt.y) else spiral.lineTo(pt.x, pt.y)
            }
            d.drawPath(spiral, Color(0xFF364FC7), style = Stroke(2.4f * u, cap = StrokeCap.Round))
        }
    }

    fun formes() {
        d.drawCircle(Color(0xFFFF6B6B), 18f * u, p(48f, 44f))
        d.drawRect(Color(0xFF4DABF7), p(86f, 26f), s(36f, 36f))
        val tri = Path().apply {
            moveTo(p(60f, 106f).x, p(60f, 106f).y)
            lineTo(p(84f, 66f).x, p(84f, 66f).y)
            lineTo(p(108f, 106f).x, p(108f, 106f).y)
            close()
        }
        d.drawPath(tri, Color(0xFFFFD43B))
    }

    fun peinture() {
        d.drawRect(Color(0xFFFDFDFD), p(24f, 16f), s(112f, 88f))
        val colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFD43B), Color(0xFF69DB7C), Color(0xFF4DABF7), Color(0xFFDA77F2))
        colors.forEachIndexed { i, c ->
            d.drawCircle(c, 13f * u, p(44f + i * 18f, 46f + (i % 2) * 22f))
        }
        d.drawLine(Color(0xFF8D5524), p(100f, 100f), p(132f, 70f), strokeWidth = 4f * u, cap = StrokeCap.Round)
    }
}

/** Icône de la carte « Photo libre » (mode libre, Souvenirs). */
@Composable
fun FreePhotoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.minDimension / 100f
        val ox = (size.width - 100f * u) / 2f
        val oy = (size.height - 100f * u) / 2f
        fun p(x: Float, y: Float) = Offset(ox + x * u, oy + y * u)
        drawRoundRect(Color.White, p(14f, 30f), Size(72f * u, 52f * u), CornerRadius(10f * u))
        drawRoundRect(Color.White, p(36f, 22f), Size(28f * u, 12f * u), CornerRadius(4f * u))
        drawCircle(Color(0xFF8B5CF6), 17f * u, p(50f, 56f))
        drawCircle(Color(0xFFE9DDFF), 10f * u, p(50f, 56f))
        drawCircle(Color(0xFFFFD43B), 5f * u, p(82f, 18f))
        drawCircle(Color(0xFFFFD43B), 3.5f * u, p(18f, 14f))
    }
}
