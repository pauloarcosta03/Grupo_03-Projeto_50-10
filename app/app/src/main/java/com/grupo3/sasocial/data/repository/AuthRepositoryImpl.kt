package com.grupo3.sasocial.data.repository

import com.grupo3.sasocial.data.remote.AuthDataSource
import com.grupo3.sasocial.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource
) : AuthRepository {
    
    override suspend fun login(email: String, password: String): Result<Unit> {
        return authDataSource.login(email, password)
    }
    
    override suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        return authDataSource.resetPassword(email, newPassword)
    }
    
    override suspend fun logout(): Result<Unit> {
        return authDataSource.logout()
    }
    
    override fun getCurrentUserId(): String? {
        return authDataSource.getCurrentUserId()
    }
    
    override fun getCurrentUserEmail(): Flow<String> {
        return authDataSource.getCurrentUserEmail()
    }
    
    override fun isUserLoggedIn(): Boolean {
        return authDataSource.isUserLoggedIn()
    }
}
