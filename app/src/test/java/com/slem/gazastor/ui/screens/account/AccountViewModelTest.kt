package com.slem.gazastor.ui.screens.account

import com.slem.gazastor.data.model.AuthUser
import com.slem.gazastor.data.model.CartLine
import com.slem.gazastor.data.model.FavoriteItem
import com.slem.gazastor.data.model.OrderDto
import com.slem.gazastor.data.model.ProfileDto
import com.slem.gazastor.testing.FakeAuthRepository
import com.slem.gazastor.testing.FakeCartRepository
import com.slem.gazastor.testing.FakeFavoritesRepository
import com.slem.gazastor.testing.FakeOrderRepository
import com.slem.gazastor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun signedInUser() =
        FakeAuthRepository(AuthUser(id = "user-1", email = "a@b.com"))

    private fun favorite(itemId: String) = FavoriteItem(
        itemId = itemId,
        title = "منتج",
        price = "10",
        priceValue = 10.0,
        imageUrl = ""
    )

    private fun cartLine(itemId: String, quantity: Int) = CartLine(
        itemId = itemId,
        title = "منتج",
        unitPrice = 10.0,
        imageUrl = "",
        quantity = quantity
    )

    @Test
    fun guest_staysSignedOut() = runTest {
        val viewModel = AccountViewModel(
            authRepository = FakeAuthRepository(),
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSignedIn)
        assertNull(state.displayName)
        assertNull(state.email)
    }

    @Test
    fun signedIn_mapsProfileAndMemberSince() = runTest {
        val auth = signedInUser().apply {
            profileResult = Result.success(
                ProfileDto(
                    full_name = "محمد أحمد",
                    phone = "0599123456",
                    role = "admin",
                    created_at = "2026-03-12T10:00:00+00:00"
                )
            )
        }
        val viewModel = AccountViewModel(
            authRepository = auth,
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSignedIn)
        assertEquals("محمد أحمد", state.displayName)
        assertEquals("0599123456", state.phone)
        assertEquals("admin", state.role)
        assertEquals("2026/03", state.memberSince)
        assertFalse(state.isProfileLoading)
        assertFalse(state.isProfileError)
    }

    @Test
    fun signedIn_withBlankAndInvalidProfileValues_hidesThemSafely() = runTest {
        val auth = signedInUser().apply {
            profileResult = Result.success(
                ProfileDto(
                    full_name = "   ",
                    phone = "",
                    role = "customer",
                    created_at = "not-a-date"
                )
            )
        }
        val viewModel = AccountViewModel(
            authRepository = auth,
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.displayName)
        assertNull(state.phone)
        assertEquals("", state.memberSince)
        assertEquals("customer", state.role)
    }

    @Test
    fun profileFailure_setsErrorAndRetryRecovers() = runTest {
        val auth = signedInUser().apply {
            profileResult = Result.failure(IllegalStateException("boom"))
        }
        val viewModel = AccountViewModel(
            authRepository = auth,
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isProfileError)
        assertFalse(viewModel.uiState.value.isProfileLoading)
        assertEquals("a@b.com", viewModel.uiState.value.email)

        auth.profileResult = Result.success(
            ProfileDto(full_name = "سارة", created_at = "2025-01-02T00:00:00+00:00")
        )
        viewModel.retryProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProfileError)
        assertEquals("سارة", state.displayName)
        assertEquals("2025/01", state.memberSince)
    }

    @Test
    fun favoriteAndCartCountsFollowTheLocalFlows() = runTest {
        val favorites = FakeFavoritesRepository(listOf(favorite("a")))
        val cart = FakeCartRepository(listOf(cartLine("a", 2), cartLine("b", 3)))
        val viewModel = AccountViewModel(
            authRepository = signedInUser(),
            favoritesRepository = favorites,
            cartRepository = cart,
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.favoritesCount)
        assertEquals(5, viewModel.uiState.value.cartCount)

        favorites.toggle(favorite("b"))
        cart.increment("a", 1)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.favoritesCount)
        assertEquals(6, viewModel.uiState.value.cartCount)
    }

    @Test
    fun ordersCountIsRequestedOnDemandAndRefreshesAfterANewOrder() = runTest {
        val orders = FakeOrderRepository().apply {
            ordersResult = Result.success(
                listOf(OrderDto(id = "1"), OrderDto(id = "2"))
            )
        }
        val viewModel = AccountViewModel(
            authRepository = signedInUser(),
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = orders
        )
        advanceUntilIdle()

        // The screen asks for the count when the tab resumes; the session
        // collector intentionally does not fetch it.
        assertEquals(0, orders.getOrdersCalls)
        assertNull(viewModel.uiState.value.ordersCount)

        viewModel.refreshOrderCount()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.ordersCount)
        assertEquals(1, orders.getOrdersCalls)

        orders.ordersResult = Result.success(
            listOf(OrderDto(id = "1"), OrderDto(id = "2"), OrderDto(id = "3"))
        )
        viewModel.refreshOrderCount()
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.ordersCount)
        assertEquals(2, orders.getOrdersCalls)
    }

    @Test
    fun ordersCountStaysUnknownOnFailure() = runTest {
        val failing = FakeOrderRepository().apply {
            ordersResult = Result.failure(IllegalStateException("boom"))
        }
        val viewModel = AccountViewModel(
            authRepository = signedInUser(),
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = failing
        )
        advanceUntilIdle()

        viewModel.refreshOrderCount()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.ordersCount)
    }

    @Test
    fun guestRefreshOrderCountDoesNothing() = runTest {
        val orders = FakeOrderRepository()
        val viewModel = AccountViewModel(
            authRepository = FakeAuthRepository(),
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = orders
        )
        advanceUntilIdle()

        viewModel.refreshOrderCount()
        advanceUntilIdle()
        assertEquals(0, orders.getOrdersCalls)
    }

    @Test
    fun beginEditProfileSeedsTheFormFromTheProfile() = runTest {
        val viewModel = signedInViewModel()
        advanceUntilIdle()

        viewModel.beginEditProfile()

        val form = viewModel.uiState.value.editForm
        assertEquals("محمد أحمد", form?.fullName)
        assertEquals("0599123456", form?.phone)
    }

    @Test
    fun guestCannotBeginEditing() = runTest {
        val auth = FakeAuthRepository()
        val viewModel = AccountViewModel(
            authRepository = auth,
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        viewModel.beginEditProfile()
        assertNull(viewModel.uiState.value.editForm)
    }

    @Test
    fun saveProfileRejectsABlankNameWithoutCallingTheRepository() = runTest {
        val auth = signedInAuth()
        val viewModel = signedInViewModel(auth)
        advanceUntilIdle()

        viewModel.beginEditProfile()
        viewModel.updateEditName("   ")
        viewModel.saveProfile()
        advanceUntilIdle()

        val form = viewModel.uiState.value.editForm
        assertTrue(form?.nameError == true)
        assertFalse(form?.isSaving == true)
        assertEquals(0, auth.updateProfileCalls)
    }

    @Test
    fun saveProfileRejectsAnInvalidPhoneWithoutCallingTheRepository() = runTest {
        val auth = signedInAuth()
        val viewModel = signedInViewModel(auth)
        advanceUntilIdle()

        viewModel.beginEditProfile()
        viewModel.updateEditPhone("123")
        viewModel.saveProfile()
        advanceUntilIdle()

        val form = viewModel.uiState.value.editForm
        assertTrue(form?.phoneError == true)
        assertEquals(0, auth.updateProfileCalls)
    }

    @Test
    fun saveProfileTrimsTheNameAndSendsNullForAClearedPhone() = runTest {
        val auth = signedInAuth()
        val viewModel = signedInViewModel(auth)
        advanceUntilIdle()

        val saved = async { viewModel.events.first() }
        viewModel.beginEditProfile()
        viewModel.updateEditName("  سارة علي  ")
        viewModel.updateEditPhone("   ")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals("سارة علي", auth.lastUpdatedName)
        assertNull(auth.lastUpdatedPhone)
        assertEquals(1, auth.updateProfileCalls)

        val state = viewModel.uiState.value
        assertNull(state.editForm)
        assertEquals("سارة علي", state.displayName)
        assertNull(state.phone)
        assertEquals(AccountEvent.ProfileSaved, saved.await())
    }

    @Test
    fun saveProfileFailureKeepsTheFormAndShowsTheInlineError() = runTest {
        val auth = signedInAuth().apply {
            updateProfileResult = Result.failure(IllegalStateException("boom"))
        }
        val viewModel = signedInViewModel(auth)
        advanceUntilIdle()

        viewModel.beginEditProfile()
        viewModel.updateEditName("سارة علي")
        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val form = state.editForm
        assertTrue(form?.saveFailed == true)
        assertFalse(form?.isSaving == true)
        assertEquals("سارة علي", form?.fullName)
        // The hub keeps the previously saved values until the retry succeeds.
        assertEquals("محمد أحمد", state.displayName)
        assertEquals(1, auth.updateProfileCalls)
    }

    @Test
    fun saveProfileIgnoresASecondTapWhileSaving() = runTest {
        val auth = signedInAuth()
        val viewModel = signedInViewModel(auth)
        advanceUntilIdle()

        viewModel.beginEditProfile()
        viewModel.saveProfile()
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(1, auth.updateProfileCalls)
    }

    @Test
    fun cancelEditDiscardsTheTypedValues() = runTest {
        val viewModel = signedInViewModel()
        advanceUntilIdle()

        viewModel.beginEditProfile()
        viewModel.updateEditName("اسم آخر")
        viewModel.cancelEdit()

        assertNull(viewModel.uiState.value.editForm)
        assertEquals("محمد أحمد", viewModel.uiState.value.displayName)
    }

    @Test
    fun signOutClosesAnyOpenEditForm() = runTest {
        val viewModel = signedInViewModel()
        advanceUntilIdle()

        viewModel.beginEditProfile()
        viewModel.signOut()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSignedIn)
        assertNull(viewModel.uiState.value.editForm)
    }

    /** A signed-in fake with a loaded profile, matching the edit tests. */
    private fun signedInAuth() = signedInUser().apply {
        profileResult = Result.success(
            ProfileDto(full_name = "محمد أحمد", phone = "0599123456")
        )
    }

    private fun signedInViewModel(
        auth: FakeAuthRepository = signedInAuth()
    ) = AccountViewModel(
        authRepository = auth,
        favoritesRepository = FakeFavoritesRepository(),
        cartRepository = FakeCartRepository(),
        orderRepository = FakeOrderRepository()
    )

    @Test
    fun signOut_success_returnsToTheGuestState() = runTest {
        val auth = signedInUser().apply {
            profileResult = Result.success(ProfileDto(full_name = "محمد"))
        }
        val viewModel = AccountViewModel(
            authRepository = auth,
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        viewModel.signOut()
        assertTrue(viewModel.uiState.value.isSigningOut)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSigningOut)
        assertFalse(viewModel.uiState.value.isSignedIn)
        assertNull(viewModel.uiState.value.displayName)
    }

    @Test
    fun signOut_keepsTheLocalCounts() = runTest {
        val viewModel = AccountViewModel(
            authRepository = signedInUser(),
            favoritesRepository = FakeFavoritesRepository(listOf(favorite("a"))),
            cartRepository = FakeCartRepository(listOf(cartLine("a", 2))),
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        viewModel.signOut()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSignedIn)
        assertEquals(1, viewModel.uiState.value.favoritesCount)
        assertEquals(2, viewModel.uiState.value.cartCount)
    }

    @Test
    fun signOutFailure_emitsTheFailureEvent() = runTest {
        val auth = signedInUser().apply {
            signOutResult = Result.failure(IllegalStateException("boom"))
        }
        val viewModel = AccountViewModel(
            authRepository = auth,
            favoritesRepository = FakeFavoritesRepository(),
            cartRepository = FakeCartRepository(),
            orderRepository = FakeOrderRepository()
        )
        advanceUntilIdle()

        val event = async { viewModel.events.first() }
        viewModel.signOut()
        advanceUntilIdle()

        assertEquals(AccountEvent.SignOutFailed, event.await())
    }
}
