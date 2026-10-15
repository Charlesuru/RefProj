package com.studiodraug.refproj.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base class for screen ViewModels (unidirectional data flow, `CLAUDE.md`):
 *  - one immutable [State] exposed as [state],
 *  - user intent comes in through [onAction],
 *  - one-off [Event]s (navigate, show a snackbar) go out through [events], each delivered once.
 */
abstract class MviViewModel<State, Action, Event>(
    initialState: State,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<State> = mutableState.asStateFlow()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    abstract fun onAction(action: Action)

    protected val currentState: State get() = mutableState.value

    protected fun updateState(reduce: State.() -> State) = mutableState.update(reduce)

    protected fun sendEvent(event: Event) {
        viewModelScope.launch { eventChannel.send(event) }
    }
}
