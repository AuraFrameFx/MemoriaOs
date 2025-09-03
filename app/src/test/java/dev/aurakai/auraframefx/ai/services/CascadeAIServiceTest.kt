package dev.aurakai.auraframefx.ai.services

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

// We will adapt imports for JUnit 4 vs 5 via sed below
import org.junit.jupiter.api.Test

// Serialization
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// Reflection utilities
import java.lang.reflect.Modifier

// NOTE ON TESTING LIBRARIES:
// Detected testing uses JUnit (4 by default here) with Kotlin test assertions and kotlinx-serialization.
// We intentionally avoid instantiating CascadeAIService to prevent JNI calls to System.loadLibrary("cascade_ai").
// These tests focus on pure, testable parts and reflective validation of class structure.

class CascadeAIServiceTest {

    // --- AgentCapabilities data class tests (pure, no JNI) ---

    @Test
    fun `AgentCapabilities serializes to expected JSON keys`() {
        val caps = AgentCapabilities(
            ai_processing = "Basic AI request processing",
            context_awareness = "Basic context handling",
            error_handling = "Basic error handling"
        )
        val json = Json.encodeToString(caps)
        // Ensure keys present and correctly mapped
        assertTrue(json.contains("\"ai_processing\""))
        assertTrue(json.contains("\"context_awareness\""))
        assertTrue(json.contains("\"error_handling\""))
        assertTrue(json.contains("Basic AI request processing"))
        assertTrue(json.contains("Basic context handling"))
        assertTrue(json.contains("Basic error handling"))
    }

    @Test
    fun `AgentCapabilities JSON roundtrip preserves values`() {
        val caps = AgentCapabilities(
            ai_processing = "Proc",
            context_awareness = "Ctx",
            error_handling = "Err"
        )
        val json = Json.encodeToString(caps)
        val decoded = Json.decodeFromString<AgentCapabilities>(json)
        assertEquals("Proc", decoded.ai_processing)
        assertEquals("Ctx", decoded.context_awareness)
        assertEquals("Err", decoded.error_handling)
    }

    // --- CascadeAIService structure tests via reflection (no instantiation) ---

    @Test
    fun `CascadeAIService is annotated and implements Agent`() {
        // Verify package load doesn't trigger JNI (only instantiation would)
        val clazz = CascadeAIService::class.java

        // Confirm it implements Agent interface
        val interfaces = clazz.interfaces.map { it.name }
        assertTrue(
            interfaces.any { it.endsWith(".ai.agents.Agent") },
            "CascadeAIService should implement Agent interface"
        )

        // Check for @Singleton annotation presence
        val hasSingleton = clazz.annotations.any { it.annotationClass.simpleName == "Singleton" }
        assertTrue(hasSingleton, "CascadeAIService should be annotated with @Singleton")
    }

    @Test
    fun `CascadeAIService declares external native methods`() {
        val clazz = CascadeAIService::class.java

        // Look for method named nativeProcessRequest(String): String on instance or companion
        val hasNativeProcess =
            clazz.declaredMethods.any { it.name == "nativeProcessRequest" } ||
            clazz.declaredClasses.any { it.simpleName == "Companion" } &&
            clazz.declaredClasses.first { it.simpleName == "Companion" }
                .declaredMethods.any { it.name == "nativeProcessRequest" }

        assertTrue(hasNativeProcess, "Expected nativeProcessRequest to be declared (instance or companion)")

        // nativeShutdown presence (companion)
        val hasShutdown =
            clazz.declaredMethods.any { it.name == "nativeShutdown" } ||
            clazz.declaredClasses.firstOrNull { it.simpleName == "Companion" }
                ?.declaredMethods?.any { it.name == "nativeShutdown" } == true

        assertTrue(hasShutdown, "Expected nativeShutdown to be declared (companion preferred)")
    }

    // --- Intentional non-instantiation guard ---
    // If future refactors make instantiation JNI-safe (e.g., behind flags), add flow tests here.

}