package com.institute.calling.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the current JWT for the session.
 *
 * NOTE: in-memory only for now — the token is lost when the app is killed, so the
 * user logs in again on next launch. Move to EncryptedSharedPreferences (encrypted
 * local storage) when we harden this.
 */
@Singleton
class TokenStore @Inject constructor() {
    @Volatile
    var token: String? = null
        private set

    fun set(value: String?) {
        token = value
    }

    fun clear() {
        token = null
    }
}

/** Adds `Authorization: Bearer <token>` to every request when a token is present. */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.token
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
