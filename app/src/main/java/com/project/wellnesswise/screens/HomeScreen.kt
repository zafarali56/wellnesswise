import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.ui.graphics.vector.ImageVector

import com.project.wellnesswise.R
import com.project.wellnesswise.components.NavigationDrawer
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.HomeViewModel
import com.project.wellnesswise.data.LoginViewModel
import com.project.wellnesswise.data.RegistrationViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    registrationViewModel: RegistrationViewModel,
    loginViewModel: LoginViewModel
) {
    val context = LocalContext.current
    val bloodPressure by homeViewModel.bloodPressure.observeAsState("N/A")
    val heartRate by homeViewModel.heartRate.observeAsState("N/A")
    val bloodSugar by homeViewModel.bloodSugar.observeAsState("N/A")

    val primaryColor = colorResource(id = R.color.primary)
    val secondaryColor = colorResource(id = R.color.secondary)
    val lightRedColor = Color(0xFFFF9999) // Light red color for heart rate

    LaunchedEffect(Unit) {
        homeViewModel.checkForActiveSession()
    }
    NavigationDrawer(
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Health Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Full-width heart rate card
                    HealthMetricCard(
                        title = "Heart Rate",
                        value = heartRate,
                        unit = "bpm",
                        color = lightRedColor,
                        icon = Icons.Filled.Favorite,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row for blood pressure and blood sugar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Blood Pressure card (half width)
                        HealthMetricCard(
                            title = "Blood Pressure",
                            value = bloodPressure,
                            unit = "mmHg",
                            color = primaryColor,
                            icon = Icons.Filled.MonitorHeart,
                            modifier = Modifier.weight(1f)
                        )

                        // Blood Sugar card (half width)
                        HealthMetricCard(
                            title = "Blood Sugar",
                            value = bloodSugar,
                            unit = "mg/dL",
                            color = secondaryColor,
                            icon = Icons.Filled.WaterDrop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        onLogoutClick = { authViewModel.logOut(registrationViewModel, loginViewModel) }
    )
}
@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = color.copy(alpha = 0.7f),
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(HomeViewModel(), AuthViewModel(), RegistrationViewModel(), LoginViewModel())
    }
}