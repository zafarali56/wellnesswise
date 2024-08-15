package com.project.wellnesswise.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun HealthDataWatchScreen(
    viewModel: RegistrationViewModel = viewModel(),
    onRequestGoogleFitPermission: () -> Unit
) {
    var isSyncing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeadingTextComponent(value = "Sync Health Data from Watch")
        Spacer(modifier = Modifier.height(16.dp))

        ButtonComponent(
            value = if (isSyncing) "Syncing..." else "Sync Heart Rate Data",
            onButtonClicked = {
                isSyncing = true
                errorMessage = null
                onRequestGoogleFitPermission()
                syncHeartRateData(context, viewModel,
                    onSuccess = {
                        isSyncing = false
                    },
                    onError = { error ->
                        isSyncing = false
                        errorMessage = error
                    },
                    onNoData = {
                        isSyncing = false
                        errorMessage = "No heart rate data found. Please use manual input."
                        // Delay navigation to allow user to read the message
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(3000) // 3 seconds delay
                            WellnessWiseAppRouter.navigateTo(Screen.HealthDataScreen)
                        }
                    }
                )
            },
            isEnabled = !isSyncing
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ButtonComponent(
            value = "Enter Data Manually",
            onButtonClicked = {
                WellnessWiseAppRouter.navigateTo(Screen.HealthDataScreen)
            },
            isEnabled = true
        )
    }
}

private fun syncHeartRateData(
    context: Context,
    viewModel: RegistrationViewModel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onNoData: () -> Unit
) {
    val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()

    val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
        GoogleSignIn.requestPermissions(
            context as Activity,
            REQUEST_OAUTH_REQUEST_CODE,
            account,
            fitnessOptions
        )
    } else {
        accessGoogleFit(context, account, viewModel, onSuccess, onError, onNoData)
    }
}

private fun accessGoogleFit(
    context: Context,
    account: GoogleSignInAccount,
    viewModel: RegistrationViewModel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onNoData: () -> Unit
) {
    val endTime = System.currentTimeMillis()
    val startTime = endTime - TimeUnit.HOURS.toMillis(1) // Get last hour of data

    val readRequest = DataReadRequest.Builder()
        .read(DataType.TYPE_HEART_RATE_BPM)
        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        .build()

    Fitness.getHistoryClient(context, account)
        .readData(readRequest)
        .addOnSuccessListener { response ->
            if (processHeartRateData(response, viewModel)) {
                onSuccess()
            } else {
                onNoData()
            }
        }
        .addOnFailureListener { e ->
            onError("Error reading data: ${e.message}")
        }
}

private fun processHeartRateData(response: DataReadResponse, viewModel: RegistrationViewModel): Boolean {
    val dataSet = response.getDataSet(DataType.TYPE_HEART_RATE_BPM)
    return if (dataSet.isEmpty) {
        false // No data available
    } else {
        val lastHeartRate = dataSet.dataPoints.last().getValue(Field.FIELD_BPM).asFloat()
        viewModel.updateHeartRate(lastHeartRate)
        true
    }
}

private const val REQUEST_OAUTH_REQUEST_CODE = 1

@Composable
@Preview
fun HealthDataWatchScreenPreview() {
    HealthDataWatchScreen(onRequestGoogleFitPermission = {})
}