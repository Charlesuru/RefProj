package com.studiodraug.refproj.domain.onboarding

import kotlinx.coroutines.flow.Flow

/** Whether the user has finished onboarding on this device. Implemented in the data layer. */
interface OnboardingRepository {
    val isCompleted: Flow<Boolean>

    suspend fun markCompleted()

    suspend fun reset()
}

/** Emits `true` once onboarding is done; decides the app's start destination. */
class ObserveOnboardingCompletedUseCase(
    private val repository: OnboardingRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.isCompleted
}

class CompleteOnboardingUseCase(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke() = repository.markCompleted()
}

/** Marks onboarding as not done, so the app shows it again at once. The session is kept. Debug tool. */
class ResetOnboardingUseCase(
    private val repository: OnboardingRepository,
) {
    suspend operator fun invoke() = repository.reset()
}
