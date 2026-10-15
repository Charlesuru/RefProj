package com.studiodraug.refproj.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.component.RefSurface
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.feature.home.resources.Res
import com.studiodraug.refproj.feature.home.resources.home_body
import com.studiodraug.refproj.feature.home.resources.home_environment
import com.studiodraug.refproj.feature.home.resources.home_title
import org.jetbrains.compose.resources.stringResource

/** Placeholder screen proving the module chain builds. Replaced by the real home tab in step 5. */
@Composable
fun HomeScreen(
    environmentName: String,
    modifier: Modifier = Modifier,
) {
    RefSurface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(RefTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.sm, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(Res.string.home_title), style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(Res.string.home_body))
            Text(text = stringResource(Res.string.home_environment, environmentName))
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    RefTheme { HomeScreen(environmentName = "DEV") }
}
