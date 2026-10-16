package com.slem.gazastor.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.BrandLockup
import com.slem.gazastor.ui.components.BrandMark

/**
 * Brand moment: the wordmark on beige with a hairline copper rule. Once the
 * onboarding flag is known it hands off to either onboarding or home.
 */
@Composable
fun SplashScreen(
    onFinished: (StartDestination) -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val startDestination by viewModel.startDestination.collectAsState()

    LaunchedEffect(startDestination) {
        startDestination?.let(onFinished)
    }

    SplashContent()
}

/** Stateless brand moment, previewable without the ViewModel. */
@Composable
internal fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                imageVector = BrandMark,
                contentDescription = null,
                modifier = Modifier.size(112.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            BrandLockup()
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
