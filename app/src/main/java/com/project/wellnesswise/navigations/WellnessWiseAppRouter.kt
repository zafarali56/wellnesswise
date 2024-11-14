package com.project.wellnesswise.navigations

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed class Screen {
    data object SignUpScreen : Screen()
    data object TermsAndConditionsScreen : Screen()
    data object LoginScreen : Screen()
    data object HomeScreen : Screen()
    data object EmailVerificationScreen : Screen()
    data object HealthDataScreen : Screen()
    data object DataVisualizationScreen : Screen()
    data object UserProfileScreen: Screen()
    data object PredictionsScreen: Screen()
    data object PersonalizedRecommendationsScreen: Screen()
    data object HealthAssessmentScreen: Screen()
    data object PredictionHistoryScreen: Screen()
}
object WellnessWiseAppRouter {
    var currentScreen: MutableState<Screen> = mutableStateOf(Screen.SignUpScreen)

    fun navigateTo(destination: Screen) {
        currentScreen.value = destination
    }
}
