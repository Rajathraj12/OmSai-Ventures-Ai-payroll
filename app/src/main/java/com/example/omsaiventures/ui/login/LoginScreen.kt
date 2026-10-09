package com.example.omsaiventures.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omsaiventures.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    // Outer container matching .login-container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperDim) // --bg mapping
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Card matching .login-card
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp, 
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = LedgerDark.copy(alpha = 0.04f),
                    spotColor = LedgerDark.copy(alpha = 0.08f)
                )
                .background(Paper, RoundedCornerShape(16.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Replace with your actual Logo if available using Image()
            // .login-logo
            Box(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.omsaiventures.R.drawable.logo),
                    contentDescription = "Company Logo",
                    modifier = Modifier.fillMaxHeight(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            // .login-title
            Text(
                text = "Sign In",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // .login-sub
            Text(
                text = "Employee Management System",
                fontSize = 14.sp,
                color = InkSoft,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // .login-error
            if (loginState is LoginState.Error) {
                val errorMsg = (loginState as LoginState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .background(ErrorBg, RoundedCornerShape(8.dp))
                        .border(1.dp, ErrorBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMsg,
                        color = ErrorText,
                        fontSize = 14.sp
                    )
                }
            }

            // .login-form
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Email field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Email / Username",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brass,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = Paper,
                            unfocusedContainerColor = Paper,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Password field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brass,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = Paper,
                            unfocusedContainerColor = Paper,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // .login-btn
                Button(
                    onClick = { viewModel.signIn(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brass,
                        contentColor = Color.White
                    ),
                    enabled = loginState !is LoginState.Loading
                ) {
                    if (loginState is LoginState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Secure Login",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
