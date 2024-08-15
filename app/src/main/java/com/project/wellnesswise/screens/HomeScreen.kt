import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.components.NavigationDrawer
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.HomeViewModel
import com.project.wellnesswise.data.LoginViewModel
import com.project.wellnesswise.data.RegistrationViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    registrationViewModel: RegistrationViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel()
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    // Fetch data when the screen is loaded
    LaunchedEffect(Unit) {
        homeViewModel.checkForActiveSession()
    }

    val bloodPressure by homeViewModel.bloodPressure.observeAsState()
    val heartRate by homeViewModel.heartRate.observeAsState()

    NavigationDrawer(
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding() // Add this modifier to handle keyboard padding
                ) {

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()

                        ) {


                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                   ,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "Blood Pressure", style = MaterialTheme.typography.bodyLarge)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = bloodPressure ?: "N/A", style = MaterialTheme.typography.bodyLarge)
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "Heart Rate", style = MaterialTheme.typography.bodyLarge)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = heartRate ?: "N/A", style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        onLogoutClick = { authViewModel.logOut(registrationViewModel, loginViewModel) }
    )
}

@Composable
@Preview
fun HomeScreenPreview() {
    HomeScreen()
}
