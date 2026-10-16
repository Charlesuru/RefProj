package com.studiodraug.refproj.data.consent

import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.storage.KeyValueStore
import com.studiodraug.refproj.domain.consent.ConsentRepository
import kotlinx.coroutines.flow.Flow

/**
 * Stores the consent choice per device and applies it to the analytics SDK — at creation (Koin
 * creates it at startup) and on every change. Without a stored choice analytics stays off.
 */
internal class ConsentRepositoryImpl(
    private val store: KeyValueStore,
    private val analytics: AnalyticsTracker,
) : ConsentRepository {
    init {
        analytics.setCollectionEnabled(store.getBoolean(KEY_ANALYTICS, default = false))
    }

    override val analyticsConsent: Flow<Boolean> = store.observeBoolean(KEY_ANALYTICS, default = false)

    override suspend fun setAnalyticsConsent(granted: Boolean) {
        store.putBoolean(KEY_ANALYTICS, granted)
        analytics.setCollectionEnabled(granted)
    }

    private companion object {
        const val KEY_ANALYTICS = "consent.analytics"
    }
}
