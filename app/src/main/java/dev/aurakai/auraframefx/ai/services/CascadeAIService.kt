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
 * Initialize the native Cascade AI instance for this object.
 *
 * Calls the instance-scoped JNI routine that performs native-side setup associated with this CascadeAIService instance.
 */
    private external fun nativeInitialize()
    /**
 * Native JNI binding that processes an AI request in the native Cascade engine.
 *
 * The `request` parameter is expected to be a JSON-encoded request payload (matching the Kotlin
 * AiRequest serialization). The native implementation processes the request and returns a JSON-encoded
 * response string representing an AgentResponse.
 *
 * Note: implemented in the native "cascade_ai" library. */
private external fun nativeProcessRequest(request: String): String
    /**
 * Instructs the native Cascade AI library to shut down and release any native resources held by this instance.
 *
 * Call this when the service is being destroyed to ensure native-side cleanup. This is a JNI binding with no return value.
 */
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
         * Initializes the native Cascade AI library with an optional platform context.
         *
         * The native side may use the provided `context` (typically an Android Application or other
         * platform-specific handle) to perform context-aware setup such as resource access or
         * environment initialization. `null` is allowed if no context is available.
         *
         * @param context Optional platform context or handle passed to native initialization.
         */
        @JvmStatic
        private external fun nativeInitialize(context: Any?)
        
        /**
         * Processes a serialized request in native code and returns the serialized response.
         *
         * The native implementation (in the "cascade_ai" library) accepts a JSON-encoded request
         * and returns a JSON-encoded response. The request is expected to include fields such as
         * `query`, `type`, and `context`. The returned JSON typically contains keys like
         * `content`, `confidence`, `error`, and `agentName`.
         *
         * @param request JSON-encoded request string.
         * @return JSON-encoded response string produced by the native implementation.
         */
        @JvmStatic
        private external fun nativeProcessRequest(request: String): String
        
        /**
         * JVM entry point invoked from native code to request a graceful shutdown of the service.
         *
         * This method is exposed to the native library and intended to be called from native code
         * when the native side needs the JVM-side components to release resources or terminate
         * background work. It performs no return value.
         */
        @JvmStatic
        fun nativeShutdown() {
            // Implementation will be called from native code
        }
    }

    private val state = mutableMapOf<String, Any>()
    
    /**
     * Returns a map of this agent's capability descriptors.
     *
     * The map contains the following keys:
     * - "ai_processing": short description of the agent's request-processing capability.
     * - "context_awareness": short description of how the agent uses contextual information.
     * - "error_handling": short description of the agent's error-handling behavior.
     *
     * @return A Map where keys are capability identifiers and values are brief descriptions.
     */
    fun getCapabilities(): Map<String, String> {
        return mapOf(
            "ai_processing" to "Basic AI request processing",
            "context_awareness" to "Basic context handling",
            "error_handling" to "Basic error handling"
        )
    }
    
    /**
     * Returns a snapshot of the agent's continuous memory state.
     *
     * The returned map is a shallow copy of the internal state so callers can read the current
     * memory without mutating the service's internal storage.
     *
     * @return A Map<String, Any> containing the current memory key/value pairs.
     */
    private fun getContinuousMemory(): Map<String, Any> {
        return state.toMap()
    }
    
    /**
     * Returns the agent's built-in ethical guidelines.
     *
     * These guidelines are static, human-readable rules the agent uses to constrain behavior
     * (e.g., safety, truthfulness, and IP respect).
     *
     * @return A list of guideline strings.
     */
    private fun getEthicalGuidelines(): List<String> {
        return listOf(
            "Prioritize user safety and privacy",
            "Avoid generating harmful or misleading content",
            "Respect intellectual property rights"
        )
    }
    
    /**
     * Returns the agent's recorded learning history.
     *
     * Currently a placeholder that returns an empty list; replace with persisted or accumulated events
     * to expose actual learning history.
     *
     * @return A list of learning event descriptions, or an empty list if none are recorded.
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
     * Process the given AiRequest and return an AgentResponse.
     *
     * Serializes the request to JSON, hands it to the native Cascade processor, and
     * deserializes the native JSON reply into an AgentResponse. If an exception
     * occurs the function returns an error AgentResponse with zero confidence.
     *
     * Note: the `context` parameter is not used by this implementation.
     *
     * @param request The request to process.
     * @param context Unused in this implementation.
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
     * Process an AiRequest and emit one or more AgentResponse values as a Flow, routing to specialized handlers by request.type.
     *
     * Supported request.type values:
     * - "state": delegated to processStateRequestFlowInternal
     * - "context": delegated to processContextRequestFlowInternal
     * - "vision": delegated to processVisionRequestFlowInternal
     * - "processing": delegated to processProcessingRequestFlowInternal
     *
     * For any other type a single default AgentResponse is emitted (basic query response, confidence 0.7).
     * The flow first emits a processing status response, then the chosen handler's response. On exception the flow emits an error response.
     *
     * @param request The AI request to process; its `type` field determines routing and its `query` is used for default responses.
     * @return A Flow that emits the processing status followed by the resulting AgentResponse (or an error response if an exception occurs).
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
     * Processes an AiRequest by encoding the request and provided context as JSON, sending it to the native Cascade processor, and returning the parsed AgentResponse.
     *
     * The native response JSON is parsed for `content`, `confidence`, `error`, and `agentName`. If the response omits a confidence value, 0.8f is used. If an exception occurs while invoking or parsing the native call, an AgentResponse with content "Error processing request", confidence 0.1f, and the exception message in `error` is returned.
     *
     * @param request The AI request to process (query and type).
     * @param context Optional context to include with the request.
     * @return The AgentResponse constructed from the native processor's JSON reply or an error response on failure.
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
     * Emits a flow containing a single response indicating that vision state processing is in progress.
     *
     * @return A [Flow] emitting one [AgentResponse] with a message about vision state processing and a confidence score of 0.9.
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
     * Emits a flow containing a single response indicating that a state transition is being processed.
     *
     * @return A flow with an AgentResponse message about state transition processing and a confidence score of 0.9.
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
     * Emits a single AgentResponse containing stored memory for the request's query key.
     *
     * Looks up state[request.query] and emits an AgentResponse with the value converted to a string.
     * If no entry exists, the content will be "No memory found for '<query>'". The emitted response
     * uses a confidence of 0.8 and sets agentName to "Cascade".
     *
     * @param request The AiRequest whose `query` field is used as the lookup key in the internal state map.
     * @return A Flow that emits one AgentResponse with the retrieved memory (or a not-found message).
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
