package com.example.data.auth

import android.content.Context
import com.example.core.security.KeystoreManager
import com.example.domain.model.UserSession
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DemoAuthRepository(
    private val context: Context,
    private val keystoreManager: KeystoreManager
) : AuthRepository {

    private val _currentUser = MutableStateFlow<UserSession?>(
        UserSession(
            name = "Mara Dev",
            email = "mara@usafe.in",
            plan = "free",
            tokensUsed = 18400L,
            vaultUsedGb = 3.2
        )
    )
    override val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): AuthResult {
        if (!email.contains("@") || !email.contains(".")) {
            return AuthResult(isSuccess = false, errorMessage = "Enter a valid email")
        }
        if (password.length < 6) {
            return AuthResult(isSuccess = false, errorMessage = "Password needs 6+ characters")
        }

        // Securely store credentials in Android KeyStore
        keystoreManager.encryptAndStore("auth_token_$email", "session_token_${System.currentTimeMillis()}")

        val name = if (email.startsWith("mara")) "Mara Dev" else email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val session = UserSession(
            name = name,
            email = email,
            plan = "free",
            tokensUsed = 18400L,
            vaultUsedGb = 3.2
        )
        _currentUser.value = session
        return AuthResult(isSuccess = true, user = session)
    }

    override suspend fun signup(name: String, email: String, password: String): AuthResult {
        if (name.isBlank()) {
            return AuthResult(isSuccess = false, errorMessage = "Enter your name")
        }
        if (!email.contains("@") || !email.contains(".")) {
            return AuthResult(isSuccess = false, errorMessage = "Enter a valid email")
        }
        if (password.length < 8) {
            return AuthResult(isSuccess = false, errorMessage = "Password needs 8+ characters")
        }

        keystoreManager.encryptAndStore("auth_token_$email", "session_token_${System.currentTimeMillis()}")

        val session = UserSession(
            name = name,
            email = email,
            plan = "free",
            tokensUsed = 0L,
            vaultUsedGb = 0.1
        )
        _currentUser.value = session
        return AuthResult(isSuccess = true, user = session)
    }

    override suspend fun updatePlan(plan: String) {
        _currentUser.update { it?.copy(plan = plan) }
    }

    override suspend fun consumeTokens(count: Long) {
        _currentUser.update { current ->
            current?.copy(tokensUsed = current.tokensUsed + count)
        }
    }

    override suspend fun updateVaultUsed(gb: Double) {
        _currentUser.update { current ->
            current?.copy(vaultUsedGb = (current.vaultUsedGb + gb).coerceAtLeast(0.0))
        }
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
