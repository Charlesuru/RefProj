package com.studiodraug.refproj.data.onboarding

import com.studiodraug.refproj.core.storage.KeyValueStore
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import kotlinx.coroutines.flow.Flow

/** Stores the onboarding state locally; it is per device, not per account. */
internal class OnboardingRepositoryImpl(
    private val store: KeyValueStore,
) : OnboardingRepository {
    override val isCompleted: Flow<Boolean> = store.observeBoolean(KEY_COMPLETED, default = false)

    override suspend fun markCompleted() = store.putBoolean(KEY_COMPLETED, true)

    override suspend fun reset() = store.remove(KEY_COMPLETED)

    private companion object {
        const val KEY_COMPLETED = "onboarding.completed"
    }
}
