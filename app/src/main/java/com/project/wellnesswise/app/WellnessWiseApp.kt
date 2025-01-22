package com.project.wellnesswise.app
import com.project.wellnesswise.screens.DataVisualizationScreen
import com.project.wellnesswise.viewModels.DataVisualizationViewModel
import com.project.wellnesswise.screens.HealthDataScreen
import com.project.wellnesswise.viewModels.HealthDataViewModel
import com.project.wellnesswise.screens.HomeScreen
import com.project.wellnesswise.viewModels.HomeViewModel
import com.project.wellnesswise.screens.LoginScreen
import com.project.wellnesswise.viewModels.LoginViewModel
import com.project.wellnesswise.viewModels.PersonalizedRecommendationsViewModel
import com.project.wellnesswise.screens.PredictionHistoryScreen
import com.project.wellnesswise.screens.UserProfileScreen
import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.project.wellnesswise.components.ui.HealthAssessmentMode
import com.project.wellnesswise.viewModels.AuthViewModel
import com.project.wellnesswise.viewModels.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.screens.EmailVerificationScreen
import com.project.wellnesswise.screens.HealthAssessmentScreen
import com.project.wellnesswise.screens.HealthAssessmentViewEditScreen
import com.project.wellnesswise.screens.HealthDataViewEditScreen
import com.project.wellnesswise.screens.PersonalizedRecommendationsScreen
import com.project.wellnesswise.screens.PredictionsScreen
import com.project.wellnesswise.screens.RecommendationSetupScreen
import com.project.wellnesswise.screens.SignUpScreen
import com.project.wellnesswise.screens.TermsAndConditionsScreen

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun WellnessWiseApp(
    registrationViewModel: RegistrationViewModel,
    loginViewModel: LoginViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    healthDataViewModel: HealthDataViewModel,
    dataVisualizationViewModel : DataVisualizationViewModel,
    personalizedRecommendationsViewModel : PersonalizedRecommendationsViewModel
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
                is Screen.HealthDataScreen -> HealthDataScreen(healthDataViewModel)
                is Screen.UserProfileScreen -> UserProfileScreen(
                    authViewModel,
                    registrationViewModel
                )

                is Screen.DataVisualizationScreen -> DataVisualizationScreen(
                    dataVisualizationViewModel
                )

                is Screen.PredictionsScreen -> PredictionsScreen()
                is Screen.PersonalizedRecommendationsScreen -> PersonalizedRecommendationsScreen(personalizedRecommendationsViewModel)
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
                is Screen.RecommendationSetupScreen -> RecommendationSetupScreen()
                is Screen.HealthDataViewEditScreen -> HealthDataViewEditScreen(authViewModel)
                is Screen.HealthAssessmentEditViewScreen -> HealthAssessmentViewEditScreen(authViewModel)

                }
            }
        }
    }
