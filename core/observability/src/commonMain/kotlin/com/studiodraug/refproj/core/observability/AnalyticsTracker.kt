package com.studiodraug.refproj.core.observability

/**
 * A product analytics event. Names and parameter keys follow Firebase Analytics rules (snake_case,
 * start with a letter, at most 40 characters; values at most 100 characters), checked at creation
 * so a bad event fails in tests instead of being dropped silently in production.
 *
 * PRIVACY: parameters must never contain personal data.
 */
data class AnalyticsEvent(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
) {
    init {
        require(NAME_PATTERN.matches(name)) { "Invalid analytics event name '$name'" }
        require(parameters.size <= MAX_PARAMETERS) { "At most $MAX_PARAMETERS parameters per event" }
        parameters.forEach { (key, value) ->
            require(NAME_PATTERN.matches(key)) { "Invalid analytics parameter name '$key'" }
            require(value.length <= MAX_VALUE_LENGTH) { "Analytics parameter '$key' is longer than $MAX_VALUE_LENGTH" }
        }
    }

    private companion object {
        val NAME_PATTERN = Regex("^[a-z][a-z0-9_]{0,39}$")
        const val MAX_PARAMETERS = 25
        const val MAX_VALUE_LENGTH = 100
    }
}

/**
 * Product analytics (Firebase Analytics). Implemented natively by each platform shell.
 */
interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)

    fun logScreenView(screenName: String)

    /** Pseudonymous user ID (never an e-mail address); `null` after logout. */
    fun setUserId(id: String?)

    /** Turns collection on or off, e.g. from the user's consent choice. */
    fun setCollectionEnabled(enabled: Boolean)
}

/** Used when no analytics backend is configured (local builds without Firebase config, tests). */
object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) = Unit

    override fun logScreenView(screenName: String) = Unit

    override fun setUserId(id: String?) = Unit

    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
