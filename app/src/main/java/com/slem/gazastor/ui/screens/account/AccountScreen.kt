package com.slem.gazastor.ui.screens.account

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.repository.SettingsRepository
import com.slem.gazastor.ui.components.AppButton
import com.slem.gazastor.ui.components.AppTextField
import com.slem.gazastor.ui.components.BrandHeader
import com.slem.gazastor.ui.components.PillBadge
import com.slem.gazastor.ui.components.SkeletonLine
import com.slem.gazastor.ui.components.pressable
import com.slem.gazastor.ui.theme.Dimens
import com.slem.gazastor.ui.theme.LatinFontFamily
import com.slem.gazastor.ui.util.openWhatsApp
import com.slem.gazastor.ui.util.resolveWhatsAppNumber
import kotlinx.coroutines.launch

private const val ADMIN_ROLE = "admin"

/**
 * Signed out: the shared auth form (guests can browse from here too).
 * Signed in: the account hub - profile summary, live counts, shortcuts, support
 * and sign out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onGuestClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: AccountViewModel = viewModel(),
    settingsRepository: SettingsRepository = AppModule.settingsRepository
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    // Resolved during composition so the coroutine below only contains the
    // suspend hand-over, and never a call that can re-key an effect.
    val signOutFailedMessage = stringResource(R.string.account_sign_out_failed)
    val whatsAppUnavailableMessage = stringResource(R.string.account_whatsapp_not_available)
    val supportMessage = stringResource(R.string.account_support_message)
    val profileSavedMessage = stringResource(R.string.account_edit_saved)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AccountEvent.SignOutFailed ->
                    snackbarHostState.showSnackbar(signOutFailedMessage)
                AccountEvent.ProfileSaved ->
                    snackbarHostState.showSnackbar(profileSavedMessage)
            }
        }
    }

    // A tab destination's lifecycle reaches RESUMED whenever the Account tab
    // becomes current again, whether navigation restores the entry or composes
    // it fresh. Keying on ON_RESUME (not on a one-shot LaunchedEffect with a
    // stable key) is what refreshes the orders stat after a new order. The
    // signed-in key also re-runs the effect when a session appears while the
    // tab stays visible; refreshOrderCount() ignores overlapping requests.
    LifecycleResumeEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) viewModel.refreshOrderCount()
        onPauseOrDispose { }
    }

    // While editing, the system back gesture cancels the form instead of
    // leaving the tab.
    BackHandler(enabled = uiState.editForm != null) {
        viewModel.cancelEdit()
    }

    val onSupportClick: () -> Unit = {
        scope.launch {
            val number = resolveWhatsAppNumber(
                settingsRepository.getWhatsAppNumber().getOrNull()
            )
            val opened = number != null && openWhatsApp(context, number, supportMessage)
            if (!opened) {
                snackbarHostState.showSnackbar(whatsAppUnavailableMessage)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The TopAppBar carries the status inset itself and the parent's
        // bottom bar owns the bottom edge — no system insets here, or they'd
        // pad the content twice.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_account)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        val editForm = uiState.editForm
        when {
            editForm != null -> EditProfileContent(
                form = editForm,
                onNameChange = viewModel::updateEditName,
                onPhoneChange = viewModel::updateEditPhone,
                onSave = viewModel::saveProfile,
                onCancel = viewModel::cancelEdit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            uiState.isSignedIn -> ProfileContent(
                uiState = uiState,
                onOrdersClick = onOrdersClick,
                onFavoritesClick = onFavoritesClick,
                onCartClick = onCartClick,
                onSupportClick = onSupportClick,
                onRetryProfile = viewModel::retryProfile,
                onEditProfile = viewModel::beginEditProfile,
                onSignOut = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            else -> GuestContent(
                onSignInClick = onGuestClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(stringResource(R.string.account_sign_out_title)) },
            text = { Text(stringResource(R.string.account_sign_out_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.account_sign_out_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.account_sign_out_cancel))
                }
            }
        )
    }
}

@Composable
private fun GuestContent(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // The message lives in the card only, so the screen does not say the
        // same thing twice.
        BrandHeader(
            compact = true,
            title = stringResource(R.string.account_welcome_guest)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.account_guest_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = stringResource(R.string.account_sign_in_register),
                    onClick = onSignInClick
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * The signed-in hub: hero card with the profile summary and live counts, the
 * shortcut/support menu, and the sign-out action.
 */
@Composable
internal fun ProfileContent(
    uiState: AccountUiState,
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCartClick: () -> Unit,
    onSupportClick: () -> Unit,
    onRetryProfile: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        ProfileHero(
            uiState = uiState,
            onOrdersClick = onOrdersClick,
            onFavoritesClick = onFavoritesClick,
            onCartClick = onCartClick,
            onRetryProfile = onRetryProfile,
            onEditProfile = onEditProfile
        )

        Spacer(modifier = Modifier.height(20.dp))

        AccountMenu(
            onOrdersClick = onOrdersClick,
            onFavoritesClick = onFavoritesClick,
            onCartClick = onCartClick,
            onSupportClick = onSupportClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        AppButton(
            text = stringResource(R.string.account_sign_out),
            onClick = onSignOut,
            enabled = !uiState.isSigningOut,
            loading = uiState.isSigningOut,
            outlined = true
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * The inline edit form. It replaces the hub only while editing, so the app
 * keeps a single Account destination and view model. The form lives inside the
 * same scrollable column as the rest of the tab, which is what makes
 * adjustResize + imePadding work exactly as they do on the auth form.
 */
@Composable
internal fun EditProfileContent(
    form: EditProfileForm,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // The form only appears after an explicit tap, so focusing the name and
        // opening the keyboard right away is what the user expects.
        nameFocusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.AccountEditCardPadding)
            ) {
                Text(
                    text = stringResource(R.string.account_edit_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = form.fullName,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.account_edit_name),
                    enabled = !form.isSaving,
                    errorText = if (form.nameError) {
                        stringResource(R.string.account_edit_name_error)
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester)
                )

                Spacer(modifier = Modifier.height(Dimens.AccountEditFieldGap))

                AppTextField(
                    value = form.phone,
                    onValueChange = onPhoneChange,
                    label = stringResource(R.string.account_edit_phone),
                    enabled = !form.isSaving,
                    errorText = if (form.phoneError) {
                        stringResource(R.string.account_edit_phone_error)
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (form.saveFailed) {
                    Spacer(modifier = Modifier.height(Dimens.AccountEditFieldGap))
                    Text(
                        text = stringResource(R.string.account_edit_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AppButton(
                    text = stringResource(R.string.account_edit_save),
                    onClick = onSave,
                    loading = form.isSaving,
                    enabled = !form.isSaving
                )

                Spacer(modifier = Modifier.height(10.dp))

                AppButton(
                    text = stringResource(R.string.account_edit_cancel),
                    onClick = onCancel,
                    outlined = true,
                    enabled = !form.isSaving
                )
            }
        }
    }
}

@Composable
private fun ProfileHero(
    uiState: AccountUiState,
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCartClick: () -> Unit,
    onRetryProfile: () -> Unit,
    onEditProfile: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                ProfileAvatar(uiState = uiState)

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (uiState.isProfileLoading && uiState.displayName == null) {
                        SkeletonLine(
                            width = Dimens.AccountSkeletonNameWidth,
                            height = 18.dp
                        )
                    } else {
                        Text(
                            text = uiState.displayName
                                ?: stringResource(R.string.account_greeting),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    uiState.email?.let { email ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = LatinFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    ProfileMetaLine(uiState = uiState)

                    if (uiState.isProfileError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.account_profile_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = onRetryProfile,
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 0.dp
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.action_retry),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Admin badge and the edit action stack at the end of the row;
                // the edit link only makes sense once the profile row is known.
                Column(horizontalAlignment = Alignment.End) {
                    if (uiState.role == ADMIN_ROLE) {
                        PillBadge(
                            text = stringResource(R.string.account_role_admin),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (!uiState.isProfileLoading && !uiState.isProfileError) {
                        TextButton(
                            onClick = onEditProfile,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.account_edit_profile),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            StatsStrip(
                uiState = uiState,
                onOrdersClick = onOrdersClick,
                onFavoritesClick = onFavoritesClick,
                onCartClick = onCartClick
            )
        }
    }
}

@Composable
private fun ProfileAvatar(uiState: AccountUiState) {
    val description = uiState.displayName
        ?: uiState.email
        ?: stringResource(R.string.account_greeting)

    Box(
        modifier = Modifier
            .size(Dimens.AccountAvatarSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(Dimens.Border, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = accountInitials(uiState.displayName, uiState.email),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/** Phone and "عضو منذ" line; each segment is omitted when it is unknown. */
@Composable
private fun ProfileMetaLine(uiState: AccountUiState) {
    val phone = uiState.phone
    val memberSince = uiState.memberSince
    if (phone == null && memberSince.isEmpty()) return

    Spacer(modifier = Modifier.height(4.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        phone?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = LatinFontFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            if (memberSince.isNotEmpty()) {
                Text(
                    text = " · ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (memberSince.isNotEmpty()) {
            Text(
                text = stringResource(R.string.account_member_since, memberSince),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatsStrip(
    uiState: AccountUiState,
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatCell(
            value = uiState.ordersCount?.toString() ?: "—",
            label = stringResource(R.string.account_stat_orders),
            onClick = onOrdersClick,
            modifier = Modifier.weight(1f)
        )
        StatDivider()
        StatCell(
            value = uiState.favoritesCount.toString(),
            label = stringResource(R.string.account_stat_favorites),
            onClick = onFavoritesClick,
            modifier = Modifier.weight(1f)
        )
        StatDivider()
        StatCell(
            value = uiState.cartCount.toString(),
            label = stringResource(R.string.account_stat_cart),
            onClick = onCartClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCell(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = Dimens.AccountStatMinHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label: $value"
            }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = LatinFontFamily),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(Dimens.Border)
            .height(Dimens.AccountStatDividerHeight)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun AccountMenu(
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCartClick: () -> Unit,
    onSupportClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            MenuRow(
                label = stringResource(R.string.tab_orders),
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.List,
                        contentDescription = null
                    )
                },
                onClick = onOrdersClick
            )
            MenuDivider()
            MenuRow(
                label = stringResource(R.string.tab_favorites),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null
                    )
                },
                onClick = onFavoritesClick
            )
            MenuDivider()
            MenuRow(
                label = stringResource(R.string.tab_cart),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null
                    )
                },
                onClick = onCartClick
            )
            MenuDivider()
            MenuRow(
                label = stringResource(R.string.account_support_whatsapp),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_payment_whatsapp),
                        contentDescription = null
                    )
                },
                onClick = onSupportClick
            )
        }
    }
}

@Composable
private fun MenuRow(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.AccountMenuRowMinHeight)
            .pressable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.AccountMenuIconDiscSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(Dimens.AccountMenuIconSize),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.primary
                ) {
                    icon()
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/**
 * Avatar text: the first letters of the first two name words, or the email's
 * first letter, or a neutral dot when neither exists.
 */
internal fun accountInitials(displayName: String?, email: String?): String {
    val source = displayName?.trim()?.takeIf { it.isNotEmpty() }
        ?: email?.substringBefore('@')?.trim()
        ?: ""

    val words = source.split(Regex("\\s+")).filter { it.isNotEmpty() }

    return when {
        words.size >= 2 -> words.take(2).mapNotNull { it.firstOrNull() }.joinToString("")
        words.size == 1 -> words.first().take(1)
        else -> "•"
    }.uppercase()
}
