package com.studiodraug.refproj.feature.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.designsystem.component.RefButton
import com.studiodraug.refproj.core.designsystem.component.RefButtonStyle
import com.studiodraug.refproj.core.designsystem.component.RefConfirmDialog
import com.studiodraug.refproj.core.designsystem.component.RefContentWidth
import com.studiodraug.refproj.core.designsystem.component.RefDivider
import com.studiodraug.refproj.core.designsystem.component.RefListItem
import com.studiodraug.refproj.core.designsystem.component.RefSwitchListItem
import com.studiodraug.refproj.core.designsystem.component.RefTopAppBar
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.featureflags.FeatureFlag
import com.studiodraug.refproj.core.featureflags.FeatureFlagState
import com.studiodraug.refproj.feature.debug.resources.Res
import com.studiodraug.refproj.feature.debug.resources.debug_api_base_url
import com.studiodraug.refproj.feature.debug.resources.debug_build_type
import com.studiodraug.refproj.feature.debug.resources.debug_build_type_debug
import com.studiodraug.refproj.feature.debug.resources.debug_build_type_release
import com.studiodraug.refproj.feature.debug.resources.debug_catalog
import com.studiodraug.refproj.feature.debug.resources.debug_catalog_subtitle
import com.studiodraug.refproj.feature.debug.resources.debug_clear
import com.studiodraug.refproj.feature.debug.resources.debug_clear_data
import com.studiodraug.refproj.feature.debug.resources.debug_clear_data_message
import com.studiodraug.refproj.feature.debug.resources.debug_clear_data_subtitle
import com.studiodraug.refproj.feature.debug.resources.debug_clear_data_title
import com.studiodraug.refproj.feature.debug.resources.debug_crash
import com.studiodraug.refproj.feature.debug.resources.debug_crash_confirm
import com.studiodraug.refproj.feature.debug.resources.debug_crash_message
import com.studiodraug.refproj.feature.debug.resources.debug_crash_title
import com.studiodraug.refproj.feature.debug.resources.debug_device
import com.studiodraug.refproj.feature.debug.resources.debug_environment
import com.studiodraug.refproj.feature.debug.resources.debug_flag_remove_override
import com.studiodraug.refproj.feature.debug.resources.debug_flag_source_default
import com.studiodraug.refproj.feature.debug.resources.debug_flag_source_override
import com.studiodraug.refproj.feature.debug.resources.debug_flag_source_remote
import com.studiodraug.refproj.feature.debug.resources.debug_flag_subtitle
import com.studiodraug.refproj.feature.debug.resources.debug_flags_remove_all_overrides
import com.studiodraug.refproj.feature.debug.resources.debug_logs
import com.studiodraug.refproj.feature.debug.resources.debug_logs_subtitle
import com.studiodraug.refproj.feature.debug.resources.debug_no_test_account
import com.studiodraug.refproj.feature.debug.resources.debug_non_fatal
import com.studiodraug.refproj.feature.debug.resources.debug_non_fatal_sent
import com.studiodraug.refproj.feature.debug.resources.debug_section_environment
import com.studiodraug.refproj.feature.debug.resources.debug_section_flags
import com.studiodraug.refproj.feature.debug.resources.debug_section_state
import com.studiodraug.refproj.feature.debug.resources.debug_section_tools
import com.studiodraug.refproj.feature.debug.resources.debug_show_onboarding
import com.studiodraug.refproj.feature.debug.resources.debug_show_onboarding_subtitle
import com.studiodraug.refproj.feature.debug.resources.debug_sign_in_failed
import com.studiodraug.refproj.feature.debug.resources.debug_sign_in_test
import com.studiodraug.refproj.feature.debug.resources.debug_sign_out
import com.studiodraug.refproj.feature.debug.resources.debug_title
import com.studiodraug.refproj.feature.debug.resources.debug_version
import org.jetbrains.compose.resources.stringResource

@Composable
fun DebugScreen(
    state: DebugUiState,
    onAction: (DebugAction) -> Unit,
    onOpenLogs: () -> Unit,
    onOpenCatalog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(DebugTestTags.MENU),
        topBar = { RefTopAppBar(title = stringResource(Res.string.debug_title), onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        RefContentWidth(modifier = Modifier.padding(padding)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = RefTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.xl),
            ) {
                EnvironmentSection(state.environment)
                FlagsSection(state.flags, onAction)
                AppStateSection(state, onAction)
                ToolsSection(state.message, onAction, onOpenLogs, onOpenCatalog)
            }
        }
    }

    when (state.confirmation) {
        DebugConfirmation.ClearData -> {
            RefConfirmDialog(
                title = stringResource(Res.string.debug_clear_data_title),
                message = stringResource(Res.string.debug_clear_data_message),
                onConfirm = { onAction(DebugAction.Confirmed) },
                onDismiss = { onAction(DebugAction.ConfirmationDismissed) },
                confirmText = stringResource(Res.string.debug_clear),
                destructive = true,
            )
        }

        DebugConfirmation.Crash -> {
            RefConfirmDialog(
                title = stringResource(Res.string.debug_crash_title),
                message = stringResource(Res.string.debug_crash_message),
                onConfirm = { onAction(DebugAction.Confirmed) },
                onDismiss = { onAction(DebugAction.ConfirmationDismissed) },
                confirmText = stringResource(Res.string.debug_crash_confirm),
                destructive = true,
            )
        }

        null -> {
            Unit
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.sm)
                    .semantics { heading() },
        )
        content()
    }
}

@Composable
private fun EnvironmentSection(environment: EnvironmentInfo) {
    Section(title = stringResource(Res.string.debug_section_environment)) {
        RefListItem(title = stringResource(Res.string.debug_environment), subtitle = environment.environment)
        RefListItem(title = stringResource(Res.string.debug_version), subtitle = environment.version)
        RefListItem(
            title = stringResource(Res.string.debug_build_type),
            subtitle =
                stringResource(
                    if (environment.isDebugBuild) Res.string.debug_build_type_debug else Res.string.debug_build_type_release,
                ),
        )
        RefListItem(title = stringResource(Res.string.debug_api_base_url), subtitle = environment.apiBaseUrl)
        RefListItem(title = stringResource(Res.string.debug_device), subtitle = environment.device)
    }
}

@Composable
private fun FlagsSection(
    flags: List<FeatureFlagState>,
    onAction: (DebugAction) -> Unit,
) {
    Section(title = stringResource(Res.string.debug_section_flags)) {
        flags.forEach { state ->
            RefSwitchListItem(
                title = state.flag.key,
                subtitle = stringResource(Res.string.debug_flag_subtitle, state.flag.description, stringResource(state.source.label())),
                checked = state.enabled,
                onCheckedChange = { onAction(DebugAction.FlagChanged(state.flag, it)) },
            )
            if (state.source == FeatureFlagState.Source.OVERRIDE) {
                RefButton(
                    text = stringResource(Res.string.debug_flag_remove_override),
                    onClick = { onAction(DebugAction.FlagOverrideRemoved(state.flag)) },
                    modifier = Modifier.padding(horizontal = RefTheme.spacing.sm),
                    style = RefButtonStyle.Tertiary,
                )
            }
            RefDivider()
        }
        if (flags.count { it.source == FeatureFlagState.Source.OVERRIDE } > 1) {
            RefButton(
                text = stringResource(Res.string.debug_flags_remove_all_overrides),
                onClick = { onAction(DebugAction.AllFlagOverridesRemoved) },
                modifier = Modifier.padding(horizontal = RefTheme.spacing.sm),
                style = RefButtonStyle.Tertiary,
            )
        }
    }
}

@Composable
private fun AppStateSection(
    state: DebugUiState,
    onAction: (DebugAction) -> Unit,
) {
    Section(title = stringResource(Res.string.debug_section_state)) {
        RefListItem(
            title = stringResource(Res.string.debug_show_onboarding),
            subtitle = stringResource(Res.string.debug_show_onboarding_subtitle),
            onClick = { onAction(DebugAction.ShowOnboarding) },
        )
        if (state.isSignedIn) {
            RefListItem(title = stringResource(Res.string.debug_sign_out), onClick = { onAction(DebugAction.SignOut) })
        } else {
            RefListItem(
                title = stringResource(Res.string.debug_sign_in_test),
                subtitle = state.testAccountEmail ?: stringResource(Res.string.debug_no_test_account),
                onClick = state.testAccountEmail?.let { { onAction(DebugAction.SignInWithTestAccount) } },
            )
        }
        RefListItem(
            title = stringResource(Res.string.debug_clear_data),
            subtitle = stringResource(Res.string.debug_clear_data_subtitle),
            onClick = { onAction(DebugAction.ClearDataClicked) },
        )
    }
}

@Composable
private fun ToolsSection(
    message: DebugMessage?,
    onAction: (DebugAction) -> Unit,
    onOpenLogs: () -> Unit,
    onOpenCatalog: () -> Unit,
) {
    Section(title = stringResource(Res.string.debug_section_tools)) {
        RefListItem(
            title = stringResource(Res.string.debug_catalog),
            subtitle = stringResource(Res.string.debug_catalog_subtitle),
            onClick = onOpenCatalog,
        )
        RefListItem(
            title = stringResource(Res.string.debug_logs),
            subtitle = stringResource(Res.string.debug_logs_subtitle),
            onClick = onOpenLogs,
        )
        RefListItem(title = stringResource(Res.string.debug_non_fatal), onClick = { onAction(DebugAction.SendNonFatal) })
        RefListItem(title = stringResource(Res.string.debug_crash), onClick = { onAction(DebugAction.CrashClicked) })
        message?.let {
            Text(
                text = stringResource(it.text()),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.sm)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                color =
                    if (it ==
                        DebugMessage.TestSignInFailed
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun FeatureFlagState.Source.label() =
    when (this) {
        FeatureFlagState.Source.DEFAULT -> Res.string.debug_flag_source_default
        FeatureFlagState.Source.REMOTE -> Res.string.debug_flag_source_remote
        FeatureFlagState.Source.OVERRIDE -> Res.string.debug_flag_source_override
    }

private fun DebugMessage.text() =
    when (this) {
        DebugMessage.TestSignInFailed -> Res.string.debug_sign_in_failed
        DebugMessage.NonFatalSent -> Res.string.debug_non_fatal_sent
    }

@Preview
@Composable
private fun DebugScreenPreview() {
    RefTheme {
        DebugScreen(
            state =
                DebugUiState(
                    environment =
                        EnvironmentInfo.from(
                            AppConfig(AppEnvironment.DEV, "https://api.dev.example", "1.0.0", "42", isDebugBuild = true),
                            device = "Android 16 (API 36) · Google Pixel 9",
                        ),
                    flags = listOf(FeatureFlagState(FeatureFlag.HOME_BUILD_INFO, enabled = false, FeatureFlagState.Source.OVERRIDE)),
                    testAccountEmail = "test@example.com",
                    message = DebugMessage.NonFatalSent,
                ),
            onAction = {},
            onOpenLogs = {},
            onOpenCatalog = {},
            onBack = {},
        )
    }
}

/** Stable IDs for UI automation (end-to-end tests): independent of the language, never shown to users. */
object DebugTestTags {
    const val BADGE = "debug.badge"
    const val MENU = "debug.menu"
}
