package dev.aurakai.colorblendr

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ChromaCoreEnhancedTest {

    @Test
    @DisplayName("calculateContrast: should return 21.0 for black and white")
    fun testCalculateContrastBlackAndWhite() {
        val white = Color.White
        val black = Color.Black

        val contrast = ChromaCoreEnhanced.calculateContrast(white, black)
        assertEquals(21.0f, contrast, 0.01f)
    }

    @Test
    @DisplayName("calculateContrast: should return 1.0 for same colors")
    fun testCalculateContrastSameColors() {
        val color = Color(0xFF123456)

        val contrast = ChromaCoreEnhanced.calculateContrast(color, color)
        assertEquals(1.0f, contrast, 0.01f)
    }

    @Test
    @DisplayName("calculateContrast: should return approx 4.5 for #767676 on white")
    fun testCalculateContrastKnownRatio() {
        val white = Color.White
        val gray = Color(0xFF767676)

        val contrast = ChromaCoreEnhanced.calculateContrast(gray, white)
        // #767676 on white is approx 4.54:1
        assertTrue(contrast >= 4.5f)
        assertEquals(4.54f, contrast, 0.01f)
    }

    @Test
    @DisplayName("meetsWCAG_AA: should correctly identify passing and failing contrasts")
    fun testMeetsWCAG_AA() {
        val white = Color.White
        val grayPass = Color(0xFF767676) // ~4.54
        val grayFail = Color(0xFF777777) // ~4.47

        assertTrue(ChromaCoreEnhanced.meetsWCAG_AA(Color.White, Color.Black), "White on black should pass AA")
        assertTrue(ChromaCoreEnhanced.meetsWCAG_AA(grayPass, white), "#767676 on white should pass AA")
        assertFalse(ChromaCoreEnhanced.meetsWCAG_AA(grayFail, white), "#777777 on white should fail AA")
    }

    @Test
    @DisplayName("meetsWCAG_AAA: should correctly identify passing and failing contrasts")
    fun testMeetsWCAG_AAA() {
        val white = Color.White
        val grayPass = Color(0xFF595959) // ~6.96 -> actually it might be 7.00 depending on rounding
        val grayFail = Color(0xFF5A5A5A) // ~6.84

        assertTrue(ChromaCoreEnhanced.meetsWCAG_AAA(Color.White, Color.Black), "White on black should pass AAA")

        // Let's use more definitive values
        val aaaPass = Color(0xFF444444) // Contrast ~9.42
        val aaaFail = Color(0xFF888888) // Contrast ~3.54

        assertTrue(ChromaCoreEnhanced.meetsWCAG_AAA(aaaPass, Color.White), "Dark gray on white should pass AAA")
        assertFalse(ChromaCoreEnhanced.meetsWCAG_AAA(aaaFail, Color.White), "Light gray on white should fail AAA")
    }

    @Test
    @DisplayName("ensureContrast: should return original color if contrast is sufficient")
    fun testEnsureContrastSufficient() {
        val textColor = Color.Black
        val backgroundColor = Color.White

        val result = ChromaCoreEnhanced.ensureContrast(textColor, backgroundColor, 4.5f)
        assertEquals(textColor, result)
    }

    @Test
    @DisplayName("ensureContrast: should return black text for light backgrounds if contrast is insufficient")
    fun testEnsureContrastInsufficientLightBg() {
        val textColor = Color(0xFFEEEEEE) // Very light gray
        val backgroundColor = Color.White

        val result = ChromaCoreEnhanced.ensureContrast(textColor, backgroundColor, 4.5f)
        assertEquals(Color.Black, result)
    }

    @Test
    @DisplayName("ensureContrast: should return white text for dark backgrounds if contrast is insufficient")
    fun testEnsureContrastInsufficientDarkBg() {
        val textColor = Color(0xFF111111) // Very dark gray
        val backgroundColor = Color.Black

        val result = ChromaCoreEnhanced.ensureContrast(textColor, backgroundColor, 4.5f)
        assertEquals(Color.White, result)
    }
}
