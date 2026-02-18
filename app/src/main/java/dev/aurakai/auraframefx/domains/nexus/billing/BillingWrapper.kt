package dev.aurakai.auraframefx.domains.nexus.billing

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import dev.aurakai.auraframefx.domains.genesis.config.FeatureToggles
import dev.aurakai.auraframefx.domains.cascade.utils.debug

/**
 * App-level billing wrapper
 *
 * Wraps entire app to enforce subscription rules:`
 * - Shows paywall when trial expires
 * - Manages feature access throughout app
 */
/**
 * Wraps app UI with subscription enforcement and paywall presentation.
 *
 * Refreshes subscription status on first composition, always renders `content`, and overlays
 * a `PaywallDialog` when the current subscription is `SubscriptionState.Free` and the
 * paywall feature toggle is enabled.
 *
 * @param viewModel The `SubscriptionViewModel` used to observe and control subscription state.
 *                  By default this is obtained via Hilt using the current `LocalViewModelStoreOwner`;
 *                  composition will fail with a clear message if no `ViewModelStoreOwner` is provided.
 * @param content Composable content to render inside the billing wrapper.
 */
@Composable
fun BillingWrapper(
    viewModel: SubscriptionViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }, null
    ),
    content: @Composable () -> Unit
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()

    // Refresh subscription status on app start
    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    // Show app content
    content()

    // Overlay paywall when trial expires
    if (subscriptionState is SubscriptionState.Free && FeatureToggles.isPaywallEnabled) {
        PaywallDialog(viewModel = viewModel)
    }
}