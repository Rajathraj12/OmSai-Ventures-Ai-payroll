package com.example.omsaiventures

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omsaiventures.ui.dashboard.DashboardScreen
import com.example.omsaiventures.ui.dashboard.DashboardViewModel
import com.example.omsaiventures.ui.login.LoginScreen
import com.example.omsaiventures.ui.login.LoginViewModel
import com.example.omsaiventures.ui.splash.SplashScreen
import com.example.omsaiventures.ui.theme.OmSaiVenturesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("om_sai_auth", MODE_PRIVATE)
        val isAlreadyLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null ||
                sharedPrefs.getBoolean("is_logged_in", false)

        setContent {
            OmSaiVenturesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // splash -> login -> home
                    var currentScreen by remember {
                        mutableStateOf("splash")
                    }

                    when (currentScreen) {
                        "splash" -> {
                            SplashScreen(onFinished = {
                                currentScreen = if (isAlreadyLoggedIn) "home" else "login"
                            })
                        }
                        "login" -> {
                            val loginViewModel: LoginViewModel = viewModel()
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = {
                                    sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
                                    currentScreen = "home"
                                }
                            )
                        }
                        "home" -> {
                            val dashboardViewModel: DashboardViewModel = viewModel()
                            Box(modifier = Modifier.padding(innerPadding)) {
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onLogout = {
                                        try { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() }
                                        catch (_: Exception) {}
                                        sharedPrefs.edit().clear().apply()
                                        currentScreen = "login"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
