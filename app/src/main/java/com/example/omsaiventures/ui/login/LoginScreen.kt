package com.example.omsaiventures.ui.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omsaiventures.R

private val NavyDeep    = Color(0xFF080E1C)
private val NavyMid     = Color(0xFF0F2438)
private val NavyLight   = Color(0xFF16324F)
private val Gold        = Color(0xFFC9962E)
private val GoldLight   = Color(0xFFE8B84B)
private val GoldFaint   = Color(0x20C9962E)
private val GlassWhite  = Color(0x14FFFFFF)
private val GlassBorder = Color(0x25FFFFFF)
private val TextPrimary = Color(0xFFE8EDF5)
private val TextSecond  = Color(0xFF8FA8C8)
private val TextHint    = Color(0xFF4A6080)
private val FieldBg     = Color(0x12FFFFFF)
private val FieldBorder = Color(0x30FFFFFF)
private val FieldFocus  = Color(0x55C9962E)
private val ErrorRed    = Color(0xFFEF4444)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()
    var username by remember { mutableStateOf("omsaiventures@gmail.com") }
    var password by remember { mutableStateOf("Omsai") }
    var passwordVisible by remember { mutableStateOf(false) }

    val contentAlpha = remember { Animatable(0f) }
    val contentSlide = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        contentSlide.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "login")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -400f, targetValue = 1600f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orbPulse"
    )

    val bgGradient = Brush.verticalGradient(
        colors = listOf(NavyDeep, Color(0xFF0A1628), NavyMid, Color(0xFF0D1B2E))
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color.Transparent, Color(0x0AC9962E), Color.Transparent),
        start = Offset(shimmerOffset, 0f), end = Offset(shimmerOffset + 500f, 700f)
    )
    val goldGradient = Brush.horizontalGradient(colors = listOf(Gold, GoldLight, Gold))
    val buttonGradient = Brush.horizontalGradient(colors = listOf(Color(0xFFC9962E), Color(0xFFE8B84B), Color(0xFFC9962E)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {

        // Animated shimmer
        Box(modifier = Modifier.fillMaxSize().background(shimmerBrush))

        // Decorative orbs
        Box(modifier = Modifier
            .size((460 * orbPulse).dp)
            .offset(x = (-130).dp, y = (-160).dp)
            .background(Brush.radialGradient(colors = listOf(Color(0x18C9962E), Color.Transparent)), CircleShape))
        Box(modifier = Modifier
            .size(360.dp)
            .offset(x = 160.dp, y = 500.dp)
            .background(Brush.radialGradient(colors = listOf(Color(0x101A7AFF), Color.Transparent)), CircleShape))
        Box(modifier = Modifier
            .size(200.dp)
            .offset(x = (-30).dp, y = 480.dp)
            .background(Brush.radialGradient(colors = listOf(Color(0x0CC9962E), Color.Transparent)), CircleShape))

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .offset(y = contentSlide.value.dp)
                    .alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Logo & Branding ──────────────────────────────────
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(modifier = Modifier.size(90.dp)
                        .background(Brush.radialGradient(colors = listOf(Color(0x25C9962E), Color.Transparent)), CircleShape))
                    Box(modifier = Modifier.size(72.dp)
                        .background(Brush.radialGradient(colors = listOf(Color(0xFF1C3354), Color(0xFF0F2438))), CircleShape)
                        .border(1.dp, Brush.linearGradient(listOf(Gold.copy(alpha = 0.6f), GoldLight.copy(alpha = 0.3f))), CircleShape)
                        .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo", modifier = Modifier.size(52.dp),
                            contentScale = ContentScale.Fit)
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text("OmSai Ventures", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary, letterSpacing = 0.5.sp)
                Text("PAYROLL MANAGEMENT SYSTEM", fontSize = 9.5.sp, fontWeight = FontWeight.Bold,
                    color = Gold, letterSpacing = 3.sp, modifier = Modifier.padding(top = 3.dp))

                Spacer(Modifier.height(8.dp))

                // Decorative divider
                Box(modifier = Modifier.width(120.dp).height(1.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Gold.copy(0.7f), Color.Transparent))))

                Spacer(Modifier.height(32.dp))

                // ── Glass Card ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassWhite, RoundedCornerShape(20.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(28.dp)
                ) {
                    Column {
                        Text("Welcome Back", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = TextPrimary)
                        Text("Sign in to your secure workspace", fontSize = 13.sp,
                            color = TextSecond, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

                        // Error banner
                        if (loginState is LoginState.Error) {
                            val msg = (loginState as LoginState.Error).message
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                                    .background(ErrorRed.copy(0.12f), RoundedCornerShape(10.dp))
                                    .border(1.dp, ErrorRed.copy(0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("\u26A0", fontSize = 14.sp, color = ErrorRed)
                                Text(msg, color = ErrorRed, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }
                        }

                        // Email field
                        Text("Email Address", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = TextSecond, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null,
                                    tint = Gold, modifier = Modifier.size(20.dp))
                            },
                            placeholder = { Text("Enter your email", color = TextHint, fontSize = 14.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = FieldBorder,
                                focusedContainerColor = FieldBg,
                                unfocusedContainerColor = FieldBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Gold,
                                focusedLeadingIconColor = Gold,
                                unfocusedLeadingIconColor = TextHint
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Password field
                        Text("Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = TextSecond, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null,
                                    tint = Gold, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = TextHint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = FieldBorder,
                                focusedContainerColor = FieldBg,
                                unfocusedContainerColor = FieldBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Gold,
                                focusedLeadingIconColor = Gold,
                                unfocusedLeadingIconColor = TextHint
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(28.dp))

                        // Login button with gradient
                        val isLoading = loginState is LoginState.Loading
                        Box(
                            modifier = if (isLoading) {
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E3452))
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(buttonGradient)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { viewModel.signIn(username, password) },
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                enabled = loginState !is LoginState.Loading,
                                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                            ) {
                                if (loginState is LoginState.Loading) {
                                    CircularProgressIndicator(color = Gold, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("\uD83D\uDD10", fontSize = 15.sp)
                                        Text("Secure Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A0F00), letterSpacing = 0.5.sp)
                                    }
                                }
                            }
                        }

                        // Admin hint chip
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0AFFFFFF), RoundedCornerShape(10.dp))
                                .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Gold, CircleShape))
                                Column {
                                    Text("Admin Access", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                        color = Gold, letterSpacing = 0.5.sp)
                                    Text("omsaiventures@gmail.com  \u2022  Omsai", fontSize = 11.sp,
                                        color = TextSecond)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Footer
                Row(horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Gold.copy(0.5f), CircleShape))
                    Text("  Enterprise Grade Security  ", fontSize = 11.sp, color = TextHint)
                    Box(modifier = Modifier.size(6.dp).background(Gold.copy(0.5f), CircleShape))
                }
                Text("v1.0.0  \u00B7  \u00A9 2025 OmSai Ventures", fontSize = 10.sp,
                    color = TextHint.copy(alpha = 0.5f), modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center)
            }
        }
    }
}
