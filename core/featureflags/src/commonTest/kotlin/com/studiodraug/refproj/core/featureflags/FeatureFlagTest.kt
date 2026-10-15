package com.studiodraug.refproj.core.featureflags

import com.studiodraug.refproj.core.config.AppEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// UNIT TEST

/** Guard rails for the flag catalogue itself. */
class FeatureFlagTest {
    @Test
    fun noFlag_isOnByDefaultInProd() {
        val onInProd = FeatureFlag.entries.filter { it.isEnabledByDefault(AppEnvironment.PROD) }
        assertTrue(onInProd.isEmpty(), "Flags must default to OFF in PROD (turn them on remotely): $onInProd")
    }

    @Test
    fun keys_areUniqueValidRemoteConfigNames() {
        val keys = FeatureFlag.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size, "Duplicate flag keys")
        keys.forEach { assertTrue(Regex("^[a-z][a-z0-9_]*$").matches(it), "Invalid flag key '$it'") }
    }
}
