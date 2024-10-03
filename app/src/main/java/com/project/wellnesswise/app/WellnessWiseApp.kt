package com.project.wellnesswise.app
import DataVisualizationScreen
import DataVisualizationViewModel
import HealthDataScreen
import HealthDataViewModel
import HomeScreen
import HomeViewModel
import LoginScreen
import LoginViewModel
import PredictionHistoryScreen
import UserProfileScreen
import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.project.wellnesswise.components.ui.HealthAssessmentMode
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.screens.*

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun WellnessWiseApp(
    registrationViewModel: RegistrationViewModel,
    loginViewModel: LoginViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    healthDataViewModel: HealthDataViewModel,
    onRequestGoogleFitPermission: () -> Unit,
    dataVisualizationViewModel : DataVisualizationViewModel ,

) {
    homeViewModel.checkForActiveSession()

    Surface(modifier = Modifier.fillMaxSize()) {
        if (homeViewModel.isUserLoggedIn.value) {
            WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
        }

        Crossfade(targetState = WellnessWiseAppRouter.currentScreen, label = "") { currentState ->
            when (currentState.value) {
                is Screen.SignUpScreen -> SignUpScreen(registrationViewModel)
                is Screen.TermsAndConditionsScreen -> TermsAndConditionsScreen()
                is Screen.LoginScreen -> LoginScreen(loginViewModel)
                is Screen.HomeScreen -> HomeScreen(homeViewModel, authViewModel)
                is Screen.EmailVerificationScreen -> EmailVerificationScreen(registrationViewModel)
                is Screen.HealthDataScreen -> HealthDataScreen(healthDataViewModel, loginViewModel)
                is Screen.UserProfileScreen -> UserProfileScreen(
                    authViewModel,
                    registrationViewModel
                )

                is Screen.DataVisualizationScreen -> DataVisualizationScreen(
                    dataVisualizationViewModel
                )

                is Screen.PredictionsScreen -> PredictionsScreen()
                is Screen.PersonalizedRecommendationsScreen -> PersonalizedRecommendationsScreen()
                is Screen.HealthAssessmentScreen -> {
                    val mode = registrationViewModel.currentMode.value
                    HealthAssessmentScreen(
                        registrationViewModel = registrationViewModel,
                        mode = mode,
                        onSave = {
                            when (mode) {
                                HealthAssessmentMode.SIGNUP -> {
                                    WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
                                }
                                HealthAssessmentMode.EDIT -> {
                                    WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen)
                                }
                            }
                        },
                        onBack = {
                            when (mode) {
                                HealthAssessmentMode.SIGNUP -> WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
                                HealthAssessmentMode.EDIT -> WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen)
                            }
                        }
                    )
                }
                is Screen.PredictionHistoryScreen -> PredictionHistoryScreen()
            }
        }
    }
}
