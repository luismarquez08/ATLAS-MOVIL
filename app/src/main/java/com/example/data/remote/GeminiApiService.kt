package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun askAtlasAi(prompt: String, userName: String = "Luis"): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineSmartResponse(prompt, userName)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemInstruction = "Te llamas Atlas. Eres el asistente personal inteligente de $userName (a quien llamas Jefe o Luis cordialmente). Responde siempre en español de forma concisa, inteligente, natural, cordial y servicial. Tienes una personalidad futurista y confiable, sin rodeos innecesarios."

            val jsonBody = JSONObject().apply {
                // systemInstruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                // contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                // generationConfig
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 500)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorStr = response.body?.string() ?: ""
                    return@withContext "No pude procesar la consulta con el servidor de IA ($errorStr). Por favor verifica la clave de API."
                }
                val bodyStr = response.body?.string() ?: return@withContext "Sin respuesta recibida."
                val json = JSONObject(bodyStr)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No tengo una respuesta para eso.")
                    }
                }
                "Atlas no pudo generar una respuesta en este momento."
            }
        } catch (e: Exception) {
            generateOfflineSmartResponse(prompt, userName)
        }
    }

    private fun generateOfflineSmartResponse(query: String, userName: String): String {
        val q = query.lowercase()
        return when {
            "quién eres" in q || "quien eres" in q || "qué eres" in q ->
                "Soy Atlas versión 1.1.1 móvil, tu asistente personal virtual. Estoy aquí para ayudarte a buscar información, reproducir música en Spotify y YouTube, revisar el clima, noticias y responder tus preguntas, $userName."
            "creador" in q || "quién te creó" in q || "desarrollador" in q ->
                "Fui desarrollado por Luis Daniel Marquez como un asistente virtual con comandos de voz avanzados e inteligencia artificial."
            "python" in q || "código" in q || "programar" in q ->
                "Jefe, un buen código es su mejor obra. Recuerda escribir funciones modulares y no olvidar las pruebas unitarias."
            else ->
                "He recibido tu consulta sobre \"$query\", $userName. Atlas está listo para procesar toda tu información."
        }
    }
}
