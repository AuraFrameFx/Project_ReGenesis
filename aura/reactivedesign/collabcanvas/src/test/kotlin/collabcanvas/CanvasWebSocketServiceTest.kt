package collabcanvas

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CanvasWebSocketServiceTest {

    private val okHttpClient: OkHttpClient = mockk()
    private val gson: Gson = mockk()
    private val service = CanvasWebSocketService(okHttpClient, gson)

    @Test
    fun `onOpen emits Connected event`() = runTest {
        val listenerSlot = slot<WebSocketListener>()
        val webSocket: WebSocket = mockk()
        val response: Response = mockk()

        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket

        service.connect("ws://test.com")

        val listener = listenerSlot.captured

        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            service.events.collect { events.add(it) }
        }

        // Trigger onOpen
        listener.onOpen(webSocket, response)

        // Verify event emitted
        assertEquals(1, events.size)
        assertEquals(CanvasWebSocketEvent.Connected, events[0])

        job.cancel()
    }

    @Test
    fun `onClosed emits Disconnected event`() = runTest {
        val listenerSlot = slot<WebSocketListener>()
        val webSocket: WebSocket = mockk()

        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket

        service.connect("ws://test.com")

        val listener = listenerSlot.captured

        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            service.events.collect { events.add(it) }
        }

        // Trigger onClosed
        listener.onClosed(webSocket, 1000, "Normal closure")

        // Verify event emitted
        assertEquals(1, events.size)
        assertEquals(CanvasWebSocketEvent.Disconnected, events[0])

        job.cancel()
    }

    @Test
    fun `onFailure emits Error event`() = runTest {
        val listenerSlot = slot<WebSocketListener>()
        val webSocket: WebSocket = mockk()
        val throwable = RuntimeException("Connection failed")

        every { okHttpClient.newWebSocket(any(), capture(listenerSlot)) } returns webSocket

        service.connect("ws://test.com")

        val listener = listenerSlot.captured

        val events = mutableListOf<CanvasWebSocketEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            service.events.collect { events.add(it) }
        }

        // Trigger onFailure
        listener.onFailure(webSocket, throwable, null)

        // Verify event emitted
        assertEquals(1, events.size)
        val event = events[0] as CanvasWebSocketEvent.Error
        assertEquals("Connection failed", event.message)

        job.cancel()
    }
}
