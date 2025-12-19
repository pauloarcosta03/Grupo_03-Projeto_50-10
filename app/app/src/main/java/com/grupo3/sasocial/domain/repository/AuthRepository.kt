package com.grupo3.sasocial.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun resetPassword(email: String, newPassword: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): Flow<String>
    fun isUserLoggedIn(): Boolean
}
