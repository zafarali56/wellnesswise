import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.HeadingTextComponent
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

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current
    // Use dynamic color scheme
    val colorScheme = when {
        useDarkIcons -> dynamicLightColorScheme(context)
        else -> dynamicDarkColorScheme(context)
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = useDarkIcons
        )
    }

    val primaryColor = colorResource(id = R.color.primary)
    val healthData by healthDataViewModel.healthData.collectAsState()
    val cholesterol by healthDataViewModel.cholesterol.collectAsState()
    val isSyncing by healthDataViewModel.isSyncing.collectAsState()
    val syncMessage by healthDataViewModel.syncMessage.collectAsState()

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

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        HeadingTextComponent(value = "Health Data")
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { isManualInput = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isManualInput) colorResource(id = R.color.primary) else colorResource(id = R.color.secondary)
                )
            ) {
                Text("Manual Input", fontWeight = FontWeight.Bold)
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
                    containerColor = if (!isManualInput) colorResource(id = R.color.primary) else colorResource(id = R.color.secondary)
                )
            ) {
                Text("Google Fit Sync", fontWeight = FontWeight.Bold)
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
            value = cholesterol,
            onValueChange = { healthDataViewModel.updateCholesterol(it) },
            label = "Cholesterol (e.g. 200)",
            isError = false,
            errorMessage = "Invalid cholesterol value",
            enabled = true
        )


        Spacer(modifier = Modifier.height(16.dp))
        ButtonComponent(value = "Submit", onButtonClicked = {   healthDataViewModel.sendHealthDataToFirestore()
            WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)}, isEnabled = true)

            }
        }
    }
}
@Composable
@Preview
fun HealthDataScreenPreview() {
    HealthDataScreen(HealthDataViewModel(), LoginViewModel())
}