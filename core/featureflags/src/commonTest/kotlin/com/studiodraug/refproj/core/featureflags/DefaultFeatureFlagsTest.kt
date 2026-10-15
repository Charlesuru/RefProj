package com.studiodraug.refproj.core.featureflags

import app.cash.turbine.test
import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.featureflags.FeatureFlagState.Source
import com.studiodraug.refproj.core.testing.inMemoryKeyValueStore
import com.studiodraug.refproj.core.testing.testAppConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

// UNIT TEST
class DefaultFeatureFlagsTest {
    private val flag = FeatureFlag.HOME_BUILD_INFO // on by default in DEV, off in PROD
    private val store = inMemoryKeyValueStore()
    private val remote = FakeRemoteFlagSource()

    private fun flags(
        environment: AppEnvironment = AppEnvironment.DEV,
        isDebugBuild: Boolean = false,
    ) = DefaultFeatureFlags(testAppConfig(environment = environment, isDebugBuild = isDebugBuild), store, remote)

    @Test
    fun default_followsEnvironment() {
        assertTrue(flags(AppEnvironment.DEV).isEnabled(flag))
        assertFalse(flags(AppEnvironment.PROD).isEnabled(flag))
    }

    @Test
    fun remote_winsOverDefault() {
        remote.values.value = mapOf(flag.key to true)
        assertTrue(flags(AppEnvironment.PROD).isEnabled(flag))
    }

    @Test
    fun override_winsOverRemote_inNonProd() {
        remote.values.value = mapOf(flag.key to true)
        val flags = flags(AppEnvironment.SIT)
        flags.setOverride(flag, false)
        assertFalse(flags.isEnabled(flag))

        flags.setOverride(flag, null)
        assertTrue(flags.isEnabled(flag))
    }

    @Test
    fun override_isIgnored_inProdReleaseBuild() {
        val flags = flags(AppEnvironment.PROD, isDebugBuild = false)
        flags.setOverride(flag, true)
        assertFalse(flags.isEnabled(flag))
    }

    @Test
    fun override_works_inProdDebugBuild() {
        val flags = flags(AppEnvironment.PROD, isDebugBuild = true)
        flags.setOverride(flag, true)
        assertTrue(flags.isEnabled(flag))
    }

    @Test
    fun observe_emitsChangesFromOverridesAndRemote() =
        runTest {
            val flags = flags(AppEnvironment.DEV)
            flags.observe(flag).test {
                assertTrue(awaitItem())
                flags.setOverride(flag, false)
                assertFalse(awaitItem())
                flags.clearOverrides()
                assertTrue(awaitItem())
                remote.values.value = mapOf(flag.key to false)
                assertFalse(awaitItem())
            }
        }

    @Test
    fun observeStates_reportsTheSourceOfEveryFlag() =
        runTest {
            val flags = flags(AppEnvironment.DEV)
            assertEquals(
                Source.DEFAULT,
                flags
                    .observeStates()
                    .first()
                    .single { it.flag == flag }
                    .source,
            )

            remote.values.value = mapOf(flag.key to false)
            assertEquals(
                Source.REMOTE,
                flags
                    .observeStates()
                    .first()
                    .single { it.flag == flag }
                    .source,
            )

            flags.setOverride(flag, true)
            val state = flags.observeStates().first().single { it.flag == flag }
            assertEquals(FeatureFlagState(flag, enabled = true, source = Source.OVERRIDE), state)
        }

    private class FakeRemoteFlagSource : RemoteFlagSource {
        override val values = MutableStateFlow(emptyMap<String, Boolean>())

        override suspend fun refresh() = Unit
    }
}
