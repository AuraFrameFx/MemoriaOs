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
 * Initialize the native Cascade AI runtime.
 *
 * Implemented in the native (JNI) library; performs any required native startup and runtime initialization
 * before native processing calls (e.g., nativeProcessRequest) can be used.
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
 * Instructs the native "cascade_ai" library to perform shutdown and cleanup.
 *
 * Implemented in native code via JNI; calling this triggers native resource release and stops native processing. */
private external fun nativeShutdown()

    init {
        System.loadLibrary("cascade_ai")
        nativeInitialize()
    }

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
         * Initialize the native Cascade AI runtime, optionally passing an Android Context to the native layer.
         *
         * When provided, `context` can be used by the native initializer to access Android-specific resources or services;
         * passing `null` instructs the native runtime to initialize without Android platform bindings.
         *
         * @param context an Android Context (or `null`) forwarded to the native initializer.
         */
        @JvmStatic
        private external fun nativeInitialize(context: Any?)
        
        /**
         * JNI native entry point that processes an AI request and returns an AI response.
         *
         * Implemented in the native "cascade_ai" library. Expects `request` to be a JSON-serialized
         * AiRequest and returns a JSON-serialized AgentResponse. Callers are responsible for
         * serializing the request and deserializing the returned JSON.
         *
         * @param request JSON string representing the AiRequest to process.
         * @return JSON string representing the resulting AgentResponse.
         */
        @JvmStatic
        private external fun nativeProcessRequest(request: String): String
        
        /**
         * Called from native code to perform JVM-side shutdown/cleanup for the Cascade native library.
         *
         * This method is expected to be invoked by the native (JNI) layer when the native library is unloading
         * or needs to trigger any managed cleanup on the JVM. It has no parameters and returns no value.
         */
        @JvmStatic
        fun nativeShutdown() {
            // Implementation will be called from native code
        }
    }

    private val state = mutableMapOf<String, Any>()
    
    /**
     * Retrieves the capabilities of this agent.
     *
     * @return A map of capability names to their descriptions.
     */
    fun getCapabilities(): Map<String, String> {
        return mapOf(
            "ai_processing" to "Basic AI request processing",
            "context_awareness" to "Basic context handling",
            "error_handling" to "Basic error handling"
        )
    }
    
    /**
     * Return a snapshot of the agent's continuous memory.
     *
     * The returned map is an immutable shallow copy of the internal `state` at the time of call.
     *
     * @return A Map<String, Any> representing the current memory state.
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
     * Return the agent's recorded learning history.
     *
     * Currently a placeholder that returns an empty list. Replace with persisted
     * learning records when a storage mechanism is implemented.
     *
     * @return A list of learning-event descriptions, or an empty list if none exist.
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
     * Returns the type of this agent as `AgentType.CASCADE`.
     *
     * @return The agent type for this agent.
     */
    override fun getType(): AgentType = AgentType.CASCADE
    
    /**
     * Process the given AiRequest via the native Cascade processor and return an AgentResponse.
     *
     * Serializes the request to JSON, forwards it to the native processing entrypoint, and
     * deserializes the native result into an AgentResponse. If processing fails, returns an
     * AgentResponse whose `content` contains the error message, `confidence` is 0.0, and
     * `error` holds the exception message.
     *
     * @param request The AiRequest to process.
     * @param context Additional context string forwarded to the native processor (may be empty).
     * @return The resulting AgentResponse (never throws; errors are represented in the returned response).
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
     * Processes an AiRequest and emits one or more AgentResponse values as a Flow, routing to specialized handlers by request.type.
     *
     * Delegates to internal handlers for types "state", "context", "vision", and "processing". For any other type emits a default basic-query response (confidence 0.7). Emits an initial processing status before producing the final response. If an exception occurs, emits a single error AgentResponse describing the failure.
     *
     * @param request The AI request to process; routing is determined by `request.type`.
     * @return A Flow that first emits a processing status and then the final AgentResponse (or an error response on failure).
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
     * Processes an AiRequest by serializing it to JSON, sending it to native processing, and returning the parsed AgentResponse.
     *
     * The request is encoded with the request's query, type, and the provided context, then passed to nativeProcessRequest.
     * The native JSON response is parsed into an AgentResponse. If any error occurs during serialization, native invocation,
     * or parsing, this method returns an AgentResponse with a low confidence (0.1) and the error message populated.
     *
     * @param request The AI request to process (query and type are used).
     * @param context Optional additional context to include with the request.
     * @return The AgentResponse produced from the native processor, or an error AgentResponse on failure.
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
     * Aggregates the first responses from both Aura and Kai AI services for a context-type AI request.
     *
     * Emits a single AgentResponse containing combined content from both services and the average of their confidence scores.
     *
     * @return A flow emitting the aggregated AgentResponse.
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
     * Emit a single-agent response indicating that vision processing is underway.
     *
     * The provided [request] is not inspected by this helper; it always returns a fixed
     * "Processing vision state..." response with high confidence.
     *
     * @param request The original AI request (unused).
     * @return A [Flow] that emits one [AgentResponse] with content "Processing vision state..." and confidence 0.9.
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
