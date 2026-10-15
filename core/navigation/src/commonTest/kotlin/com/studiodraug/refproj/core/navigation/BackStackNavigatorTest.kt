package com.studiodraug.refproj.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// UNIT TEST
class BackStackNavigatorTest {
    private data class Screen(
        val id: Int,
    ) : NavKey

    private val backStack = mutableListOf<NavKey>(Screen(1))
    private val navigator = BackStackNavigator(backStack)

    @Test
    fun navigate_pushes_butNotTheSameScreenTwice() {
        navigator.navigate(Screen(2))
        navigator.navigate(Screen(2))
        assertEquals(listOf<NavKey>(Screen(1), Screen(2)), backStack)
    }

    @Test
    fun back_popsUntilTheRoot() {
        navigator.navigate(Screen(2))
        assertTrue(navigator.back())
        assertFalse(navigator.back())
        assertEquals(listOf<NavKey>(Screen(1)), backStack)
    }

    @Test
    fun replaceAll_leavesOnlyTheNewRoot() {
        navigator.navigate(Screen(2))
        navigator.replaceAll(Screen(3))
        assertEquals(listOf<NavKey>(Screen(3)), backStack)
    }
}
