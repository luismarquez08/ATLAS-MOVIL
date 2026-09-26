package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private var speechRate = 1.0f
    private var speechPitch = 1.0f

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val esResult = tts?.setLanguage(Locale.forLanguageTag("es-CO"))
                if (esResult == TextToSpeech.LANG_MISSING_DATA || esResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val genericEs = tts?.setLanguage(Locale.forLanguageTag("es"))
                    if (genericEs == TextToSpeech.LANG_MISSING_DATA || genericEs == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.getDefault())
                    }
                }
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                isTtsReady = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun updateSpeechConfig(rate: Float, pitch: Float) {
        this.speechRate = rate
        this.speechPitch = pitch
        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isTtsReady || text.isBlank()) {
            onDone?.invoke()
            return
        }
        stopSpeaking()
        val utteranceId = "atlas_utterance_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _isSpeaking.value = false
    }

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Reconocimiento de voz no disponible en este dispositivo.")
            return
        }

        stopSpeaking()

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _audioRms.value = 0f
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Normalizing dB level roughly between 0.0 and 1.0
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _audioRms.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _audioRms.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _audioRms.value = 0f
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No logré escucharte."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tiempo de espera agotado."
                        SpeechRecognizer.ERROR_AUDIO -> "Error de grabación de audio."
                        SpeechRecognizer.ERROR_CLIENT -> "Reconocimiento cancelado."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Error de conexión de red."
                        else -> "No pude entender el comando."
                    }
                    onError(errorMsg)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _audioRms.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""
                    _lastRecognizedText.value = spoken
                    if (spoken.isNotBlank()) {
                        onResult(spoken)
                    } else {
                        onError("No logré escuchar el comando.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""
                    if (spoken.isNotBlank()) {
                        _lastRecognizedText.value = spoken
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError("Error al iniciar micrófono: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
        _isListening.value = false
        _audioRms.value = 0f
    }

    fun release() {
        stopSpeaking()
        tts?.shutdown()
        tts = null
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
