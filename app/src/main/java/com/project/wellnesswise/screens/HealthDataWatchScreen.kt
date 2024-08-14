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
import com.project.wellnesswise.components.NormalTextComponent
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun HealthDataWatchScreen() {

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
                HeadingTextComponent(value = "Sync Health Data from Watch")
                Spacer(modifier = Modifier.height(16.dp))

                NormalTextComponent(value = "Please ensure your watch is connected and press the button below to sync your health data.")
                Spacer(modifier = Modifier.height(16.dp))

                ButtonComponent(
                    value = "Sync Health Data",
                    onButtonClicked = {
                        WellnessWiseAppRouter.navigateTo(Screen.HealthDataScreen)
                    },
                    isEnabled = true
                )
            }
        }
    }
}


@Composable
@Preview
fun HealthDataWatchScreenPreview ()
{
     HealthDataWatchScreen()
}