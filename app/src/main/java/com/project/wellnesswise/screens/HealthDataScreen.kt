import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.HealthDataTextField
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.launch
@Composable
fun HealthDataScreen(
    healthDataViewModel: HealthDataViewModel,
    loginViewModel: LoginViewModel
) {
    val primaryColor = colorResource(id = R.color.primary)
    val healthData by healthDataViewModel.healthData.collectAsState()
    val isSyncing by healthDataViewModel.isSyncing.collectAsState()
    val syncMessage by healthDataViewModel.syncMessage.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isManualInput by remember { mutableStateOf(healthData["dataSourcePreference"] != "GOOGLE_FIT") }

    val fitnessOptions = remember {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
            .build()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            coroutineScope.launch {
                healthDataViewModel.syncWithGoogleFit(context, fitnessOptions)
                isManualInput = false
            }
        } else {
            healthDataViewModel.setSyncMessage("Failed to obtain Google Fit permissions")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Health Data",
            style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { isManualInput = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isManualInput) primaryColor else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Manual Input")
            }
            Button(
                onClick = {
                    isManualInput = false
                    coroutineScope.launch {
                        healthDataViewModel.handleGoogleFitSync(
                            context,
                            fitnessOptions,
                            googleSignInLauncher
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isManualInput) primaryColor else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Google Fit Sync")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isManualInput) {
            HealthDataTextField(
                value = healthData["bloodPressure"] as? String ?: "",
                onValueChange = { healthDataViewModel.updateManualHealthData(bloodPressure = it) },
                label = "Blood Pressure (e.g. 120/80)",
                isError = false, // Add validation logic if needed
                errorMessage = "Invalid blood pressure format",
                enabled = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            HealthDataTextField(
                value = healthData["heartRate"] as? String ?: "",
                onValueChange = { healthDataViewModel.updateManualHealthData(heartRate = it) },
                label = "Heart Rate (e.g. 70)",
                isError = false, // Add validation logic if needed
                errorMessage = "Invalid heart rate",
                enabled = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            HealthDataTextField(
                value = healthData["bloodSugar"] as? String ?: "",
                onValueChange = { healthDataViewModel.updateManualHealthData(bloodSugar = it) },
                label = "Blood Sugar (e.g. 100)",
                isError = false, // Add validation logic if needed
                errorMessage = "Invalid blood sugar value",
                enabled = true
            )
        } else {
            if (isSyncing) {
                CircularProgressIndicator(color = primaryColor)
            } else {
                Text("Data synced from Google Fit:")
                Text("Blood Pressure: ${healthData["bloodPressure"] ?: "N/A"}")
                Text("Heart Rate: ${healthData["heartRate"] ?: "N/A"}")
                Text("Blood Sugar: ${healthData["bloodSugar"] ?: "N/A"}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Cholesterol (Manual Input Required)",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryColor
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        HealthDataTextField(
            value = healthData["cholesterol"] as? String ?: "",
            onValueChange = { healthDataViewModel.updateManualHealthData(cholesterol = it) },
            label = "Cholesterol (e.g. 200)",
            isError = false, // Add validation logic if needed
            errorMessage = "Invalid cholesterol value",
            enabled = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isManualInput) {
                    healthDataViewModel.sendManualHealthDataToFirestore()
                } else {
                    coroutineScope.launch {
                        healthDataViewModel.syncWithGoogleFit(context, fitnessOptions)
                    }
                }
                WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isManualInput) "Submit Health Data" else "Sync with Google Fit")
        }

    }
}
@Composable
@Preview
fun HealthDataScreenPreview() {
    HealthDataScreen(HealthDataViewModel(), LoginViewModel())
}