package com.grupo3.sasocial.di

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.grupo3.sasocial.data.remote.AuthDataSource
import com.grupo3.sasocial.data.remote.FirestoreDataSource
import com.grupo3.sasocial.data.repository.*
import com.grupo3.sasocial.domain.repository.*
import com.grupo3.sasocial.domain.usecase.*

object AppModule {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
    // Data Sources - Singleton para manter listeners ativos
    private val authDataSource: AuthDataSource by lazy { AuthDataSource(firebaseAuth) }
    private val firestoreDataSource: FirestoreDataSource by lazy { FirestoreDataSource(firestore) }
    
    fun provideAuthDataSource(): AuthDataSource {
        return authDataSource
    }
    
    fun provideFirestoreDataSource(): FirestoreDataSource {
        return firestoreDataSource
    }
    
    // Repositories
    fun provideAuthRepository(@Suppress("UNUSED_PARAMETER") application: Application): AuthRepository {
        return AuthRepositoryImpl(provideAuthDataSource())
    }
    
    fun provideBeneficiarioRepository(): BeneficiarioRepository {
        return BeneficiarioRepositoryImpl(provideFirestoreDataSource(), provideAuthDataSource())
    }
    
    fun provideBemRepository(): BemRepository {
        return BemRepositoryImpl(provideFirestoreDataSource())
    }
    
    fun provideEntregaRepository(): EntregaRepository {
        return EntregaRepositoryImpl(provideFirestoreDataSource())
    }
    
    fun provideAlteracaoRepository(): AlteracaoRepository {
        return AlteracaoRepositoryImpl(provideFirestoreDataSource())
    }
    
    fun providePedidoRepository(): com.grupo3.sasocial.domain.repository.PedidoRepository {
        return com.grupo3.sasocial.data.repository.PedidoRepositoryImpl(provideFirestoreDataSource())
    }
    
    // Use Cases
    fun provideLoginUseCase(application: Application): LoginUseCase {
        return LoginUseCase(provideAuthRepository(application))
    }
    
    fun provideLogoutUseCase(application: Application): LogoutUseCase {
        return LogoutUseCase(provideAuthRepository(application))
    }
    
    fun provideIsUserLoggedInUseCase(application: Application): IsUserLoggedInUseCase {
        return IsUserLoggedInUseCase(provideAuthRepository(application))
    }
    
    fun provideResetPasswordUseCase(application: Application): ResetPasswordUseCase {
        return ResetPasswordUseCase(provideAuthRepository(application))
    }
    
    fun provideIsBeneficiarioUseCase(): com.grupo3.sasocial.domain.usecase.IsBeneficiarioUseCase {
        return com.grupo3.sasocial.domain.usecase.IsBeneficiarioUseCase(
            provideAuthDataSource(),
            provideFirestoreDataSource()
        )
    }
}
