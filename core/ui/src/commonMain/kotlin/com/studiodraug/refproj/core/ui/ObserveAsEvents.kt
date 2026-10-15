package com.studiodraug.refproj.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects one-off events while the screen is at least STARTED. `Main.immediate` makes sure an
 * event is handled in the same frame it is received and never lost during a configuration change.
 */
@Composable
fun <T> ObserveAsEvents(
    events: Flow<T>,
    onEvent: (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEvent = rememberUpdatedState(onEvent)
    LaunchedEffect(events, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                events.collect { currentOnEvent.value(it) }
            }
        }
    }
}
