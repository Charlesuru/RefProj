package com.studiodraug.refproj.app.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Analytics screen name of a route: `TasksRoute` → `tasks`. Only the route type, never its
 * arguments — they could contain personal data.
 */
internal fun screenName(route: NavKey): String =
    route::class
        .simpleName
        .orEmpty()
        .removeSuffix("Route")
        .lowercase()
