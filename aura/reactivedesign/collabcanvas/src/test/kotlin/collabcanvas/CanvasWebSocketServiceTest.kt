package collabcanvas

import collabcanvas.model.ColorTypeAdapter
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okio.ByteString.Companion.decodeHex
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CanvasWebSocketServiceTest {

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var gson: Gson
    private lateinit var service: CanvasWebSocketService
    private lateinit var mockWebSocket: WebSocket
    private lateinit var capturedListener: WebSocketListener

    @Before
    fun setup() {
        okHttpClient = mockk(relaxed = true)
        gson = GsonBuilder()
            .registerTypeAdapter(CanvasWebSocketMessage::class.java, CanvasWebSocketMessageAdapter())
            .registerTypeAdapter(Color::class.java, ColorTypeAdapter())
            .create()
        service = CanvasWebSocketService(okHttpClient, gson)
        mockWebSocket = mockk(relaxed = true)

        val listenerSlot = slot<WebSocketListener>()
        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns mockWebSocket

        service.connect("ws://test")
        capturedListener = listenerSlot.captured
    }

    @Test
    fun `onOpen emits Connected event`() = runTest {
        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            service.events.collect { events.add(it) }
        }

        capturedListener.onOpen(mockWebSocket, mockk(relaxed = true))

        assertTrue("Expected Connected event in $events", events.contains(CanvasWebSocketEvent.Connected))
        job.cancel()
    }

    @Test
    fun `onMessage with valid JSON emits MessageReceived`() = runTest {
        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            service.events.collect { events.add(it) }
        }

        val json = """{"type":"ELEMENT_ADDED","canvasId":"c1","userId":"u1","timestamp":123456789,"element":{"id":"e1","type":"PATH","path":{"points":[],"isComplete":false},"color":-16777216,"strokeWidth":5.0,"zIndex":0,"isSelected":false,"createdBy":"u1","createdAt":123456789,"updatedAt":123456789}}"""

        capturedListener.onMessage(mockWebSocket, json)

        val event = events.filterIsInstance<CanvasWebSocketEvent.MessageReceived>().firstOrNull()
        assertNotNull("Expected MessageReceived event, but got $events", event)
        assertTrue(event?.message is ElementAddedMessage)
        assertEquals("c1", event?.message?.canvasId)

        job.cancel()
    }

    @Test
    fun `onMessage with invalid JSON emits Error`() = runTest {
        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            service.events.collect { events.add(it) }
        }

        capturedListener.onMessage(mockWebSocket, "invalid json")

        val event = events.filterIsInstance<CanvasWebSocketEvent.Error>().firstOrNull()
        assertNotNull("Expected Error event, but got $events", event)
        assertTrue(event?.message?.contains("Error parsing message") == true)

        job.cancel()
    }

    @Test
    fun `onMessage with binary data emits BinaryMessageReceived`() = runTest {
        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            service.events.collect { events.add(it) }
        }

        val bytes = "deadbeef".decodeHex()
        capturedListener.onMessage(mockWebSocket, bytes)

        val event = events.filterIsInstance<CanvasWebSocketEvent.BinaryMessageReceived>().firstOrNull()
        assertNotNull("Expected BinaryMessageReceived event, but got $events", event)
        assertEquals(bytes, event?.bytes)

        job.cancel()
    }

    @Test
    fun `onClosing emits Closing event`() = runTest {
        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            service.events.collect { events.add(it) }
        }

        capturedListener.onClosing(mockWebSocket, 1000, "Normal closure")

        val event = events.filterIsInstance<CanvasWebSocketEvent.Closing>().firstOrNull()
        assertNotNull("Expected Closing event, but got $events", event)
        assertEquals(1000, event?.code)
        assertEquals("Normal closure", event?.reason)

        job.cancel()
    }

    @Test
    fun `onClosed emits Disconnected event`() = runTest {
        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            service.events.collect { events.add(it) }
        }

        capturedListener.onClosed(mockWebSocket, 1000, "Normal closure")

        assertTrue("Expected Disconnected event, but got $events", events.contains(CanvasWebSocketEvent.Disconnected))
        job.cancel()
    }

    @Test
    fun `onFailure emits Error event`() = runTest {
        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher()) {
            service.events.collect { events.add(it) }
        }

        capturedListener.onFailure(mockWebSocket, Exception("Socket error"), null)

        val event = events.filterIsInstance<CanvasWebSocketEvent.Error>().firstOrNull()
        assertNotNull("Expected Error event, but got $events", event)
        assertEquals("Socket error", event?.message)

        job.cancel()
    }

    @Test
    fun `sendMessage returns true on success`() {
        val message = ElementRemovedMessage("c1", "u1", elementId = "e1")
        every { mockWebSocket.send(any<String>()) } returns true

        val result = service.sendMessage(message)

        assertTrue(result)
        verify { mockWebSocket.send(any<String>()) }
    }

    @Test
    fun `sendMessage returns false when not connected`() {
        val serviceNotConnected = CanvasWebSocketService(okHttpClient, gson)
        val message = ElementRemovedMessage("c1", "u1", elementId = "e1")

        val result = serviceNotConnected.sendMessage(message)

        assertFalse(result)
    }

    @Test
    fun `disconnect closes websocket and clears reference`() {
        service.disconnect()

        verify { mockWebSocket.close(1000, "User initiated disconnect") }
        assertFalse(service.isConnected())
    }
}
