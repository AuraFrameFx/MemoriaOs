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
 * Must be invoked after the native library has been loaded; performs any platform-native setup required
 * by the other JNI-backed methods in this class.
 */
    private external fun nativeInitialize()
    /**
 * Sends a JSON-encoded request to the native Cascade AI pipeline and returns the native JSON response.
 *
 * The `request` string is expected to be a JSON object containing at least:
 * - `query` (String): the user's query or prompt
 * - `type` (String): request category (e.g., "state", "context", "vision", "processing")
 * - `context` (String): optional contextual information
 *
 * The returned string is a JSON object with fields typically including:
 * - `content` (String): the agent's textual response
 * - `confidence` (Number): confidence score
 * - `error` (String|null): error message when applicable
 * - `agentName` (String): name of the responding agent
 */
private external fun nativeProcessRequest(request: String): String
    /**
 * Requests the native Cascade AI library to perform shutdown and cleanup of native resources.
 *
 * This calls into the native layer to release any allocated resources, stop background threads, and perform library-specific teardown.
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
         * This JNI entrypoint performs any required native-side setup for the Cascade AI pipeline.
         *
         * @param context Optional platform/context object (typically an Android `Context`) passed to native code; may be `null`.
         */
        @JvmStatic
        private external fun nativeInitialize(context: Any?)
        
        /**
         * Sends a serialized request to the native Cascade AI pipeline and returns the serialized response.
         *
         * The function is a JNI bridge into the native "cascade_ai" library. The `request` parameter is a JSON
         * string (e.g., with keys such as `query`, `type`, and `context`) and the return value is a JSON string
         * encoding the agent response (typically containing keys like `content`, `confidence`, `error`, and `agentName`).
         *
         * @param request JSON-encoded request payload consumed by the native pipeline.
         * @return JSON-encoded response produced by the native pipeline.
         */
        @JvmStatic
        private external fun nativeProcessRequest(request: String): String
        
        /**
         * Called from native (JNI) code to request the Kotlin-side shutdown/cleanup for the Cascade AI service.
         *
         * This method is exposed as `@JvmStatic` so native code can invoke it. Implementations should perform any
         * necessary resource release or state cleanup on the Kotlin side when the native library requests shutdown.
         */
        @JvmStatic
        fun nativeShutdown() {
            // Implementation will be called from native code
        }
    }

    private val state = mutableMapOf<String, Any>()
    
    /**
     * Returns the agent's capability descriptions.
     *
     * The returned map contains the following keys:
     * - "ai_processing": description of the agent's AI request processing capability.
     * - "context_awareness": description of how the agent handles contextual information.
     * - "error_handling": description of the agent's error handling behavior.
     *
     * @return A map from capability name to its human-readable description.
     */
    fun getCapabilities(): Map<String, String> {
        return mapOf(
            "ai_processing" to "Basic AI request processing",
            "context_awareness" to "Basic context handling",
            "error_handling" to "Basic error handling"
        )
    }
    
    /**
     * Returns a snapshot of the agent's continuous memory.
     *
     * Provides a shallow, immutable copy of the internal mutable state map so callers cannot modify the original state.
     *
     * @return A Map<String, Any> containing the current memory entries.
     */
    private fun getContinuousMemory(): Map<String, Any> {
        return state.toMap()
    }
    
    /**
     * Retrieves the ethical guidelines for this agent.
     *
     * @return A list of ethical guidelines.
     */
    private fun getEthicalGuidelines(): List<String> {
        return listOf(
            "Prioritize user safety and privacy",
            "Avoid generating harmful or misleading content",
            "Respect intellectual property rights"
        )
    }
    
    /**
     * Retrieves the learning history of the agent.
     *
     * @return A list of learning events or an empty list if none.
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
     * Process an AI request and return an AgentResponse from the Cascade pipeline.
     *
     * This suspending call forwards the provided request to the underlying Cascade processing (native pipeline)
     * and returns the resulting AgentResponse. If processing fails, an error-valued AgentResponse is returned
     * (no exception is thrown).
     *
     * @param request The AiRequest to be processed.
     * @param context Optional contextual string for the request. NOTE: currently not used by the implementation.
     * @return An AgentResponse representing the agent's result; on failure the response will contain an error message
     *         and a low confidence score.
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
     * Processes an AI request and emits agent responses as a flow, routing to specialized handlers based on the request type.
     *
     * Requests with types "state", "context", "vision", or "processing" are delegated to corresponding internal handlers. For other types, emits a default response indicating a basic query with a confidence score of 0.7.
     *
     * @param request The AI request to process.
     * @return A flow emitting agent responses relevant to the request type.
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
     * Processes an AiRequest with optional context by sending a JSON payload to the native Cascade pipeline
     * and returning the parsed AgentResponse.
     *
     * The request is marshalled to JSON (fields: `query`, `type`, `context`) and passed to the native `nativeProcessRequest`.
     * The native JSON response is parsed into an AgentResponse (fields: `content`, `confidence`, `error`, `agentName`).
     * If parsing or the native call fails, a fallback AgentResponse is returned with an error message and a low confidence.
     *
     * @param request The AI request to process.
     * @param context Optional contextual information to include in the request payload.
     * @return An AgentResponse constructed from the native JSON response, or an error response on failure.
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
     * Calls Aura and Kai with the given context-type request, combines their first responses,
     * and emits a single AgentResponse with merged content and the averaged confidence.
     *
     * The combined content is formatted as "Aura: {aura.content}, Kai: {kai.content}" where empty
     * responses are replaced with "No content". On error emits an error AgentResponse with low confidence.
     *
     * @param request The context-type AiRequest to forward to Aura and Kai.
     * @return A Flow that emits exactly one aggregated AgentResponse (or an error response).
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
     * Emits the stored memory entry for the given request's query as a single AgentResponse.
     *
     * Looks up `request.query` in the service's internal `state` map and emits an AgentResponse
     * whose `content` is the stored value (or a "No memory found" message when absent).
     * The emitted response uses confidence 0.8 and `agentName` "Cascade".
     *
     * @param request The AiRequest whose `query` field is used as the memory lookup key.
     * @return A Flow that emits one AgentResponse containing the retrieved memory (or not-found message).
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
