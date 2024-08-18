package com.project.wellnesswise.data

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.data.HealthFields

import kotlinx.coroutines.tasks.await


class HealthDataViewModel : ViewModel() {
    public suspend fun handleGoogleFitSync(
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

    suspend fun syncWithGoogleFit(
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

}