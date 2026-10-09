package com.example.omsaiventures

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.omsaiventures.ui.theme.OmSaiVenturesTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omsaiventures.ui.login.LoginScreen
import com.example.omsaiventures.ui.login.LoginViewModel

import com.example.omsaiventures.ui.dashboard.DashboardScreen
import com.example.omsaiventures.ui.dashboard.DashboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // --- DATA MANAGEMENT ---
        // Uncomment below to add mock data:
        // com.example.omsaiventures.utils.FirebaseSeeder.seedData()
        
        setContent {
            OmSaiVenturesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Set up basic Navigation state (for demonstration)
                    var currentScreen by androidx.compose.runtime.remember { 
                        // Bypass login for testing: start on "home" instead of "login"
                        androidx.compose.runtime.mutableStateOf("home") 
                    }

                    if (currentScreen == "login") {
                        val loginViewModel: LoginViewModel = viewModel()
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                currentScreen = "home"
                            }
                        )
                    } else {
                        val dashboardViewModel: DashboardViewModel = viewModel()
                        Box(modifier = Modifier.padding(innerPadding)) {
                            DashboardScreen(viewModel = dashboardViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OmSaiVenturesTheme {
        Greeting("Android")
    }
}