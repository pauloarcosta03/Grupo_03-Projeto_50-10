package com.grupo3.sasocial.domain.usecase

import com.grupo3.sasocial.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Por favor, preencha todos os campos"))
        }
        
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("A password deve ter pelo menos 6 caracteres"))
        }
        
        return repository.login(email, password)
    }
}
