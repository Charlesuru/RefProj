package com.studiodraug.refproj.feature.debug

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.featureflags.FeatureFlag
import com.studiodraug.refproj.core.featureflags.FeatureFlagOverrides
import com.studiodraug.refproj.core.featureflags.FeatureFlagState
import com.studiodraug.refproj.core.featureflags.FeatureFlags
import com.studiodraug.refproj.core.observability.CrashReporter
import com.studiodraug.refproj.core.ui.MviViewModel
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.GetTestAccountUseCase
import com.studiodraug.refproj.domain.auth.LoginResult
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.ObserveAuthStateUseCase
import com.studiodraug.refproj.domain.debug.ClearLocalDataUseCase
import com.studiodraug.refproj.domain.debug.SignInWithTestAccountUseCase
import com.studiodraug.refproj.domain.onboarding.ResetOnboardingUseCase
import kotlinx.coroutines.launch

/** Build and device facts shown at the top of the debug menu. All of it is public, non-secret configuration. */
data class EnvironmentInfo(
    val environment: String,
    val version: String,
    val isDebugBuild: Boolean,
    val apiBaseUrl: String,
    val device: String,
) {
    companion object {
        fun from(
            config: AppConfig,
            device: String,
        ) = EnvironmentInfo(
            environment = config.environment.name,
            version = "${config.versionName} (${config.buildNumber})",
            isDebugBuild = config.isDebugBuild,
            apiBaseUrl = config.apiBaseUrl,
            device = device,
        )
    }
}

/** Something the debug menu reports after an action. */
enum class DebugMessage {
    TestSignInFailed,
    NonFatalSent,
}

/** Actions that need a confirmation first. */
enum class DebugConfirmation {
    ClearData,
    Crash,
}

data class DebugUiState(
    val environment: EnvironmentInfo,
    val flags: List<FeatureFlagState> = emptyList(),
    /** E-mail address of the environment's test account; `null` when there is none. */
    val testAccountEmail: String? = null,
    val isSignedIn: Boolean = false,
    val isSigningIn: Boolean = false,
    val message: DebugMessage? = null,
    val confirmation: DebugConfirmation? = null,
)

sealed interface DebugAction {
    data class FlagChanged(
        val flag: FeatureFlag,
        val enabled: Boolean,
    ) : DebugAction

    data class FlagOverrideRemoved(
        val flag: FeatureFlag,
    ) : DebugAction

    data object AllFlagOverridesRemoved : DebugAction

    data object ShowOnboarding : DebugAction

    data object SignInWithTestAccount : DebugAction

    data object SignOut : DebugAction

    data object ClearDataClicked : DebugAction

    data object CrashClicked : DebugAction

    data object SendNonFatal : DebugAction

    data object Confirmed : DebugAction

    data object ConfirmationDismissed : DebugAction
}

/** The shortcuts that change the app's state (who is signed in, onboarding, stored data). */
class AppStateShortcuts(
    val observeAuthState: ObserveAuthStateUseCase,
    val getTestAccount: GetTestAccountUseCase,
    val signInWithTestAccount: SignInWithTestAccountUseCase,
    val resetOnboarding: ResetOnboardingUseCase,
    val logout: LogoutUseCase,
    val clearLocalData: ClearLocalDataUseCase,
)

/** Thrown on purpose by "Crash the app", to check that crashes reach Crashlytics. */
class DebugTestCrashException : RuntimeException("Test crash from the debug menu")

/** Reported on purpose by "Send a non-fatal error". */
class DebugNonFatalException : RuntimeException("Test non-fatal error from the debug menu")

/**
 * The debug menu. Its state shortcuts never navigate: the app follows the new state by itself
 * (`AppViewModel`), which also closes the menu, because the root back stack starts over.
 */
class DebugViewModel(
    environment: EnvironmentInfo,
    featureFlags: FeatureFlags,
    private val flagOverrides: FeatureFlagOverrides,
    private val shortcuts: AppStateShortcuts,
    private val crashReporter: CrashReporter,
    logger: Logger,
) : MviViewModel<DebugUiState, DebugAction, Nothing>(
        DebugUiState(
            environment = environment,
            testAccountEmail = shortcuts.getTestAccount()?.email,
        ),
    ) {
    private val log = logger.withTag("Debug")

    init {
        viewModelScope.launch { featureFlags.observeStates().collect { flags -> updateState { copy(flags = flags) } } }
        viewModelScope.launch {
            shortcuts.observeAuthState().collect { auth -> updateState { copy(isSignedIn = auth is AuthState.SignedIn) } }
        }
    }

    override fun onAction(action: DebugAction) {
        when (action) {
            is DebugAction.FlagChanged -> flagOverrides.setOverride(action.flag, action.enabled)
            is DebugAction.FlagOverrideRemoved -> flagOverrides.setOverride(action.flag, null)
            DebugAction.AllFlagOverridesRemoved -> flagOverrides.clearOverrides()
            DebugAction.ShowOnboarding -> viewModelScope.launch { shortcuts.resetOnboarding() }
            DebugAction.SignInWithTestAccount -> signInWithTestAccount()
            DebugAction.SignOut -> viewModelScope.launch { shortcuts.logout() }
            DebugAction.ClearDataClicked -> updateState { copy(confirmation = DebugConfirmation.ClearData) }
            DebugAction.CrashClicked -> updateState { copy(confirmation = DebugConfirmation.Crash) }
            DebugAction.SendNonFatal -> sendNonFatal()
            DebugAction.Confirmed -> confirm()
            DebugAction.ConfirmationDismissed -> updateState { copy(confirmation = null) }
        }
    }

    private fun signInWithTestAccount() {
        if (currentState.isSigningIn) return
        updateState { copy(isSigningIn = true, message = null) }
        viewModelScope.launch {
            val result = shortcuts.signInWithTestAccount()
            // On success the app shows the tab shell and this screen goes away.
            if (result !is LoginResult.Success) {
                updateState { copy(isSigningIn = false, message = DebugMessage.TestSignInFailed) }
            }
        }
    }

    private fun sendNonFatal() {
        log.i { "Sending a test non-fatal error" }
        crashReporter.recordNonFatal(DebugNonFatalException())
        updateState { copy(message = DebugMessage.NonFatalSent) }
    }

    private fun confirm() {
        val confirmation = currentState.confirmation ?: return
        updateState { copy(confirmation = null) }
        when (confirmation) {
            DebugConfirmation.ClearData -> {
                viewModelScope.launch {
                    flagOverrides.clearOverrides()
                    shortcuts.clearLocalData()
                    log.i { "Local data cleared" }
                }
            }

            DebugConfirmation.Crash -> {
                log.w { "Crashing on purpose" }
                throw DebugTestCrashException()
            }
        }
    }
}
