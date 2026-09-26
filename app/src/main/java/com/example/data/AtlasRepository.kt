package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AtlasDatabase
import com.example.data.local.ContactEntity
import com.example.data.local.InteractionEntity
import kotlinx.coroutines.flow.Flow
import java.text.Normalizer

data class AtlasSettings(
    val userName: String = "Luis",
    val city: String = "Bogota",
    val autoSpeak: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val openLinksDirectly: Boolean = true,
    val overlayAlwaysActive: Boolean = false,
    val continuousWakeWord: Boolean = true, // Always listen for "Atlas" in background / overlay
    val wakeWord: String = "Atlas",
    val vibrateOnWake: Boolean = true,
    val strictOwnerSecurity: Boolean = true, // Personalized responses strictly for Luis
    val defaultEmotionName: String = "PHILOSOPHICAL", // HAPPY, PHILOSOPHICAL, SERENE, CAPABLE, SENTINEL
    val emotionAutoSwitch: Boolean = true // Autonomous mood switching based on user query
)

class AtlasRepository(context: Context) {
    private val db = AtlasDatabase.getInstance(context)
    private val interactionDao = db.interactionDao()
    private val contactDao = db.contactDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("atlas_prefs", Context.MODE_PRIVATE)

    fun getAllInteractions(): Flow<List<InteractionEntity>> = interactionDao.getAllInteractions()

    fun getRecentInteractions(limit: Int = 20): Flow<List<InteractionEntity>> =
        interactionDao.getRecentInteractions(limit)

    suspend fun recordInteraction(command: String, response: String, category: String) {
        interactionDao.insertInteraction(
            InteractionEntity(
                command = command,
                response = response,
                category = category
            )
        )
    }

    suspend fun toggleFavorite(id: Long, isFav: Boolean) = interactionDao.updateFavorite(id, isFav)

    suspend fun deleteInteraction(id: Long) = interactionDao.deleteById(id)

    suspend fun clearHistory() = interactionDao.clearAll()

    // Contacts
    fun getAllContacts(): Flow<List<ContactEntity>> = contactDao.getAllContacts()

    private fun normalize(str: String): String {
        return Normalizer.normalize(str.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    suspend fun findContact(query: String): ContactEntity? {
        val q = query.trim()
        if (q.isBlank()) return null
        val normQ = normalize(q)

        // 1. Direct database queries
        val exact = contactDao.findByName(q)
        if (exact != null) return exact

        val byName = contactDao.searchByName(q)
        if (byName != null) return byName

        val byAlias = contactDao.searchByAlias(q)
        if (byAlias != null) return byAlias

        val byRel = contactDao.searchByRelationship(q)
        if (byRel != null) return byRel

        // 2. In-memory normalized scan over all contacts for fuzzier match
        val all = contactDao.getAllContactsList()
        return all.firstOrNull { contact ->
            val normName = normalize(contact.name)
            val normRel = normalize(contact.relationship)
            val normAliases = normalize(contact.aliases)

            normName == normQ ||
            normName.contains(normQ) ||
            normQ.contains(normName) ||
            normRel.contains(normQ) ||
            normQ.contains(normRel) ||
            normAliases.split(",").any { a ->
                val trimmedA = a.trim()
                trimmedA.isNotBlank() && (trimmedA == normQ || normQ.contains(trimmedA) || trimmedA.contains(normQ))
            }
        }
    }

    suspend fun saveContact(name: String, phoneNumber: String, relationship: String = "", aliases: String = "") {
        contactDao.insertContact(
            ContactEntity(
                name = name.trim(),
                phoneNumber = phoneNumber.trim(),
                relationship = relationship.trim(),
                aliases = aliases.trim()
            )
        )
    }

    suspend fun deleteContact(id: Long) = contactDao.deleteContact(id)

    suspend fun seedInitialContactsIfEmpty() {
        if (contactDao.count() == 0) {
            contactDao.insertContact(
                ContactEntity(
                    name = "My Wife",
                    phoneNumber = "+573001234567",
                    relationship = "Esposa",
                    aliases = "my wife, wife, esposa, mi esposa, mujer, mi mujer, amor, mi amor"
                )
            )
            contactDao.insertContact(
                ContactEntity(
                    name = "Mamá",
                    phoneNumber = "+573007654321",
                    relationship = "Madre",
                    aliases = "mama, mamá, mami, mom, mother, madre"
                )
            )
            contactDao.insertContact(
                ContactEntity(
                    name = "Papá",
                    phoneNumber = "+573008889999",
                    relationship = "Padre",
                    aliases = "papa, papá, papi, dad, father, padre"
                )
            )
            contactDao.insertContact(
                ContactEntity(
                    name = "Carlos",
                    phoneNumber = "+573109876543",
                    relationship = "Amigo",
                    aliases = "carlos, socio, parcero, compa"
                )
            )
        }
    }

    fun getSettings(): AtlasSettings {
        return AtlasSettings(
            userName = prefs.getString("user_name", "Luis") ?: "Luis",
            city = prefs.getString("city", "Bogota") ?: "Bogota",
            autoSpeak = prefs.getBoolean("auto_speak", true),
            speechRate = prefs.getFloat("speech_rate", 1.0f),
            speechPitch = prefs.getFloat("speech_pitch", 1.0f),
            openLinksDirectly = prefs.getBoolean("open_links", true),
            overlayAlwaysActive = prefs.getBoolean("overlay_active", false),
            continuousWakeWord = prefs.getBoolean("continuous_wake_word", true),
            wakeWord = prefs.getString("wake_word", "Atlas") ?: "Atlas",
            vibrateOnWake = prefs.getBoolean("vibrate_on_wake", true),
            strictOwnerSecurity = prefs.getBoolean("strict_security", true),
            defaultEmotionName = prefs.getString("default_emotion", "PHILOSOPHICAL") ?: "PHILOSOPHICAL",
            emotionAutoSwitch = prefs.getBoolean("emotion_auto_switch", true)
        )
    }

    fun saveSettings(settings: AtlasSettings) {
        prefs.edit()
            .putString("user_name", settings.userName)
            .putString("city", settings.city)
            .putBoolean("auto_speak", settings.autoSpeak)
            .putFloat("speech_rate", settings.speechRate)
            .putFloat("speech_pitch", settings.speechPitch)
            .putBoolean("open_links", settings.openLinksDirectly)
            .putBoolean("overlay_active", settings.overlayAlwaysActive)
            .putBoolean("continuous_wake_word", settings.continuousWakeWord)
            .putString("wake_word", settings.wakeWord)
            .putBoolean("vibrate_on_wake", settings.vibrateOnWake)
            .putBoolean("strict_security", settings.strictOwnerSecurity)
            .putString("default_emotion", settings.defaultEmotionName)
            .putBoolean("emotion_auto_switch", settings.emotionAutoSwitch)
            .apply()
    }
}
