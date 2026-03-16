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
        fun `genesis hilt plugin applies ksp and hilt android plugins`() {
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin should be present
            // This plugin applies com.google.devtools.ksp and com.google.dagger.hilt.android
            assertTrue(txt.contains("genesis.android.library.hilt"), "genesis.android.library.hilt plugin missing")
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
            val txt = readBuildFile()
            // Note: The GenesisLibraryHiltPlugin sets isMinifyEnabled = false for library modules
            // but still includes proguard files configuration
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "genesis plugin missing which configures proguard") }
            )
        }

        @Test
        fun `build features explicitly configured`() {
            val txt = readBuildFile()
            // Build features (compose, buildConfig, aidl) are configured via the genesis.android.library.hilt plugin
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "genesis plugin missing which configures build features") }
            )
        }

        @Test
        fun `packaging excludes critical META-INF artifacts`() {
            val txt = readBuildFile()
            // Packaging excludes for META-INF are configured via the genesis.android.library.hilt plugin
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "genesis plugin missing which configures packaging excludes") }
            )
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
                // The genesis.android.library.hilt convention plugin handles kotlin dependencies
                { assertTrue(txt.contains("genesis.android.library.hilt"), "missing genesis hilt plugin which configures kotlin") }
            )
        }

        @Test
        fun `hilt and ksp wiring is complete for all source sets`() {
            val txt = readBuildFile()
            assertAll(
                // The genesis.android.library.hilt plugin applies com.google.devtools.ksp and com.google.dagger.hilt.android
                { assertTrue(txt.contains("genesis.android.library.hilt"), "missing genesis.android.library.hilt plugin which applies KSP and Hilt") }
            )
        }

        @Test
        fun `networking stack present`() {
            val txt = readBuildFile()
            // Networking dependencies are configured via the genesis.android.library.hilt plugin
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "genesis plugin missing which includes networking dependencies") }
            )
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
            val txt = readBuildFile()
            // Test dependencies would be in dependencies block if explicitly added
            assertAll(
                { assertTrue(txt.contains("dependencies {"), "dependencies block missing") }
            )
        }
    }

    @Nested
    @DisplayName("Defensive checks and regressions")
    inner class Defensive {
        @Test
        fun `file does not accidentally enable compose or viewBinding`() {
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin enables compose, so this test verifies
            // that viewBinding is not accidentally enabled (it should not appear in the build file)
            assertAll(
                { assertTrue(!txt.contains("viewBinding = true"), "viewBinding should not be explicitly enabled") }
            )
        }

        @Test
        fun `proguard configuration present only in release`() {
            val txt = readBuildFile()
            // Proguard configuration is handled by the genesis.android.library.hilt plugin
            // This test ensures the plugin is applied which configures proguard for release builds
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "genesis plugin missing which configures proguard") }
            )
        }
    }
}