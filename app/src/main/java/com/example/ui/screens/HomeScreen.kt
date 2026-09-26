package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AtlasUiState
import com.example.ui.components.AtlasOrbVisualizer
import com.example.ui.theme.AtlasAmber
import com.example.ui.theme.AtlasBgDark
import com.example.ui.theme.AtlasBorder
import com.example.ui.theme.AtlasBorderGlow
import com.example.ui.theme.AtlasCardDark
import com.example.ui.theme.AtlasCardElevated
import com.example.ui.theme.AtlasCyan
import com.example.ui.theme.AtlasEmerald
import com.example.ui.theme.AtlasMint
import com.example.ui.theme.AtlasNeonGreen
import com.example.ui.theme.AtlasTextMuted
import com.example.ui.theme.AtlasTextPrimary
import com.example.ui.theme.AtlasTextSecondary

data class QuickChip(val label: String, val query: String, val icon: ImageVector)

@Composable
fun HomeScreen(
    uiState: AtlasUiState,
    onMicClick: () -> Unit,
    onInputChanged: (String) -> Unit,
    onSubmitText: () -> Unit,
    onQuickAction: (String) -> Unit,
    onRepeatSpeech: () -> Unit,
    onToggleOverlay: () -> Unit,
    onOpenExternal: (android.content.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val quickChips = listOf(
        QuickChip("WhatsApp Wife", "enviar mensaje por whatsapp a my wife que la amo", Icons.AutoMirrored.Filled.Chat),
        QuickChip("Llamar Mamá", "llama a mamá por whatsapp", Icons.Default.Call),
        QuickChip("Spotify", "reproduce queen en spotify", Icons.Default.MusicNote),
        QuickChip("YouTube", "reproduce daft punk en youtube", Icons.Default.PlayArrow),
        QuickChip("Alarma 7:00", "alarma a las 7 de la mañana", Icons.Default.Alarm),
        QuickChip("Linterna", "enciende la linterna", Icons.Default.FlashlightOn),
        QuickChip("Diagnóstico Jarvis", "estado del sistema", Icons.Default.Layers),
        QuickChip("Cálculo Rápido", "cuánto es 45 por 12", Icons.Default.Calculate),
        QuickChip("Mensaje Jefe", "mensaje jefe", Icons.Default.Code)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AtlasBgDark)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Subtítulo inspiracional: "Tu asistente, siempre contigo"
            Text(
                text = "Tu asistente, siempre contigo",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AtlasTextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Always Active Floating Overlay Banner (Siri / Centinela)
            val isSentinelActive = uiState.settings.overlayAlwaysActive || uiState.currentEmotion == com.example.ui.theme.AtlasEmotion.SENTINEL
            val bannerColor = if (isSentinelActive) uiState.currentEmotion.primaryColor else uiState.currentEmotion.textMuted
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSentinelActive) uiState.currentEmotion.cardElevated else uiState.currentEmotion.cardDark,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSentinelActive) uiState.currentEmotion.primaryColor.copy(alpha = 0.6f) else uiState.currentEmotion.borderDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleOverlay() }
                    .testTag("overlay_toggle_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSentinelActive) "Modo Centinela: ACTIVO" else "Modo Centinela: APAGADO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSentinelActive) uiState.currentEmotion.primaryColor else uiState.currentEmotion.textPrimary
                            )
                            Text(
                                text = if (uiState.currentEmotion == com.example.ui.theme.AtlasEmotion.SENTINEL) "Vigilancia de seguridad activa • Diagnóstico total" else "Silencioso tipo Siri • Di 'Atlas' en cualquier app",
                                fontSize = 10.sp,
                                color = uiState.currentEmotion.textMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (isSentinelActive) uiState.currentEmotion.primaryColor else uiState.currentEmotion.cardElevated,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isSentinelActive) "ACTIVO" else "ACTIVAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSentinelActive) uiState.currentEmotion.bgDark else uiState.currentEmotion.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Visualizer Orb con logo central "A" y anillos holográficos reactivos a la emoción
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                AtlasOrbVisualizer(
                    isListening = uiState.isListening,
                    isSpeaking = uiState.isSpeaking,
                    isThinking = uiState.isThinking,
                    audioRms = uiState.audioRms,
                    emotion = uiState.currentEmotion,
                    onClick = onMicClick
                )
            }

            // Status feedback banner (En línea / Listo para ayudarte / Escuchando...)
            val (statusText, statusSubtext) = when {
                uiState.isListening -> Pair("Escuchando...", "Di tu orden ahora...")
                uiState.isThinking -> Pair("Procesando...", "Ejecutando acción nativa...")
                uiState.isSpeaking -> Pair("Hablando...", "Reproduciendo respuesta...")
                else -> Pair("En línea", "Listo para ayudarte")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isListening) Color.White else uiState.currentEmotion.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isListening) Color.White else uiState.currentEmotion.textPrimary
                    )
                }
                Text(
                    text = statusSubtext,
                    fontSize = 11.sp,
                    color = uiState.currentEmotion.textMuted,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Mini Status Nodes: Voz, Internet, IA, Batería (Tomado de las maquetas de UI)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStatusNode(icon = Icons.Default.Mic, label = "Voz", status = "Activo", color = uiState.currentEmotion.primaryColor)
                MiniStatusNode(icon = Icons.Default.Language, label = "Internet", status = "Online", color = uiState.currentEmotion.secondaryColor)
                MiniStatusNode(icon = Icons.Default.Psychology, label = "IA", status = "Gemini", color = uiState.currentEmotion.primaryColor)
                MiniStatusNode(icon = Icons.Default.BatteryStd, label = "Batería", status = "OK", color = uiState.currentEmotion.secondaryColor)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickChips.forEach { chip ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AtlasCardDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasBorder),
                        modifier = Modifier
                            .clickable { onQuickAction(chip.query) }
                            .testTag("quick_chip_${chip.label.lowercase().replace(" ", "_")}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = chip.icon,
                                contentDescription = chip.label,
                                tint = AtlasNeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = chip.label,
                                fontSize = 12.sp,
                                color = AtlasTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User transcription bubble if active or recent
            AnimatedVisibility(visible = uiState.liveTranscript.isNotBlank() || uiState.lastCommand.isNotBlank()) {
                val displayUserText = uiState.liveTranscript.ifBlank { uiState.lastCommand }
                Card(
                    colors = CardDefaults.cardColors(containerColor = AtlasCardElevated),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AtlasBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("user_transcript_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AtlasEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.settings.userName.take(1).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AtlasEmerald
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = uiState.settings.userName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AtlasMint
                            )
                            Text(
                                text = displayUserText,
                                fontSize = 14.sp,
                                color = AtlasTextPrimary
                            )
                        }
                    }
                }
            }

            // Atlas Response Card
            Card(
                colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AtlasBorderGlow),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = AtlasNeonGreen, spotColor = AtlasNeonGreen)
                    .testTag("atlas_response_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(uiState.currentEmotion.primaryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = uiState.currentEmotion.primaryColor
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATLAS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = uiState.currentEmotion.primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(uiState.currentEmotion.cardElevated, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = uiState.lastCategory,
                                    fontSize = 10.sp,
                                    color = uiState.currentEmotion.textMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row {
                            // Copy button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Atlas", uiState.lastResponse)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Respuesta copiada", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp).testTag("copy_response_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar texto",
                                    tint = AtlasTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Repeat Voice button
                            IconButton(
                                onClick = onRepeatSpeech,
                                modifier = Modifier.size(32.dp).testTag("speak_response_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Escuchar de nuevo",
                                    tint = if (uiState.isSpeaking) AtlasNeonGreen else AtlasTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState.isThinking) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AtlasNeonGreen,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Atlas ejecutando comando nativo...",
                                fontSize = 13.sp,
                                color = AtlasTextMuted
                            )
                        }
                    } else {
                        Text(
                            text = uiState.lastResponse,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = AtlasTextPrimary
                        )
                    }

                    // External action button (e.g. Open WhatsApp, Spotify, YouTube)
                    val result = uiState.lastResult
                    if (result?.externalIntent != null && result.actionLabel != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AtlasNeonGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AtlasNeonGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { onOpenExternal(result.externalIntent) }
                                .testTag("external_action_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = result.actionLabel,
                                    tint = AtlasNeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = result.actionLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AtlasNeonGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom Console: Text input + Big Mic Button (Estilo Maqueta)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.currentInputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        text = "Escribe un mensaje o comando...",
                        fontSize = 13.sp,
                        color = AtlasTextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_text_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = uiState.currentEmotion.primaryColor,
                    unfocusedBorderColor = uiState.currentEmotion.borderDark,
                    focusedTextColor = uiState.currentEmotion.textPrimary,
                    unfocusedTextColor = uiState.currentEmotion.textPrimary,
                    focusedContainerColor = uiState.currentEmotion.cardDark,
                    unfocusedContainerColor = uiState.currentEmotion.cardDark
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    focusManager.clearFocus()
                    onSubmitText()
                }),
                trailingIcon = {
                    if (uiState.currentInputText.isNotBlank()) {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            onSubmitText()
                        }, modifier = Modifier.testTag("send_command_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar comando",
                                tint = uiState.currentEmotion.primaryColor
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Main Voice Button Glowing with Active Emotion Palette
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (uiState.isListening) Color.White else uiState.currentEmotion.primaryColor)
                    .clickable { onMicClick() }
                    .shadow(12.dp, CircleShape, ambientColor = uiState.currentEmotion.primaryColor, spotColor = uiState.currentEmotion.primaryColor)
                    .testTag("main_mic_button")
            ) {
                Icon(
                    imageVector = if (uiState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (uiState.isListening) "Detener micrófono" else "Hablar",
                    tint = uiState.currentEmotion.bgDark,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniStatusNode(
    icon: ImageVector,
    label: String,
    status: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(AtlasCardDark)
                .border(1.dp, color.copy(alpha = 0.35f), CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = AtlasTextSecondary
        )
        Text(
            text = status,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
