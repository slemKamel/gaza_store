package com.slem.gazastor.core.di

import android.content.Context
import com.slem.gazastor.data.preferences.UserPreferences
import com.slem.gazastor.data.remote.supabase.SupabaseClientProvider
import com.slem.gazastor.data.repository.AuthRepository
import com.slem.gazastor.data.repository.AuthRepositoryImpl
import com.slem.gazastor.data.repository.CartRepository
import com.slem.gazastor.data.repository.ContentRepository
import com.slem.gazastor.data.repository.ContentRepositoryImpl
import com.slem.gazastor.data.repository.DataStoreCartRepository
import com.slem.gazastor.data.repository.DataStoreFavoritesRepository
import com.slem.gazastor.data.repository.FavoritesRepository
import com.slem.gazastor.data.repository.OrderRepository
import com.slem.gazastor.data.repository.OrderRepositoryImpl
import io.github.jan.supabase.SupabaseClient

/**
 * Minimal manual dependency container.
 *
 * Repositories are singletons so that state which must be shared across screens
 * (auth session, cart) has a single owner. ViewModels take these as default
 * constructor arguments, which keeps `viewModel()` working while still allowing
 * a fake to be injected in tests.
 */
object AppModule {

    private lateinit var applicationContext: Context

    /** Called once from [com.slem.gazastor.GazaStorApp] so local storage has a Context. */
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    val supabaseClient: SupabaseClient by lazy { SupabaseClientProvider.client }

    val contentRepository: ContentRepository by lazy { ContentRepositoryImpl(supabaseClient) }

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(supabaseClient) }

    val cartRepository: CartRepository by lazy { DataStoreCartRepository(applicationContext) }

    val favoritesRepository: FavoritesRepository by lazy { DataStoreFavoritesRepository(applicationContext) }

    val orderRepository: OrderRepository by lazy { OrderRepositoryImpl(supabaseClient) }

    val userPreferences: UserPreferences by lazy { UserPreferences(applicationContext) }
}
