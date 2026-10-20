package com.slem.gazastor.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.AppButton
import com.slem.gazastor.ui.components.AppTextField
import com.slem.gazastor.ui.components.BrandHeader
import com.slem.gazastor.ui.theme.Dimens

/**
 * First-launch sign-in screen. Guests reach the app through
 * "متابعة كزائر", which marks the prompt as seen and calls [onFinished].
 *
 * The brand hero (mark + lockup + copper rule) makes this screen feel like a
 * continuation of the splash; the form itself sits on a hairline card.
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

    AuthForm(
        uiState = uiState,
        onModeChange = viewModel::onModeChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
        onSubmit = viewModel::submit,
        onGuest = viewModel::continueAsGuest,
        showHeader = showHeader,
        modifier = modifier
    )
}

/** Stateless email/password form, so it can be previewed and reused. */
@Composable
internal fun AuthForm(
    uiState: AuthUiState,
    onModeChange: (AuthMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onGuest: () -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = false
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showHeader) {
            BrandHeader(
                animated = true,
                title = stringResource(R.string.auth_welcome_title),
                subtitle = stringResource(R.string.auth_welcome_subtitle)
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ModeSelector(
                    mode = uiState.mode,
                    onModeChange = onModeChange
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = stringResource(R.string.auth_email_label),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_mail),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.auth_password_label),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (uiState.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (uiState.mode == AuthMode.SignUp) {
                            ImeAction.Next
                        } else {
                            ImeAction.Done
                        }
                    ),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_lock),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                painter = painterResource(
                                    if (uiState.isPasswordVisible) {
                                        R.drawable.ic_visibility_off
                                    } else {
                                        R.drawable.ic_visibility
                                    }
                                ),
                                contentDescription = stringResource(
                                    if (uiState.isPasswordVisible) {
                                        R.string.auth_hide_password
                                    } else {
                                        R.string.auth_show_password
                                    }
                                )
                            )
                        }
                    }
                )

                AnimatedVisibility(
                    visible = uiState.mode == AuthMode.SignUp,
                    enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(160)) + shrinkVertically(tween(160))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTextField(
                            value = uiState.confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = stringResource(R.string.auth_confirm_password_label),
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (uiState.isConfirmPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lock),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                                    Icon(
                                        painter = painterResource(
                                            if (uiState.isConfirmPasswordVisible) {
                                                R.drawable.ic_visibility_off
                                            } else {
                                                R.drawable.ic_visibility
                                            }
                                        ),
                                        contentDescription = stringResource(
                                            if (uiState.isConfirmPasswordVisible) {
                                                R.string.auth_hide_password
                                            } else {
                                                R.string.auth_show_password
                                            }
                                        )
                                    )
                                }
                            }
                        )
                    }
                }

                val error = uiState.error
                AnimatedVisibility(
                    visible = error != null,
                    enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(160)) + shrinkVertically(tween(160))
                ) {
                    if (error != null) {
                        AuthBanner(
                            message = error.message(),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                val info = uiState.info
                AnimatedVisibility(
                    visible = info != null,
                    enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(160)) + shrinkVertically(tween(160))
                ) {
                    if (info != null) {
                        AuthBanner(
                            message = info.message(),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = stringResource(
                        if (uiState.mode == AuthMode.SignIn) {
                            R.string.auth_action_sign_in
                        } else {
                            R.string.auth_action_sign_up
                        }
                    ),
                    onClick = onSubmit,
                    enabled = !uiState.isSubmitting,
                    loading = uiState.isSubmitting
                )

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

                AppButton(
                    text = stringResource(R.string.auth_continue_as_guest),
                    onClick = onGuest,
                    enabled = !uiState.isSubmitting,
                    outlined = true,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

/** The two-way sign in / sign up switch, styled as a segmented control. */
@Composable
private fun ModeSelector(
    mode: AuthMode,
    onModeChange: (AuthMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            ModeButton(
                text = stringResource(R.string.auth_mode_sign_in),
                selected = mode == AuthMode.SignIn,
                onClick = { onModeChange(AuthMode.SignIn) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(4.dp))
            ModeButton(
                text = stringResource(R.string.auth_mode_sign_up),
                selected = mode == AuthMode.SignUp,
                onClick = { onModeChange(AuthMode.SignUp) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(220),
        label = "modeContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(220),
        label = "modeContent"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = container,
        modifier = modifier
            .semantics { role = Role.Tab }
            .clickable(onClick = onClick, enabled = !selected)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}

/** Inline notice (error or confirmation) with a tinted container and hairline border. */
@Composable
private fun AuthBanner(
    message: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun AuthError.message(): String = stringResource(
    when (this) {
        AuthError.InvalidEmail -> R.string.auth_error_invalid_email
        AuthError.PasswordTooShort -> R.string.auth_error_password_short
        AuthError.PasswordMismatch -> R.string.auth_error_password_mismatch
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
