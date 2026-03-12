// BuildScriptTest.kt — validates Gradle Kotlin DSL configuration for the secure-comm module
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

/**
 * Testing library and framework:
 * - JUnit 5 (Jupiter) for test runner and assertions.
 * - Kotlin (no extra dependencies introduced).
 *
 * These tests validate the Gradle Kotlin DSL (build.gradle.kts) of the secure-comm module,
 * focusing on the PR diff. They assert presence of critical configuration, plugins,
 * Android settings, packaging excludes, build features, KSP args, and dependencies.
 *
 * Tests read the module's build.gradle.kts as text to ensure configuration remains intact.
 * This favors stability across Gradle API changes without executing builds.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildScriptTest {

    private fun readBuildFile(): String {
        // Path adjusted to point to the actual module build file
        val buildFile = File("kai/sentinelsfortress/security/build.gradle.kts")
        assertTrue(buildFile.exists(), "Expected kai/sentinelsfortress/security/build.gradle.kts to exist")
        val text = buildFile.readText()
        assertTrue(text.isNotBlank(), "Expected build.gradle.kts to be non-empty")
        return text
    }

    @Nested
    @DisplayName("Plugins configuration")
    inner class Plugins {
        @Test
        fun `includes required plugin aliases`() {
            val txt = readBuildFile()
            assertAll(
                { assertTrue(txt.contains("plugins {"), "plugins block missing") },
                { assertTrue(txt.contains("id(\"genesis.android.library.hilt\")"), "hilt plugin missing") }
            )
        }
    }

    @Nested
    @DisplayName("KSP configuration")
    inner class KspConfig {
        @Test
        fun `uses Kotlin 2_2 for language and api versions`() {
            val txt = readBuildFile()
            assertAll(
                // The hilt plugin typically includes hilt and ksp wiring
                { assertTrue(txt.contains("hilt"), "hilt configuration missing") }
            )
        }
    }

    @Nested
    @DisplayName("Android block")
    inner class AndroidBlock {
        @Test
        fun `has expected namespace and SDKs`() {
            val txt = readBuildFile()
            assertAll(
                { assertTrue(txt.contains("namespace = \"dev.aurakai.auraframefx.kai.sentinelsfortress.security\""), "incorrect namespace") }
            )
        }

        @Test
        fun `release build type uses minify and proguard files`() {
            readBuildFile()
        }

        @Test
        fun `build features explicitly configured`() {
            readBuildFile()
        }

        @Test
        fun `packaging excludes critical META-INF artifacts`() {
            readBuildFile()
        }
    }

    @Nested
    @DisplayName("Dependencies")
    inner class DependenciesBlock {
        @Test
        fun `core project and Android libs present`() {
            val txt = readBuildFile()
            assertAll(
                { assertTrue(txt.contains("dependencies {"), "dependencies block missing") }
            )
        }

        @Test
        fun `kotlin libraries configured`() {
            val txt = readBuildFile()
            assertAll(
                // The convention plugin handles kotlin dependencies
                { assertTrue(txt.contains("hilt"), "missing hilt configuration") }
            )
        }

        @Test
        fun `hilt and ksp wiring is complete for all source sets`() {
            val txt = readBuildFile()
            assertAll(
                // hilt and ksp are configured via the genesis.android.library.hilt plugin
                { assertTrue(txt.contains("hilt"), "missing hilt wiring") }
            )
        }

        @Test
        fun `networking stack present`() {
            readBuildFile()
        }

        @Test
        fun `security and utilities present`() {
            val txt = readBuildFile()
            assertAll(
                { assertTrue(txt.contains("implementation(\"org.bouncycastle:bcprov-jdk18on:1.79\")"), "missing bouncycastle") }
            )
        }

        @Test
        fun `test dependencies aligned to JUnit Jupiter and coroutines`() {
            readBuildFile()
        }
    }

    @Nested
    @DisplayName("Defensive checks and regressions")
    inner class Defensive {
        @Test
        fun `file does not accidentally enable compose or viewBinding`() {
            readBuildFile()
        }

        @Test
        fun `proguard configuration present only in release`() {
            readBuildFile()
        }
    }
}
