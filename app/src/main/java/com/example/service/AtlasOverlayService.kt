package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AtlasRepository
import com.example.data.AtlasSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.abs

class AtlasOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private lateinit var repository: AtlasRepository
    private lateinit var speechManager: SpeechManager
    private lateinit var commandProcessor: AtlasCommandProcessor

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isExpanded = false

    // Always-on Wake-Word Engine
    private var wakeRecognizer: SpeechRecognizer? = null
    private var isWakeListening = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentSettings: AtlasSettings = AtlasSettings()

    // UI references in HUD
    private var statusTv: TextView? = null
    private var sentinelStatusTv: TextView? = null
    private var bubbleButton: ImageView? = null

    override fun onCreate() {
        super.onCreate()
        repository = AtlasRepository(applicationContext)
        speechManager = SpeechManager(applicationContext)
        commandProcessor = AtlasCommandProcessor(applicationContext, repository)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        currentSettings = repository.getSettings()

        startForegroundNotification()

        if (Settings.canDrawOverlays(this)) {
            setupOverlayBubble()
            if (currentSettings.continuousWakeWord) {
                startContinuousWakeListening()
            }
        } else {
            stopSelf()
        }
    }

    private fun startForegroundNotification() {
        val channelId = "atlas_overlay_channel"
        val channelName = "Atlas Asistente Siempre Activo"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Atlas vigilante y disponible en todo el dispositivo"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Atlas está siempre activo")
            .setContentText("Modo Centinela: Di 'Atlas' en cualquier pantalla para ordenar")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun setupOverlayBubble() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 350
        }

        val rootLayout = FrameLayout(this)

        // 1. Collapsed Floating Orb Button
        bubbleButton = ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher)
            layoutParams = FrameLayout.LayoutParams(160, 160)
            elevation = 20f
        }

        // 2. Expanded Floating HUD Card (Jarvis Style Emerald)
        val hudLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0C1610.toInt())
            setPadding(32, 24, 32, 24)
            visibility = View.GONE
            elevation = 25f
        }

        val titleTv = TextView(this).apply {
            text = "⚡ ATLAS ASISTENTE JARVIS"
            setTextColor(0xFF00E676.toInt())
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_HORIZONTAL
        }
        hudLayout.addView(titleTv)

        sentinelStatusTv = TextView(this).apply {
            text = if (currentSettings.continuousWakeWord) "🟢 Centinela Silencioso: Di 'Atlas' en cualquier momento" else "⚪ Escucha manual activada"
            setTextColor(if (currentSettings.continuousWakeWord) 0xFF00E676.toInt() else 0xFFA7D7B5.toInt())
            textSize = 11f
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 4, 0, 8)
        }
        hudLayout.addView(sentinelStatusTv)

        statusTv = TextView(this).apply {
            text = "Toca para hablar o di 'Atlas'"
            setTextColor(0xFFF1FDF5.toInt())
            textSize = 12f
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 4, 0, 16)
        }
        hudLayout.addView(statusTv)

        // Action Buttons Row
        val actionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        // Talk Button
        val micBtn = TextView(this).apply {
            text = "🎤 HABLAR"
            setTextColor(0xFF060B08.toInt())
            setBackgroundColor(0xFF00E676.toInt())
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(26, 16, 26, 16)
        }
        micBtn.setOnClickListener {
            stopContinuousWakeListening()
            statusTv?.text = "Escuchando... Di tu orden ahora"
            statusTv?.setTextColor(0xFF00E676.toInt())
            speechManager.startListening(
                onResult = { spokenText ->
                    processSpokenCommand(spokenText)
                },
                onError = { err ->
                    statusTv?.text = err
                    statusTv?.setTextColor(0xFFEF4444.toInt())
                    resumeWakeListeningDelayed()
                }
            )
        }
        actionsRow.addView(micBtn)

        // WhatsApp Direct Button
        val waBtn = TextView(this).apply {
            text = "💬 WA"
            setTextColor(0xFFF1FDF5.toInt())
            setBackgroundColor(0xFF102016.toInt())
            textSize = 12f
            setPadding(20, 16, 20, 16)
        }
        waBtn.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        actionsRow.addView(waBtn)

        // Spotify Button
        val spotifyBtn = TextView(this).apply {
            text = "🎵 SPOTIFY"
            setTextColor(0xFFF1FDF5.toInt())
            setBackgroundColor(0xFF102016.toInt())
            textSize = 12f
            setPadding(20, 16, 20, 16)
        }
        spotifyBtn.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.spotify.music")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("spotify:"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        actionsRow.addView(spotifyBtn)

        // Close HUD Button
        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(0xFF94A3B8.toInt())
            textSize = 16f
            setPadding(24, 16, 16, 16)
        }
        closeBtn.setOnClickListener {
            hudLayout.visibility = View.GONE
            bubbleButton?.visibility = View.VISIBLE
            isExpanded = false
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(rootLayout, params)
        }
        actionsRow.addView(closeBtn)

        hudLayout.addView(actionsRow)

        rootLayout.addView(hudLayout)
        rootLayout.addView(bubbleButton)

        // Touch & Drag Handling for Floating Orb
        bubbleButton?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(rootLayout, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)
                    if (diffX < 15 && diffY < 15) {
                        isExpanded = !isExpanded
                        if (isExpanded) {
                            bubbleButton?.visibility = View.GONE
                            hudLayout.visibility = View.VISIBLE
                        }
                    }
                    true
                }
                else -> false
            }
        }

        overlayView = rootLayout
        windowManager.addView(rootLayout, params)
    }

    // --- ALWAYS-ON WAKE-WORD DETECTION ENGINE ---

    private fun startContinuousWakeListening() {
        if (isWakeListening || !SpeechRecognizer.isRecognitionAvailable(this)) return

        wakeRecognizer?.destroy()
        wakeRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isWakeListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isWakeListening = false
                }
                override fun onError(error: Int) {
                    isWakeListening = false
                    // Re-arm cleanly on silence or timeout without spamming errors
                    if (currentSettings.continuousWakeWord) {
                        mainHandler.postDelayed({ startContinuousWakeListening() }, 1000)
                    }
                }
                override fun onResults(results: Bundle?) {
                    isWakeListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""

                    if (spoken.isNotBlank()) {
                        handleContinuousSpeechResult(spoken)
                    } else if (currentSettings.continuousWakeWord) {
                        mainHandler.postDelayed({ startContinuousWakeListening() }, 500)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull()?.lowercase() ?: ""
                    if (partial.contains("atlas")) {
                        vibrateDevice()
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            wakeRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isWakeListening = false
        }
    }

    private fun stopContinuousWakeListening() {
        try {
            wakeRecognizer?.stopListening()
            wakeRecognizer?.destroy()
        } catch (e: Exception) {}
        wakeRecognizer = null
        isWakeListening = false
    }

    private fun resumeWakeListeningDelayed() {
        if (currentSettings.continuousWakeWord) {
            mainHandler.postDelayed({ startContinuousWakeListening() }, 1500)
        }
    }

    private fun handleContinuousSpeechResult(spokenText: String) {
        val lower = spokenText.lowercase()
        val wakeWord = currentSettings.wakeWord.lowercase()

        // Check if wake-word is present ("Atlas", "Oye Atlas", etc.)
        if (lower.contains(wakeWord) || lower.contains("atlas")) {
            vibrateDevice()
            processSpokenCommand(spokenText)
        } else {
            // Re-arm listener immediately
            resumeWakeListeningDelayed()
        }
    }

    private fun processSpokenCommand(spokenText: String) {
        statusTv?.text = "\"$spokenText\""
        statusTv?.setTextColor(0xFFF8FAFC.toInt())

        serviceScope.launch {
            val result = commandProcessor.procesarComando(spokenText, currentSettings.userName)
            statusTv?.text = result.displayResponse
            speechManager.speak(result.speechResponse)

            // Direct execution on native phone system:
            if (result.externalIntent != null) {
                try {
                    result.externalIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(result.externalIntent)
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "No se pudo abrir la app nativa", Toast.LENGTH_SHORT).show()
                }
            }

            // Save to database
            repository.recordInteraction(spokenText, result.displayResponse, result.category)

            // Re-arm wake listening after speech completes
            resumeWakeListeningDelayed()
        }
    }

    private fun vibrateDevice() {
        if (!currentSettings.vibrateOnWake) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(120)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopContinuousWakeListening()
        speechManager.release()
        serviceScope.cancel()
        if (overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {}
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AtlasOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AtlasOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
