package com.studiodraug.refproj.feature.debug

import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

// UNIT TEST
class ScreenCatalogTest {
    private val wrongPassword = ScreenPreview("Wrong password") {}
    private val catalog =
        ScreenCatalog(
            listOf(
                ScreenPreviewGroup("Tasks", listOf(ScreenPreview("Empty") {})),
                ScreenPreviewGroup("Login", listOf(ScreenPreview("Empty") {}, wrongPassword)),
            ),
        )

    @Test
    fun groups_areSortedByTitle() {
        assertEquals(listOf("Login", "Tasks"), catalog.groups.map { it.title })
    }

    @Test
    fun find_looksUpByGroupAndName() {
        assertSame(wrongPassword, catalog.find("Login", "Wrong password"))
        assertNull(catalog.find("Tasks", "Wrong password"))
        assertNull(catalog.find("Missing", "Empty"))
    }

    @Test
    fun viewModel_listsTheTableOfContents() =
        runViewModelTest {
            assertEquals(
                listOf(CatalogGroup("Login", listOf("Empty", "Wrong password")), CatalogGroup("Tasks", listOf("Empty"))),
                DebugCatalogViewModel(catalog).state.value.groups,
            )
        }

    @Test
    fun designSystemPreviews_haveUniqueNames() {
        val names = designSystemPreviews.previews.map { it.name }
        assertEquals(names.distinct(), names)
    }
}
