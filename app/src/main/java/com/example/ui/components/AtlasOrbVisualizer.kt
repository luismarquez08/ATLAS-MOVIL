package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AtlasAmber
import com.example.ui.theme.AtlasEmotion
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AtlasOrbVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    audioRms: Float,
    emotion: AtlasEmotion = AtlasEmotion.PHILOSOPHICAL,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "atlas_orb_transition")

    // Ambient continuous rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking || emotion == AtlasEmotion.SENTINEL) 3200 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Reverse scanner rotation for Sentinel mode
    val scannerAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sentinel_scanner"
    )

    // Breathing pulse for idle
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    // Speaking rhythm wave
    val speakingWave by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_wave"
    )

    // Animated RMS scale for speech audio reactivity
    val rmsAnim = remember { Animatable(0f) }
    LaunchedEffect(audioRms) {
        rmsAnim.animateTo(audioRms, tween(60, easing = FastOutSlowInEasing))
    }

    // Dynamic primary color based on Emotion & Runtime State
    val coreColor = when {
        isThinking -> AtlasAmber
        isListening -> Color.White
        else -> emotion.primaryColor
    }

    val secondaryColor = when {
        isThinking -> AtlasAmber.copy(alpha = 0.7f)
        isListening -> emotion.primaryColor
        else -> emotion.secondaryColor
    }

    val glowColor = when {
        isThinking -> AtlasAmber.copy(alpha = 0.35f)
        isListening -> emotion.primaryColor.copy(alpha = 0.50f)
        isSpeaking -> emotion.primaryColor.copy(alpha = 0.45f)
        else -> emotion.glowColor
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(230.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("atlas_orb_visualizer")
    ) {
        Canvas(modifier = Modifier.size(230.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.30f

            val dynamicScale = when {
                isListening -> 1f + (rmsAnim.value * 0.40f)
                isSpeaking -> speakingWave
                isThinking -> idlePulse * 1.05f
                else -> idlePulse
            }

            // 1. Ambient Holographic Glow (Radial Gradient)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, glowColor.copy(alpha = 0.08f), Color.Transparent),
                    center = center,
                    radius = baseRadius * dynamicScale * 1.65f
                ),
                radius = baseRadius * dynamicScale * 1.65f,
                center = center
            )

            // 2. Outer Rotating Orbital Rings with Tech Dash Effect
            rotate(rotationAngle, pivot = center) {
                // Outer ring
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.35f),
                    radius = baseRadius * dynamicScale * 1.30f,
                    center = center,
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f, 6f, 14f), 0f)
                    )
                )

                // 4 Satellite telemetry blips around outer ring
                val numBlips = 4
                for (i in 0 until numBlips) {
                    val angle = (i * (360f / numBlips)) * (Math.PI / 180f)
                    val blipX = center.x + (baseRadius * dynamicScale * 1.30f) * cos(angle).toFloat()
                    val blipY = center.y + (baseRadius * dynamicScale * 1.30f) * sin(angle).toFloat()
                    drawCircle(
                        color = coreColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(blipX, blipY)
                    )
                }
            }

            // Counter-rotating Middle Ring (or Sentinel Scanner Line in Sentinel mode)
            rotate(if (emotion == AtlasEmotion.SENTINEL) scannerAngle else -rotationAngle * 0.7f, pivot = center) {
                drawCircle(
                    color = coreColor.copy(alpha = if (emotion == AtlasEmotion.SENTINEL) 0.85f else 0.45f),
                    radius = baseRadius * dynamicScale * 1.12f,
                    center = center,
                    style = Stroke(
                        width = if (emotion == AtlasEmotion.SENTINEL) 2.5.dp.toPx() else 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), 0f)
                    )
                )
            }

            // 3. Audio Frequency Reactive Halo Wave (Expanding while listening/speaking)
            if (isListening || isSpeaking) {
                val waveAlpha = if (isListening) (0.25f + rmsAnim.value * 0.35f) else 0.30f
                drawCircle(
                    color = coreColor.copy(alpha = waveAlpha.coerceIn(0f, 0.7f)),
                    radius = baseRadius * (dynamicScale * 1.18f),
                    center = center,
                    style = Stroke(width = 2.2.dp.toPx())
                )
            }

            // 4. Central Tech Spherical Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        emotion.cardDark,
                        emotion.surfaceDark,
                        emotion.bgDark,
                        coreColor.copy(alpha = 0.5f)
                    ),
                    center = center,
                    radius = baseRadius * dynamicScale
                ),
                radius = baseRadius * dynamicScale,
                center = center
            )

            // Inner thin ring border
            drawCircle(
                color = coreColor.copy(alpha = 0.6f),
                radius = baseRadius * dynamicScale * 0.95f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 5. Stylized "A" in the center (Signature ATLAS Logo from Reference Images)
            val aPath = Path().apply {
                val topY = center.y - (baseRadius * 0.48f)
                val bottomY = center.y + (baseRadius * 0.48f)
                val halfBase = baseRadius * 0.38f

                // Left leg to apex to right leg
                moveTo(center.x - halfBase, bottomY)
                lineTo(center.x, topY)
                lineTo(center.x + halfBase, bottomY)

                // Crossbar
                val crossbarY = center.y + (baseRadius * 0.12f)
                val crossbarHalfW = halfBase * 0.65f
                moveTo(center.x - crossbarHalfW, crossbarY)
                lineTo(center.x + crossbarHalfW, crossbarY)
            }

            drawPath(
                path = aPath,
                color = if (isListening) Color.White else coreColor,
                style = Stroke(
                    width = 4.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
