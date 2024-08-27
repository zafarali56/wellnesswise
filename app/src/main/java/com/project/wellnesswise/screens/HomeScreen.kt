
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.HealthMetricCard
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
) {
    val context = LocalContext.current
    val bloodPressure by homeViewModel.bloodPressure.collectAsStateWithLifecycle()
    val heartRate by homeViewModel.heartRate.collectAsStateWithLifecycle()
    val bloodSugar by homeViewModel.bloodSugar.collectAsStateWithLifecycle()
    val isRefreshing by homeViewModel.isRefreshing.collectAsStateWithLifecycle()
    val cholesterol by homeViewModel.cholesterol.collectAsStateWithLifecycle()
    val primaryColor = colorResource(id = R.color.primary)
    val secondaryColor = colorResource(id = R.color.secondary)
    val lightRedColor = Color(0xFFE53935)
    val lightBlueColor = Color(0xFF1E88E5)

    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.surface
    val useDarkIcons = !isSystemInDarkTheme()

    LaunchedEffect(statusBarColor, useDarkIcons) {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = useDarkIcons,
        )
    }
    LaunchedEffect(Unit) {
        homeViewModel.checkForActiveSession()
    }

    NavigationDrawer(
        content = {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { homeViewModel.refreshData() },
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                ) {
                    HealthMetricCard(
                        title = "Heart Rate",
                        value = heartRate,
                        unit = "bpm",
                        color = lightRedColor,
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
                            color = primaryColor,
                            icon = Icons.Filled.MonitorHeart,
                            modifier = Modifier.weight(1f),
                            isLargeCard = false,
                        )

                        HealthMetricCard(
                            title = "Blood Sugar",
                            value = bloodSugar,
                            unit = "mg/dL",
                            color = secondaryColor,
                            icon = Icons.Filled.WaterDrop,
                            modifier = Modifier.weight(1f),
                            isLargeCard = false,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { WellnessWiseAppRouter.navigateTo(Screen.DataVisualizationScreen) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(28.dp),
                                ),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(28.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = "Data Visualization",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Data Visualization",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HealthMetricCard(
                        title = "Cholesterol",
                        value = cholesterol,
                        unit = "mg/dL",
                        color = lightBlueColor,
                        icon = Icons.Filled.Analytics,
                        modifier = Modifier.fillMaxWidth(),
                        isLargeCard = false,
                    )
                }
            }
        },
        onLogoutClick = { authViewModel.logOut() },
        onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) },
        onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) },
    )
}

@Composable
@Preview
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            HomeViewModel(),
            AuthViewModel(RegistrationViewModel(), LoginViewModel(), HealthDataViewModel()),
        )
    }
}
