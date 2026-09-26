package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AtlasRepository
import com.example.service.AtlasCommandProcessor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AtlasCommandProcessorTest {

    private lateinit var processor: AtlasCommandProcessor
    private lateinit var context: Context
    private lateinit var repository: AtlasRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = AtlasRepository(context)
        processor = AtlasCommandProcessor(context, repository)
    }

    @Test
    fun testWakePhrase() = runBlocking {
        val result = processor.procesarComando("atlas")
        assertEquals("SYSTEM", result.category)
        assertTrue(processor.respuestasActivacion.contains(result.speechResponse))
    }

    @Test
    fun testStructuredWhatsAppMessageToWife() = runBlocking {
        repository.saveContact(
            name = "My Wife",
            phoneNumber = "+573001234567",
            relationship = "Esposa",
            aliases = "my wife, wife, esposa, amor"
        )
        // User request: "atlas, enviar mensaje por whatsapp a my wife, que la amo"
        val result = processor.procesarComando("atlas enviar mensaje por whatsapp a my wife que la amo")
        assertEquals("WHATSAPP", result.category)
        assertTrue(result.displayResponse.contains("My Wife"))
        // Check pronoun normalization: "que la amo" -> "Te amo"
        assertTrue(result.displayResponse.contains("Te amo") || result.speechResponse.contains("Te amo"))
        assertNotNull(result.externalIntent)
    }

    @Test
    fun testStructuredWhatsAppCallToMama() = runBlocking {
        repository.saveContact(
            name = "Mamá",
            phoneNumber = "+573007654321",
            relationship = "Madre",
            aliases = "mama, mamá, mom, mother"
        )
        // User request: "atlas llama a mamá por whatsapp"
        val result = processor.procesarComando("atlas llama a mamá por whatsapp")
        assertEquals("WHATSAPP", result.category)
        assertTrue(result.speechResponse.contains("Mamá"))
        assertNotNull(result.externalIntent)
    }

    @Test
    fun testSpotifyPlayback() = runBlocking {
        val result = processor.procesarComando("atlas reproduce bohemian rhapsody en spotify")
        assertEquals("SPOTIFY", result.category)
        assertTrue(result.speechResponse.contains("bohemian rhapsody"))
        assertNotNull(result.externalIntent)
    }

    @Test
    fun testYouTubePlayback() = runBlocking {
        val result = processor.procesarComando("atlas reproduce daft punk en youtube")
        assertEquals("YOUTUBE", result.category)
        assertTrue(result.speechResponse.contains("daft punk"))
        assertNotNull(result.externalIntent)
    }

    @Test
    fun testSmartAlarm() = runBlocking {
        val result = processor.procesarComando("atlas pon una alarma a las 7 de la mañana")
        assertEquals("ALARM", result.category)
        assertTrue(result.displayResponse.contains("07:00") || result.speechResponse.contains("7"))
        assertNotNull(result.externalIntent)
    }

    @Test
    fun testJarvisQuickMath() = runBlocking {
        val result = processor.procesarComando("atlas cuánto es 45 por 12")
        assertEquals("MATH", result.category)
        assertTrue(result.displayResponse.contains("540"))
    }

    @Test
    fun testJarvisPercentageMath() = runBlocking {
        val result = processor.procesarComando("atlas cuánto es el 15% de 80000")
        assertEquals("MATH", result.category)
        assertTrue(result.displayResponse.contains("12000"))
    }

    @Test
    fun testHoraCommand() = runBlocking {
        val result = processor.procesarComando("qué hora es")
        assertEquals("SYSTEM", result.category)
        assertTrue(result.speechResponse.contains("La hora actual es"))
    }

    @Test
    fun testMensajeJefe() = runBlocking {
        val result = processor.procesarComando("mensaje jefe")
        assertEquals("MOTIVATION", result.category)
        assertTrue(processor.mensajesJefe.contains(result.speechResponse))
    }
}
