package dev.aurakai.auraframefx.romtools

import android.content.Context
import android.net.Uri
import dev.aurakai.auraframefx.domains.genesis.models.AgentResponse
import dev.aurakai.auraframefx.domains.genesis.models.AgentType
import dev.aurakai.auraframefx.romtools.bootloader.BootloaderManager
import dev.aurakai.auraframefx.romtools.bootloader.BootloaderSafetyManager
import dev.aurakai.auraframefx.romtools.retention.AurakaiRetentionManager
import dev.aurakai.auraframefx.romtools.retention.RetentionMechanism
import dev.aurakai.auraframefx.romtools.retention.RetentionStatus
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class RomToolsManagerTest {

    private lateinit var romToolsManager: RomToolsManagerImpl
    private lateinit var mockBootloaderManager: BootloaderManager
    private lateinit var mockRecoveryManager: RecoveryManager
    private lateinit var mockSystemModificationManager: SystemModificationManager
    private lateinit var mockFlashManager: FlashManager
    private lateinit var mockVerificationManager: RomVerificationManager
    private lateinit var mockBackupManager: BackupManager
    private lateinit var mockRetentionManager: AurakaiRetentionManager
    private lateinit var mockBootloaderSafetyManager: BootloaderSafetyManager

    @Before
    fun setup() {
        mockBootloaderManager = mockk(relaxed = true)
        mockRecoveryManager = mockk(relaxed = true)
        mockSystemModificationManager = mockk(relaxed = true)
        mockFlashManager = mockk(relaxed = true)
        mockVerificationManager = mockk(relaxed = true)
        mockBackupManager = mockk(relaxed = true)
        mockRetentionManager = mockk(relaxed = true)
        mockBootloaderSafetyManager = mockk(relaxed = true)
        
        val mockContext = mockk<Context>(relaxed = true)
        romToolsManager = RomToolsManagerImpl(
            context = mockContext,
            bootloaderManager = mockBootloaderManager,
            bootloaderSafetyManager = mockBootloaderSafetyManager,
            recoveryManager = mockRecoveryManager,
            flashManager = mockFlashManager,
            backupManager = mockBackupManager,
            systemModificationManager = mockSystemModificationManager,
            romVerificationManager = mockVerificationManager,
            retentionManager = mockRetentionManager
        )
    }

    @Test
    fun shouldRouteFlashRomOperation() = runTest {
        val mockUri = mockk<Uri>(relaxed = true)
        val request = RomOperationRequest(
            operation = RomOperation.FlashRom,
            uri = mockUri,
            context = mockk(relaxed = true)
        )

        val response = romToolsManager.processRomOperation(request)
        assertNotNull(response)
    }

    @Test
    fun shouldRouteCreateBackupOperation() = runTest {
        val request = RomOperationRequest(
            operation = RomOperation.CreateBackup,
            context = mockk(relaxed = true)
        )
        coEvery { mockBackupManager.createNandroidBackup(any(), any()) } returns Result.success(mockk(relaxed = true))

        val response = romToolsManager.processRomOperation(request)
        assertTrue(response.isSuccess)
    }
}
