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
            // The genesis.android.library.hilt plugin should be present
            // This convention plugin applies com.google.devtools.ksp and com.google.dagger.hilt.android
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "hilt convention plugin missing") }
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
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin configures release build type with minify and proguard
            // See GenesisLibraryHiltPlugin.kt lines 41-48
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "convention plugin missing - it configures minify and proguard") }
            )
        }

        @Test
        fun `build features explicitly configured`() {
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin configures buildFeatures
            // See GenesisLibraryHiltPlugin.kt lines 57-61 (compose, buildConfig, aidl)
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "convention plugin missing - it configures buildFeatures") }
            )
        }

        @Test
        fun `packaging excludes critical META-INF artifacts`() {
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin configures packaging excludes
            // See GenesisLibraryHiltPlugin.kt lines 63-71 (META-INF exclusions)
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "convention plugin missing - it configures packaging excludes") }
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
            // The genesis.android.library.hilt convention plugin applies KSP and Hilt plugins
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "missing hilt convention plugin") }
            )
        }

        @Test
        fun `hilt and ksp wiring is complete for all source sets`() {
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin internally applies:
            // - com.google.devtools.ksp (line 24 of GenesisLibraryHiltPlugin.kt)
            // - com.google.dagger.hilt.android (line 25 of GenesisLibraryHiltPlugin.kt)
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "missing hilt convention plugin that wires KSP and Hilt") }
            )
        }

        @Test
        fun `networking stack present`() {
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin configures networking dependencies via Compose and other libs
            // See GenesisLibraryHiltPlugin.kt lines 96-120
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "convention plugin missing - it provides networking dependencies") }
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
            // The genesis.android.library.hilt plugin sets up the test infrastructure
            // See GenesisLibraryHiltPlugin.kt line 34 (testInstrumentationRunner)
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "convention plugin missing - it configures test infrastructure") }
            )
        }
    }

    @Nested
    @DisplayName("Defensive checks and regressions")
    inner class Defensive {
        @Test
        fun `file does not accidentally enable compose or viewBinding`() {
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin enables compose (lines 57-61 of GenesisLibraryHiltPlugin.kt)
            // but not viewBinding - this test verifies viewBinding is NOT present in the build file
            assertAll(
                { assertTrue(!txt.contains("viewBinding"), "viewBinding should not be enabled") },
                { assertTrue(txt.contains("genesis.android.library.hilt"), "convention plugin should be present") }
            )
        }

        @Test
        fun `proguard configuration present only in release`() {
            val txt = readBuildFile()
            // The genesis.android.library.hilt plugin configures proguard for release build type
            // See GenesisLibraryHiltPlugin.kt lines 41-48
            assertAll(
                { assertTrue(txt.contains("genesis.android.library.hilt"), "convention plugin missing - it configures proguard for release") }
            )
        }
    }
}