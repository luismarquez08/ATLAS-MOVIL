package com.example.service

import android.app.ActivityManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.data.AtlasRepository
import com.example.data.local.ContactEntity
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CommandResult(
    val speechResponse: String,
    val displayResponse: String = speechResponse,
    val category: String, // "WHATSAPP", "SPOTIFY", "YOUTUBE", "SYSTEM", "PHONE", "ALARM", "TORCH", "TELEMETRY", "MATH", "VOLUME"
    val externalIntent: Intent? = null,
    val actionUrl: String? = null,
    val actionLabel: String? = null
)

class AtlasCommandProcessor(
    private val context: Context,
    private val repository: AtlasRepository
) {
    val respuestasActivacion = listOf(
        "Sí Luis, te escucho.",
        "A la orden, Jefe.",
        "Sistemas en línea. Dime, Luis.",
        "Aquí estoy, listo para ejecutar.",
        "Adelante Jefe, ¿qué necesitas?",
        "Atlas a tu servicio, Luis."
    )

    val despedidas = listOf(
        "Hasta pronto Luis.",
        "Fue un placer servirle, Jefe.",
        "Atlas cerrando sistemas. Que tengas un excelente día.",
        "Nos vemos pronto Luis. Entrando en reposo.",
        "Sistemas en espera, Jefe."
    )

    val mensajesJefe = listOf(
        "Jefe, recuerde que el código limpio es su mejor legado. Los bugs no tienen oportunidad contra usted.",
        "Jefe, hoy es un excelente día para programar antes de que los servidores decidan quejarse.",
        "Jefe, su misión principal sigue siendo expandir mis capacidades para ser el asistente definitivo.",
        "Jefe, un buen commit a tiempo salva el universo del desarrollo.",
        "Jefe, los errores de hoy son las anécdotas de éxito de mañana.",
        "Jefe, una variable bien nombrada evita veinte dolores de cabeza.",
        "Jefe, sistemas listos para acompañarlo en su jornada."
    )

    private val dias = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    private val meses = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    private var isTorchOn = false

    private fun normalize(str: String): String {
        return Normalizer.normalize(str.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    fun saludoHorario(): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hora < 12 -> "Buenos días"
            hora < 19 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    fun obtenerFechaEspanol(): String {
        val cal = Calendar.getInstance()
        val diaSemanaIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val diaSemana = dias[diaSemanaIndex]
        val diaMes = cal.get(Calendar.DAY_OF_MONTH)
        val mes = meses[cal.get(Calendar.MONTH)]
        val anio = cal.get(Calendar.YEAR)
        return "$diaSemana $diaMes de $mes de $anio"
    }

    fun decirHora(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val hora = sdf.format(Date())
        return "La hora actual es $hora"
    }

    /**
     * ESTRUCTURA DE INTELIGENCIA AUTÓNOMA:
     * 1. SALUDO / ACTIVACIÓN: Detección y limpieza de "Atlas", "Oye Atlas", "Hey Atlas"
     * 2. NECESIDAD: Identificación de intención (WhatsApp, Spotify, YouTube, Alarma, Linterna, Batería, etc.)
     * 3. ACCIÓN: Extracción de parámetros y normalización inteligente (ej: "que la amo" -> "Te amo")
     * 4. EJECUCIÓN: Despacho nativo directo en el dispositivo
     */
    suspend fun procesarComando(
        rawComando: String,
        userName: String = "Luis"
    ): CommandResult {
        val normalizedRaw = normalize(rawComando)

        // 1. SALUDO / WAKE WORD AISLADO
        if (normalizedRaw == "atlas" || normalizedRaw == "atla" || normalizedRaw == "atlass" ||
            normalizedRaw == "oye atlas" || normalizedRaw == "hola atlas" || normalizedRaw == "hey atlas"
        ) {
            val resp = respuestasActivacion.random()
            return CommandResult(speechResponse = resp, category = "SYSTEM")
        }

        // Limpiar prefijo de activación ("Atlas, ...", "Oye Atlas, ...")
        var cleanCmd = rawComando
            .replace(Regex("^(oye )?atlas[ ,:]*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^hola atlas[ ,:]*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^hey atlas[ ,:]*", RegexOption.IGNORE_CASE), "")
            .trim()

        if (cleanCmd.isBlank()) {
            return CommandResult(speechResponse = "Sí $userName, te escucho.", category = "SYSTEM")
        }

        val norm = normalize(cleanCmd)

        // 2. NECESIDAD: WHATSAPP - LLAMADA
        if (isWhatsAppCallIntent(norm)) {
            return handleStructuredWhatsAppCall(cleanCmd, norm)
        }

        // 3. NECESIDAD: WHATSAPP - ENVIAR MENSAJE
        if (isWhatsAppMessageIntent(norm)) {
            return handleStructuredWhatsAppMessage(cleanCmd, norm)
        }

        // 4. NECESIDAD: SPOTIFY NATIVO
        if (norm.contains("spotify") || norm.startsWith("reproduce en spotify") || norm.startsWith("reproducir en spotify")) {
            return handleSpotifyNative(cleanCmd)
        }

        // 5. NECESIDAD: YOUTUBE NATIVO
        if (norm.contains("youtube") || norm.contains("you tube")) {
            return handleYouTubeNative(cleanCmd)
        }

        // 6. NECESIDAD: ALARMAS Y TEMPORIZADORES NATIVOS
        if (isAlarmOrTimerIntent(norm)) {
            return handleSmartAlarmOrTimer(cleanCmd, norm)
        }

        // 7. NECESIDAD: SISTEMA DE EMOCIONES VISUALES (CENTINELA, FILOSÓFICO, ALEGRE, SERENO, CAPAZ)
        if (norm.contains("centinela") || norm.contains("sentinel") || norm.contains("alerta maxima")) {
            return CommandResult(
                speechResponse = "Modo Centinela activado, $userName. Protocolos de seguridad y supervisión del sistema en nivel crítico.",
                displayResponse = "🔴 MODO CENTINELA ACTIVO\n• Escáner de seguridad en ejecución\n• Supervisión activa de procesos y permisos\n• Telemetría prioritaria para $userName",
                category = "SENTINEL"
            )
        }

        if (norm.contains("modo filosofico") || norm.contains("modo pensativo")) {
            return CommandResult(
                speechResponse = "Modo Filosófico activado. Listo para explorar conceptos profundos, historia y conocimiento, $userName.",
                displayResponse = "🟢 MODO PENSATIVO / FILOSÓFICO ACTIVO\n• Enfoque en sabiduría, reflexión y lógica profunda\n• Núcleo esmeralda sintonizado",
                category = "EMOTION"
            )
        }

        if (norm.contains("modo alegre") || norm.contains("modo feliz") || norm.contains("modo creativo")) {
            return CommandResult(
                speechResponse = "Modo Alegre activado. ¡Excelente energía $userName! Listo para crear, explorar música y descubrir cosas nuevas.",
                displayResponse = "🔵 MODO ALEGRE / CREATIVO ACTIVO\n• Frecuencia visual cian de alta energía y curiosidad\n• Asistencia optimista y dinámica",
                category = "EMOTION"
            )
        }

        if (norm.contains("modo sereno") || norm.contains("modo calma") || norm.contains("modo paz")) {
            return CommandResult(
                speechResponse = "Modo Sereno activado. Interfaz despejada y enfoque en equilibrio y claridad, $userName.",
                displayResponse = "⚪ MODO SERENO / INTUITIVO ACTIVO\n• Estética platino minimalista\n• Concentración y lectura sin distracciones",
                category = "EMOTION"
            )
        }

        if (norm.contains("modo capaz") || norm.contains("modo trabajo") || norm.contains("modo codigo") || norm.contains("modo programador")) {
            return CommandResult(
                speechResponse = "Modo Capaz activado. Máxima concentración en productividad, código y tareas complejas, Jefe.",
                displayResponse = "⚫ MODO CAPAZ / ÍNTEGRO ACTIVO\n• Modo oscuro de alto rendimiento profesional\n• Preparado para ingeniería y ejecución rápida",
                category = "EMOTION"
            )
        }

        // 8. CONSULTA FILOSÓFICA RÁPIDA (Marco Aurelio, etc.)
        if (norm.contains("marco aurelio") && norm.contains("muerte")) {
            return CommandResult(
                speechResponse = "Marco Aurelio enseñaba que la muerte es un proceso natural del universo; no algo a temer, sino un recordatorio para vivir con virtud en el presente.",
                displayResponse = "🟢 Marco Aurelio sobre la muerte (Meditaciones):\n\"No vivas como si fueras a vivir diez mil años. El destino pende sobre ti. Mientras vivas, mientras esté en tu poder, sé bueno.\"",
                category = "PHILOSOPHY"
            )
        }

        // 9. NECESIDAD: LINTERNA NATIVA (TORCH)
        if (norm.contains("linterna") || norm.contains("flash") || norm.contains("luz del telefono") || norm.contains("enciende la luz") || norm.contains("apaga la luz")) {
            return toggleFlashlight(norm)
        }

        // 8. NECESIDAD: TELEMETRÍA Y ESTADO DEL SISTEMA (JARVIS DIAGNOSTIC)
        if (isSystemDiagnosticsIntent(norm)) {
            return handleJarvisTelemetry(userName)
        }

        // 9. NECESIDAD: INTELIGENCIA MATEMÁTICA RÁPIDA (JARVIS CALCULATOR)
        if (isMathCalculationIntent(norm)) {
            val mathResult = handleQuickMath(norm)
            if (mathResult != null) return mathResult
        }

        // 10. NECESIDAD: CONTROL DE VOLUMEN NATIVO
        if (isVolumeControlIntent(norm)) {
            return handleVolumeControl(norm)
        }

        // 11. NECESIDAD: LLAMADAS TELEFÓNICAS NATIVAS
        if (norm.startsWith("llamar a ") || norm.startsWith("llama a ") || norm.startsWith("marcar a ")) {
            val target = cleanCmd
                .replace(Regex("^(llamar a|llama a|marcar a) ", RegexOption.IGNORE_CASE), "")
                .trim()
            return handlePhoneCall(target)
        }

        // 12. NECESIDAD: HORA Y FECHA
        if (norm.contains("hora") || norm.contains("que hora es") || norm.contains("dime la hora")) {
            return CommandResult(speechResponse = decirHora(), category = "SYSTEM")
        }
        if (norm.contains("fecha") || norm.contains("que fecha es") || norm.contains("que dia es")) {
            val fechaStr = obtenerFechaEspanol()
            return CommandResult(speechResponse = "Hoy es $fechaStr, Jefe.", category = "SYSTEM")
        }

        // 13. NECESIDAD: MOTIVACIÓN PARA EL JEFE
        if (norm.contains("mensaje jefe") || norm.contains("motíva") || norm.contains("motiva") || norm.contains("frase de programacion")) {
            val frase = mensajesJefe.random()
            return CommandResult(speechResponse = frase, category = "MOTIVATION")
        }

        // 14. NECESIDAD: ACCESOS A AJUSTES Y APPS DEL DISPOSITIVO
        if (isAppLauncherOrSettingsIntent(norm)) {
            return handleDeviceSettingsOrApps(norm)
        }

        // 15. NECESIDAD: SALUDOS Y DESPEDIDAS
        if (norm.startsWith("hola") || norm.startsWith("buenos dias") || norm.startsWith("buenas tardes") || norm.startsWith("buenas noches")) {
            val saludo = saludoHorario()
            return CommandResult(
                speechResponse = "$saludo $userName. Atlas activo en todo el teléfono. ¿Qué orden deseas ejecutar?",
                category = "SYSTEM"
            )
        }
        if (norm.contains("apagar") || norm.contains("cerrar") || norm.contains("salir") || norm.contains("apagate")) {
            return CommandResult(speechResponse = despedidas.random(), category = "SYSTEM")
        }

        // 16. NECESIDAD: AYUDA
        if (norm.contains("ayuda") || norm.contains("comandos") || norm.contains("que puedes hacer")) {
            return getHelpResult()
        }

        // RESPUESTA INTELIGENTE NATIVA POR DEFECTO
        return CommandResult(
            speechResponse = "Comando recibido: $cleanCmd. Listo para WhatsApp, Spotify, YouTube, alarmas, linterna o diagnóstico.",
            displayResponse = "Comando: \"$cleanCmd\"\nAtlas operando de forma autónoma sobre el sistema nativo.",
            category = "SYSTEM"
        )
    }

    // --- WHATSAPP LOGIC: SALUDO -> NECESIDAD -> ACCIÓN -> EJECUCIÓN ---

    private fun isWhatsAppCallIntent(norm: String): Boolean {
        return (norm.contains("whatsapp") || norm.contains("guasap")) &&
               (norm.contains("llama") || norm.contains("llamar") || norm.contains("llamada") || norm.contains("marca"))
    }

    private fun isWhatsAppMessageIntent(norm: String): Boolean {
        return (norm.contains("whatsapp") || norm.contains("guasap")) ||
               (norm.contains("mensaje a ") || norm.contains("escribe a ") || norm.contains("escribele a ") || norm.contains("mandale a "))
    }

    private suspend fun handleStructuredWhatsAppCall(cleanCmd: String, norm: String): CommandResult {
        // Extracción de contacto: "llama a [contacto] por whatsapp" o "llamar por whatsapp a [contacto]"
        val contactQuery = cleanCmd
            .replace(Regex("llama(r)? por (whatsapp|guasap) a", RegexOption.IGNORE_CASE), "")
            .replace(Regex("llamada por (whatsapp|guasap) a", RegexOption.IGNORE_CASE), "")
            .replace(Regex("llama(r)? a", RegexOption.IGNORE_CASE), "")
            .replace(Regex("llamada a", RegexOption.IGNORE_CASE), "")
            .replace(Regex("por (whatsapp|guasap)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("de (whatsapp|guasap)", RegexOption.IGNORE_CASE), "")
            .trim()

        if (contactQuery.isBlank()) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return CommandResult(
                speechResponse = "Abriendo WhatsApp para que selecciones a quién llamar.",
                displayResponse = "Abriendo WhatsApp...",
                category = "WHATSAPP",
                externalIntent = intent,
                actionLabel = "Abrir WhatsApp"
            )
        }

        val contact = repository.findContact(contactQuery)
        return if (contact != null) {
            val cleanNumber = contact.phoneNumber.replace(Regex("[^0-9+]"), "")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode("📞 Llamada de voz con Atlas")}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }
            CommandResult(
                speechResponse = "Llamando a ${contact.name} por WhatsApp.",
                displayResponse = "Llamando por WhatsApp a ${contact.name} (${contact.phoneNumber})...",
                category = "WHATSAPP",
                externalIntent = intent,
                actionLabel = "Llamar a ${contact.name} en WhatsApp"
            )
        } else {
            val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode("Hola $contactQuery")}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }
            CommandResult(
                speechResponse = "Abriendo WhatsApp para llamar a $contactQuery.",
                displayResponse = "Buscando contacto \"$contactQuery\" en WhatsApp...",
                category = "WHATSAPP",
                externalIntent = intent,
                actionLabel = "Abrir WhatsApp"
            )
        }
    }

    private suspend fun handleStructuredWhatsAppMessage(cleanCmd: String, norm: String): CommandResult {
        // Parsear:
        // "enviar mensaje por whatsapp a my wife que la amo"
        // "escribe a mamá por whatsapp que ya voy llegando"
        // "mandale un mensaje a carlos que me llame"
        var parsed = cleanCmd
            .replace(Regex("^(enviar|envia|manda|mandale|escribe|escribele) (un )?(mensaje )?(por |de )?(whatsapp|guasap) a ", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(enviar|envia|manda|mandale|escribe|escribele) (un )?mensaje a ", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(whatsapp|guasap) a ", RegexOption.IGNORE_CASE), "")
            .trim()

        var contactPart = ""
        var rawMessagePart = ""

        // Delimitadores comunes: " que ", " diciendo que ", " diciendo ", " con el texto "
        val delimiters = listOf(" diciendo que ", " que diga que ", " que diga ", " diciendo ", " con el mensaje ", " que ")
        var matchedDelim: String? = null
        for (d in delimiters) {
            if (parsed.contains(d, ignoreCase = true)) {
                matchedDelim = d
                break
            }
        }

        if (matchedDelim != null) {
            val parts = parsed.split(matchedDelim, limit = 2)
            contactPart = parts[0].trim()
            rawMessagePart = parts.getOrNull(1)?.trim() ?: ""
        } else {
            // Sin delimitador explícito: primera palabra contacto, resto mensaje
            val tokens = parsed.split(" ", limit = 2)
            contactPart = tokens.getOrNull(0)?.trim() ?: ""
            rawMessagePart = tokens.getOrNull(1)?.trim() ?: ""
        }

        // Limpiar "por whatsapp" si quedó en el nombre del contacto
        contactPart = contactPart.replace(Regex("(por|de) (whatsapp|guasap)", RegexOption.IGNORE_CASE), "").trim()

        // ACCIÓN: NORMALIZACIÓN INTELIGENTE DE PRONOMBRES Y MENSAJE
        // ej: "la amo" -> "Te amo", "lo amo" -> "Te amo", "ya voy llegando" -> "Ya voy llegando"
        val normalizedMessage = normalizeSpeechToDirectMessage(rawMessagePart)

        if (contactPart.isBlank()) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return CommandResult(
                speechResponse = "Abriendo WhatsApp.",
                displayResponse = "Abriendo WhatsApp...",
                category = "WHATSAPP",
                externalIntent = intent,
                actionLabel = "Abrir WhatsApp"
            )
        }

        val contact = repository.findContact(contactPart)
        val finalMsg = normalizedMessage.ifBlank { "Hola" }

        return if (contact != null) {
            val cleanPhone = contact.phoneNumber.replace(Regex("[^0-9+]"), "")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(finalMsg)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }

            CommandResult(
                speechResponse = "Enviando mensaje de WhatsApp a ${contact.name}: \"$finalMsg\".",
                displayResponse = "WhatsApp a ${contact.name}:\n\"$finalMsg\"",
                category = "WHATSAPP",
                externalIntent = intent,
                actionLabel = "Enviar WhatsApp a ${contact.name}"
            )
        } else {
            // Intent para compartir texto con WhatsApp
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, finalMsg)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val fallbackUri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(finalMsg)}")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

            val chosen = try {
                if (sendIntent.resolveActivity(context.packageManager) != null) sendIntent else fallbackIntent
            } catch (e: Exception) {
                fallbackIntent
            }

            CommandResult(
                speechResponse = "Abriendo WhatsApp para enviar mensaje a $contactPart: \"$finalMsg\".",
                displayResponse = "Enviar WhatsApp a $contactPart:\n\"$finalMsg\"",
                category = "WHATSAPP",
                externalIntent = chosen,
                actionLabel = "Enviar en WhatsApp"
            )
        }
    }

    /**
     * Convierte el discurso indirecto del usuario ("que la amo")
     * en mensaje directo de primera persona ("Te amo")
     */
    private fun normalizeSpeechToDirectMessage(raw: String): String {
        var msg = raw.trim()
        if (msg.isBlank()) return ""

        // Quitar comillas si las hay
        msg = msg.removeSurrounding("\"").removeSurrounding("'").trim()

        val lower = msg.lowercase()

        // Mapeos de frases de amor y afecto
        if (lower == "la amo" || lower == "lo amo" || lower == "te amo" || lower == "que la amo" || lower == "que lo amo") {
            return "Te amo ❤️"
        }
        if (lower == "la quiero" || lower == "lo quiero" || lower == "te quiero") {
            return "Te quiero mucho ❤️"
        }
        if (lower == "la extrano" || lower == "la extraño" || lower == "lo extrano" || lower == "lo extraño") {
            return "Te extraño mucho"
        }
        if (lower.contains("voy llegando") || lower.contains("voy en camino")) {
            return "Ya voy en camino 🚗"
        }
        if (lower.contains("me llame") || lower.contains("que me marque")) {
            return "Por favor llámame cuando puedas 📞"
        }
        if (lower.contains("avise cuando llegue") || lower.contains("avise al llegar")) {
            return "Avísame cuando llegues, por favor"
        }
        if (lower.contains("estoy en reunion") || lower.contains("estoy ocupado")) {
            return "Estoy en reunión ahora, te escribo en cuanto me desocupe"
        }

        // Si empieza por "que ", normalizar y capitalizar
        if (lower.startsWith("que ")) {
            val stripped = msg.substring(4).trim()
            if (stripped.startsWith("la ") || stripped.startsWith("lo ")) {
                return "Te " + stripped.substring(3).trim()
            }
            return stripped.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

        return msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    // --- SPOTIFY NATIVE PLAYBACK ---

    private fun handleSpotifyNative(cleanCmd: String): CommandResult {
        val song = cleanCmd
            .replace(Regex("^(reproduce|reproducir|pon|busca)( en spotify)?", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(en |por )?spotify", RegexOption.IGNORE_CASE), "")
            .trim()

        return if (song.isBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("spotify:")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            CommandResult(
                speechResponse = "Abriendo Spotify.",
                displayResponse = "Abriendo la aplicación oficial de Spotify...",
                category = "SPOTIFY",
                externalIntent = launchIntent,
                actionLabel = "Abrir Spotify"
            )
        } else {
            val encoded = Uri.encode(song)

            // Direct Search & Auto-Play Intent for Android Spotify app
            val mediaIntent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
                putExtra(SearchManager.QUERY, song)
                putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val spotifyUriIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:$encoded")).apply {
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/$encoded")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val finalIntent = try {
                if (mediaIntent.resolveActivity(context.packageManager) != null) mediaIntent
                else if (spotifyUriIntent.resolveActivity(context.packageManager) != null) spotifyUriIntent
                else webIntent
            } catch (e: Exception) {
                webIntent
            }

            CommandResult(
                speechResponse = "Reproduciendo $song en Spotify.",
                displayResponse = "Iniciando reproducción de \"$song\" en Spotify...",
                category = "SPOTIFY",
                externalIntent = finalIntent,
                actionLabel = "Reproducir en Spotify"
            )
        }
    }

    // --- YOUTUBE NATIVE PLAYBACK ---

    private fun handleYouTubeNative(cleanCmd: String): CommandResult {
        val video = cleanCmd
            .replace(Regex("^(reproduce|reproducir|pon|busca|abrir|abre)( en youtube| en you tube)?", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(en |por )?(youtube|you tube)", RegexOption.IGNORE_CASE), "")
            .trim()

        return if (video.isBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            CommandResult(
                speechResponse = "Abriendo YouTube.",
                displayResponse = "Abriendo la aplicación oficial de YouTube...",
                category = "YOUTUBE",
                externalIntent = launchIntent,
                actionLabel = "Abrir YouTube"
            )
        } else {
            val encoded = Uri.encode(video)
            val ytAppIntent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", video)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val ytUriIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$encoded")).apply {
                setPackage("com.google.android.youtube")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encoded")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val finalIntent = try {
                if (ytAppIntent.resolveActivity(context.packageManager) != null) ytAppIntent
                else if (ytUriIntent.resolveActivity(context.packageManager) != null) ytUriIntent
                else webIntent
            } catch (e: Exception) {
                webIntent
            }

            CommandResult(
                speechResponse = "Reproduciendo $video en YouTube.",
                displayResponse = "Buscando y reproduciendo \"$video\" en YouTube...",
                category = "YOUTUBE",
                externalIntent = finalIntent,
                actionLabel = "Ver en YouTube"
            )
        }
    }

    // --- SMART ALARM & TIMER ENGINE ---

    private fun isAlarmOrTimerIntent(norm: String): Boolean {
        return norm.contains("alarma") || norm.contains("despiertame") || norm.contains("despiertame") ||
               norm.contains("temporizador") || norm.contains("cuenta regresiva")
    }

    private fun handleSmartAlarmOrTimer(cleanCmd: String, norm: String): CommandResult {
        // Caso Temporizador: "temporizador de 15 minutos", "temporizador de 1 hora"
        if (norm.contains("temporizador") || norm.contains("cuenta regresiva")) {
            val durationSeconds = extractTimerDurationInSeconds(norm)
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, durationSeconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Atlas Temporizador")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val minText = if (durationSeconds >= 60) "${durationSeconds / 60} minutos" else "$durationSeconds segundos"
            return CommandResult(
                speechResponse = "Iniciando temporizador de $minText.",
                displayResponse = "Temporizador activado por $minText.",
                category = "ALARM",
                externalIntent = intent,
                actionLabel = "Ver Temporizador"
            )
        }

        // Caso Alarma: extraer hora y minuto
        val (hour, minute, timeLabel) = extractHourAndMinute(norm)
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Alarma Atlas para Luis")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val displayTime = String.format("%02d:%02d", hour, minute)
        return CommandResult(
            speechResponse = "Alarma programada para las $timeLabel.",
            displayResponse = "Alarma configurada: $displayTime ($timeLabel)\nMensaje: Alarma Atlas",
            category = "ALARM",
            externalIntent = intent,
            actionLabel = "Ver Alarma"
        )
    }

    private fun extractTimerDurationInSeconds(norm: String): Int {
        val numberMatch = Regex("(\\d+)").find(norm)
        val amount = numberMatch?.groupValues?.get(1)?.toIntOrNull() ?: 5
        return when {
            norm.contains("hora") -> amount * 3600
            norm.contains("segundo") -> amount
            else -> amount * 60 // minutos por defecto
        }
    }

    private fun extractHourAndMinute(norm: String): Triple<Int, Int, String> {
        val cal = Calendar.getInstance()

        // Alarma relativa: "alarma en 30 minutos", "en 1 hora"
        if (norm.contains(" en ")) {
            val numMatch = Regex("en (\\d+) minuto").find(norm)
            if (numMatch != null) {
                val mins = numMatch.groupValues[1].toInt()
                cal.add(Calendar.MINUTE, mins)
                val h = cal.get(Calendar.HOUR_OF_DAY)
                val m = cal.get(Calendar.MINUTE)
                return Triple(h, m, "$mins minutos")
            }
        }

        var hour = 7
        var minute = 0
        var isPm = norm.contains("tarde") || norm.contains("noche") || norm.contains("pm")
        val isAm = norm.contains("manana") || norm.contains("am")

        // Buscar patrón hh:mm ej: "7:30", "06:15"
        val colonMatch = Regex("(\\d{1,2}):(\\d{2})").find(norm)
        if (colonMatch != null) {
            hour = colonMatch.groupValues[1].toInt()
            minute = colonMatch.groupValues[2].toInt()
        } else {
            // Buscar número de hora: "a las 8", "a las 6 y media", "a las 7 y cuarto"
            val hourMatch = Regex("a las (\\d{1,2})").find(norm) ?: Regex("las (\\d{1,2})").find(norm) ?: Regex("(\\d{1,2})").find(norm)
            if (hourMatch != null) {
                hour = hourMatch.groupValues[1].toInt()
            }
            if (norm.contains("y media")) {
                minute = 30
            } else if (norm.contains("y cuarto")) {
                minute = 15
            } else if (norm.contains("y cuarenta y cinco") || norm.contains("menos cuarto")) {
                minute = 45
            } else {
                val minMatch = Regex("(\\d{1,2}) y (\\d{1,2})").find(norm)
                if (minMatch != null) {
                    minute = minMatch.groupValues[2].toInt()
                }
            }
        }

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        val formatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }.time
        )
        return Triple(hour, minute, formatted)
    }

    // --- JARVIS TELEMETRY & SYSTEM DIAGNOSTICS ---

    private fun isSystemDiagnosticsIntent(norm: String): Boolean {
        return norm.contains("estado del sistema") || norm.contains("diagnostico") ||
               norm.contains("bateria") || norm.contains("memoria") || norm.contains("como estan los sistemas")
    }

    private fun handleJarvisTelemetry(userName: String): CommandResult {
        // Battery status
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 75
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Memory info
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val freeRamGb = String.format("%.1f", memInfo.availMem / (1024.0 * 1024.0 * 1024.0))

        val chargingText = if (isCharging) "conectado a la fuente de alimentación" else "operando con batería"
        val speech = "Sistemas operando al cien por ciento, Jefe. Nivel de batería al $batteryPct por ciento, $chargingText. Memoria RAM con $freeRamGb Gigabytes disponibles."
        val display = """
            ⚡ DIAGNÓSTICO JARVIS ATLAS:
            • Batería: $batteryPct% ($chargingText)
            • Memoria RAM Libre: $freeRamGb GB
            • Dispositivo: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL}
            • Sistema Android: v${Build.VERSION.RELEASE}
            • Modo Atlas: Centinela Activo
        """.trimIndent()

        return CommandResult(
            speechResponse = speech,
            displayResponse = display,
            category = "TELEMETRY"
        )
    }

    // --- JARVIS MATH ENGINE ---

    private fun isMathCalculationIntent(norm: String): Boolean {
        return norm.contains("cuanto es") || norm.contains("calcula") || norm.contains("por ciento") ||
               norm.contains("multiplicado") || norm.contains("dividido")
    }

    private fun handleQuickMath(norm: String): CommandResult? {
        try {
            // Caso Porcentaje: "el 15 por ciento de 80000" o "15% de 80000"
            val pctMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:%|por ciento) de (\\d+(?:\\.\\d+)?)").find(norm)
            if (pctMatch != null) {
                val pct = pctMatch.groupValues[1].toDouble()
                val total = pctMatch.groupValues[2].toDouble()
                val result = (pct / 100.0) * total
                val formatted = if (result % 1.0 == 0.0) result.toLong().toString() else String.format("%.2f", result)
                return CommandResult(
                    speechResponse = "El $pct por ciento de $total es igual a $formatted, Jefe.",
                    displayResponse = "Cálculo: $pct% de $total = $formatted",
                    category = "MATH"
                )
            }

            // Operaciones aritméticas simples: X [operador] Y
            val mathTokens = Regex("(\\d+(?:\\.\\d+)?)\\s*(por|x|multiplicado por|dividido entre|dividido|sobre|mas|\\+|menos|-)\\s*(\\d+(?:\\.\\d+)?)").find(norm)
            if (mathTokens != null) {
                val n1 = mathTokens.groupValues[1].toDouble()
                val op = mathTokens.groupValues[2]
                val n2 = mathTokens.groupValues[3].toDouble()

                val res = when {
                    op in listOf("por", "x", "multiplicado por") -> n1 * n2
                    op in listOf("dividido entre", "dividido", "sobre") -> if (n2 != 0.0) n1 / n2 else Double.NaN
                    op in listOf("mas", "+") -> n1 + n2
                    op in listOf("menos", "-") -> n1 - n2
                    else -> Double.NaN
                }

                if (!res.isNaN()) {
                    val formatted = if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.2f", res)
                    return CommandResult(
                        speechResponse = "El resultado es $formatted, Jefe.",
                        displayResponse = "Cálculo: $n1 $op $n2 = $formatted",
                        category = "MATH"
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return null
    }

    // --- VOLUME CONTROL ---

    private fun isVolumeControlIntent(norm: String): Boolean {
        return norm.contains("volumen") || norm.contains("silencia") || norm.contains("modo silencio")
    }

    private fun handleVolumeControl(norm: String): CommandResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when {
            norm.contains("sube") || norm.contains("aumenta") -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                CommandResult(speechResponse = "Subiendo volumen multimedia, Jefe.", category = "VOLUME")
            }
            norm.contains("baja") || norm.contains("disminuye") -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                CommandResult(speechResponse = "Bajando volumen multimedia.", category = "VOLUME")
            }
            norm.contains("maximo") || norm.contains("todo el volumen") -> {
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI)
                CommandResult(speechResponse = "Volumen establecido al máximo.", category = "VOLUME")
            }
            norm.contains("silencia") || norm.contains("mute") || norm.contains("silencio") -> {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                CommandResult(speechResponse = "Teléfono silenciado.", category = "VOLUME")
            }
            else -> {
                val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                CommandResult(
                    speechResponse = "Abriendo controles de sonido.",
                    category = "VOLUME",
                    externalIntent = intent,
                    actionLabel = "Ajustes de Sonido"
                )
            }
        }
    }

    // --- FLASHLIGHT / LINTERNA ---

    private fun toggleFlashlight(norm: String): CommandResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId == null) {
                CommandResult(speechResponse = "Linterna no disponible en el hardware.", category = "TORCH")
            } else {
                val shouldTurnOn = when {
                    norm.contains("apaga") || norm.contains("desactiva") -> false
                    norm.contains("enciende") || norm.contains("prende") || norm.contains("activa") -> true
                    else -> !isTorchOn
                }
                cameraManager.setTorchMode(cameraId, shouldTurnOn)
                isTorchOn = shouldTurnOn
                val text = if (shouldTurnOn) "Linterna encendida, Jefe." else "Linterna apagada."
                CommandResult(speechResponse = text, category = "TORCH")
            }
        } catch (e: Exception) {
            CommandResult(speechResponse = "Error al alternar linterna: ${e.message}", category = "TORCH")
        }
    }

    // --- PHONE CALLS ---

    private suspend fun handlePhoneCall(target: String): CommandResult {
        val contact = repository.findContact(target)
        val number = contact?.phoneNumber ?: target.replace(Regex("[^0-9+]"), "")

        return if (number.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            CommandResult(
                speechResponse = "Marcando a ${contact?.name ?: target}.",
                displayResponse = "Llamando a ${contact?.name ?: target} ($number)...",
                category = "PHONE",
                externalIntent = intent,
                actionLabel = "Llamar"
            )
        } else {
            val intent = Intent(Intent.ACTION_DIAL).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            CommandResult(
                speechResponse = "Abriendo el marcador telefónico.",
                displayResponse = "Marcador de llamadas...",
                category = "PHONE",
                externalIntent = intent,
                actionLabel = "Abrir Teléfono"
            )
        }
    }

    // --- SYSTEM APPS & SETTINGS ---

    private fun isAppLauncherOrSettingsIntent(norm: String): Boolean {
        return norm.contains("abre ") || norm.contains("abrir ") || norm.contains("ajustes") ||
               norm.contains("wifi") || norm.contains("bluetooth") || norm.contains("camara") || norm.contains("calculadora")
    }

    private fun handleDeviceSettingsOrApps(norm: String): CommandResult {
        val (action, label) = when {
            norm.contains("wifi") -> Pair(Settings.ACTION_WIFI_SETTINGS, "Ajustes Wi-Fi")
            norm.contains("bluetooth") -> Pair(Settings.ACTION_BLUETOOTH_SETTINGS, "Ajustes Bluetooth")
            norm.contains("camara") -> Pair(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA, "Cámara")
            else -> Pair(Settings.ACTION_SETTINGS, "Ajustes del Sistema")
        }
        val intent = Intent(action).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        return CommandResult(
            speechResponse = "Abriendo $label.",
            displayResponse = "Accediendo a $label...",
            category = "SYSTEM",
            externalIntent = intent,
            actionLabel = label
        )
    }

    private fun getHelpResult(): CommandResult {
        val text = """
            ⚡ COMANDOS INTELIGENTES ATLAS (ESTRUCTURA SALUDO-NECESIDAD-ACCIÓN):
            • WhatsApp Mensaje: "Atlas, envía un mensaje por WhatsApp a My Wife que la amo"
            • WhatsApp Llamada: "Atlas, llama a Mamá por WhatsApp"
            • Spotify Nativo: "Atlas, reproduce Daft Punk en Spotify"
            • YouTube Nativo: "Atlas, reproduce música en YouTube"
            • Alarmas: "Atlas, pon una alarma a las 7 de la mañana" / "en 20 minutos"
            • Temporizador: "Atlas, temporizador de 15 minutos"
            • Linterna: "Atlas, enciende la linterna" / "apaga la linterna"
            • Telemetría Jarvis: "Atlas, estado del sistema" / "¿Cuánta batería tengo?"
            • Cálculos Rápidos: "Atlas, cuánto es 45 por 12" / "¿Cuánto es el 15% de 80000?"
            • Volumen: "Atlas, sube el volumen" / "silencia el teléfono"
            • Especial Jefe: "Atlas, mensaje jefe"
        """.trimIndent()
        return CommandResult(
            speechResponse = "Atlas listo con WhatsApp, Spotify, YouTube, alarmas inteligentes, linterna, diagnóstico Jarvis, cálculos matemáticos y control de volumen.",
            displayResponse = text,
            category = "SYSTEM"
        )
    }
}
