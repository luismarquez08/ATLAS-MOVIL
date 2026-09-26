package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.InteractionEntity
import com.example.ui.theme.AtlasAmber
import com.example.ui.theme.AtlasBgDark
import com.example.ui.theme.AtlasBlue
import com.example.ui.theme.AtlasBorder
import com.example.ui.theme.AtlasCardDark
import com.example.ui.theme.AtlasCardElevated
import com.example.ui.theme.AtlasCyan
import com.example.ui.theme.AtlasError
import com.example.ui.theme.AtlasNeonGreen
import com.example.ui.theme.AtlasTextMuted
import com.example.ui.theme.AtlasTextPrimary
import com.example.ui.theme.AtlasTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    interactions: List<InteractionEntity>,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onSpeakText: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("TODOS") }
    var showClearConfirm by remember { mutableStateOf(false) }

    val categories = listOf("TODOS", "AI", "WEATHER", "NEWS", "SPOTIFY", "YOUTUBE", "SYSTEM", "MOTIVATION")

    val filteredList = remember(interactions, selectedCategory) {
        if (selectedCategory == "TODOS") {
            interactions
        } else {
            interactions.filter { it.category == selectedCategory }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("¿Borrar todo el historial?", color = AtlasTextPrimary) },
            text = { Text("Esta acción eliminará todas las interacciones guardadas en Atlas.", color = AtlasTextSecondary) },
            containerColor = AtlasCardDark,
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearConfirm = false
                }) {
                    Text("Borrar", color = AtlasError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancelar", color = AtlasTextSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AtlasBgDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MEMORIA Y REGISTROS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasNeonGreen,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tus recuerdos, interacciones y contexto de ATLAS",
                    fontSize = 12.sp,
                    color = AtlasTextSecondary
                )
            }

            if (interactions.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Borrar todo",
                        tint = AtlasTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) AtlasCyan.copy(alpha = 0.2f) else AtlasCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AtlasCyan else AtlasBorder),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AtlasCyan else AtlasTextMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // History list
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = AtlasTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sin interacciones en esta categoría",
                        fontSize = 14.sp,
                        color = AtlasTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onToggleFavorite = { onToggleFavorite(item.id, !item.isFavorite) },
                        onDelete = { onDeleteItem(item.id) },
                        onSpeak = { onSpeakText(item.response) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Atlas History", "${item.command}\n${item.response}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: InteractionEntity,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    val dateStr = remember(item.timestamp) {
        val sdf = SimpleDateFormat("d MMM, h:mm a", Locale.forLanguageTag("es-CO"))
        sdf.format(Date(item.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasBorder),
        modifier = Modifier.fillMaxWidth().testTag("history_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(AtlasCardElevated, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AtlasCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = dateStr, fontSize = 11.sp, color = AtlasTextMuted)
                }

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorito",
                            tint = if (item.isFavorite) AtlasAmber else AtlasTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Escuchar",
                            tint = AtlasTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar",
                            tint = AtlasTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar",
                            tint = AtlasTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User input command
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "Tú: ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AtlasBlue
                )
                Text(
                    text = item.command,
                    fontSize = 12.sp,
                    color = AtlasTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Atlas Response
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "Atlas: ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AtlasCyan
                )
                Text(
                    text = item.response,
                    fontSize = 12.sp,
                    color = AtlasTextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
