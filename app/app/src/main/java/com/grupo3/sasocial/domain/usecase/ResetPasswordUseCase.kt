package com.grupo3.sasocial.domain.usecase

import com.grupo3.sasocial.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, newPassword: String): Result<Unit> {
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("A password deve ter pelo menos 6 caracteres"))
        }
        return repository.resetPassword(email, newPassword)
    }
}
