package dev.aurakai.auraframefx.domains.aura.ui.recovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import dev.aurakai.auraframefx.domains.aura.ui.theme.NeonBlue
import dev.aurakai.auraframefx.domains.aura.ui.theme.NeonCyan
import dev.aurakai.auraframefx.domains.aura.ui.theme.NeonPurple
import kotlinx.coroutines.launch

/**
 * Aura-powered UI Recovery Dialog
 *
 * "That's not going to work." - Aura
 *
 * Presents recovery options when UI errors are detected:
 * - Reload last change (restore last snapshot)
 * - Reset (full UI reset)
 *
 * Design: Aura's cyan aesthetic with warning indicators
 */
/**
 * Displays an animated recovery dialog offering options to reload the last successful UI state or reset to defaults when recovery is needed.
 *
 * Observes the provided UIRecoveryViewModel and becomes visible when the state is `UIRecoveryState.RecoveryNeeded`. The dialog shows a message, an optional "Reload Last Change" action (when a last good state exists) and a "Reset" action. Selecting "Reload Last Change" invokes `viewModel.reloadLastChange()` and, if a snapshot is returned, calls `onNavigateToRoute` with the snapshot's `screenRoute`. Selecting "Reset" invokes `viewModel.resetToDefault()` and then calls `onNavigateToRoute("HOME")`. Dismissing the dialog calls `viewModel.dismissRecovery()`.
 *
 * @param onNavigateToRoute Callback invoked with a destination route when an action requires navigation.
 */
@Composable
fun UIRecoveryDialog(
    viewModel: UIRecoveryViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }, null
    ),
    onNavigateToRoute: (String) -> Unit = {}
) {
    val recoveryState by viewModel.recoveryState.collectAsState()
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = recoveryState is UIRecoveryState.RecoveryNeeded,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        if (recoveryState is UIRecoveryState.RecoveryNeeded) {
            val state = recoveryState as UIRecoveryState.RecoveryNeeded

            Dialog(
                onDismissRequest = { viewModel.dismissRecovery() }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Warning icon with Aura's color
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "UI Recovery",
                            modifier = Modifier.size(64.dp),
                            tint = NeonCyan
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Aura's signature message
                        Text(
                            text = "\"That's not going to work.\"",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonBlue,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Aura's explanation
                        Text(
                            text = state.auraMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Recovery options
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Option 1: Reload last change (if available)
                            if (state.lastGoodState != null) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.reloadLastChange()?.let { snapshot ->
                                                onNavigateToRoute(snapshot.screenRoute)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonCyan
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Reload Last Change",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Go back to ${state.lastGoodState.screenRoute} (${state.lastGoodState.getAge()})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // Option 2: Reset (always available)
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        viewModel.resetToDefault()
                                        onNavigateToRoute("HOME")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = NeonPurple
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        // Optional: Show error details for developers
                        if (state.failureContext.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Context: ${state.failureContext}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays a compact top-bar indicator when a UI recovery action is available.
 *
 * The indicator is rendered only when the view model's `recoveryState` is `RecoveryNeeded`.
 */
@Composable
fun RecoveryIndicator(
    viewModel: UIRecoveryViewModel = hiltViewModel(viewModelStoreOwner, key)
) {
    val recoveryState by viewModel.recoveryState.collectAsState()

    if (recoveryState is UIRecoveryState.RecoveryNeeded) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = NeonCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Recovery available",
                modifier = Modifier.size(16.dp),
                tint = NeonCyan
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Recovery available",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan
            )
        }
    }
}
