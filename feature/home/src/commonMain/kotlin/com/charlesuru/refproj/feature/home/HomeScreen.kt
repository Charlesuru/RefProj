package com.charlesuru.refproj.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charlesuru.refproj.core.designsystem.theme.RefTheme
import com.charlesuru.refproj.feature.home.resources.Res
import com.charlesuru.refproj.feature.home.resources.home_environment
import com.charlesuru.refproj.feature.home.resources.home_title
import org.jetbrains.compose.resources.stringResource

/** Placeholder screen proving the module chain builds. Replaced by the real home tab in step 5. */
@Composable
fun HomeScreen(
    environmentName: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(Res.string.home_title), style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(Res.string.home_environment, environmentName))
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    RefTheme { HomeScreen(environmentName = "DEV") }
}
