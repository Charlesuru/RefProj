package com.studiodraug.refproj.app.navigation

import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.navigation.HomeRoute
import com.studiodraug.refproj.core.navigation.LoginRoute
import com.studiodraug.refproj.core.navigation.ProfileRoute
import com.studiodraug.refproj.core.navigation.TasksRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// UNIT TEST
class MainTabsStateTest {
    /** Stands in for a detail screen pushed inside a tab. */
    private val detail: NavKey = LoginRoute

    private val tabs =
        MainTabsState(
            stacks = MainTab.entries.associateWith { mutableListOf(it.route) },
            selectedIndex = mutableIntStateOf(MainTab.Home.ordinal),
        )

    @Test
    fun startsOnHome() {
        assertEquals(MainTab.Home, tabs.selected)
        assertEquals(listOf<NavKey>(HomeRoute), tabs.backStack)
    }

    @Test
    fun anotherTab_isShownOnTopOfHome_soBackReturnsToHome() {
        tabs.select(MainTab.Profile)
        assertEquals(listOf(HomeRoute, ProfileRoute), tabs.backStack)

        assertTrue(tabs.back())
        assertEquals(MainTab.Home, tabs.selected)
        assertEquals(listOf<NavKey>(HomeRoute), tabs.backStack)
    }

    @Test
    fun backOnHomesRoot_isLeftToThePlatform() {
        assertFalse(tabs.back())
    }

    @Test
    fun eachTabKeepsItsOwnStack() {
        tabs.select(MainTab.Profile)
        tabs.navigate(detail)
        tabs.select(MainTab.Home)
        assertEquals(listOf<NavKey>(HomeRoute), tabs.backStack)

        tabs.select(MainTab.Profile)
        assertEquals(listOf(HomeRoute, ProfileRoute, detail), tabs.backStack)
        assertTrue(tabs.back())
        assertEquals(listOf(HomeRoute, ProfileRoute), tabs.backStack)
    }

    @Test
    fun selectingTheCurrentTabAgain_returnsToItsRoot() {
        tabs.navigate(detail)
        tabs.select(MainTab.Home)
        assertEquals(listOf<NavKey>(HomeRoute), tabs.backStack)
    }

    @Test
    fun navigatingToTheCurrentScreen_doesNotDuplicateIt() {
        tabs.navigate(detail)
        tabs.navigate(detail)
        assertEquals(listOf(HomeRoute, detail), tabs.backStack)
    }

    @Test
    fun switchingBetweenNonStartTabs_keepsHomeUnderneath() {
        tabs.select(MainTab.Tasks)
        tabs.select(MainTab.Profile)
        assertEquals(listOf(HomeRoute, ProfileRoute), tabs.backStack)
    }

    @Test
    fun screenNames_areTheRouteTypeWithoutSuffix() {
        assertEquals("home", screenName(HomeRoute))
        assertEquals("tasks", screenName(TasksRoute))
        assertEquals("profile", screenName(ProfileRoute))
    }
}
