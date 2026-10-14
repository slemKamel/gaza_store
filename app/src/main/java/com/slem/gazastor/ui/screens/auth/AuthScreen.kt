package com.slem.gazastor.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R

/**
 * First-launch sign-in screen. Guests reach the app through
 * "متابعة كزائر", which marks the prompt as seen and calls [onFinished].
 */
@Composable
fun AuthScreen(
    onFinished: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        AuthContent(
            onAuthenticated = onFinished,
            onGuest = onFinished,
            viewModel = viewModel,
            showHeader = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

/**
 * The shared email/password form. Used full-screen after onboarding and inside
 * the Account tab when signed out.
 */
@Composable
fun AuthContent(
    onAuthenticated: () -> Unit,
    onGuest: () -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.Authenticated -> onAuthenticated()
                AuthEvent.GuestSelected -> onGuest()
            }
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showHeader) {
            Text(
                text = stringResource(R.string.auth_welcome_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.auth_welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        ModeSelector(
            mode = uiState.mode,
            onModeChange = viewModel::onModeChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(stringResource(R.string.auth_email_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text(stringResource(R.string.auth_password_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (uiState.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                TextButton(onClick = viewModel::onTogglePasswordVisibility) {
                    Text(
                        text = stringResource(
                            if (uiState.isPasswordVisible) {
                                R.string.auth_hide_password
                            } else {
                                R.string.auth_show_password
                            }
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        )

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error.message(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        uiState.info?.let { info ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = info.message(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::submit,
            enabled = !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.height(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = stringResource(
                        if (uiState.mode == AuthMode.SignIn) {
                            R.string.auth_action_sign_in
                        } else {
                            R.string.auth_action_sign_up
                        }
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.auth_or),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = viewModel::continueAsGuest,
            enabled = !uiState.isSubmitting
        ) {
            Text(
                text = stringResource(R.string.auth_continue_as_guest),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ModeSelector(
    mode: AuthMode,
    onModeChange: (AuthMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModeButton(
            text = stringResource(R.string.auth_mode_sign_in),
            selected = mode == AuthMode.SignIn,
            onClick = { onModeChange(AuthMode.SignIn) },
            modifier = Modifier.weight(1f)
        )
        ModeButton(
            text = stringResource(R.string.auth_mode_sign_up),
            selected = mode == AuthMode.SignUp,
            onClick = { onModeChange(AuthMode.SignUp) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(text) }
    }
}

@Composable
private fun AuthError.message(): String = stringResource(
    when (this) {
        AuthError.InvalidEmail -> R.string.auth_error_invalid_email
        AuthError.PasswordTooShort -> R.string.auth_error_password_short
        AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
        AuthError.EmailNotConfirmed -> R.string.auth_error_email_not_confirmed
        AuthError.EmailAlreadyRegistered -> R.string.auth_error_email_exists
        AuthError.WeakPassword -> R.string.auth_error_weak_password
        AuthError.Network -> R.string.auth_error_network
        AuthError.Unknown -> R.string.auth_error_unknown
    }
)

@Composable
private fun AuthInfo.message(): String = stringResource(
    when (this) {
        AuthInfo.ConfirmationEmailSent -> R.string.auth_confirmation_sent
    }
)
