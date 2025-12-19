package com.grupo3.sasocial.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo3.sasocial.ui.theme.SASGreen
import com.grupo3.sasocial.ui.theme.SASWhite

@Composable
fun SASLogo(
    modifier: Modifier = Modifier,
    size: Int = 120
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SASGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SERVIÇOS",
                color = SASWhite,
                fontSize = (size / 10).sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "DE AÇÃO",
                color = SASWhite,
                fontSize = (size / 10).sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SOCIAL",
                color = SASWhite,
                fontSize = (size / 10).sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "IPCA",
                color = SASWhite,
                fontSize = (size / 8).sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
