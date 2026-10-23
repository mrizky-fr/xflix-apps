package com.xflix.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xflix.app.data.Face
import com.xflix.app.data.Profile
import com.xflix.app.ui.theme.BigShoulders

private val KidsLetters = listOf(Color(0xFFFF5A5F), Color(0xFFFFC23D), Color(0xFF4CD07D), Color(0xFF3FA9FF))

/** Avatar wajah, digambar canvas */
@Composable
fun ProfileAvatar(profile: Profile, modifier: Modifier = Modifier, corner: Dp = 6.dp) {
    Box(
        modifier
            .clip(RoundedCornerShape(corner))
            .background(profile.color),
        contentAlignment = Alignment.Center,
    ) {
        if (profile.face == Face.Kids) {
            KidsLabel()
        } else {
            val ink = if (profile.color.luminance() > 0.5f) Color(0xFF2B1D00) else Color.White
            Canvas(Modifier.fillMaxSize()) { drawFace(profile.face, ink) }
        }
    }
}

@Composable
private fun KidsLabel() {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val size = with(LocalDensity.current) { (maxWidth * 0.36f).toSp() }
        Text(
            buildAnnotatedString {
                "anak".forEachIndexed { i, c ->
                    withStyle(SpanStyle(color = KidsLetters[i % KidsLetters.size])) { append(c) }
                }
            },
            style = TextStyle(fontFamily = BigShoulders, fontWeight = FontWeight.Black, fontSize = size),
        )
    }
}

private fun DrawScope.drawFace(face: Face, ink: Color) {
    val w = size.width
    val h = size.height
    val eyeW = w * 0.075f
    val eyeH = h * 0.17f
    val eyeY = h * 0.3f
    val leftEye = w * 0.32f
    val rightEye = w * 0.68f
    val line = Stroke(width = w * 0.06f, cap = StrokeCap.Round)

    fun eye(cx: Float) = drawRoundRect(
        color = ink,
        topLeft = Offset(cx - eyeW / 2f, eyeY),
        size = Size(eyeW, eyeH),
        cornerRadius = CornerRadius(eyeW / 2f),
    )

    fun closedEye(cx: Float) = drawArc(
        color = ink, startAngle = 200f, sweepAngle = 140f, useCenter = false,
        topLeft = Offset(cx - w * 0.07f, eyeY + eyeH * 0.35f), size = Size(w * 0.14f, h * 0.12f), style = line,
    )

    fun smile(width: Float, depth: Float, y: Float) {
        val path = Path().apply {
            moveTo(w * (0.5f - width / 2f), h * y)
            quadraticTo(w * 0.5f, h * (y + depth), w * (0.5f + width / 2f), h * y)
        }
        drawPath(path, ink, style = line)
    }

    when (face) {
        Face.Smile -> {
            eye(leftEye); eye(rightEye)
            smile(width = 0.5f, depth = 0.22f, y = 0.6f)
        }
        Face.Grin -> {
            eye(leftEye); eye(rightEye)
            val mouth = Path().apply {
                moveTo(w * 0.28f, h * 0.6f)
                lineTo(w * 0.72f, h * 0.6f)
                quadraticTo(w * 0.7f, h * 0.84f, w * 0.5f, h * 0.84f)
                quadraticTo(w * 0.3f, h * 0.84f, w * 0.28f, h * 0.6f)
                close()
            }
            drawPath(mouth, ink)
        }
        Face.Wink -> {
            eye(leftEye)
            drawLine(ink, Offset(rightEye - w * 0.07f, eyeY + eyeH * 0.6f), Offset(rightEye + w * 0.07f, eyeY + eyeH * 0.6f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
            smile(width = 0.42f, depth = 0.18f, y = 0.62f)
        }
        Face.Calm -> {
            closedEye(leftEye); closedEye(rightEye)
            smile(width = 0.3f, depth = 0.12f, y = 0.66f)
        }
        Face.Kids -> Unit
    }
}
