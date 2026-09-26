package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.local.ContactEntity
import com.example.ui.theme.AtlasAmber
import com.example.ui.theme.AtlasBgDark
import com.example.ui.theme.AtlasBlue
import com.example.ui.theme.AtlasBorder
import com.example.ui.theme.AtlasCardDark
import com.example.ui.theme.AtlasCardElevated
import com.example.ui.theme.AtlasCyan
import com.example.ui.theme.AtlasNeonGreen
import com.example.ui.theme.AtlasTextMuted
import com.example.ui.theme.AtlasTextPrimary
import com.example.ui.theme.AtlasTextSecondary

@Composable
fun ContactsScreen(
    contacts: List<ContactEntity>,
    onAddContact: (String, String, String, String) -> Unit,
    onDeleteContact: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newRelationship by remember { mutableStateOf("") }
    var newAliases by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Nuevo Contacto de WhatsApp",
                    fontWeight = FontWeight.Bold,
                    color = AtlasCyan
                )
            },
            text = {
                Column {
                    Text(
                        text = "Registra a quién deseas enviar mensajes o llamar por WhatsApp mediante órdenes de voz de Atlas.",
                        fontSize = 12.sp,
                        color = AtlasTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre (ej. My Wife, Mamá, Carlos)") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AtlasCyan,
                            unfocusedBorderColor = AtlasBorder,
                            focusedTextColor = AtlasTextPrimary,
                            unfocusedTextColor = AtlasTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Teléfono (+573001234567)") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_phone_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AtlasCyan,
                            unfocusedBorderColor = AtlasBorder,
                            focusedTextColor = AtlasTextPrimary,
                            unfocusedTextColor = AtlasTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newAliases,
                        onValueChange = { newAliases = it },
                        label = { Text("Alias de voz (ej. my wife, esposa, amor)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AtlasCyan,
                            unfocusedBorderColor = AtlasBorder,
                            focusedTextColor = AtlasTextPrimary,
                            unfocusedTextColor = AtlasTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newRelationship,
                        onValueChange = { newRelationship = it },
                        label = { Text("Parentesco (ej. Esposa, Madre, Amigo)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AtlasCyan,
                            unfocusedBorderColor = AtlasBorder,
                            focusedTextColor = AtlasTextPrimary,
                            unfocusedTextColor = AtlasTextPrimary
                        )
                    )
                }
            },
            containerColor = AtlasCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            onAddContact(newName, newPhone, newRelationship, newAliases)
                            newName = ""
                            newPhone = ""
                            newRelationship = ""
                            newAliases = ""
                            showAddDialog = false
                            Toast.makeText(context, "Contacto guardado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Guardar", color = AtlasBgDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = AtlasTextMuted)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AtlasBgDark)
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMUNICACIÓN & WHATSAPP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasNeonGreen,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Contactos vinculados a llamadas y mensajes de voz autónomos",
                        fontSize = 12.sp,
                        color = AtlasTextSecondary
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasCyan),
                    modifier = Modifier.height(36.dp).testTag("add_contact_top_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AtlasBgDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo", color = AtlasBgDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Instruction Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = AtlasCardElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AtlasNeonGreen.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = AtlasNeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Di: \"Atlas enviar mensaje a [nombre o alias] que [mensaje]\" o \"Atlas llama a [contacto] por WhatsApp\".",
                        fontSize = 11.sp,
                        color = AtlasTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AtlasTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay contactos registrados aún",
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
                    items(contacts, key = { it.id }) { contact ->
                        ContactRow(
                            contact = contact,
                            onWhatsAppMessage = {
                                val cleanNum = contact.phoneNumber.replace(Regex("[^0-9+]"), "")
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNum&text=${Uri.encode("Hola ${contact.name}")}")
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.whatsapp")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                }
                            },
                            onWhatsAppCall = {
                                val cleanNum = contact.phoneNumber.replace(Regex("[^0-9+]"), "")
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNum&text=${Uri.encode("📞 Solicitud de llamada")}")
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.whatsapp")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                                }
                            },
                            onPhoneCall = {
                                val cleanNum = contact.phoneNumber.replace(Regex("[^0-9+]"), "")
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNum")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            onDelete = { onDeleteContact(contact.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: ContactEntity,
    onWhatsAppMessage: () -> Unit,
    onWhatsAppCall: () -> Unit,
    onPhoneCall: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AtlasCardDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasBorder),
        modifier = Modifier.fillMaxWidth().testTag("contact_item_${contact.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AtlasNeonGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AtlasNeonGreen
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AtlasTextPrimary
                        )
                        if (contact.relationship.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(AtlasNeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = contact.relationship,
                                    fontSize = 10.sp,
                                    color = AtlasNeonGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Text(
                        text = contact.phoneNumber,
                        fontSize = 12.sp,
                        color = AtlasTextSecondary
                    )
                    if (contact.aliases.isNotBlank()) {
                        Text(
                            text = "Alias: ${contact.aliases}",
                            fontSize = 10.sp,
                            color = AtlasTextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            // Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Send WhatsApp
                IconButton(onClick = onWhatsAppMessage, modifier = Modifier.size(32.dp).testTag("wa_msg_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "WhatsApp Chat",
                        tint = AtlasNeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Call WhatsApp
                IconButton(onClick = onWhatsAppCall, modifier = Modifier.size(32.dp).testTag("wa_call_btn")) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Llamar WhatsApp",
                        tint = AtlasCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Regular Phone call
                IconButton(onClick = onPhoneCall, modifier = Modifier.size(32.dp).testTag("phone_call_btn")) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Teléfono",
                        tint = AtlasBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).testTag("delete_contact_btn")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar contacto",
                        tint = AtlasTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
