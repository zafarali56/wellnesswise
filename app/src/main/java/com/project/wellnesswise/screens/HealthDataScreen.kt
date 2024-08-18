import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.data.HealthFields
import com.project.wellnesswise.R
import com.project.wellnesswise.components.HealthDataTextField
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
@Composable

fun HealthDataScreen(
    registrationViewModel: RegistrationViewModel,
    onRequestGoogleFitPermission: () -> Unit
) {
    val primaryColor = colorResource(id = R.color.primary)
    val uiState by registrationViewModel.registrationUIState
    val isSyncing by registrationViewModel.isSyncing
    val syncMessage by registrationViewModel.syncMessage
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
                syncWithGoogleFit(context, registrationViewModel, fitnessOptions)
            }
        } else {
            registrationViewModel.setSyncMessage("Failed to obtain Google Fit permissions")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Health Data", style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary))
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    handleGoogleFitSync(context, registrationViewModel, fitnessOptions, googleSignInLauncher)
                }
            },
            enabled = !isSyncing,

            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),

        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sync",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isSyncing) "Syncing..." else "Sync with Google Fit")
        }
        syncMessage?.let { message ->
            Text(
                text = message,
                color = if (message.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HealthDataTextField(
            value = uiState.bloodPressure,
            onValueChange = { registrationViewModel.updateHealthParameters(bloodPressure = it) },
            label = "Blood Pressure (e.g. 120/80)",
            isError = uiState.bloodPressureError,
            errorMessage = "Invalid blood pressure format"
        )

        Spacer(modifier = Modifier.height(8.dp))

        HealthDataTextField(
            value = uiState.heartRate,
            onValueChange = { registrationViewModel.updateHealthParameters(heartRate = it) },
            label = "Heart Rate (e.g. 70)",
            isError = uiState.heartRateError,
            errorMessage = "Invalid heart rate"
        )

        Spacer(modifier = Modifier.height(8.dp))

        HealthDataTextField(
            value = uiState.bloodSugar,
            onValueChange = { registrationViewModel.updateHealthParameters(bloodSugar = it) },
            label = "Blood Sugar (e.g. 100)",
            isError = uiState.bloodSugarError,
            errorMessage = "Invalid blood sugar value"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Put cholesterol manually please",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryColor
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        HealthDataTextField(
            value = uiState.cholesterol,
            onValueChange = { registrationViewModel.updateHealthParameters(cholesterol = it) },
            label = "Cholesterol (e.g. 200)",
            isError = uiState.cholesterolError,
            errorMessage = "Invalid cholesterol value"
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                registrationViewModel.sendHealthDataToFirestore()
                HealthDataSyncWorker.startPeriodicSync(context)
                WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
            },

            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),

        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Submit Health Data")
        }
    }
}
private suspend fun handleGoogleFitSync(
    context: Context,
    registrationViewModel: RegistrationViewModel,
    fitnessOptions: FitnessOptions,
    googleSignInLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
        try {
            registrationViewModel.setIsSyncing(true)
            registrationViewModel.setSyncMessage("Requesting Google Fit permissions...")

            val signInOptions = GoogleSignInOptions.Builder()
                .addExtension(fitnessOptions)
                .build()
            val intent = GoogleSignIn.getClient(context, signInOptions).signInIntent
            googleSignInLauncher.launch(intent)
        } catch (e: Exception) {
            registrationViewModel.setSyncMessage("Error requesting Google Fit permissions: ${e.message}")
        } finally {
            registrationViewModel.setIsSyncing(false)
        }
    } else {
        syncWithGoogleFit(context, registrationViewModel, fitnessOptions)
    }
}

private suspend fun syncWithGoogleFit(
    context: android.content.Context,
    registrationViewModel: RegistrationViewModel,
    fitnessOptions: FitnessOptions
) {
    try {
        registrationViewModel.setIsSyncing(true)
        registrationViewModel.setSyncMessage("Syncing with Google Fit...")

        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

        var heartRate: Float? = null
        var bloodPressure: String? = null
        var bloodSugar: Float? = null

        // Read heart rate
        val heartRateResult = Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .await()
        if (!heartRateResult.isEmpty) {
            heartRate = heartRateResult.dataPoints.firstOrNull()
                ?.getValue(Field.FIELD_AVERAGE)?.asFloat()
        }

        // Read blood pressure and blood sugar
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000 // 24 hours ago
        val readRequest = com.google.android.gms.fitness.request.DataReadRequest.Builder()
            .read(HealthDataTypes.TYPE_BLOOD_PRESSURE)
            .read(HealthDataTypes.TYPE_BLOOD_GLUCOSE)
            .setTimeRange(startTime, endTime, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        val dataResponse = Fitness.getHistoryClient(context, account)
            .readData(readRequest)
            .await()

        for (dataSet in dataResponse.dataSets) {
            when (dataSet.dataType) {
                HealthDataTypes.TYPE_BLOOD_PRESSURE -> {
                    for (dataPoint in dataSet.dataPoints.sortedByDescending { it.getEndTime(java.util.concurrent.TimeUnit.MILLISECONDS) }) {
                        val systolic = dataPoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC).asFloat()
                        val diastolic = dataPoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC).asFloat()
                        bloodPressure = "${systolic.toInt()}/${diastolic.toInt()}"
                        break // Take the most recent reading
                    }
                }
                HealthDataTypes.TYPE_BLOOD_GLUCOSE -> {
                    for (dataPoint in dataSet.dataPoints.sortedByDescending { it.getEndTime(java.util.concurrent.TimeUnit.MILLISECONDS) }) {
                        bloodSugar = dataPoint.getValue(HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL).asFloat()
                        break // Take the most recent reading
                    }
                }
            }
        }

        registrationViewModel.syncWithGoogleFit(heartRate, bloodPressure, bloodSugar)
    } catch (e: Exception) {
        registrationViewModel.setSyncMessage("Error syncing with Google Fit: ${e.message}")
    } finally {
        registrationViewModel.setIsSyncing(false)
    }
}


@Composable
@Preview
fun HealthDataScreenPreview() {
    HealthDataScreen(RegistrationViewModel(), {})
}