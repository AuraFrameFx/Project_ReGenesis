package dev.aurakai.colorblendr

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ChromaCoreEnhancedTest {

    @Test
    @DisplayName("calculateContrast should return 21.0 for black and white")
    fun testContrastBlackAndWhite() {
        val white = Color.White
        val black = Color.Black

        val contrast = ChromaCoreEnhanced.calculateContrast(white, black)
        assertEquals(21.0f, contrast, 0.01f)
    }

    @Test
    @DisplayName("calculateContrast should return 1.0 for same colors")
    fun testContrastSameColor() {
        val color = Color(0xFF123456)
        val contrast = ChromaCoreEnhanced.calculateContrast(color, color)
        assertEquals(1.0f, contrast, 0.01f)
    }

    @Test
    @DisplayName("meetsWCAG_AA should correctly identify sufficient contrast")
    fun testMeetsWCAG_AA() {
        // White on Black is 21.0 (> 4.5)
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AA(Color.White, Color.Black))

        // Mid gray (approx 0.214 luminance) on White (1.0 luminance)
        // Ratio: (1.0 + 0.05) / (0.214 + 0.05) = 1.05 / 0.264 = 3.97 (< 4.5)
        val gray = Color(0xFF808080)
        assertFalse(ChromaCoreEnhanced.meetsWCAG_AA(gray, Color.White))
    }

    @Test
    @DisplayName("meetsWCAG_AAA should correctly identify sufficient contrast")
    fun testMeetsWCAG_AAA() {
        // White on Black is 21.0 (> 7.0)
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AAA(Color.White, Color.Black))

        // Pure Red (L approx 0.2126) on Black (L=0)
        // Ratio: (0.2126 + 0.05) / (0.0 + 0.05) = 0.2626 / 0.05 = 5.25 (> 4.5, but < 7.0)
        val red = Color.Red
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AA(red, Color.Black))
        assertFalse(ChromaCoreEnhanced.meetsWCAG_AAA(red, Color.Black))
    }

    @Test
    @DisplayName("ensureContrast should return original color if contrast is enough")
    fun testEnsureContrastSufficient() {
        val textColor = Color.Black
        val bgColor = Color.White
        val result = ChromaCoreEnhanced.ensureContrast(textColor, bgColor)
        assertEquals(textColor, result)
    }

    @Test
    @DisplayName("ensureContrast should return alternative if contrast is insufficient")
    fun testEnsureContrastInsufficient() {
        val textColor = Color(0xFFEEEEEE) // Very light gray
        val bgColor = Color.White

        // Should return Black for white background when contrast is insufficient
        val result = ChromaCoreEnhanced.ensureContrast(textColor, bgColor)
        assertEquals(Color.Black, result)

        val darkBg = Color.Black
        // Very dark gray on black
        val resultDark = ChromaCoreEnhanced.ensureContrast(Color(0xFF111111), darkBg)
        assertEquals(Color.White, resultDark)
    }

    @Test
    @DisplayName("Nexus palette contrast check")
    fun testNexusPaletteContrast() {
        val nexus = ChromaCoreEnhanced.DomainPalettes.Nexus
        // Nexus primary/onPrimary only meets AA (approx 5.7:1), not AAA (7:1)
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AA(nexus.onPrimary, nexus.primary))
        assertFalse(ChromaCoreEnhanced.meetsWCAG_AAA(nexus.onPrimary, nexus.primary))

        assertTrue(ChromaCoreEnhanced.meetsWCAG_AAA(nexus.onSecondary, nexus.secondary))
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AAA(nexus.onBackground, nexus.background))
    }

    @Test
    @DisplayName("Kai palette should meet claimed AAA contrast")
    fun testKaiPaletteContrast() {
        val kai = ChromaCoreEnhanced.DomainPalettes.Kai
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AAA(kai.onPrimary, kai.primary))
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AAA(kai.onSecondary, kai.secondary))
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AAA(kai.onBackground, kai.background))
    }
}
