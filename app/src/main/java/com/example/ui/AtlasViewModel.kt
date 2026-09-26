package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AtlasRepository
import com.example.data.AtlasSettings
import com.example.data.local.ContactEntity
import com.example.service.AtlasCommandProcessor
import com.example.service.AtlasOverlayService
import com.example.service.CommandResult
import com.example.service.SpeechManager
import com.example.ui.theme.AtlasEmotion
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AtlasTab(val title: String) {
    ASSISTANT("Atlas"),
    CONTACTS("WhatsApp"),
    HISTORY("Historial"),
    COMMANDS("Comandos"),
    SETTINGS("Ajustes")
}

data class AtlasUiState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isThinking: Boolean = false,
    val audioRms: Float = 0f,
    val currentInputText: String = "",
    val liveTranscript: String = "",
    val lastCommand: String = "",
    val lastResponse: String = "Atlas listo para recibir tus órdenes. Habla o escribe para usar WhatsApp, Spotify, YouTube y más.",
    val lastCategory: String = "SYSTEM",
    val lastResult: CommandResult? = null,
    val activeTab: AtlasTab = AtlasTab.ASSISTANT,
    val settings: AtlasSettings = AtlasSettings(),
    val isOverlayPermissionGranted: Boolean = false,
    val currentEmotion: AtlasEmotion = AtlasEmotion.PHILOSOPHICAL
)

class AtlasViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AtlasRepository(application)
    private val speechManager = SpeechManager(application)
    private val commandProcessor = AtlasCommandProcessor(application, repository)

    private val initialSettings = repository.getSettings()
    private val initialEmotion = try {
        AtlasEmotion.valueOf(initialSettings.defaultEmotionName)
    } catch (e: Exception) {
        AtlasEmotion.PHILOSOPHICAL
    }

    private val _uiState = MutableStateFlow(
        AtlasUiState(
            settings = initialSettings,
            isOverlayPermissionGranted = Settings.canDrawOverlays(application),
            currentEmotion = initialEmotion
        )
    )
    val uiState: StateFlow<AtlasUiState> = _uiState.asStateFlow()

    val history = repository.getAllInteractions()
    val contacts = repository.getAllContacts()

    private val _launchIntentEvent = MutableSharedFlow<Intent>()
    val launchIntentEvent: SharedFlow<Intent> = _launchIntentEvent.asSharedFlow()

    init {
        // Pre-seed default contacts (Mamá, Carlos, Trabajo) if empty
        viewModelScope.launch {
            repository.seedInitialContactsIfEmpty()
        }

        // Synchronize speech manager state with UI state
        viewModelScope.launch {
            speechManager.isListening.collect { listening ->
                _uiState.value = _uiState.value.copy(isListening = listening)
            }
        }
        viewModelScope.launch {
            speechManager.isSpeaking.collect { speaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = speaking)
            }
        }
        viewModelScope.launch {
            speechManager.audioRms.collect { rms ->
                _uiState.value = _uiState.value.copy(audioRms = rms)
            }
        }
        viewModelScope.launch {
            speechManager.lastRecognizedText.collect { text ->
                if (text.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(liveTranscript = text)
                }
            }
        }

        // Apply saved speech settings
        val s = _uiState.value.settings
        speechManager.updateSpeechConfig(s.speechRate, s.speechPitch)

        // Check overlay state on start
        checkOverlayStatus()
    }

    fun checkOverlayStatus() {
        val hasPermission = Settings.canDrawOverlays(getApplication())
        _uiState.value = _uiState.value.copy(isOverlayPermissionGranted = hasPermission)
    }

    fun onTabSelected(tab: AtlasTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(currentInputText = text)
    }

    fun startListening() {
        speechManager.stopSpeaking()
        _uiState.value = _uiState.value.copy(liveTranscript = "")
        speechManager.startListening(
            onResult = { spokenText ->
                _uiState.value = _uiState.value.copy(liveTranscript = spokenText)
                executeCommand(spokenText)
            },
            onError = { errMsg ->
                _uiState.value = _uiState.value.copy(
                    lastResponse = errMsg,
                    lastCategory = "SYSTEM"
                )
            }
        )
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
    }

    fun repeatLastSpeech() {
        val response = _uiState.value.lastResponse
        if (response.isNotBlank()) {
            speechManager.speak(response)
        }
    }

    fun submitTextCommand() {
        val text = _uiState.value.currentInputText.trim()
        if (text.isNotBlank()) {
            _uiState.value = _uiState.value.copy(currentInputText = "")
            executeCommand(text)
        }
    }

    fun executeCommand(rawCommand: String) {
        if (rawCommand.isBlank()) return
        val currentSettings = _uiState.value.settings

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isThinking = true,
                lastCommand = rawCommand,
                liveTranscript = rawCommand
            )

            val result = commandProcessor.procesarComando(
                rawComando = rawCommand,
                userName = currentSettings.userName
            )

            // Detect emotional context from user command if auto-switch is enabled
            if (currentSettings.emotionAutoSwitch) {
                val detectedEmotion = detectEmotionFromCommand(rawCommand)
                if (detectedEmotion != null) {
                    _uiState.value = _uiState.value.copy(currentEmotion = detectedEmotion)
                }
            }

            _uiState.value = _uiState.value.copy(
                isThinking = false,
                lastResponse = result.displayResponse,
                lastCategory = result.category,
                lastResult = result
            )

            // Save to database
            repository.recordInteraction(
                command = rawCommand,
                response = result.displayResponse,
                category = result.category
            )

            // Speak if auto-speak is enabled
            if (currentSettings.autoSpeak) {
                speechManager.speak(result.speechResponse)
            }

            // Launch external app intent (WhatsApp, Spotify, YouTube, Phone, etc.)
            if (currentSettings.openLinksDirectly && result.externalIntent != null) {
                _launchIntentEvent.emit(result.externalIntent)
            }
        }
    }

    private fun detectEmotionFromCommand(cmd: String): AtlasEmotion? {
        val lower = cmd.lowercase()
        return when {
            lower.contains("centinela") || lower.contains("sentinel") || lower.contains("alerta") || lower.contains("seguridad") || lower.contains("diagnostico") || lower.contains("peligro") -> AtlasEmotion.SENTINEL
            lower.contains("filosof") || lower.contains("marco aurelio") || lower.contains("pensar") || lower.contains("sentido de la vida") || lower.contains("por que") || lower.contains("reflexion") || lower.contains("aprender") -> AtlasEmotion.PHILOSOPHICAL
            lower.contains("alegre") || lower.contains("feliz") || lower.contains("broma") || lower.contains("chiste") || lower.contains("animo") || lower.contains("musica") || lower.contains("cancion") || lower.contains("divert") -> AtlasEmotion.HAPPY
            lower.contains("seren") || lower.contains("calma") || lower.contains("paz") || lower.contains("silencio") || lower.contains("descanso") || lower.contains("medita") || lower.contains("leer") || lower.contains("libro") -> AtlasEmotion.SERENE
            lower.contains("capaz") || lower.contains("trabajo") || lower.contains("codigo") || lower.contains("programar") || lower.contains("tarea") || lower.contains("reunion") || lower.contains("urgente") || lower.contains("ejecuta") -> AtlasEmotion.CAPABLE
            else -> null
        }
    }

    fun setEmotion(emotion: AtlasEmotion) {
        _uiState.value = _uiState.value.copy(currentEmotion = emotion)
        val updated = _uiState.value.settings.copy(defaultEmotionName = emotion.name)
        updateSettings(updated)
    }

    fun setOverlayActive(enable: Boolean, context: Context) {
        if (enable) {
            if (Settings.canDrawOverlays(context)) {
                AtlasOverlayService.start(context)
                val updated = _uiState.value.settings.copy(overlayAlwaysActive = true)
                updateSettings(updated)
            }
        } else {
            AtlasOverlayService.stop(context)
            val updated = _uiState.value.settings.copy(overlayAlwaysActive = false)
            updateSettings(updated)
        }
    }

    fun addContact(name: String, phone: String, relationship: String = "", aliases: String = "") {
        viewModelScope.launch {
            repository.saveContact(name, phone, relationship, aliases)
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            repository.deleteContact(id)
        }
    }

    fun updateSettings(newSettings: AtlasSettings) {
        repository.saveSettings(newSettings)
        speechManager.updateSpeechConfig(newSettings.speechRate, newSettings.speechPitch)
        _uiState.value = _uiState.value.copy(settings = newSettings)
    }

    fun toggleFavorite(id: Long, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, isFav)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteInteraction(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.release()
    }
}
