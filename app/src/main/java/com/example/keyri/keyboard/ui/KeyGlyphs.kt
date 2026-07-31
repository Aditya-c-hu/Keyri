package com.example.keyri.keyboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

enum class KeyIconType { Shift, Backspace, Enter, Emoji }

/**
 * Crisp vector key icons drawn on Canvas. Font/emoji glyphs render differently
 * per device (often as color emoji), which looks muddy; these stay monochrome
 * and sharp everywhere.
 */
@Composable
fun KeyGlyph(
    type: KeyIconType,
    tint: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    capsBar: Boolean = false
) {
    val dim = if (type == KeyIconType.Backspace) DpSize(22.dp, 20.dp) else DpSize(20.dp, 20.dp)
    Canvas(modifier.size(dim)) {
        val w = size.width
        val h = size.height
        val sw = 1.8.dp.toPx()
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (type) {
            KeyIconType.Shift -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.06f)
                    lineTo(w * 0.94f, h * 0.52f)
                    lineTo(w * 0.66f, h * 0.52f)
                    lineTo(w * 0.66f, h * 0.8f)
                    lineTo(w * 0.34f, h * 0.8f)
                    lineTo(w * 0.34f, h * 0.52f)
                    lineTo(w * 0.06f, h * 0.52f)
                    close()
                }
                if (filled) drawPath(path, tint) else drawPath(path, tint, style = stroke)
                if (capsBar) {
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.34f, h * 0.96f),
                        end = Offset(w * 0.66f, h * 0.96f),
                        strokeWidth = sw,
                        cap = StrokeCap.Round
                    )
                }
            }

            KeyIconType.Backspace -> {
                val body = Path().apply {
                    moveTo(w * 0.36f, h * 0.16f)
                    lineTo(w * 0.92f, h * 0.16f)
                    lineTo(w * 0.92f, h * 0.84f)
                    lineTo(w * 0.36f, h * 0.84f)
                    lineTo(w * 0.06f, h * 0.5f)
                    close()
                }
                drawPath(body, tint, style = stroke)
                drawLine(tint, Offset(w * 0.5f, h * 0.36f), Offset(w * 0.72f, h * 0.64f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.72f, h * 0.36f), Offset(w * 0.5f, h * 0.64f), sw, StrokeCap.Round)
            }

            KeyIconType.Enter -> {
                val path = Path().apply {
                    moveTo(w * 0.8f, h * 0.2f)
                    lineTo(w * 0.8f, h * 0.6f)
                    lineTo(w * 0.22f, h * 0.6f)
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(w * 0.38f, h * 0.44f), Offset(w * 0.2f, h * 0.6f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.38f, h * 0.76f), Offset(w * 0.2f, h * 0.6f), sw, StrokeCap.Round)
            }

            KeyIconType.Emoji -> {
                drawCircle(tint, radius = w * 0.42f, center = center, style = Stroke(sw))
                drawCircle(tint, radius = sw * 0.62f, center = Offset(w * 0.37f, h * 0.41f))
                drawCircle(tint, radius = sw * 0.62f, center = Offset(w * 0.63f, h * 0.41f))
                drawArc(
                    color = tint,
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(w * 0.28f, h * 0.28f),
                    size = Size(w * 0.44f, h * 0.44f),
                    style = Stroke(sw, cap = StrokeCap.Round)
                )
            }
        }
    }
}
