package com.slem.gazastor.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.repository.AuthRepository
import com.slem.gazastor.data.repository.CartRepository
import com.slem.gazastor.data.repository.FavoritesRepository
import com.slem.gazastor.data.repository.OrderRepository
import com.slem.gazastor.ui.model.formatMemberSince
import com.slem.gazastor.ui.model.isValidPhone
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The editable name/phone form, non-null only while the account is in edit mode. */
data class EditProfileForm(
    val fullName: String = "",
    val phone: String = "",
    val nameError: Boolean = false,
    val phoneError: Boolean = false,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false
)

data class AccountUiState(
    val isSignedIn: Boolean = false,
    val email: String? = null,
    val displayName: String? = null,
    val phone: String? = null,
    /** Already formatted as `yyyy/MM`; empty when unknown or invalid. */
    val memberSince: String = "",
    val role: String? = null,
    val isProfileLoading: Boolean = false,
    val isProfileError: Boolean = false,
    /** `null` while unknown or after a failed load, rendered as "—". */
    val ordersCount: Int? = null,
    val favoritesCount: Int = 0,
    val cartCount: Int = 0,
    val isSigningOut: Boolean = false,
    /** Non-null while the inline edit form replaces the hub. */
    val editForm: EditProfileForm? = null
)

/** One-shot things that must not be replayed when the screen recomposes. */
sealed interface AccountEvent {
    data object SignOutFailed : AccountEvent
    data object ProfileSaved : AccountEvent
}

/**
 * Mirrors the auth session, loads the matching `profiles` row and exposes the
 * live favorites/cart counts plus the (one-shot) order count for the account
 * hub. Guests keep the same state as before: signed out, no profile.
 */
class AccountViewModel(
    private val authRepository: AuthRepository = AppModule.authRepository,
    favoritesRepository: FavoritesRepository = AppModule.favoritesRepository,
    cartRepository: CartRepository = AppModule.cartRepository,
    private val orderRepository: OrderRepository = AppModule.orderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _events = Channel<AccountEvent>(Channel.BUFFERED)
    val events: Flow<AccountEvent> = _events.receiveAsFlow()

    private var orderCountJob: Job? = null

    init {
        viewModelScope.launch {
            favoritesRepository.favorites.collect { favorites ->
                _uiState.update { it.copy(favoritesCount = favorites.size) }
            }
        }

        viewModelScope.launch {
            cartRepository.lines.collect { lines ->
                _uiState.update { state -> state.copy(cartCount = lines.sumOf { it.quantity }) }
            }
        }

        viewModelScope.launch {
            authRepository.sessionState.collect { user ->
                if (user == null) {
                    signedOutState()
                } else {
                    _uiState.update { it.copy(isSignedIn = true, email = user.email) }
                    loadProfile()
                }
            }
        }
    }

    /** Retries the profile row after a failed load; safe to call repeatedly. */
    fun retryProfile() = loadProfile()

    /**
     * Fetches the order count. The screen calls this on every resume of the
     * Account tab (not just on first composition), so placing an order and
     * coming back updates the strip. An in-flight request is never duplicated.
     */
    fun refreshOrderCount() {
        if (!_uiState.value.isSignedIn) return
        if (orderCountJob?.isActive == true) return

        orderCountJob = viewModelScope.launch {
            orderRepository.getOrders().fold(
                onSuccess = { orders ->
                    if (!_uiState.value.isSignedIn) return@fold
                    _uiState.update { it.copy(ordersCount = orders.size) }
                },
                onFailure = {
                    if (!_uiState.value.isSignedIn) return@fold
                    // Leave it unknown so the stats strip shows "—" instead of
                    // a misleading zero or a blocking error.
                    _uiState.update { it.copy(ordersCount = null) }
                }
            )
        }
    }

    /** Opens the inline edit form, seeded from the (read-only) profile. */
    fun beginEditProfile() {
        val state = _uiState.value
        if (!state.isSignedIn || state.isProfileLoading || state.isProfileError) return
        if (state.editForm != null) return

        _uiState.update {
            it.copy(
                editForm = EditProfileForm(
                    fullName = it.displayName.orEmpty(),
                    phone = it.phone.orEmpty()
                )
            )
        }
    }

    fun updateEditName(value: String) = updateEditForm {
        it.copy(fullName = value, nameError = false, saveFailed = false)
    }

    fun updateEditPhone(value: String) = updateEditForm {
        it.copy(phone = value, phoneError = false, saveFailed = false)
    }

    fun cancelEdit() {
        if (_uiState.value.editForm?.isSaving == true) return
        _uiState.update { it.copy(editForm = null) }
    }

    /**
     * Validates and persists the form. The name is required; the phone may be
     * cleared, in which case the repository writes SQL NULL. Only a successful
     * save closes the form, so a failure keeps the typed values for a retry.
     */
    fun saveProfile() {
        val form = _uiState.value.editForm ?: return
        if (form.isSaving) return

        val name = form.fullName.trim()
        val phone = form.phone.trim()
        val nameError = name.isEmpty()
        val phoneError = phone.isNotEmpty() && !isValidPhone(phone)

        if (nameError || phoneError) {
            _uiState.update {
                it.copy(
                    editForm = form.copy(
                        nameError = nameError,
                        phoneError = phoneError,
                        saveFailed = false
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                editForm = form.copy(
                    nameError = false,
                    phoneError = false,
                    isSaving = true,
                    saveFailed = false
                )
            )
        }

        viewModelScope.launch {
            val phoneOrNull = phone.ifEmpty { null }
            authRepository.updateProfile(fullName = name, phone = phoneOrNull).fold(
                onSuccess = {
                    if (!_uiState.value.isSignedIn) return@fold
                    _uiState.update {
                        it.copy(
                            displayName = name,
                            phone = phoneOrNull,
                            editForm = null
                        )
                    }
                    _events.send(AccountEvent.ProfileSaved)
                },
                onFailure = {
                    _uiState.update { state ->
                        val current = state.editForm ?: return@update state
                        state.copy(editForm = current.copy(isSaving = false, saveFailed = true))
                    }
                }
            )
        }
    }

    fun signOut() {
        if (_uiState.value.isSigningOut) return
        _uiState.update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            val result = authRepository.signOut()
            if (result.isFailure) {
                // A failed sign-out keeps the user signed in; tell the screen so
                // it can explain why nothing changed.
                _events.send(AccountEvent.SignOutFailed)
            }
            _uiState.update { it.copy(isSigningOut = false) }
        }
    }

    /**
     * Resets the session-specific state while keeping the local favorites/cart
     * counts, which belong to the device rather than to the signed-in account.
     */
    private fun signedOutState() {
        orderCountJob?.cancel()
        orderCountJob = null
        _uiState.update { state ->
            AccountUiState(
                isSignedIn = false,
                favoritesCount = state.favoritesCount,
                cartCount = state.cartCount
            )
        }
    }

    private fun updateEditForm(transform: (EditProfileForm) -> EditProfileForm) {
        val state = _uiState.value
        val form = state.editForm ?: return
        if (form.isSaving) return
        _uiState.value = state.copy(editForm = transform(form))
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true, isProfileError = false) }
            authRepository.profile().fold(
                onSuccess = { profile ->
                    // A response that lands after sign-out must not repopulate
                    // the guest screen with the previous account's details.
                    if (!_uiState.value.isSignedIn) return@fold
                    _uiState.update {
                        it.copy(
                            displayName = profile?.full_name?.takeIf { name -> name.isNotBlank() },
                            phone = profile?.phone?.takeIf { phone -> phone.isNotBlank() },
                            role = profile?.role,
                            memberSince = formatMemberSince(profile?.created_at),
                            isProfileLoading = false,
                            isProfileError = false
                        )
                    }
                },
                onFailure = {
                    if (!_uiState.value.isSignedIn) return@fold
                    // Keep the email we already have from the session; the hero
                    // shows an inline retry instead of an empty card.
                    _uiState.update { it.copy(isProfileLoading = false, isProfileError = true) }
                }
            )
        }
    }
}
