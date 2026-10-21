package com.studiodraug.refproj.core.observability

import co.touchlab.kermit.Logger

/**
 * Logs every analytics call before forwarding it, so the debug menu's log viewer shows what the app
 * reports — debug builds never send analytics to Firebase. Debug tools only (never in a PROD release build).
 *
 * Event parameters never contain personal data ([AnalyticsEvent]), so they may be logged; the user ID is not.
 */
class LoggingAnalyticsTracker(
    private val delegate: AnalyticsTracker,
    logger: Logger = Logger,
) : AnalyticsTracker {
    private val log = logger.withTag("Analytics")

    override fun logEvent(event: AnalyticsEvent) {
        log.i { if (event.parameters.isEmpty()) "Event ${event.name}" else "Event ${event.name} ${event.parameters}" }
        delegate.logEvent(event)
    }

    override fun logScreenView(screenName: String) {
        log.i { "Screen $screenName" }
        delegate.logScreenView(screenName)
    }

    override fun setUserId(id: String?) {
        log.i { if (id == null) "User ID cleared" else "User ID set" }
        delegate.setUserId(id)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        log.i { "Collection ${if (enabled) "on" else "off"}" }
        delegate.setCollectionEnabled(enabled)
    }
}
