package com.studiodraug.refproj.core.featureflags

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Flag values from a remote service, keyed by [FeatureFlag.key]. A missing key means "use the
 * default". Firebase Remote Config plugs in here without touching any caller.
 */
interface RemoteFlagSource {
    val values: StateFlow<Map<String, Boolean>>

    /** Fetches and activates the latest values; failures keep the last known values. */
    suspend fun refresh()
}

/** No remote service configured: every flag uses its default (or debug override). */
class NoRemoteFlagSource : RemoteFlagSource {
    private val empty = MutableStateFlow(emptyMap<String, Boolean>())
    override val values: StateFlow<Map<String, Boolean>> = empty.asStateFlow()

    override suspend fun refresh() = Unit
}
