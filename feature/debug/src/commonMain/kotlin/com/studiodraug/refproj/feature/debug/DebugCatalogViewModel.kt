package com.studiodraug.refproj.feature.debug

import androidx.lifecycle.ViewModel
import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Every sample screen the features (and the design system) contribute, sorted by group title. */
class ScreenCatalog(
    groups: List<ScreenPreviewGroup>,
) {
    val groups: List<ScreenPreviewGroup> = groups.sortedBy { it.title }

    fun find(
        group: String,
        preview: String,
    ): ScreenPreview? = groups.firstOrNull { it.title == group }?.previews?.firstOrNull { it.name == preview }
}

data class CatalogGroup(
    val title: String,
    val previews: List<String>,
)

data class DebugCatalogUiState(
    val groups: List<CatalogGroup>,
)

/** The catalog's table of contents. Read-only: selecting a preview is navigation, handled by the screen. */
class DebugCatalogViewModel(
    catalog: ScreenCatalog,
) : ViewModel() {
    val state: StateFlow<DebugCatalogUiState> =
        MutableStateFlow(
            DebugCatalogUiState(catalog.groups.map { group -> CatalogGroup(group.title, group.previews.map { it.name }) }),
        ).asStateFlow()
}
