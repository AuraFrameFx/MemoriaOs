package dev.aurakai.collabcanvas.network

import com.google.gson.Gson
import dev.aurakai.collabcanvas.model.CanvasElement
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanvasWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {
    // Removed TAG property
    private var webSocket: WebSocket? = null
    private val _events = MutableSharedFlow<CanvasWebSocketEvent>()
    val events: SharedFlow<CanvasWebSocketEvent> = _events.asSharedFlow()

    private val webSocketListener = object : WebSocketListener() {
        /**
         * Called when the WebSocket connection is successfully opened.
         *
         * Emits a CanvasWebSocketEvent.Connected to the service's event stream to notify consumers that the socket is ready.
         *
         * @param webSocket The opened WebSocket instance.
         * @param response The HTTP handshake response for the opened connection.
         */
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connection opened")
            _events.tryEmit(CanvasWebSocketEvent.Connected)
        }

        /**
         * Handles an incoming text WebSocket message.
         *
         * Parses the incoming JSON payload into a CanvasWebSocketMessage and emits a
         * CanvasWebSocketEvent.MessageReceived on success. If parsing fails, emits
         * CanvasWebSocketEvent.Error with the parse error message.
         *
         * @param webSocket The source WebSocket that delivered the message.
         * @param text The raw text payload received from the socket (expected to be JSON).
         */
        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("Message received: $text") // Changed to Timber
            try {
                val message = gson.fromJson(text, CanvasWebSocketMessage::class.java)
                _events.tryEmit(CanvasWebSocketEvent.MessageReceived(message))
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "Error parsing WebSocket message"
                ) // Changed to Timber, added exception first for stack trace
                _events.tryEmit(CanvasWebSocketEvent.Error("Error parsing message: ${e.message}"))
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Timber.d("Binary message received") // Changed to Timber
            _events.tryEmit(CanvasWebSocketEvent.BinaryMessageReceived(bytes))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closing: $code / $reason") // Changed to Timber
            _events.tryEmit(CanvasWebSocketEvent.Closing(code, reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closed: $code / $reason") // Changed to Timber
            _events.tryEmit(CanvasWebSocketEvent.Disconnected)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket error") // Changed to Timber
            _events.tryEmit(CanvasWebSocketEvent.Error(t.message ?: "Unknown error"))
        }
    }

    /**
     * Opens a WebSocket connection to the given URL and stores the active socket.
     *
     * If a connection already exists this function does nothing. The connection attempt is
     * performed asynchronously; successful or failed lifecycle events are emitted via the
     * service's event flow.
     *
     * @param url The WebSocket endpoint to connect to (e.g. `ws://...` or `wss://...`).
     */
    fun connect(url: String) {
        if (webSocket != null) {
            Timber.w("WebSocket already connected") // Changed to Timber
            return
        }

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
    }

    /**
     * Closes the active WebSocket connection (normal closure) and clears the stored reference.
     *
     * If a WebSocket is present it is closed with code 1000 ("User initiated disconnect") and the
     * internal `webSocket` reference is set to null. No action is taken if there is no active socket.
     */
    fun disconnect() {
        webSocket?.close(1000, "User initiated disconnect")
        webSocket = null
    }

    /**
     * Serializes the given CanvasWebSocketMessage to JSON and sends it over the active WebSocket.
     *
     * @param message The message to serialize and send.
     * @return true if the JSON was successfully handed to the WebSocket for sending; false if there is no active connection or an error occurred during serialization/sending.
     */
    fun sendMessage(message: CanvasWebSocketMessage): Boolean {
        return try {
            val json = gson.toJson(message)
            webSocket?.send(json) ?: run {
                Timber.e("WebSocket is not connected") // Changed to Timber
                false
            }
        } catch (e: Exception) {
            Timber.e(
                e,
                "Error sending WebSocket message"
            ) // Changed to Timber, added exception first for stack trace
            false
        }
    }

    fun isConnected(): Boolean {
        return webSocket != null
    }
}

sealed class CanvasWebSocketEvent {
    object Connected : CanvasWebSocketEvent()
    object Disconnected : CanvasWebSocketEvent()
    data class MessageReceived(val message: CanvasWebSocketMessage) : CanvasWebSocketEvent()
    data class BinaryMessageReceived(val bytes: ByteString) : CanvasWebSocketEvent()
    data class Error(val message: String) : CanvasWebSocketEvent()
    data class Closing(val code: Int, val reason: String) : CanvasWebSocketEvent()
}

sealed class CanvasWebSocketMessage {
    abstract val type: String
    abstract val canvasId: String
    abstract val userId: String
    abstract val timestamp: Long
}

data class ElementAddedMessage(
    override val canvasId: String,
    override val userId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val element: CanvasElement,
) : CanvasWebSocketMessage() {
    override val type: String = "ELEMENT_ADDED"
}

data class ElementUpdatedMessage(
    override val canvasId: String,
    override val userId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val elementId: String,
    val updates: Map<String, Any>,
) : CanvasWebSocketMessage() {
    override val type: String = "ELEMENT_UPDATED"
}

data class ElementRemovedMessage(
    override val canvasId: String,
    override val userId: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val elementId: String,
) : CanvasWebSocketMessage() {
    override val type: String = "ELEMENT_REMOVED"
}
