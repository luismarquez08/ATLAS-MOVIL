package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AtlasTab
import com.example.ui.AtlasViewModel
import com.example.ui.components.AtlasBottomBar
import com.example.ui.components.AtlasTopBar
import com.example.ui.screens.CommandsScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AtlasBgDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AtlasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val historyList by viewModel.history.collectAsStateWithLifecycle(initialValue = emptyList())
                val contactsList by viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList())

                // Audio permission launcher
                val audioPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.startListening()
                    } else {
                        Toast.makeText(
                            this,
                            "Se requiere permiso de micrófono para comandos de voz",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                val onMicClick: () -> Unit = {
                    if (uiState.isListening) {
                        viewModel.stopListening()
                    } else {
                        val hasAudioPerm = ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasAudioPerm) {
                            viewModel.startListening()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }

                // Handle external intents (WhatsApp, Spotify, YouTube, Phone, etc.)
                LaunchedEffect(Unit) {
                    viewModel.launchIntentEvent.collect { intent ->
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "No se pudo abrir la aplicación en el dispositivo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                val requestOverlayPermission = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AtlasBgDark)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = AtlasBgDark,
                    topBar = {
                        AtlasTopBar(
                            isListening = uiState.isListening,
                            isSpeaking = uiState.isSpeaking,
                            isThinking = uiState.isThinking,
                            currentEmotion = uiState.currentEmotion,
                            onEmotionSelected = { viewModel.setEmotion(it) },
                            onStopSpeaking = { viewModel.stopSpeaking() }
                        )
                    },
                    bottomBar = {
                        AtlasBottomBar(
                            currentTab = uiState.activeTab,
                            onTabSelected = { viewModel.onTabSelected(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = uiState.activeTab, label = "tab_fade") { tab ->
                            when (tab) {
                                AtlasTab.ASSISTANT -> {
                                    HomeScreen(
                                        uiState = uiState,
                                        onMicClick = onMicClick,
                                        onInputChanged = { viewModel.onInputTextChanged(it) },
                                        onSubmitText = { viewModel.submitTextCommand() },
                                        onQuickAction = { viewModel.executeCommand(it) },
                                        onRepeatSpeech = { viewModel.repeatLastSpeech() },
                                        onToggleOverlay = {
                                            if (!uiState.isOverlayPermissionGranted) {
                                                requestOverlayPermission()
                                            } else {
                                                viewModel.setOverlayActive(!uiState.settings.overlayAlwaysActive, this@MainActivity)
                                            }
                                        },
                                        onOpenExternal = { intent ->
                                            try {
                                                startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(this@MainActivity, "No se pudo abrir", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }

                                AtlasTab.CONTACTS -> {
                                    ContactsScreen(
                                        contacts = contactsList,
                                        onAddContact = { name, phone, rel, aliases ->
                                            viewModel.addContact(name, phone, rel, aliases)
                                        },
                                        onDeleteContact = { id ->
                                            viewModel.deleteContact(id)
                                        },
                                        onNavigateBack = { viewModel.onTabSelected(AtlasTab.ASSISTANT) }
                                    )
                                }

                                AtlasTab.HISTORY -> {
                                    HistoryScreen(
                                        interactions = historyList,
                                        onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                                        onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                                        onClearAll = { viewModel.clearAllHistory() },
                                        onSpeakText = { text -> viewModel.executeCommand("repite $text") },
                                        onNavigateBack = { viewModel.onTabSelected(AtlasTab.ASSISTANT) }
                                    )
                                }

                                AtlasTab.COMMANDS -> {
                                    CommandsScreen(
                                        onRunCommand = { cmd ->
                                            viewModel.onTabSelected(AtlasTab.ASSISTANT)
                                            viewModel.executeCommand(cmd)
                                        },
                                        onNavigateBack = { viewModel.onTabSelected(AtlasTab.ASSISTANT) }
                                    )
                                }

                                AtlasTab.SETTINGS -> {
                                    SettingsScreen(
                                        currentSettings = uiState.settings,
                                        isOverlayPermissionGranted = uiState.isOverlayPermissionGranted,
                                        onSaveSettings = { viewModel.updateSettings(it) },
                                        onToggleOverlay = { enable ->
                                            viewModel.setOverlayActive(enable, this@MainActivity)
                                        },
                                        onRequestOverlayPermission = requestOverlayPermission,
                                        onTestVoice = {
                                            viewModel.executeCommand("hola atlas")
                                        },
                                        onNavigateBack = { viewModel.onTabSelected(AtlasTab.ASSISTANT) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkOverlayStatus()
    }
}
