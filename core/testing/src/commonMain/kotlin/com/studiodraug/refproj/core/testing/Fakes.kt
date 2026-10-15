package com.studiodraug.refproj.core.testing

import com.studiodraug.refproj.core.observability.AnalyticsEvent
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.observability.CrashReporter
import com.studiodraug.refproj.core.storage.SecureStore

/** In-memory [SecureStore]. */
class FakeSecureStore : SecureStore {
    val values = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? = values[key]

    override suspend fun put(
        key: String,
        value: String,
    ) {
        values[key] = value
    }

    override suspend fun remove(key: String) {
        values.remove(key)
    }

    override suspend fun clear() = values.clear()
}

/** Records everything reported, for assertions. */
class FakeCrashReporter : CrashReporter {
    val breadcrumbs = mutableListOf<String>()
    val nonFatals = mutableListOf<Throwable>()
    val customKeys = mutableMapOf<String, String>()
    var userId: String? = null
        private set

    override fun log(message: String) {
        breadcrumbs += message
    }

    override fun recordNonFatal(throwable: Throwable) {
        nonFatals += throwable
    }

    override fun setUserId(id: String?) {
        userId = id
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        customKeys[key] = value
    }
}

/** Records every analytics call, for assertions. */
class FakeAnalyticsTracker : AnalyticsTracker {
    val events = mutableListOf<AnalyticsEvent>()
    val screens = mutableListOf<String>()
    var userId: String? = null
        private set
    var collectionEnabled: Boolean = true
        private set

    override fun logEvent(event: AnalyticsEvent) {
        events += event
    }

    override fun logScreenView(screenName: String) {
        screens += screenName
    }

    override fun setUserId(id: String?) {
        userId = id
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        collectionEnabled = enabled
    }
}
