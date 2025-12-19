package com.grupo3.sasocial.domain.usecase

import com.grupo3.sasocial.domain.repository.AuthRepository

class IsUserLoggedInUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return repository.isUserLoggedIn()
    }
}
