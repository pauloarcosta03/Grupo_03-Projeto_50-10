package com.grupo3.sasocial.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val loginUseCase = AppModule.provideLoginUseCase(application)
    
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = loginUseCase(email, password)
            
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { exception ->
                    onError(exception.message ?: "Erro ao fazer login")
                }
            )
        }
    }
}
