package com.studiodraug.refproj.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.component.RefButton
import com.studiodraug.refproj.core.designsystem.component.RefButtonStyle
import com.studiodraug.refproj.core.designsystem.component.RefContentWidth
import com.studiodraug.refproj.core.designsystem.component.RefPagerIndicator
import com.studiodraug.refproj.core.designsystem.component.RefSurface
import com.studiodraug.refproj.core.designsystem.component.RefSwitchListItem
import com.studiodraug.refproj.core.designsystem.icon.RefIcons
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.feature.onboarding.resources.Res
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_consent_body
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_consent_switch
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_consent_title
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_get_started
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_next
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_private_body
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_private_title
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_skip
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_tasks_body
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_tasks_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The last page asks for analytics consent, so "Skip" jumps there instead of past it. */
private enum class OnboardingPage(
    val title: StringResource,
    val body: StringResource,
) {
    Tasks(Res.string.onboarding_tasks_title, Res.string.onboarding_tasks_body),
    Private(Res.string.onboarding_private_title, Res.string.onboarding_private_body),
    Consent(Res.string.onboarding_consent_title, Res.string.onboarding_consent_body),
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = OnboardingPage.entries
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    RefSurface(modifier = modifier.fillMaxSize()) {
        RefContentWidth(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = RefTheme.sizes.minTouchTarget)) {
                    if (!isLastPage) {
                        RefButton(
                            text = stringResource(Res.string.onboarding_skip),
                            onClick = { scope.launch { pagerState.animateScrollToPage(pages.lastIndex) } },
                            modifier = Modifier.align(Alignment.CenterEnd),
                            style = RefButtonStyle.Tertiary,
                        )
                    }
                }
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                    OnboardingPageContent(page = pages[index], state = state, onAction = onAction)
                }
                RefPagerIndicator(pageCount = pages.size, currentPage = pagerState.currentPage)
                if (isLastPage) {
                    RefButton(
                        text = stringResource(Res.string.onboarding_get_started),
                        onClick = { onAction(OnboardingAction.Finish) },
                        modifier = Modifier.fillMaxWidth().padding(top = RefTheme.spacing.xl),
                        loading = state.isFinishing,
                    )
                } else {
                    RefButton(
                        text = stringResource(Res.string.onboarding_next),
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        modifier = Modifier.fillMaxWidth().padding(top = RefTheme.spacing.xl),
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = page.icon(),
            // Decorative: the title below says the same.
            contentDescription = null,
            modifier = Modifier.size(RefTheme.sizes.illustration),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(page.body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (page == OnboardingPage.Consent) {
            RefSwitchListItem(
                title = stringResource(Res.string.onboarding_consent_switch),
                checked = state.analyticsConsent,
                onCheckedChange = { onAction(OnboardingAction.AnalyticsConsentChanged(it)) },
            )
        }
    }
}

@Composable
private fun OnboardingPage.icon(): ImageVector =
    when (this) {
        OnboardingPage.Tasks -> RefIcons.Tasks
        OnboardingPage.Private -> RefIcons.Lock
        OnboardingPage.Consent -> RefIcons.Insights
    }

@Preview
@Composable
private fun OnboardingScreenPreview() {
    RefTheme { OnboardingScreen(state = OnboardingUiState(), onAction = {}) }
}
