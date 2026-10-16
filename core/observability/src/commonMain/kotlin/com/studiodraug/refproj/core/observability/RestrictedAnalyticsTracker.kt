package com.studiodraug.refproj.core.observability

/**
 * Enforces the build-level collection policy on top of the user's consent: collection is only
 * switched on when the user consents **and** [collectionAllowed] (release builds — debug builds
 * never pollute production dashboards). Events are still forwarded; the SDK drops them while off.
 */
class RestrictedAnalyticsTracker(
    private val delegate: AnalyticsTracker,
    private val collectionAllowed: Boolean,
) : AnalyticsTracker by delegate {
    override fun setCollectionEnabled(enabled: Boolean) = delegate.setCollectionEnabled(enabled && collectionAllowed)
}
