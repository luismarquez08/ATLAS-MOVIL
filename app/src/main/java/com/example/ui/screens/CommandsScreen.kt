package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtlasAmber
import com.example.ui.theme.AtlasBgDark
import com.example.ui.theme.AtlasBlue
import com.example.ui.theme.AtlasBorder
import com.example.ui.theme.AtlasCardDark
import com.example.ui.theme.AtlasCardElevated
import com.example.ui.theme.AtlasCyan
import com.example.ui.theme.AtlasNeonGreen
import com.example.ui.theme.AtlasPurple
import com.example.ui.theme.AtlasTextMuted
import com.example.ui.theme.AtlasTextPrimary
import com.example.ui.theme.AtlasTextSecondary

data class CommandExample(val title: String, val example: String)
data class CommandCategory(val name: String, val icon: ImageVector, val color: androidx.compose.ui.graphics.Color, val commands: List<CommandExample>)

@Composable
fun CommandsScreen(
    onRunCommand: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val categories = listOf(
        CommandCategory(
            name = "WHATSAPP AUTÓNOMO (SALUDO-NECESIDAD-ACCIÓN)",
            icon = Icons.AutoMirrored.Filled.Chat,
            color = AtlasNeonGreen,
            commands = listOf(
                CommandExample("Mensaje a My Wife (amor)", "atlas enviar mensaje por whatsapp a my wife que la amo"),
                CommandExample("Mensaje a Mamá (camino)", "atlas escribe a mamá por whatsapp que ya voy llegando"),
                CommandExample("Llamada por WhatsApp a Mamá", "atlas llama a mamá por whatsapp"),
                CommandExample("Mensaje con saludo y aviso", "atlas dile a carlos por whatsapp que me llame")
            )
        ),
        CommandCategory(
            name = "SPOTIFY NATIVO (AUTO-PLAY)",
            icon = Icons.Default.MusicNote,
            color = AtlasCyan,
            commands = listOf(
                CommandExample("Reproducir artista", "atlas reproduce queen en spotify"),
                CommandExample("Reproducir canción directa", "atlas reproduce bohemian rhapsody en spotify"),
                CommandExample("Abrir app de Spotify", "atlas abrir spotify")
            )
        ),
        CommandCategory(
            name = "YOUTUBE NATIVO",
            icon = Icons.Default.PlayArrow,
            color = AtlasAmber,
            commands = listOf(
                CommandExample("Reproducir en app YouTube", "atlas reproduce daft punk en youtube"),
                CommandExample("Música para programar", "atlas reproduce música para programar en youtube"),
                CommandExample("Abrir app de YouTube", "atlas abrir youtube")
            )
        ),
        CommandCategory(
            name = "ALARMAS Y TEMPORIZADORES INTELIGENTES",
            icon = Icons.Default.Alarm,
            color = AtlasBlue,
            commands = listOf(
                CommandExample("Alarma con hora exacta", "atlas pon una alarma a las 7 de la mañana"),
                CommandExample("Alarma con minutos", "atlas alarma a las 6 y media"),
                CommandExample("Alarma relativa", "atlas pon una alarma en 20 minutos"),
                CommandExample("Temporizador", "atlas temporizador de 15 minutos")
            )
        ),
        CommandCategory(
            name = "FUNCIONES NATIVAS JARVIS",
            icon = Icons.Default.Layers,
            color = AtlasPurple,
            commands = listOf(
                CommandExample("Diagnóstico del sistema", "atlas estado del sistema"),
                CommandExample("Consultar batería", "atlas cuánta batería tengo"),
                CommandExample("Encender Linterna", "atlas enciende la linterna"),
                CommandExample("Apagar Linterna", "atlas apaga la linterna"),
                CommandExample("Llamada telefónica directa", "atlas llamar a carlos")
            )
        ),
        CommandCategory(
            name = "CÁLCULOS MATEMÁTICOS Y VOLUMEN",
            icon = Icons.Default.Calculate,
            color = AtlasAmber,
            commands = listOf(
                CommandExample("Multiplicación rápida", "atlas cuánto es 45 por 12"),
                CommandExample("Cálculo de porcentaje", "atlas cuánto es el 15% de 80000"),
                CommandExample("División", "atlas cuánto es 1500 dividido entre 3"),
                CommandExample("Subir volumen", "atlas sube el volumen"),
                CommandExample("Silenciar teléfono", "atlas silencia el teléfono")
            )
        ),
        CommandCategory(
            name = "ESPECIAL PARA EL JEFE",
            icon = Icons.Default.Code,
            color = AtlasPurple,
            commands = listOf(
                CommandExample("Consejo de programación", "atlas mensaje jefe"),
                CommandExample("Motivación para programar", "atlas motívame")
            )
        ),
        CommandCategory(
            name = "SISTEMA DE EMOCIONES Y CENTINELA (5 MODOS)",
            icon = Icons.Default.Layers,
            color = com.example.ui.theme.AtlasEmotion.SENTINEL.primaryColor,
            commands = listOf(
                CommandExample("Activar Modo Centinela (Rojo)", "atlas activa centinela"),
                CommandExample("Modo Pensativo / Filosófico (Verde)", "atlas modo filosófico"),
                CommandExample("Modo Alegre / Creativo (Azul)", "atlas modo alegre"),
                CommandExample("Modo Sereno / Calma (Blanco)", "atlas modo sereno"),
                CommandExample("Modo Capaz / Concentración (Negro)", "atlas modo capaz"),
                CommandExample("Pregunta profunda filosófica", "atlas qué pensaba marco aurelio sobre la muerte")
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AtlasBgDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "HERRAMIENTAS Y COMANDOS",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasNeonGreen,
            letterSpacing = 1.sp
        )
        Text(
            text = "Accede a todas las funciones nativas y de voz de ATLAS",
            fontSize = 12.sp,
            color = AtlasTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            categories.forEach { category ->
                item {
                    CategorySection(category = category, onRun = onRunCommand)
                }
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CategorySection(category: CommandCategory, onRun: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasBorder),
        modifier = Modifier.fillMaxWidth().testTag("command_category_${category.name.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = category.color,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            category.commands.forEachIndexed { index, cmd ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AtlasCardElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cmd.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AtlasTextPrimary
                            )
                            Text(
                                text = "\"${cmd.example}\"",
                                fontSize = 11.sp,
                                color = AtlasTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = category.color.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, category.color.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { onRun(cmd.example) }
                                .testTag("run_command_${cmd.title.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Probar",
                                    tint = category.color,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Probar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = category.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
