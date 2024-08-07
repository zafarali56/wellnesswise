package com.project.wellnesswise.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.screens.HabitsScreen
import com.project.wellnesswise.screens.LoginScreen
import com.project.wellnesswise.screens.SignUpScreen
import com.project.wellnesswise.screens.TermsAndConditionsScreen
import com.project.wellnesswise.screens.MedicalHistoryScreen

@Composable
fun WellnessWiseApp() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Crossfade(targetState = WellnessWiseAppRouter.currentScreen, label = "") { currentState ->
            when (currentState.value) {
                is Screen.SignUpScreen -> SignUpScreen()
                is Screen.TermsAndConditionsScreen -> TermsAndConditionsScreen()
                is Screen.LoginScreen -> LoginScreen()
                is Screen.HabitsScreen -> HabitsScreen()
                is Screen.MedicalHistoryScreen -> MedicalHistoryScreen()
            }
        }
    }
}
