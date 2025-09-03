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
 * Initialize native JNI resources used by CascadeAIService.
 *
 * Implemented in the native `cascade_ai` library; must be called once before any other native calls
 * (for example, before `nativeProcessRequest`) to set up native-side state.
 */
    private external fun nativeInitialize()
    /**
 * Calls the native (JNI) implementation to process a serialized AI request and returns a serialized response.
 *
 * The `request` parameter is expected to be a JSON string representing an AiRequest. The return value is a JSON
 * string representing an AgentResponse that the Kotlin code will parse.
 *
 * Implemented in the native "cascade_ai" library.
 *
 * @param request JSON-serialized AiRequest
 * @return JSON-serialized AgentResponse
 */
private external fun nativeProcessRequest(request: String): String
    /**
 * Requests the native library to perform shutdown and release any native resources.
 *
 * This external JNI entry triggers native-side cleanup (for example freeing native memory,
 * stopping background native threads, and closing native handles). It does not return a value
 * and should be called when the service is being destroyed to avoid native resource leaks.
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

        @JvmStatic
        private external fun nativeInitialize(context: Any?)
        
        @JvmStatic
        private external fun nativeProcessRequest(request: String): String
        
        @JvmStatic
        fun nativeShutdown() {
            // Implementation will be called from native code
        }
    }

    private val state = mutableMapOf<String, Any>()
    
    /**
     * Returns human-readable descriptions of the agent's capabilities.
     *
     * The returned map's keys are capability identifiers ("ai_processing", "context_awareness", "error_handling")
     * and the values are short descriptions of each capability.
     *
     * @return Map of capability identifier to its short description.
     */
    fun getCapabilities(): Map<String, String> {
        return mapOf(
            "ai_processing" to "Basic AI request processing",
            "context_awareness" to "Basic context handling",
            "error_handling" to "Basic error handling"
        )
    }
    
    /**
     * Retrieves the continuous memory state of the agent.
     *
     * @return The current memory state as a map.
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
     * Processes an AI request and returns a response.
     *
     * @param request The AI request to process.
     * @param context Additional context for the request.
     * @return The agent's response.
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
     * Generates a direct response to an AI request, including the provided context.
     *
     * The response contains both the original query and the given context, with a fixed confidence score of 0.75.
     *
     * @param request The AI request to respond to.
     * @param context Additional context to include in the response.
     * @return An [AgentResponse] containing the combined query and context.
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
     * Retrieves memory based on the given request.
     *
     * @param request The AI request containing the memory query.
     * @return A flow emitting the memory retrieval results.
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
