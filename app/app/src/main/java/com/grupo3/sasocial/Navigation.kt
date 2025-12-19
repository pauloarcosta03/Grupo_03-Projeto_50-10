package com.grupo3.sasocial

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.presentation.auth.LoginView
import com.grupo3.sasocial.presentation.auth.ForgotPasswordView
import com.grupo3.sasocial.presentation.dashboard.DashboardView
import com.grupo3.sasocial.presentation.calendario.CalendarioView
import com.grupo3.sasocial.presentation.beneficiarios.BeneficiariosView
import com.grupo3.sasocial.presentation.beneficiarios.BeneficiarioDetailView
import com.grupo3.sasocial.presentation.stock.StockView
import com.grupo3.sasocial.presentation.stock.AddBemView
import com.grupo3.sasocial.presentation.stock.EditBemView
import com.grupo3.sasocial.presentation.stock.InventarioView
import com.grupo3.sasocial.presentation.suporte.SuporteView
import com.grupo3.sasocial.presentation.alteracoes.AlteracoesView
import com.grupo3.sasocial.presentation.entregas.HistoricoEntregasView
import com.grupo3.sasocial.presentation.beneficiario.*
import com.grupo3.sasocial.presentation.pedidos.PedidosAdminView
import com.grupo3.sasocial.presentation.pedidos.PedidoDetailView
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SASocialNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val isUserLoggedInUseCase = AppModule.provideIsUserLoggedInUseCase(application)
    val isBeneficiarioUseCase = AppModule.provideIsBeneficiarioUseCase()
    
    var isBeneficiario by remember { mutableStateOf<Boolean?>(null) }
    var beneficiario by remember { mutableStateOf<com.grupo3.sasocial.domain.model.Beneficiario?>(null) }
    
    LaunchedEffect(Unit) {
        if (isUserLoggedInUseCase()) {
            try {
                val isBenef = isBeneficiarioUseCase.invoke()
                isBeneficiario = isBenef
                if (isBenef) {
                    beneficiario = isBeneficiarioUseCase.getBeneficiarioAprovado()
                }
            } catch (e: Exception) {
                android.util.Log.e("Navigation", "Erro ao verificar beneficiário", e)
                isBeneficiario = false
            }
        }
    }
    
    val startDestination = if (isUserLoggedInUseCase()) {
        // Aguardar detecção antes de decidir
        when (isBeneficiario) {
            true -> "beneficiarioDashboard"
            false -> "dashboard"
            null -> "dashboard" // Default enquanto verifica
        }
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            var shouldNavigate by remember { mutableStateOf(false) }
            
            LaunchedEffect(shouldNavigate) {
                if (shouldNavigate) {
                    try {
                        val isBenef = isBeneficiarioUseCase.invoke()
                        val dest = if (isBenef) {
                            val benef = isBeneficiarioUseCase.getBeneficiarioAprovado()
                            if (benef != null) {
                                beneficiario = benef
                                isBeneficiario = true
                                "beneficiarioDashboard"
                            } else {
                                "dashboard"
                            }
                        } else {
                            isBeneficiario = false
                            "dashboard"
                        }
                        navController.navigate(dest) {
                            popUpTo("login") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        // Em caso de erro, redirecionar para dashboard padrão
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            }
            
            LoginView(
                onLoginSuccess = {
                    shouldNavigate = true
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgotPassword")
                }
            )
        }
        
        composable("forgotPassword") {
            ForgotPasswordView(
                onSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("dashboard") {
            DashboardView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable("calendario") {
            CalendarioView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        
        composable("beneficiarios") {
            BeneficiariosView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onBeneficiarioClick = { id ->
                    navController.navigate("beneficiarioDetail/$id")
                }
            )
        }
        
        composable("beneficiarioDetail/{beneficiarioId}") { backStackEntry ->
            val beneficiarioId = backStackEntry.arguments?.getString("beneficiarioId") ?: ""
            BeneficiarioDetailView(
                beneficiarioId = beneficiarioId,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("stock") {
            StockView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddBem = {
                    navController.navigate("addBem")
                },
                onEditBem = { id ->
                    navController.navigate("editBem/$id")
                }
            )
        }
        
        composable("addBem") {
            AddBemView(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("editBem/{bemId}") { backStackEntry ->
            val bemId = backStackEntry.arguments?.getString("bemId") ?: ""
            EditBemView(
                bemId = bemId,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("inventario") {
            InventarioView(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("suporte") {
            SuporteView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                isBeneficiario = false
            )
        }
        
        composable("beneficiarioSuporte") {
            SuporteView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("beneficiarioDashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                isBeneficiario = true
            )
        }
        
        composable("alteracoes") {
            AlteracoesView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        
        composable("pedidosAdmin") {
            PedidosAdminView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onPedidoClick = { pedidoId ->
                    navController.navigate("pedidoDetail/$pedidoId")
                }
            )
        }
        
        composable("pedidoDetail/{pedidoId}") { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
            PedidoDetailView(
                pedidoId = pedidoId,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("historicoEntregas") {
            HistoricoEntregasView(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        
        // Rotas para Beneficiários
        composable("beneficiarioDashboard") {
            beneficiario?.let { benef ->
                // Usar key para garantir que o ViewModel é mantido
                BeneficiarioDashboardView(
                    beneficiario = benef,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("beneficiarioDashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        composable("beneficiarioStock") {
            beneficiario?.let { benef ->
                val categorias = isBeneficiarioUseCase.getCategoriasAceites(benef)
                BeneficiarioStockView(
                    categoriasAceites = categorias,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("beneficiarioDashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
        
        composable("beneficiarioPedidos") {
            beneficiario?.let { benef ->
                BeneficiarioPedidosView(
                    beneficiarioEmail = benef.email,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("beneficiarioDashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
        
        composable("criarPedido") {
            beneficiario?.let { benef ->
                val categorias = isBeneficiarioUseCase.getCategoriasAceites(benef)
                CriarPedidoView(
                    beneficiarioId = benef.id,
                    beneficiarioEmail = benef.email,
                    beneficiarioNome = benef.nome,
                    categoriasAceites = categorias,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("beneficiarioDashboard") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
