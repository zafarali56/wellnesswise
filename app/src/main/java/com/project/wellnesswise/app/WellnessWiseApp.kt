package com.project.wellnesswise.app

import HomeScreen
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.HomeViewModel
import com.project.wellnesswise.data.LoginViewModel
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.screens.EmailVerificationScreen
import com.project.wellnesswise.screens.HabitsScreen
import com.project.wellnesswise.screens.LoginScreen
import com.project.wellnesswise.screens.SignUpScreen
import com.project.wellnesswise.screens.TermsAndConditionsScreen
import com.project.wellnesswise.screens.MedicalHistoryScreen

@Composable
fun WellnessWiseApp(registrationViewModel: RegistrationViewModel, loginViewModel: LoginViewModel,  authViewModel: AuthViewModel, homeViewModel: HomeViewModel) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Crossfade(targetState = WellnessWiseAppRouter.currentScreen, label = "") { currentState ->
            when (currentState.value) {
                is Screen.SignUpScreen -> SignUpScreen(registrationViewModel)
                is Screen.TermsAndConditionsScreen -> TermsAndConditionsScreen(registrationViewModel)
                is Screen.LoginScreen -> LoginScreen(loginViewModel)
                is Screen.HabitsScreen -> HabitsScreen(registrationViewModel)
                is Screen.MedicalHistoryScreen -> MedicalHistoryScreen(registrationViewModel)
                is Screen.HomeScreen -> HomeScreen( homeViewModel , authViewModel, registrationViewModel, loginViewModel)
                is Screen.EmailVerificationScreen -> EmailVerificationScreen(registrationViewModel)

            }
        }
    }
}
