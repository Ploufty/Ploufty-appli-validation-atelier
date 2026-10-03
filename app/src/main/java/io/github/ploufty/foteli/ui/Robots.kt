package io.github.ploufty.foteli.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/** Les 36 robots, tous différents : 9 couleurs × 4 formes de tête, plus yeux, antenne, bouche, oreilles. */
data class RobotSpec(
    val color: Color,
    val head: Int,
    val eyes: Int,
    val antenna: Int,
    val smile: Boolean,
    val ears: Boolean,
)

object Robots {
    const val COUNT = 36

    private val colors = listOf(
        Color(0xFFFF6B6B), Color(0xFFFFA94D), Color(0xFFFFD43B),
        Color(0xFF69DB7C), Color(0xFF38D9A9), Color(0xFF4DABF7),
        Color(0xFF748FFC), Color(0xFFDA77F2), Color(0xFFF783AC),
    )

    fun spec(index: Int): RobotSpec {
        val i = index.mod(COUNT)
        val head = (i / 9) % 4
        return RobotSpec(
            color = colors[i % 9],
            head = head,
            eyes = i % 3,
            antenna = (i + head) % 3,
            smile = i % 4 < 2,
            ears = i % 5 == 0 || i % 5 == 3,
        )
    }
}

@Composable
fun RobotAvatar(index: Int, modifier: Modifier = Modifier) {
    val spec = remember(index) { Robots.spec(index) }
    Canvas(modifier) { drawRobot(spec) }
}

private val Ink = Color(0xFF27324A)

/** Dessin dans un repère 100 × 100, centré et mis à l'échelle de la place disponible. */
private fun DrawScope.drawRobot(r: RobotSpec) {
    val u = size.minDimension / 100f
    val ox = (size.width - 100f * u) / 2f
    val oy = (size.height - 100f * u) / 2f
    fun p(x: Float, y: Float) = Offset(ox + x * u, oy + y * u)
    fun s(w: Float, h: Float) = Size(w * u, h * u)
    val top = listOf(26f, 22f, 32f, 22f)[r.head]

    // Antenne (derrière la tête)
    when (r.antenna) {
        0 -> {
            drawLine(Ink, p(50f, top), p(50f, top - 12f), strokeWidth = 4f * u, cap = StrokeCap.Round)
            drawCircle(r.color, 5f * u, p(50f, top - 14f))
            drawCircle(Ink, 5f * u, p(50f, top - 14f), style = Stroke(3f * u))
        }
        1 -> {
            val zig = Path().apply {
                moveTo(p(44f, top).x, p(44f, top).y)
                lineTo(p(50f, top - 7f).x, p(50f, top - 7f).y)
                lineTo(p(44f, top - 12f).x, p(44f, top - 12f).y)
                lineTo(p(52f, top - 18f).x, p(52f, top - 18f).y)
            }
            drawPath(zig, Ink, style = Stroke(4f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        else -> {
            drawLine(Ink, p(38f, top + 2f), p(32f, top - 10f), strokeWidth = 4f * u, cap = StrokeCap.Round)
            drawLine(Ink, p(62f, top + 2f), p(68f, top - 10f), strokeWidth = 4f * u, cap = StrokeCap.Round)
            drawCircle(Ink, 4f * u, p(32f, top - 11f))
            drawCircle(Ink, 4f * u, p(68f, top - 11f))
        }
    }

    if (r.ears) {
        drawRoundRect(Ink, p(10f, 44f), s(8f, 14f), CornerRadius(3f * u))
        drawRoundRect(Ink, p(82f, 44f), s(8f, 14f), CornerRadius(3f * u))
    }

    // Tête
    when (r.head) {
        0 -> drawRoundRect(r.color, p(22f, 26f), s(56f, 46f), CornerRadius(12f * u))
        1 -> drawCircle(r.color, 28f * u, p(50f, 50f))
        2 -> drawRoundRect(r.color, p(14f, 32f), s(72f, 38f), CornerRadius(10f * u))
        else -> {
            val dome = Path().apply {
                moveTo(p(28f, 74f).x, p(28f, 74f).y)
                lineTo(p(28f, 44f).x, p(28f, 44f).y)
                arcTo(Rect(p(28f, 22f), p(72f, 66f)), 180f, 180f, false)
                lineTo(p(72f, 74f).x, p(72f, 74f).y)
                close()
            }
            drawPath(dome, r.color)
        }
    }

    // Yeux
    when (r.eyes) {
        0 -> {
            drawCircle(Color.White, 7f * u, p(39f, 48f))
            drawCircle(Color.White, 7f * u, p(61f, 48f))
            drawCircle(Ink, 3.5f * u, p(40f, 49f))
            drawCircle(Ink, 3.5f * u, p(62f, 49f))
        }
        1 -> {
            drawRoundRect(Ink, p(30f, 41f), s(40f, 13f), CornerRadius(6.5f * u))
            drawCircle(Color(0xFF7CF5FF), 3f * u, p(41f, 47.5f))
            drawCircle(Color(0xFF7CF5FF), 3f * u, p(59f, 47.5f))
        }
        else -> {
            drawCircle(Color.White, 10f * u, p(50f, 47f))
            drawCircle(Ink, 5f * u, p(51f, 48f))
        }
    }

    // Bouche
    if (r.smile) {
        val mouth = Path().apply {
            moveTo(p(40f, 61f).x, p(40f, 61f).y)
            quadraticTo(p(50f, 69f).x, p(50f, 69f).y, p(60f, 61f).x, p(60f, 61f).y)
        }
        drawPath(mouth, Ink, style = Stroke(3.5f * u, cap = StrokeCap.Round))
    } else {
        drawRoundRect(Ink, p(39f, 59f), s(22f, 6f), CornerRadius(3f * u))
    }

    // Cou
    drawRoundRect(r.color.copy(alpha = 0.7f), p(36f, 78f), s(28f, 12f), CornerRadius(4f * u))
}
