package com.project.wellnesswise.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.components.ui.ActionButton
import com.project.wellnesswise.components.ui.HealthMetricCard
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.ui.theme.WellnessWiseTheme
import com.project.wellnesswise.viewModels.AuthViewModel
import com.project.wellnesswise.viewModels.HomeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
) {
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }

    val bloodPressure by homeViewModel.bloodPressure.collectAsState()
    val heartRate by homeViewModel.heartRate.collectAsState()
    val bloodSugar by homeViewModel.bloodSugar.collectAsState()
    val cholesterol by homeViewModel.cholesterol.collectAsState()

    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    val useDarkIcons = !isSystemInDarkTheme()

    val dynamicColors = if (useDarkIcons) {
        dynamicLightColorScheme(context)
    } else {
        dynamicDarkColorScheme(context)
    }

    LaunchedEffect(dynamicColors) {
        systemUiController.setSystemBarsColor(
            color = dynamicColors.surface,
            darkIcons = useDarkIcons
        )
    }

    LaunchedEffect(Unit) {
        homeViewModel.checkForActiveSession()
        homeViewModel.getUserData { fetchedUserData ->
            userData = fetchedUserData
        }
    }

    val userName = userData?.get("fullName") as? String ?: "User Name"

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingText = remember(currentHour) {
        when {
            currentHour < 12 -> "Good Morning"
            currentHour < 17 -> "Good Afternoon"
            currentHour < 19 -> "Good Evening"
            else -> "Good night"
        }
    }

    WellnessWiseTheme {
        NavigationDrawer(
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "$greetingText, $userName!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    )
                    Text(
                        text = "Check the prediction of your health below:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    HealthMetricCard(
                        title = "Heart Rate",
                        value = heartRate,
                        unit = "bpm",
                        color = MaterialTheme.colorScheme.onSurface,
                        icon = Icons.Filled.Favorite,
                        modifier = Modifier.fillMaxWidth(),
                        isLargeCard = true,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HealthMetricCard(
                            title = "Blood Pressure",
                            value = bloodPressure,
                            unit = "mmHg",
                            color = MaterialTheme.colorScheme.onSurface,
                            icon = Icons.Filled.MonitorHeart,
                            modifier = Modifier.weight(1f),
                            isLargeCard = false,
                        )

                        HealthMetricCard(
                            title = "Blood Sugar",
                            value = bloodSugar,
                            unit = "mg/dL",
                            color = MaterialTheme.colorScheme.onSurface,
                            icon = Icons.Filled.WaterDrop,
                            modifier = Modifier.weight(1f),
                            isLargeCard = false,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    HealthMetricCard(
                        title = "Cholesterol",
                        value = cholesterol,
                        unit = "mg/dL",
                        color = MaterialTheme.colorScheme.onSurface,
                        icon = Icons.Filled.Analytics,
                        modifier = Modifier.fillMaxWidth(),
                        isLargeCard = false,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ActionButton(
                        text = "Health Risk Predictions",
                        icon = Icons.Filled.BubbleChart,
                        onClick = {
                            WellnessWiseAppRouter.navigateTo(Screen.PredictionsScreen)
                        },
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            },
            onLogoutClick = { authViewModel.logOut() },
            onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) },
            onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) },
            userData = userData,
            currentScreen = Screen.HomeScreen,
            onHealthDataClick = { WellnessWiseAppRouter.navigateTo(Screen.HealthDataViewEditScreen) }
            , onAssessmentClick = {WellnessWiseAppRouter.navigateTo(Screen.HealthAssessmentEditViewScreen)}
        )
    }
}
