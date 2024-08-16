import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    val bloodPressure by homeViewModel.bloodPressure.observeAsState("N/A")
    val heartRate by homeViewModel.heartRate.observeAsState("N/A")
    val bloodSugar by homeViewModel.bloodSugar.observeAsState("N/A")
    val cholesterol by homeViewModel.cholesterol.observeAsState("N/A")

    LaunchedEffect(Unit) {
        homeViewModel.checkForActiveSession()
    }

    NavigationDrawer(
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Health Dashboard", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    HealthMetricCard("Blood Pressure", bloodPressure, Color.Blue)
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthMetricCard("Heart Rate", heartRate, Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthMetricCard("Blood Sugar", bloodSugar, Color.Green)
                    Spacer(modifier = Modifier.height(8.dp))
                    HealthMetricCard("Cholesterol", cholesterol, Color.Yellow)
                }
            }
        },
        onLogoutClick = { authViewModel.logOut(registrationViewModel, loginViewModel) }
    )
}

@Composable
fun HealthMetricCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}@Composable
@Preview
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(HomeViewModel(), AuthViewModel(), RegistrationViewModel(), LoginViewModel())
    }
}