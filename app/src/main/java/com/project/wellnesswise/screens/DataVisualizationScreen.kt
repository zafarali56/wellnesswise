package com.project.wellnesswise.screens


import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun DataVisualizationScreen() {
    Surface {

    }
    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
    }
}


@Preview
@Composable
fun DataVisualizationScreenPreview() {
    DataVisualizationScreen()
}