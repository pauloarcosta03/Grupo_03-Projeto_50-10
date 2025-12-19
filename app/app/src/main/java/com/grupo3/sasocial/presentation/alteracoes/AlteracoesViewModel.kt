package com.grupo3.sasocial.presentation.alteracoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Alteracao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlteracoesViewModel(application: Application) : AndroidViewModel(application) {
    private val alteracaoRepository = AppModule.provideAlteracaoRepository()
    
    private val _alteracoes = MutableStateFlow<List<Alteracao>>(emptyList())
    val alteracoes: StateFlow<List<Alteracao>> = _alteracoes.asStateFlow()
    
    init {
        loadAlteracoes()
    }
    
    private fun loadAlteracoes() {
        viewModelScope.launch {
            alteracaoRepository.getAllAlteracoes().collect { _alteracoes.value = it }
        }
    }
}
