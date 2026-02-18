package dev.aurakai.auraframefx.domains.aura.aura.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import collabcanvas.ui.CanvasViewModel

/**
 * Wrapper for Collaborative Canvas with WebSocket support
 *
 * This delegates to the real collabcanvas.ui.CanvasScreen with WebSocket integration
 */
/**
 * Displays a collaborative canvas screen and manages its WebSocket lifecycle.
 *
 * When composed, this screen establishes the ViewModel's WebSocket connection and delegates UI rendering to the shared collabcanvas CanvasScreen configured for collaborative use.
 *
 * @param modifier UI modifier applied to the canvas screen.
 * @param onNavigateBack Callback invoked when the user requests navigation back.
 * @param viewModel The CanvasViewModel used to manage collaboration state and WebSocket connection. By default, a Hilt-provided ViewModel is obtained from the current LocalViewModelStoreOwner; a runtime exception is thrown if no ViewModelStoreOwner is available.
 */
@Composable
fun CanvasScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: CanvasViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }, null
    )
) {
    // Connect to WebSocket when screen opens
    LaunchedEffect(Unit) {
        viewModel.connect("genesis-canvas-session")
    }

    // Use the real collaborative canvas from the collabcanvas module
    collabcanvas.ui.CanvasScreen(
        modifier = modifier,
        onBack = onNavigateBack,
        isCollaborative = true,
        collaborationEvents = null // TODO: Wire to viewModel.webSocketEvents
    )
}