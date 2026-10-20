package com.studiodraug.refproj.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.component.RefButton
import com.studiodraug.refproj.core.designsystem.component.RefButtonStyle
import com.studiodraug.refproj.core.designsystem.component.RefCard
import com.studiodraug.refproj.core.designsystem.component.RefConfirmDialog
import com.studiodraug.refproj.core.designsystem.component.RefContentWidth
import com.studiodraug.refproj.core.designsystem.component.RefDivider
import com.studiodraug.refproj.core.designsystem.component.RefListItem
import com.studiodraug.refproj.core.designsystem.component.RefSwitchListItem
import com.studiodraug.refproj.core.designsystem.component.RefTopAppBar
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.feature.profile.resources.Res
import com.studiodraug.refproj.feature.profile.resources.profile_analytics
import com.studiodraug.refproj.feature.profile.resources.profile_analytics_subtitle
import com.studiodraug.refproj.feature.profile.resources.profile_logout
import com.studiodraug.refproj.feature.profile.resources.profile_logout_message
import com.studiodraug.refproj.feature.profile.resources.profile_logout_title
import com.studiodraug.refproj.feature.profile.resources.profile_test
import com.studiodraug.refproj.feature.profile.resources.profile_title
import com.studiodraug.refproj.feature.profile.resources.profile_version
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { RefTopAppBar(title = stringResource(Res.string.profile_title)) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        RefContentWidth(modifier = Modifier.padding(padding)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.lg),
            ) {
                Text(text = stringResource(Res.string.profile_test))
                RefCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = state.displayName, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = state.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column {
                    RefSwitchListItem(
                        title = stringResource(Res.string.profile_analytics),
                        subtitle = stringResource(Res.string.profile_analytics_subtitle),
                        checked = state.analyticsConsent,
                        onCheckedChange = { onAction(ProfileAction.AnalyticsConsentChanged(it)) },
                    )
                    RefDivider()
                    RefListItem(title = stringResource(Res.string.profile_version), subtitle = state.version)
                }
                RefButton(
                    text = stringResource(Res.string.profile_logout),
                    onClick = { onAction(ProfileAction.LogoutClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    style = RefButtonStyle.Destructive,
                    loading = state.isLoggingOut,
                )
            }
        }
    }

    if (state.showLogoutConfirmation) {
        RefConfirmDialog(
            title = stringResource(Res.string.profile_logout_title),
            message = stringResource(Res.string.profile_logout_message),
            onConfirm = { onAction(ProfileAction.LogoutConfirmed) },
            onDismiss = { onAction(ProfileAction.LogoutDismissed) },
            confirmText = stringResource(Res.string.profile_logout),
            destructive = true,
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    RefTheme {
        ProfileScreen(
            state = ProfileUiState(displayName = "Test User", email = "test@example.com", version = "1.0.0 (42)"),
            onAction = {},
        )
    }
}
