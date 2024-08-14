package com.project.wellnesswise.navigations

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed class Screen {
    object SignUpScreen : Screen()
    object TermsAndConditionsScreen : Screen()
    object LoginScreen : Screen()
    object HabitsScreen : Screen()
    object MedicalHistoryScreen : Screen()
    object HomeScreen: Screen()
    object EmailVerificationScreen: Screen()
    object HealthDataScreen: Screen()
    object HealthDataWatchScreen: Screen()
    object HealthDataSelectionScreen: Screen()
}

object WellnessWiseAppRouter {
    var currentScreen: MutableState<Screen> = mutableStateOf(Screen.SignUpScreen)

    fun navigateTo(destination: Screen) {
        currentScreen.value = destination
    }
}
