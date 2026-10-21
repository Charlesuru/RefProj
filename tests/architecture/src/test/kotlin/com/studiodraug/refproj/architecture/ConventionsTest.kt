package com.studiodraug.refproj.architecture

import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

// ARCHITECTURE TEST

/** MVVM and Clean Architecture naming and shape conventions (CLAUDE.md §1, docs/architecture.md). */
class ConventionsTest {
    private val viewModels = productionSources.classes().withNameEndingWith("ViewModel").filterNot { it.name == "MviViewModel" }

    @Test
    fun viewModels_extendAViewModelBase() {
        viewModels.assertTrue { viewModel ->
            viewModel.parents().any {
                it.name
                    .substringBefore('<')
                    .substringAfterLast('.')
                    .trim() in VIEW_MODEL_BASES
            }
        }
    }

    @Test
    fun viewModels_liveInAFeatureOrTheAppModule() {
        viewModels.assertTrue {
            it.resideInPackage("$BASE_PACKAGE.feature..") || it.resideInPackage("$BASE_PACKAGE.app..")
        }
    }

    @Test
    fun viewModels_exposeOnlyReadOnlyState() {
        // One StateFlow<UiState> out (MviViewModel.state), actions in (onAction), events through a Flow.
        viewModels.assertTrue(additionalMessage = "Expose StateFlow/Flow, never a mutable holder") { viewModel ->
            viewModel.properties().none { property ->
                // The declared type, or the initializer when the type is inferred.
                val exposed = property.type?.name ?: property.text.substringAfter('=', "").trim()
                !property.hasPrivateModifier && MUTABLE_HOLDERS.any { exposed.substringAfterLast('.').startsWith(it) }
            }
        }
    }

    @Test
    fun useCases_liveInDomain_andAreInvokable() {
        productionSources.classes().withNameEndingWith("UseCase").assertTrue { useCase ->
            useCase.resideInPackage("$BASE_PACKAGE.domain..") &&
                useCase.functions().any { it.name == "invoke" && it.hasOperatorModifier }
        }
    }

    @Test
    fun repositoryInterfaces_liveInDomain() {
        productionSources.interfaces().withNameEndingWith("Repository").assertTrue {
            it.resideInPackage("$BASE_PACKAGE.domain..")
        }
    }

    @Test
    fun repositoryImplementations_areInternalToData_andImplementTheirDomainInterface() {
        productionSources.classes().withNameEndingWith("RepositoryImpl").assertTrue { implementation ->
            implementation.resideInPackage("$BASE_PACKAGE.data..") &&
                implementation.hasInternalModifier &&
                implementation.hasParentWithName(implementation.name.removeSuffix("Impl"))
        }
    }

    private companion object {
        val VIEW_MODEL_BASES = setOf("ViewModel", "MviViewModel")
        val MUTABLE_HOLDERS = listOf("MutableStateFlow", "MutableSharedFlow", "MutableState", "mutableStateOf", "Channel")
    }
}
