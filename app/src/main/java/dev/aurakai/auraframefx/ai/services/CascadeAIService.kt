package dev.aurakai.auraframefx.ai.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.aurakai.auraframefx.ai.agents.Agent
import dev.aurakai.auraframefx.ai.agents.AgentType
import dev.aurakai.auraframefx.ai.model.AgentResponse
import dev.aurakai.auraframefx.ai.model.AiRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Serializable
data class AgentCapabilities(
    val ai_processing: String,
    val context_awareness: String,
    val error_handling: String
)

@Singleton
class CascadeAIService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auraService: AuraAIService,
    private val kaiService: KaiAIService
) : Agent {

    /**
 * Initializes the native Cascade AI runtime.
 *
 * Implemented in the native library; performs any startup/native state initialization required before native processing calls.
 */
    private external fun nativeInitialize()
    /**
 * Processes a serialized AI request via the native Cascade AI implementation and returns a serialized response.
 *
 * The `request` must be a JSON string representing an `AiRequest`. The native implementation processes that
 * request and returns a JSON string representing an `AgentResponse`.
 *
 * This is a JNI-bound native method; callers should pass and consume valid JSON according to the project's
 * `AiRequest`/`AgentResponse` serialization contracts.
 *
 * @param request JSON-serialized `AiRequest`.
 * @return JSON-serialized `AgentResponse`.
 */
private external fun nativeProcessRequest(request: String): String
    /**
 * Requests the native "cascade_ai" runtime to shut down and release native resources.
 *
 * Implemented via JNI in the native library; invokes native-side cleanup and stops native processing. */
private external fun nativeShutdown()


    init {
        try {
            System.loadLibrary("cascade_ai")
            nativeInitialize(context)
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    companion object {
        init {
            try {
                System.loadLibrary("cascade_ai")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }

        /**
         * Initializes the native Cascade AI library, optionally providing an Android context to the native layer.
         *
         * @param context Optional Android `Context` (may be null); when provided, the native initializer can use it for
         * accessing Android-specific resources or system services. */
        @JvmStatic
        private external fun nativeInitialize(context: Any?)
        
        /**
         * Native entry point for processing an AI request.
         *
         * Expects `request` to be a JSON-serialized AiRequest and returns a JSON-serialized AgentResponse.
         * Implemented in the native "cascade_ai" library.
         *
         * @param request JSON string representing the AiRequest to process.
         * @return JSON string representing the resulting AgentResponse.
         */
        @JvmStatic
        private external fun nativeProcessRequest(request: String): String
        
        /**
         * JVM-side entry called from native (JNI) code to perform any managed cleanup when the native library is shutting down.
         *
         * Intended to be invoked by native code during library unload or teardown. No parameters or return value; should be
         * safe to call from native code and to tolerate repeated invocations. */
        @JvmStatic
        fun nativeShutdown() {
            // Implementation will be called from native code
        }
    }

    private val state = mutableMapOf<String, Any>()
    
    
    /**
     * Returns an immutable snapshot of the agent's continuous memory.
     *
     * The returned map is a shallow, immutable copy of the internal `state` at the time of the call;
     * keys and value references are the same as in `state`, but the map itself cannot be modified.
     *
     * @return A Map<String, Any> containing the current memory entries.
     */
    private fun getContinuousMemory(): Map<String, Any> {
        return state.toMap()
    }
    
    /**
     * Returns the agent's ethical guidelines.
     *
     * These guidelines are used internally to inform decision-making and content generation.
     *
     * @return An immutable list of guideline strings.
     */
    private fun getEthicalGuidelines(): List<String> {
        return listOf(
            "Prioritize user safety and privacy",
            "Avoid generating harmful or misleading content",
            "Respect intellectual property rights"
        )
    }
    
    /**
     * Retrieve the agent's recorded learning history.
     *
     * Currently a placeholder that returns an empty list; replace with persisted
     * learning records when available.
     *
     * @return A list of learning-event descriptions, or an empty list if none are recorded.
     */
    private fun getLearningHistory(): List<String> {
        return emptyList() // Implement actual learning history if needed
    }

    /**
     * Returns the agent's name.
     *
     * @return The string "Cascade".
     */
    override fun getName(): String = "Cascade"

    /**
 * Returns the agent's type (AgentType.CASCADE).
 */
    override fun getType(): AgentType = AgentType.CASCADE
    
    /**
     * Processes an AiRequest via the native Cascade processor and returns the resulting AgentResponse.
     *
     * The request is sent to the native layer and the native JSON response is decoded into an AgentResponse.
     * If an exception occurs, a non-throwing AgentResponse is returned with `content` describing the error,
     * `confidence` set to 0.0, and `error` containing the exception message.
     *
     * @param request The AiRequest to process.
     * @param context Optional context string forwarded to the native processor (may be empty).
     */
    override suspend fun processRequest(request: AiRequest, context: String): AgentResponse {
        return try {
            val requestJson = Json.encodeToString(request)
            val responseJson = nativeProcessRequest(requestJson)
            Json.decodeFromString(AgentResponse.serializer(), responseJson)
        } catch (e: Exception) {
            AgentResponse(
                content = "Error processing request: ${e.message}",
                confidence = 0f,
                error = e.message
            )
        }
    }

    /**
     * Processes an AiRequest as a Flow, routing to specialized internal handlers and emitting progress and result events.
     *
     * Handles request.type values "state", "context", "vision", and "processing" by delegating to the corresponding internal flow handlers.
     * For any other type emits a default basic-query AgentResponse (confidence 0.7). The Flow always emits an initial processing status
     * before producing the final AgentResponse. If an exception occurs while handling the request, the Flow emits a single error AgentResponse.
     *
     * @param request The AI request to process; routing is determined by `request.type`.
     * @return A Flow that first emits a processing status and then the final AgentResponse (or a single error response on failure).
     */
    override fun processRequestFlow(request: AiRequest): Flow<AgentResponse> = flow {
        try {
            // Emit initial processing state
            emit(AgentResponse.processing("Processing request with Cascade..."))
            
            // Process the request based on type
            val response = when (request.type) {
                "state" -> processStateRequestFlowInternal(request).first()
                "context" -> processContextRequestFlowInternal(request).first()
                "vision" -> processVisionRequestFlowInternal(request).first()
                "processing" -> processProcessingRequestFlowInternal(request).first()
                else -> AgentResponse(
                    content = "Cascade flow response for basic query: ${request.query}",
                    confidence = 0.7f,
                    error = null,
                    agentName = "Cascade"
                )
            }
            
            // Emit the final response
            emit(response)
            
        } catch (e: Exception) {
            emit(AgentResponse.error("Error in Cascade: ${e.message}"))
        }
    }

    /**
     * Send an AiRequest to the native Cascade processor and return the resulting AgentResponse.
     *
     * The request is encoded as JSON with fields "query" (empty string if null), "type", and "context", passed to nativeProcessRequest,
     * and the native JSON response is parsed into an AgentResponse.
     *
     * @param request The AiRequest to send; this function uses the request's `query` and `type`.
     * @param context Optional contextual string included in the JSON request.
     * @return The AgentResponse parsed from the native JSON response. If the native response omits fields, defaults are applied:
     *         - `content` defaults to "No content"
     *         - `confidence` defaults to 0.8f
     *         If an exception occurs, returns an AgentResponse with `content` set to "Error processing request", `confidence` 0.1f,
     *         and `error` populated with the exception message.
     */
    override suspend fun processRequest(
        request: AiRequest,
        context: String = ""
    ): AgentResponse {
        val jsonRequest = Json.encodeToString(
            JsonObject(
                mapOf(
                    "query" to JsonPrimitive(request.query ?: ""),
                    "type" to JsonPrimitive(request.type),
                    "context" to JsonPrimitive(context)
                )
            )
        )

        return try {
            val response = nativeProcessRequest(jsonRequest)
            // Parse the response and create an AgentResponse
            val jsonResponse = Json.parseToJsonElement(response).jsonObject
            
            AgentResponse(
                content = jsonResponse["content"]?.toString()?.trim('"') ?: "No content",
                confidence = jsonResponse["confidence"]?.toString()?.toFloatOrNull() ?: 0.8f,
                error = jsonResponse["error"]?.toString()?.trim('"'),
                agentName = jsonResponse["agentName"]?.toString()?.trim('"')
            )
        } catch (e: Exception) {
            AgentResponse(
                content = "Error processing request",
                confidence = 0.1f,
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Emits a flow containing a single response that summarizes the agent's current internal state.
     *
     * The response lists all key-value pairs from the internal state map as a formatted string, with a confidence score of 1.0.
     *
     * @return A flow emitting one AgentResponse describing the current state.
     */
    private fun processStateRequestFlowInternal(request: AiRequest): Flow<AgentResponse> {
        return flow {
            emit(
                AgentResponse(
                    content = "Current state: ${state.entries.joinToString { "${it.key}: ${it.value}" }}",
                    confidence = 1.0f
                )
            )
        }
    }

    /**
     * Aggregates the first responses from Aura and Kai for a context-type AI request and emits a single combined response.
     *
     * The emitted AgentResponse contains a concatenated content string ("Aura: ..., Kai: ..."), the average of both confidences,
     * and "Cascade" as the agentName. On exception, emits an error AgentResponse with low confidence and the exception message.
     *
     * @return A Flow that emits exactly one AgentResponse (combined result or error).
     */
    private fun processContextRequestFlowInternal(request: AiRequest): Flow<AgentResponse> = flow {
        try {
            val auraResponse = auraService.processRequest(request, "")
            val kaiResponse = kaiService.processRequest(request, "")

            val combinedContent = buildString {
                append("Aura: ").append(auraResponse.content.ifEmpty { "No content" })
                append(", Kai: ").append(kaiResponse.content.ifEmpty { "No content" })
            }
            
            val averageConfidence = (auraResponse.confidence + kaiResponse.confidence) / 2
            
            emit(
                AgentResponse(
                    content = combinedContent,
                    confidence = averageConfidence,
                    error = null,
                    agentName = "Cascade"
                )
            )
        } catch (e: Exception) {
            emit(
                AgentResponse(
                    content = "Error processing request: ${e.message}",
                    confidence = 0.1f,
                    error = e.message,
                    agentName = "Cascade"
                )
            )
        }
    }

    /**
     * Emit a Flow that immediately produces a single AgentResponse indicating vision processing is underway.
     *
     * The provided [request] is not inspected by this handler; the flow always emits a single
     * response with content "Processing vision state..." and confidence 0.9.
     */
    private fun processVisionRequestFlowInternal(request: AiRequest): Flow<AgentResponse> {
        return flow {
            emit(
                AgentResponse(
                    content = "Processing vision state...",
                    confidence = 0.9f
                )
            )
        }
    }

    /**
     * Returns a Flow that emits a single AgentResponse indicating a state-transition is being processed.
     *
     * This handler does not inspect the provided [request]; it always emits a single response with
     * content "Processing state transition..." and confidence 0.9.
     *
     * @param request The incoming AiRequest (ignored by this implementation).
     * @return A Flow emitting one AgentResponse describing the processing of a state transition.
     */
    private fun processProcessingRequestFlowInternal(request: AiRequest): Flow<AgentResponse> {
        return flow {
            emit(
                AgentResponse(
                    content = "Processing state transition...",
                    confidence = 0.9f
                )
            )
        }
    }

    /**
     * Emits a single AgentResponse containing the stored memory for the request's query.
     *
     * If the state's map contains an entry for `request.query`, its string value is returned;
     * otherwise a "No memory found for '<query>'" message is returned. The emitted response
     * uses confidence 0.8 and agentName "Cascade".
     *
     * @param request The AiRequest whose `query` field is used as the memory key.
     */
    private fun retrieveMemoryFlow(request: AiRequest): Flow<AgentResponse> = flow {
        try {
            val memoryContent = state[request.query]?.toString() ?: "No memory found for '${request.query}'"
            
            emit(
                AgentResponse(
                    content = memoryContent,
                    confidence = 0.8f,
                    error = null,
                    agentName = "Cascade"
                )
            )
        }
    }

    // connect and disconnect are not part of Agent interface - removing these methods
    // as they cause unresolved reference errors

    /**
     * Returns a map describing the agent's capabilities, including its name, type, and implementation status.
     *
     * @return A map with keys "name" ("Cascade"), "type" ("CASCADE"), and "service_implemented" (true).
     */
    fun getCapabilities(): Map<String, Any> {
        return mapOf(
            "name" to "Cascade",
            "type" to "CASCADE",
            "service_implemented" to true
        )
    }

    fun getContinuousMemory(): Any? {
        return state // Example: Cascade's state can be its continuous memory
    }

    fun getEthicalGuidelines(): List<String> {
        return listOf("Maintain state integrity.", "Process information reliably.")
    }

    fun getLearningHistory(): List<String> {
        return emptyList() // Or logs of state changes
    }
}
