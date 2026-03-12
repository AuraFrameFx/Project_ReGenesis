package dev.aurakai.auraframefx.romtools

import org.junit.Assert.*
import org.junit.Test

class RomOperationExtensionsTest {
    @Test
    fun testLabels() {
        assertEquals("Flash ROM", RomOperation.FlashRom.getDisplayName())
    }
}
