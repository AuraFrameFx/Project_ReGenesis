package dev.aurakai.auraframefx.domains.aura.screens.uxui_engine

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import dev.aurakai.auraframefx.domains.aura.chromacore.iconify.iconify.IconPicker
import dev.aurakai.auraframefx.domains.aura.chromacore.iconify.iconify.IconPickerViewModel

/**
 * 🎨 ICONIFY PICKER SCREEN WRAPPER
 * Wraps the full-featured IconPicker component
 * Integrates with Dr. Disagree's Iconify root app
 */
/**
 * Displays an icon picker that is bound to the ViewModel's Iconify service and dismisses via the provided callback.
 *
 * The picker currently does not persist or apply selected icons (the `onIconSelected` handler is a placeholder).
 *
 * @param viewModel The ViewModel that provides the `iconifyService`. By default this is obtained via Hilt using
 *   the current `LocalViewModelStoreOwner`; if `LocalViewModelStoreOwner.current` is null a runtime error is thrown
 *   with the message "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner".
 * @param onNavigateBack Callback invoked when the picker is dismissed.
 */
@Composable
fun IconifyPickerScreen(
    viewModel: IconPickerViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }, null
    ),
    onNavigateBack: () -> Unit = {}
) {
    IconPicker(
        iconifyService = viewModel.iconifyService,
        currentIcon = null,
        onIconSelected = { iconId ->
            // TODO: Handle icon selection
            // component.icon = iconId
        },
        onDismiss = onNavigateBack
    )
}
