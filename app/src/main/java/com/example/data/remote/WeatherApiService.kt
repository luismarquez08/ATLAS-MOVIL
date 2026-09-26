package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class WeatherData(
    val city: String,
    val tempC: String,
    val description: String,
    val humidity: String,
    val windKmph: String,
    val feelsLikeC: String
)

class WeatherApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getWeather(city: String = "Bogota"): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val cleanCity = city.replace(" ", "+")
            val url = "https://wttr.in/$cleanCity?format=j1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AtlasAssistant/1.1 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val current = json.getJSONArray("current_condition").getJSONObject(0)
                val tempC = current.optString("temp_C", "--")
                val feelsLikeC = current.optString("FeelsLikeC", tempC)
                val humidity = current.optString("humidity", "--")
                val windKmph = current.optString("windspeedKmph", "--")

                val descArray = current.optJSONArray("weatherDesc")
                val description = if (descArray != null && descArray.length() > 0) {
                    descArray.getJSONObject(0).optString("value", "Despejado")
                } else "Normal"

                WeatherData(
                    city = city,
                    tempC = tempC,
                    description = translateWeatherDesc(description),
                    humidity = "$humidity%",
                    windKmph = "$windKmph km/h",
                    feelsLikeC = feelsLikeC
                )
            }
        } catch (e: Exception) {
            // Fallback default info
            null
        }
    }

    private fun translateWeatherDesc(desc: String): String {
        val lower = desc.lowercase()
        return when {
            "clear" in lower || "sunny" in lower -> "Soleado y despejado"
            "partly cloudy" in lower -> "Parcialmente nublado"
            "cloudy" in lower || "overcast" in lower -> "Nublado"
            "rain" in lower || "drizzle" in lower -> "Lluvioso"
            "thunder" in lower || "storm" in lower -> "Tormenta eléctrica"
            "mist" in lower || "fog" in lower -> "Niebla ligera"
            else -> desc
        }
    }
}
