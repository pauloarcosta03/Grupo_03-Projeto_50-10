package com.grupo3.sasocial.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.grupo3.sasocial.ui.theme.SASGreen
import com.grupo3.sasocial.ui.theme.SASWhite
import com.grupo3.sasocial.ui.theme.SASGray

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    isBeneficiario: Boolean = false
) {
    val items = if (isBeneficiario) {
        // Menu para beneficiários
        listOf(
            BottomNavItem("beneficiarioDashboard", Icons.Default.Home, "Início"),
            BottomNavItem("beneficiarioStock", Icons.Default.List, "Stock"),
            BottomNavItem("beneficiarioPedidos", Icons.Default.ShoppingCart, "Pedidos"),
            BottomNavItem("beneficiarioSuporte", Icons.Default.Headset, "Suporte")
        )
    } else {
        // Menu para administradores (6 itens - pode ser ajustado se necessário)
        listOf(
            BottomNavItem("dashboard", Icons.Default.Home, "Início"),
            BottomNavItem("pedidosAdmin", Icons.Default.ShoppingCart, "Pedidos"),
            BottomNavItem("stock", Icons.Default.List, "Stock"),
            BottomNavItem("historicoEntregas", Icons.Default.Assignment, "Histórico"),
            BottomNavItem("beneficiarios", Icons.Default.People, "Beneficiários"),
            BottomNavItem("suporte", Icons.Default.Headset, "Suporte")
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SASGreen)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate(item.route) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) SASWhite else SASGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
