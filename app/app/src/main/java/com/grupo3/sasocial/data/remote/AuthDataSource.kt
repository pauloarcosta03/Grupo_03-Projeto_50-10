package com.grupo3.sasocial.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthDataSource(
    private val firebaseAuth: FirebaseAuth
) {
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    fun getCurrentUserEmail(): Flow<String> {
        val stateFlow = MutableStateFlow(firebaseAuth.currentUser?.email ?: "")
        
        firebaseAuth.addAuthStateListener { auth ->
            stateFlow.value = auth.currentUser?.email ?: ""
        }
        
        return stateFlow.asStateFlow()
    }
    
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(@Suppress("UNUSED_PARAMETER") email: String, newPassword: String): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun generateTemporaryPassword(): String {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%&*"
        val allChars = uppercase + lowercase + numbers + symbols
        
        val password = StringBuilder()
        
        // Garantir pelo menos um de cada tipo
        password.append(uppercase.random())
        password.append(lowercase.random())
        password.append(numbers.random())
        password.append(symbols.random())
        
        // Adicionar caracteres aleatórios até ter 12 caracteres
        repeat(8) {
            password.append(allChars.random())
        }
        
        // Embaralhar a palavra-passe
        return password.toString().toCharArray().apply { shuffle() }.concatToString()
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
