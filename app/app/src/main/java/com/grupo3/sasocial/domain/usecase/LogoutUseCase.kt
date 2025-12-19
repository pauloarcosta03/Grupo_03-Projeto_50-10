package com.grupo3.sasocial.domain.usecase

import com.grupo3.sasocial.domain.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.logout()
    }
}
