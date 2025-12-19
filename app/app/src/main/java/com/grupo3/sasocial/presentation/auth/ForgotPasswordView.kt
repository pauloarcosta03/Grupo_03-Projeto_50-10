package com.grupo3.sasocial.presentation.auth

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo3.sasocial.presentation.components.SASLogo
import com.grupo3.sasocial.ui.theme.*

@Composable
fun ForgotPasswordView(
    @Suppress("UNUSED_PARAMETER") onSuccess: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SASGreen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        SASLogo(size = 150)
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = SASWhite) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SASWhite,
                    unfocusedTextColor = SASWhite,
                    focusedBorderColor = SASWhite,
                    unfocusedBorderColor = SASLightGray,
                    cursorColor = SASWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = SASWhite) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SASWhite,
                    unfocusedTextColor = SASWhite,
                    focusedBorderColor = SASWhite,
                    unfocusedBorderColor = SASLightGray,
                    cursorColor = SASWhite
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nova Password", color = SASWhite) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SASWhite,
                    unfocusedTextColor = SASWhite,
                    focusedBorderColor = SASWhite,
                    unfocusedBorderColor = SASLightGray,
                    cursorColor = SASWhite
                )
            )
            
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = if (isError) SASRedLight else SASWhite,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (newPassword.length >= 6) {
                        message = "Password alterada com sucesso!"
                        isError = false
                    } else {
                        message = "Password deve ter pelo menos 6 caracteres"
                        isError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SASWhite,
                    contentColor = SASGreen
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Alterar Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
