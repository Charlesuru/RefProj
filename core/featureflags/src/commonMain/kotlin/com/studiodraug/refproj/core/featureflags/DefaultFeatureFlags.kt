package com.studiodraug.refproj.core.featureflags

import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.storage.KeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Resolves a flag in this order:
 *  1. debug override — only when [AppConfig.isDebugMenuEnabled] (never in a PROD release build)
 *  2. remote value ([RemoteFlagSource])
 *  3. default for the current environment ([FeatureFlag.enabledByDefaultIn])
 */
internal class DefaultFeatureFlags(
    private val config: AppConfig,
    private val store: KeyValueStore,
    private val remote: RemoteFlagSource,
) : FeatureFlags,
    FeatureFlagOverrides {
    override fun isEnabled(flag: FeatureFlag): Boolean = resolve(flag, readOverride(flag), remote.values.value).enabled

    override fun observe(flag: FeatureFlag): Flow<Boolean> =
        combine(observeOverride(flag), remote.values) { override, remoteValues -> resolve(flag, override, remoteValues).enabled }
            .distinctUntilChanged()

    override fun observeStates(): Flow<List<FeatureFlagState>> {
        val perFlag =
            FeatureFlag.entries.map { flag ->
                combine(observeOverride(flag), remote.values) { override, remoteValues -> resolve(flag, override, remoteValues) }
            }
        return combine(perFlag) { it.toList() }.distinctUntilChanged()
    }

    override fun setOverride(
        flag: FeatureFlag,
        enabled: Boolean?,
    ) {
        if (enabled == null) store.remove(overrideKey(flag)) else store.putString(overrideKey(flag), enabled.toString())
    }

    override fun clearOverrides() {
        store.keys().filter { it.startsWith(OVERRIDE_PREFIX) }.forEach(store::remove)
    }

    private fun resolve(
        flag: FeatureFlag,
        override: Boolean?,
        remoteValues: Map<String, Boolean>,
    ): FeatureFlagState {
        val remoteValue = remoteValues[flag.key]
        return when {
            override != null -> FeatureFlagState(flag, override, FeatureFlagState.Source.OVERRIDE)
            remoteValue != null -> FeatureFlagState(flag, remoteValue, FeatureFlagState.Source.REMOTE)
            else -> FeatureFlagState(flag, flag.isEnabledByDefault(config.environment), FeatureFlagState.Source.DEFAULT)
        }
    }

    private fun readOverride(flag: FeatureFlag): Boolean? =
        if (config.isDebugMenuEnabled) store.getString(overrideKey(flag))?.toBooleanStrictOrNull() else null

    private fun observeOverride(flag: FeatureFlag): Flow<Boolean?> =
        if (config.isDebugMenuEnabled) {
            store.observeString(overrideKey(flag)).map { it?.toBooleanStrictOrNull() }
        } else {
            flowOf(null)
        }

    private fun overrideKey(flag: FeatureFlag) = OVERRIDE_PREFIX + flag.key

    private companion object {
        const val OVERRIDE_PREFIX = "featureflag.override."
    }
}
