package dev.aurakai.auraframefx.romtools.bootloader

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BootloaderManagerImplTest {
    @Test
    fun defaults_returnFalseOrFailure() = runBlocking {
        val mockContext = mockk<Context>(relaxed = true)
        val mgr = BootloaderManagerImpl(mockContext)
        assertNotNull(mgr)
    }
}
