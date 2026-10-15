package com.studiodraug.refproj.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.studiodraug.refproj.core.designsystem.resources.Res
import com.studiodraug.refproj.core.designsystem.resources.ic_arrow_back
import com.studiodraug.refproj.core.designsystem.resources.ic_chevron_right
import com.studiodraug.refproj.core.designsystem.resources.ic_error_outline
import com.studiodraug.refproj.core.designsystem.resources.ic_inbox
import org.jetbrains.compose.resources.vectorResource

/**
 * Icons shared by the design system and features. Vector drawables from Material Icons
 * (Apache License 2.0) live in this module's `composeResources/drawable`; add new ones there
 * instead of depending on the frozen `material-icons` artifacts.
 */
object RefIcons {
    val ArrowBack: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_arrow_back)

    val ChevronRight: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_chevron_right)

    val Error: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_error_outline)

    val Empty: ImageVector
        @Composable get() = vectorResource(Res.drawable.ic_inbox)
}
