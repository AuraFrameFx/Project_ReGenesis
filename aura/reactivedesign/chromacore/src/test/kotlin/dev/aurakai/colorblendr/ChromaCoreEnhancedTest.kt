package dev.aurakai.colorblendr

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChromaCoreEnhancedTest {

    private val chromaCore = ChromaCoreEnhanced

    @Test
    fun `test calculateContrast with extreme colors`() {
        val white = Color.White
        val black = Color.Black

        val contrast = chromaCore.calculateContrast(white, black)

        // White (1.0) and Black (0.0) -> (1.0 + 0.05) / (0.0 + 0.05) = 21.0
        assertEquals(21.0f, contrast, 0.01f)
    }

    @Test
    fun `test calculateContrast with same color`() {
        val white = Color.White
        val contrast = chromaCore.calculateContrast(white, white)

        assertEquals(1.0f, contrast, 0.01f)
    }

    @Test
    fun `test meetsWCAG_AA with various colors`() {
        // High contrast: White on Black (21:1) -> Should pass (>= 4.5)
        assertTrue(chromaCore.meetsWCAG_AA(Color.White, Color.Black))

        // Medium contrast: #757575 on White (~4.6:1) -> Should pass (>= 4.5)
        val gray = Color(0xFF757575)
        assertTrue(chromaCore.meetsWCAG_AA(gray, Color.White))

        // Low contrast: Lime on White (~1.37:1) -> Should fail (< 4.5)
        val lime = Color(0xFF00FF00)
        assertFalse(chromaCore.meetsWCAG_AA(lime, Color.White))

        // Same color -> Should fail (< 4.5)
        assertFalse(chromaCore.meetsWCAG_AA(Color.White, Color.White))
    }

    @Test
    fun `test meetsWCAG_AAA with various colors`() {
        // High contrast: White on Black (21:1) -> Should pass (>= 7.0)
        assertTrue(chromaCore.meetsWCAG_AAA(Color.White, Color.Black))

        // Medium contrast: #757575 on White (~4.6:1) -> Should fail (< 7.0)
        val gray = Color(0xFF757575)
        assertFalse(chromaCore.meetsWCAG_AAA(gray, Color.White))

        // Low contrast: Lime on White (~1.37:1) -> Should fail (< 7.0)
        val lime = Color(0xFF00FF00)
        assertFalse(chromaCore.meetsWCAG_AAA(lime, Color.White))

        // Same color -> Should fail (< 7.0)
        assertFalse(chromaCore.meetsWCAG_AAA(Color.White, Color.White))
    }
}
