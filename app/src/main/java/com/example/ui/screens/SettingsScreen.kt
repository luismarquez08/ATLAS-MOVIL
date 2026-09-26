package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AtlasSettings
import com.example.ui.theme.AtlasAmber
import com.example.ui.theme.AtlasBgDark
import com.example.ui.theme.AtlasBorder
import com.example.ui.theme.AtlasCardDark
import com.example.ui.theme.AtlasCardElevated
import com.example.ui.theme.AtlasCyan
import com.example.ui.theme.AtlasNeonGreen
import com.example.ui.theme.AtlasPurple
import com.example.ui.theme.AtlasTextMuted
import com.example.ui.theme.AtlasTextPrimary
import com.example.ui.theme.AtlasTextSecondary

@Composable
fun SettingsScreen(
    currentSettings: AtlasSettings,
    isOverlayPermissionGranted: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onToggleOverlay: (Boolean) -> Unit,
    onSaveSettings: (AtlasSettings) -> Unit,
    onTestVoice: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    var userName by remember(currentSettings) { mutableStateOf(currentSettings.userName) }
    var autoSpeak by remember(currentSettings) { mutableStateOf(currentSettings.autoSpeak) }
    var openLinks by remember(currentSettings) { mutableStateOf(currentSettings.openLinksDirectly) }
    var continuousWakeWord by remember(currentSettings) { mutableStateOf(currentSettings.continuousWakeWord) }
    var wakeWord by remember(currentSettings) { mutableStateOf(currentSettings.wakeWord) }
    var vibrateOnWake by remember(currentSettings) { mutableStateOf(currentSettings.vibrateOnWake) }
    var strictOwnerSecurity by remember(currentSettings) { mutableStateOf(currentSettings.strictOwnerSecurity) }
    var speechRate by remember(currentSettings) { mutableFloatStateOf(currentSettings.speechRate) }
    var speechPitch by remember(currentSettings) { mutableFloatStateOf(currentSettings.speechPitch) }
    var selectedEmotionName by remember(currentSettings) { mutableStateOf(currentSettings.defaultEmotionName) }
    var emotionAutoSwitch by remember(currentSettings) { mutableStateOf(currentSettings.emotionAutoSwitch) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AtlasBgDark)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "CONFIGURACIÓN",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasNeonGreen,
            letterSpacing = 1.sp
        )
        Text(
            text = "Personaliza tu experiencia, parámetros de seguridad y voz",
            fontSize = 12.sp,
            color = AtlasTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. ALWAYS-ACTIVE FLOATING BUBBLE OVER ALL APPS
        Card(
            colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (currentSettings.overlayAlwaysActive) AtlasCyan.copy(alpha = 0.5f) else AtlasBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("overlay_settings_card")
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AtlasCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = AtlasCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ATLAS SIEMPRE ACTIVO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AtlasCyan,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Burbuja flotante sobre todo el teléfono",
                                fontSize = 11.sp,
                                color = AtlasTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = currentSettings.overlayAlwaysActive,
                        onCheckedChange = { enable ->
                            if (enable && !isOverlayPermissionGranted) {
                                onRequestOverlayPermission()
                            } else {
                                onToggleOverlay(enable)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AtlasBgDark,
                            checkedTrackColor = AtlasCyan,
                            uncheckedThumbColor = AtlasTextMuted,
                            uncheckedTrackColor = AtlasBorder
                        ),
                        modifier = Modifier.testTag("overlay_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Permite que Atlas esté siempre flotando sobre cualquier app (WhatsApp, Spotify, YouTube o juegos) para ejecutar órdenes inmediatas con un solo toque.",
                    fontSize = 11.sp,
                    color = AtlasTextMuted,
                    lineHeight = 16.sp
                )

                if (!isOverlayPermissionGranted) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onRequestOverlayPermission,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AtlasAmber),
                        modifier = Modifier.fillMaxWidth().testTag("grant_overlay_perm_button")
                    ) {
                        Text(
                            text = "Conceder permiso de ventana flotante",
                            color = AtlasBgDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. WAKE-WORD & SEGURIDAD PERSONAL CONTINUA
        Card(
            colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AtlasNeonGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = AtlasNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MODO CENTINELA: ESCUCHA CONTINUA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AtlasNeonGreen,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Continuous wake-word switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Escucha activa continua del wake-word",
                            fontSize = 12.sp,
                            color = AtlasTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Atlas escucha en segundo plano la palabra de activación",
                            fontSize = 10.sp,
                            color = AtlasTextMuted
                        )
                    }
                    Switch(
                        checked = continuousWakeWord,
                        onCheckedChange = { continuousWakeWord = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AtlasBgDark,
                            checkedTrackColor = AtlasNeonGreen,
                            uncheckedThumbColor = AtlasTextMuted,
                            uncheckedTrackColor = AtlasBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = wakeWord,
                    onValueChange = { wakeWord = it },
                    label = { Text("Palabra clave de activación") },
                    leadingIcon = {
                        Icon(Icons.Default.Security, contentDescription = null, tint = AtlasNeonGreen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AtlasNeonGreen,
                        unfocusedBorderColor = AtlasBorder,
                        focusedTextColor = AtlasTextPrimary,
                        unfocusedTextColor = AtlasTextPrimary,
                        focusedContainerColor = AtlasCardElevated,
                        unfocusedContainerColor = AtlasCardElevated
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Vibration feedback on wake-word
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Respuesta háptica (vibración)",
                            fontSize = 12.sp,
                            color = AtlasTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Vibra al detectar la palabra '$wakeWord'",
                            fontSize = 10.sp,
                            color = AtlasTextMuted
                        )
                    }
                    Switch(
                        checked = vibrateOnWake,
                        onCheckedChange = { vibrateOnWake = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AtlasBgDark,
                            checkedTrackColor = AtlasNeonGreen,
                            uncheckedThumbColor = AtlasTextMuted,
                            uncheckedTrackColor = AtlasBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Strict owner security toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Seguridad personal para $userName (El Jefe)",
                            fontSize = 12.sp,
                            color = AtlasTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Identificación personalizada y respuesta exclusiva",
                            fontSize = 10.sp,
                            color = AtlasTextMuted
                        )
                    }
                    Switch(
                        checked = strictOwnerSecurity,
                        onCheckedChange = { strictOwnerSecurity = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AtlasBgDark,
                            checkedTrackColor = AtlasNeonGreen,
                            uncheckedThumbColor = AtlasTextMuted,
                            uncheckedTrackColor = AtlasBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. User Profile
        Card(
            colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AtlasBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PERFIL DEL JEFE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AtlasCyan,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Nombre del Jefe / Usuario") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AtlasCyan)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("setting_username_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AtlasCyan,
                        unfocusedBorderColor = AtlasBorder,
                        focusedTextColor = AtlasTextPrimary,
                        unfocusedTextColor = AtlasTextPrimary,
                        focusedContainerColor = AtlasCardElevated,
                        unfocusedContainerColor = AtlasCardElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Voice Settings (TTS)
        Card(
            colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AtlasBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOZ NATIVA DE ATLAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AtlasCyan,
                        letterSpacing = 0.5.sp
                    )

                    Button(
                        onClick = onTestVoice,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AtlasCyan.copy(alpha = 0.2f)),
                        modifier = Modifier.height(32.dp).testTag("test_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = AtlasCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar voz", color = AtlasCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Speech Rate
                Text(
                    text = "Velocidad de voz: ${String.format("%.2f", speechRate)}x",
                    fontSize = 12.sp,
                    color = AtlasTextSecondary
                )
                Slider(
                    value = speechRate,
                    onValueChange = { speechRate = it },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = AtlasCyan,
                        activeTrackColor = AtlasCyan,
                        inactiveTrackColor = AtlasBorder
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Speech Pitch
                Text(
                    text = "Tono de voz: ${String.format("%.2f", speechPitch)}x",
                    fontSize = 12.sp,
                    color = AtlasTextSecondary
                )
                Slider(
                    value = speechPitch,
                    onValueChange = { speechPitch = it },
                    valueRange = 0.6f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = AtlasCyan,
                        activeTrackColor = AtlasCyan,
                        inactiveTrackColor = AtlasBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Auto speak toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voz automática en cada orden",
                            fontSize = 13.sp,
                            color = AtlasTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Atlas confirma verbalmente la ejecución",
                            fontSize = 11.sp,
                            color = AtlasTextMuted
                        )
                    }
                    Switch(
                        checked = autoSpeak,
                        onCheckedChange = { autoSpeak = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AtlasBgDark,
                            checkedTrackColor = AtlasCyan,
                            uncheckedThumbColor = AtlasTextMuted,
                            uncheckedTrackColor = AtlasBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Open links directly toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Abrir aplicaciones de inmediato",
                            fontSize = 13.sp,
                            color = AtlasTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Lanzar WhatsApp, Spotify o YouTube de inmediato",
                            fontSize = 11.sp,
                            color = AtlasTextMuted
                        )
                    }
                    Switch(
                        checked = openLinks,
                        onCheckedChange = { openLinks = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AtlasBgDark,
                            checkedTrackColor = AtlasCyan,
                            uncheckedThumbColor = AtlasTextMuted,
                            uncheckedTrackColor = AtlasBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. SISTEMA DE EMOCIONES VISUALES (5 MODOS)
        Card(
            colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AtlasNeonGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().testTag("emotions_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AtlasNeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SISTEMA DE EMOCIONES VISUALES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AtlasNeonGreen,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Selecciona la personalidad visual y cromática activa para ATLAS.",
                    fontSize = 11.sp,
                    color = AtlasTextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Grid of 5 emotion options
                val emotions = com.example.ui.theme.AtlasEmotion.values()
                emotions.forEach { emotion ->
                    val isSelected = selectedEmotionName == emotion.name
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) emotion.cardElevated else AtlasCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) emotion.primaryColor else AtlasBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedEmotionName = emotion.name }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(emotion.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = emotion.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) emotion.primaryColor else AtlasTextPrimary
                                    )
                                    Text(
                                        text = emotion.subtitle,
                                        fontSize = 10.sp,
                                        color = AtlasTextMuted
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .background(emotion.primaryColor, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AtlasBgDark
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto emotional switch based on query
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Conmutación Emocional Inteligente",
                            fontSize = 13.sp,
                            color = AtlasTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Atlas cambia automáticamente de emoción según el contexto de tu consulta",
                            fontSize = 11.sp,
                            color = AtlasTextMuted
                        )
                    }
                    Switch(
                        checked = emotionAutoSwitch,
                        onCheckedChange = { emotionAutoSwitch = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AtlasBgDark,
                            checkedTrackColor = AtlasNeonGreen,
                            uncheckedThumbColor = AtlasTextMuted,
                            uncheckedTrackColor = AtlasBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Save Button
        Button(
            onClick = {
                val updated = currentSettings.copy(
                    userName = userName.trim().ifBlank { "Luis" },
                    autoSpeak = autoSpeak,
                    speechRate = speechRate,
                    speechPitch = speechPitch,
                    openLinksDirectly = openLinks,
                    continuousWakeWord = continuousWakeWord,
                    wakeWord = wakeWord.trim().ifBlank { "Atlas" },
                    vibrateOnWake = vibrateOnWake,
                    strictOwnerSecurity = strictOwnerSecurity,
                    defaultEmotionName = selectedEmotionName,
                    emotionAutoSwitch = emotionAutoSwitch
                )
                onSaveSettings(updated)
                Toast.makeText(context, "Ajustes y sistema emocional guardados correctamente", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("save_settings_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AtlasCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = AtlasBgDark)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Guardar Parámetros de Seguridad", color = AtlasBgDark, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Native Integration Info
        Card(
            colors = CardDefaults.cardColors(containerColor = AtlasCardElevated),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AtlasNeonGreen.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneIphone,
                        contentDescription = null,
                        tint = AtlasNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AUTONOMÍA TOTAL TIPO JARVIS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AtlasNeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Estructura inteligente: Saludo ➔ Necesidad ➔ Acción ➔ Ejecución.\n" +
                           "• Normalización de lenguaje: \"que la amo\" se traduce automáticamente a \"Te amo\".\n" +
                           "• Integraciones nativas directas: WhatsApp (mensajes y llamadas), Spotify (reproducción desde búsqueda), YouTube, Alarmas exactas y Linterna.\n" +
                           "• Telemetría de hardware: Batería, memoria RAM y cálculos aritméticos instantáneos.",
                    fontSize = 11.sp,
                    color = AtlasTextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
