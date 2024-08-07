package com.project.wellnesswise.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.screens.HabitsScreen
import com.project.wellnesswise.screens.LoginScreen
import com.project.wellnesswise.screens.SignUpScreen
import com.project.wellnesswise.screens.TermsAndConditionsScreen
import com.project.wellnesswise.screens.MedicalHistoryScreen
import com.project.wellnesswise.data.LoginViewModel

@Composable
fun WellnessWiseApp(loginViewModel: LoginViewModel = viewModel()) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Crossfade(targetState = WellnessWiseAppRouter.currentScreen, label = "") { currentState ->
            when (currentState.value) {
                is Screen.SignUpScreen -> SignUpScreen(loginViewModel)
                is Screen.TermsAndConditionsScreen -> TermsAndConditionsScreen(loginViewModel)
                is Screen.LoginScreen -> LoginScreen(loginViewModel)
                is Screen.HabitsScreen -> HabitsScreen(loginViewModel)
                is Screen.MedicalHistoryScreen -> MedicalHistoryScreen(loginViewModel)
            }
        }
    }
}
