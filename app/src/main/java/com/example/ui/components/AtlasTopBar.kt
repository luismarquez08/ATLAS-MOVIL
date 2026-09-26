package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtlasAmber
import com.example.ui.theme.AtlasEmotion

@Composable
fun AtlasTopBar(
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    currentEmotion: AtlasEmotion = AtlasEmotion.PHILOSOPHICAL,
    onEmotionSelected: (AtlasEmotion) -> Unit = {},
    onStopSpeaking: () -> Unit,
    modifier: Modifier = Modifier
) {
    var emotionMenuExpanded by remember { mutableStateOf(false) }

    val statusText = when {
        isListening -> "ESCUCHANDO"
        isThinking -> "PROCESANDO"
        isSpeaking -> "HABLANDO"
        currentEmotion == AtlasEmotion.SENTINEL -> "CENTINELA"
        else -> "EN LÍNEA"
    }

    val statusColor = when {
        isListening -> currentEmotion.primaryColor
        isThinking -> AtlasAmber
        isSpeaking -> currentEmotion.secondaryColor
        currentEmotion == AtlasEmotion.SENTINEL -> currentEmotion.primaryColor
        else -> currentEmotion.primaryColor
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ATLAS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = currentEmotion.primaryColor,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(currentEmotion.cardDark, RoundedCornerShape(4.dp))
                        .border(1.dp, currentEmotion.borderDark, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OS 2.0",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentEmotion.secondaryColor
                    )
                }
            }
            Text(
                text = "Tu asistente, siempre contigo",
                fontSize = 11.sp,
                color = currentEmotion.textMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Stop speaking button if speaking
            AnimatedVisibility(visible = isSpeaking) {
                IconButton(
                    onClick = onStopSpeaking,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(36.dp)
                        .background(currentEmotion.cardDark, CircleShape)
                        .border(1.dp, currentEmotion.borderDark, CircleShape)
                        .testTag("stop_speaking_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Detener voz",
                        tint = currentEmotion.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Emotion Selector Tag Button
            Box {
                Box(
                    modifier = Modifier
                        .background(currentEmotion.cardDark, RoundedCornerShape(14.dp))
                        .border(1.dp, currentEmotion.borderGlow, RoundedCornerShape(14.dp))
                        .clickable { emotionMenuExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("emotion_selector_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(currentEmotion.primaryColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = currentEmotion.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentEmotion.textPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = emotionMenuExpanded,
                    onDismissRequest = { emotionMenuExpanded = false },
                    modifier = Modifier.background(currentEmotion.surfaceDark)
                ) {
                    AtlasEmotion.values().forEach { emotion ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(emotion.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = emotion.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = emotion.textPrimary
                                        )
                                        Text(
                                            text = emotion.subtitle,
                                            fontSize = 10.sp,
                                            color = emotion.textMuted
                                        )
                                    }
                                }
                            },
                            onClick = {
                                emotionMenuExpanded = false
                                onEmotionSelected(emotion)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Badge
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
                    .testTag("status_badge")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
