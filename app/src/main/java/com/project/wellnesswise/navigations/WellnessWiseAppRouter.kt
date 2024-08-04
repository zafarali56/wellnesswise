package com.project.wellnesswise.navigations

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed class Screen {
    object SignUpScreen : Screen()
    object TermsAndConditionsScreen : Screen()

}

object WellnessWiseAppRouter {
    var currentScreen: MutableState<Screen> = mutableStateOf(Screen.SignUpScreen)
    fun navigateTo(destination: Screen) {
        currentScreen.value = destination
    }
}