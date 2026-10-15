package com.studiodraug.refproj.core.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * Navigation commands for screens. Screens receive it from their [NavEntryInstaller] and call it
 * in response to one-off ViewModel events; ViewModels never hold a navigator.
 */
interface Navigator {
    fun navigate(route: NavKey)

    /** Pops the current screen. Returns `false` when it is the last one (the platform then exits). */
    fun back(): Boolean

    /** Replaces the whole back stack, e.g. after login or logout. */
    fun replaceAll(route: NavKey)
}

/** [Navigator] over a Navigation 3 back stack (any mutable list of keys). */
class BackStackNavigator(
    private val backStack: MutableList<NavKey>,
) : Navigator {
    override fun navigate(route: NavKey) {
        if (backStack.lastOrNull() != route) backStack.add(route)
    }

    override fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    override fun replaceAll(route: NavKey) {
        backStack.clear()
        backStack.add(route)
    }
}

/**
 * How a feature contributes its screens to the app's `NavDisplay`. Each feature binds one in its
 * Koin module; `app:shared` installs all of them. Features therefore never reference each other.
 */
fun interface NavEntryInstaller {
    fun EntryProviderScope<NavKey>.install(navigator: Navigator)
}
