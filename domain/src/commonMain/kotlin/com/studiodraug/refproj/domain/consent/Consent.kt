package com.studiodraug.refproj.domain.consent

import kotlinx.coroutines.flow.Flow

/**
 * The user's privacy choices on this device (GDPR). Analytics is **opt-in**: off until the user
 * turns it on (onboarding or profile). Implemented in the data layer, which also applies the choice
 * to the analytics SDK.
 */
interface ConsentRepository {
    val analyticsConsent: Flow<Boolean>

    suspend fun setAnalyticsConsent(granted: Boolean)
}

class ObserveAnalyticsConsentUseCase(
    private val repository: ConsentRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.analyticsConsent
}

class SetAnalyticsConsentUseCase(
    private val repository: ConsentRepository,
) {
    suspend operator fun invoke(granted: Boolean) = repository.setAnalyticsConsent(granted)
}
