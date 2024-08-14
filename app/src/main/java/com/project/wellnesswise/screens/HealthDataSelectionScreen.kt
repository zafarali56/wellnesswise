package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun HealthDataSelectionScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeadingTextComponent(value = "Choose Health Data Input Method")
                Spacer(modifier = Modifier.height(16.dp))

                ButtonComponent(
                    value = "Enter Manually",
                    onButtonClicked = {
                        WellnessWiseAppRouter.navigateTo(Screen.HealthDataScreen)
                    },
                    isEnabled = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                ButtonComponent(
                    value = "Sync from Watch",
                    onButtonClicked = {

                        // For now, navigate to a placeholder screen or show a message
                        WellnessWiseAppRouter.navigateTo(Screen.HealthDataWatchScreen) // Replace with actual sync logic
                    },
                    isEnabled = true
                )
            }
        }
    }
}


@Composable
@Preview
fun HealthDataSelectionScreenPreview() {
    HealthDataSelectionScreen()
}
