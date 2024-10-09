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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
) {
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val context = LocalContext.current
    val bloodPressure by homeViewModel.bloodPressure.collectAsStateWithLifecycle()
    val heartRate by homeViewModel.heartRate.collectAsStateWithLifecycle()
    val bloodSugar by homeViewModel.bloodSugar.collectAsStateWithLifecycle()
    val isRefreshing by homeViewModel.isRefreshing.collectAsStateWithLifecycle()
    val cholesterol by homeViewModel.cholesterol.collectAsStateWithLifecycle()
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    val colorScheme = when {
        useDarkIcons -> dynamicLightColorScheme(context)
        else -> dynamicDarkColorScheme(context)
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.surface,
            darkIcons = useDarkIcons
        )
    }

    LaunchedEffect(Unit) {
        homeViewModel.checkForActiveSession()
        homeViewModel.getUserData { fetchedUserData ->
            userData = fetchedUserData
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        NavigationDrawer(
            content = {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = { homeViewModel.refreshData() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                    ) {
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

                        ActionButton(
                            text = "Data Visualization",
                            icon = Icons.Filled.BarChart,
                            onClick = { WellnessWiseAppRouter.navigateTo(Screen.DataVisualizationScreen) },
                            color = MaterialTheme.colorScheme.primary,
                        )

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
                        Spacer(modifier = Modifier.height(10.dp))
                        ActionButton(
                            text = "Health Risk Predictions",
                            icon = Icons.Filled.BubbleChart,
                            onClick = { WellnessWiseAppRouter.navigateTo(Screen.PredictionsScreen) },
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ActionButton(
                            text = "Personalized Recommendations",
                            icon = Icons.Filled.Spa,
                            onClick = { WellnessWiseAppRouter.navigateTo(Screen.PersonalizedRecommendationsScreen) },
                            color = MaterialTheme.colorScheme.primary,
                        )

                    }
                }
            },
            onLogoutClick = { authViewModel.logOut() },
            onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) },
            onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) },
            userData = userData,
            currentScreen = Screen.HomeScreen // Specify the current screen
        )
    }
}

