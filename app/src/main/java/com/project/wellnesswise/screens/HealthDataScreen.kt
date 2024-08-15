package com.project.wellnesswise.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.MyTextField
import com.project.wellnesswise.data.RegistrationViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

@Composable
fun HealthDataScreen(
    registrationViewModel: RegistrationViewModel,
    onRequestGoogleFitPermission: () -> Unit
) {
    val uiState by registrationViewModel.registrationUIState
    val isSyncing by registrationViewModel.isSyncing
    val syncMessage by registrationViewModel.syncMessage
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HeadingTextComponent(value = stringResource(id = R.string.HealthData))

        ButtonComponent(
            value = if (isSyncing) "Syncing..." else "Sync with Google Fit",
            onButtonClicked = {
                registrationViewModel.setIsSyncing(true)
                registrationViewModel.setSyncMessage(null)
                onRequestGoogleFitPermission()
                coroutineScope.launch {
                    syncWithGoogleFit(context, registrationViewModel)
                }
            },
            isEnabled = !isSyncing
        )

        syncMessage?.let { message ->
            Text(
                text = message,
                color = if (message.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        MyTextField(
            labelValue = "Blood Pressure (e.g. 80/120)",
            initialValue = uiState.bloodPressure,
            onTextSelected = { registrationViewModel.updateHealthParameters(bloodPressure = it) },
        )
        if (uiState.bloodPressureError) {
            Text(text = "Invalid blood pressure format", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(8.dp))

        MyTextField(
            labelValue = "Heart Rate (e.g. 70)",
            initialValue = uiState.heartRate,
            onTextSelected = { registrationViewModel.updateHealthParameters(heartRate = it) },
        )
        if (uiState.heartRateError) {
            Text(text = "Invalid heart rate", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(8.dp))

        MyTextField(
            labelValue = "Blood Sugar Levels (e.g. 100)",
            initialValue = uiState.bloodSugar,
            onTextSelected = { registrationViewModel.updateHealthParameters(bloodSugar = it) },
        )
        if (uiState.bloodSugarError) {
            Text(text = "Invalid blood sugar level", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(8.dp))

        MyTextField(
            labelValue = "Cholesterol Levels (e.g. 200)",
            initialValue = uiState.cholesterol,
            onTextSelected = { registrationViewModel.updateHealthParameters(cholesterol = it) },
        )
        if (uiState.cholesterolError) {
            Text(text = "Invalid cholesterol level", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))

        ButtonComponent(
            value = "Submit Health Data",
            onButtonClicked = {
                registrationViewModel.sendHealthDataToFirestore()
            },
            isEnabled = true
        )
    }
}

private suspend fun syncWithGoogleFit(
    context: android.content.Context,
    registrationViewModel: RegistrationViewModel
) {
    try {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            registrationViewModel.setSyncMessage("Google Fit permissions not granted. Please grant permissions and try again.")
            return
        }

        var dataFound = false
        var heartRateFound = false
        var bloodPressureFound = false

        // Read heart rate
        try {
            val heartRateResult = Fitness.getHistoryClient(context, account)
                .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
                .await()
            if (!heartRateResult.isEmpty) {
                val heartRate = heartRateResult.dataPoints.firstOrNull()
                    ?.getValue(Field.FIELD_AVERAGE)?.asFloat()
                if (heartRate != null) {
                    registrationViewModel.updateHealthParameters(heartRate = heartRate.toString())
                    dataFound = true
                    heartRateFound = true
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleFit", "Error reading heart rate data: ${e.message}")
        }

        // Read blood pressure
        try {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 7 * 24 * 60 * 60 * 1000 // 7 days ago

            val readRequest = DataReadRequest.Builder()
                .read(HealthDataTypes.TYPE_BLOOD_PRESSURE)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val bloodPressureResponse = Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .await()

            Log.d("GoogleFit", "Blood Pressure Response: ${bloodPressureResponse.dataSets}")

            for (dataSet in bloodPressureResponse.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    val systolic = dataPoint.getValue(HealthDataTypes.TYPE_BLOOD_PRESSURE.fields[0]).asFloat()
                    val diastolic = dataPoint.getValue(HealthDataTypes.TYPE_BLOOD_PRESSURE.fields[1]).asFloat()
                    Log.d("GoogleFit", "Blood Pressure: $systolic / $diastolic")
                    registrationViewModel.updateHealthParameters(bloodPressure = "$systolic/$diastolic")
                    dataFound = true
                    bloodPressureFound = true
                    break // Just take the most recent reading
                }
                if (bloodPressureFound) break
            }
        } catch (e: Exception) {
            Log.e("GoogleFit", "Error reading blood pressure data: ${e.message}")
        }

        if (dataFound) {
            var message = "Sync successful. "
            if (heartRateFound) message += "Heart rate data retrieved. "
            if (bloodPressureFound) message += "Blood pressure data retrieved. "
            if (!heartRateFound) message += "No recent heart rate data found. "
            if (!bloodPressureFound) message += "No recent blood pressure data found. "
            registrationViewModel.setSyncMessage(message.trim())
        } else {
            registrationViewModel.setSyncMessage("No recent health data found in Google Fit. Make sure you have recorded data recently.")
        }
    } catch (e: Exception) {
        registrationViewModel.setSyncMessage("Error syncing with Google Fit: ${e.message}")
        Log.e("GoogleFit", "Error syncing with Google Fit", e)
    } finally {
        registrationViewModel.setIsSyncing(false)
    }
}

@Composable
@Preview
fun HealthDataScreenPreview() {
    HealthDataScreen(RegistrationViewModel(), {})
}