import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                            .padding(16.dp),
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
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

                        Spacer(modifier = Modifier.height(16.dp))

                        ActionButton(
                            text = "Data Visualization",
                            icon = Icons.Filled.BarChart,
                            onClick = { WellnessWiseAppRouter.navigateTo(Screen.DataVisualizationScreen) },
                            color = MaterialTheme.colorScheme.primaryContainer,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HealthMetricCard(
                            title = "Cholesterol",
                            value = cholesterol,
                            unit = "mg/dL",
                            color = MaterialTheme.colorScheme.onSurface,
                            icon = Icons.Filled.Analytics,
                            modifier = Modifier.fillMaxWidth(),
                            isLargeCard = false,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CustomShapeButton(
                                text = "Prediction",
                                onClick = { /* TODO: Handle click */ },
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomStart = 8.dp, bottomEnd = 24.dp),
                                icon = Icons.Filled.Analytics,
                                containerColor = MaterialTheme.colorScheme.inversePrimary,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,

                            )

                            CustomShapeButton(
                                text = "Recommendation",
                                onClick = { /* TODO: Handle click */ },
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomStart = 8.dp, bottomEnd = 24.dp),
                                icon = Icons.Filled.HealthAndSafety,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                }
            },
            onLogoutClick = { authViewModel.logOut() },
            onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) },
            onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) },
            userData = userData
        )
    }
}

