package com.milkcocoa.info.authentocator

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.milkcocoa.info.authentocator.FirebaseAuthProvider.Companion.DEFAULT_PROVIDER_NAME
import com.milkcocoa.info.authentocator.FirebaseAuthProvider.Companion.DecodedTokenKey
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.ApplicationCallPredicate
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.util.AttributeKey

class FirebaseAuthProvider(config: FirebaseAuthProviderConfig) : AuthenticationProvider(config) {
    companion object {
        public const val DEFAULT_PROVIDER_NAME = "FIREBASE_AUTH"
        internal val DecodedTokenKey: AttributeKey<FirebaseToken> =
            AttributeKey("FirebaseAuthenticationDecodedTokenKey")
    }

    sealed class AuthorizationType(val authPhrase: String) {
        fun getIdToken(call: ApplicationCall): String {
            val headerValue = call.request.headers.get(HttpHeaders.Authorization) ?: throw TokenNotProvidedException()
            return headerValue.trim().split(" ", limit = 2).takeIf { it.count() == 2 }?.let { authorization ->
                val (phrase, token) = authorization.let { it.getOrNull(0) to it.getOrNull(1) }
                phrase?.equals(authPhrase, ignoreCase = true)?.takeIf { it }?.let { token?.trim() }
                    ?: throw ClaimNotProvidedException()
            } ?: run {
                // 単一要素
                throw ClaimNotProvidedException()
            }
        }

        object Bearer : AuthorizationType("Bearer")
        object Jwt : AuthorizationType("JWT")
    }

    class FirebaseAuthProviderConfig(name: String?) : AuthenticationProvider.Config(name) {

        /**
         * firebase instance
         */
        var firebaseApp: FirebaseApp? = null

        /**
         * authentication schema
         */
        var authorizationType: AuthorizationType = AuthorizationType.Jwt

        /**
         * Check user's availability.
         * If true, authentication is failed when user is disabled, otherwise allow disabled user.
         */
        var checkRevoked = false

        /**
         * skip authentication if someone's condition becomes true
         */
        var skipWhen: List<ApplicationCallPredicate>? = null

        /**
         * handler which called on reject
         */
        var whenReject: (suspend (ApplicationCall, Throwable) -> Unit)? = null
    }

    /**
     * Firebase token is not provided
     */
    class TokenNotProvidedException : Exception()

    /**
     * Malformed 'Authorization' header. (ex. 'Authorization: xxxxxxxx.yyyyyyyy.zzzzzzzzzzzz' has no 'JWT' or 'Bearer' keyword.)
     */
    class ClaimNotProvidedException : Exception()

    private val auth = FirebaseAuth.getInstance(config.firebaseApp ?: FirebaseApp.getInstance())
    private val authorizationType = config.authorizationType
    private val checkRevoked = config.checkRevoked
    private val whenReject = config.whenReject

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        with(context.call) {
            kotlin.runCatching {
                val idToken = authorizationType.getIdToken(this)
                if (idToken.isBlank()) {
                    throw TokenNotProvidedException()
                }
                val decodedToken = auth.verifyIdToken(idToken, checkRevoked)
                attributes.put(DecodedTokenKey, decodedToken)
            }.onFailure {
                whenReject?.invoke(this, it)
            }
        }
    }
}

/**
 * get verified firebase token
 */
fun ApplicationCall.getDecodedToken(): FirebaseToken? =
    attributes.getOrNull(DecodedTokenKey).also { Result }

fun AuthenticationConfig.firebase(
    name: String? = DEFAULT_PROVIDER_NAME,
    configure: FirebaseAuthProvider.FirebaseAuthProviderConfig.() -> Unit,
) {
    val provider = FirebaseAuthProvider(
        FirebaseAuthProvider.FirebaseAuthProviderConfig(name).apply(configure)
    )
    register(provider)
}
