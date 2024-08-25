package com.project.wellnesswise.components.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.data.HealthFields
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

object HealthDataUtils {
    suspend fun fetchHealthData(
        context: Context,
        account: GoogleSignInAccount
    ): Map<String, Any> {
        val healthData = mutableMapOf<String, Any>()

        // Read heart rate
        val heartRateResult = Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .await()
        if (!heartRateResult.isEmpty) {
            healthData["heartRate"] = heartRateResult.dataPoints.firstOrNull()
                ?.getValue(Field.FIELD_AVERAGE)?.asFloat() ?: 0f
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000 // 24 hours ago
        val readRequest = com.google.android.gms.fitness.request.DataReadRequest.Builder()
            .read(HealthDataTypes.TYPE_BLOOD_PRESSURE)
            .read(HealthDataTypes.TYPE_BLOOD_GLUCOSE)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        val dataResponse = Fitness.getHistoryClient(context, account)
            .readData(readRequest)
            .await()

        for (dataSet in dataResponse.dataSets) {
            when (dataSet.dataType) {
                HealthDataTypes.TYPE_BLOOD_PRESSURE -> {
                    for (dataPoint in dataSet.dataPoints.sortedByDescending { it.getEndTime(TimeUnit.MILLISECONDS) }) {
                        val systolic = dataPoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC).asFloat()
                        val diastolic = dataPoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC).asFloat()
                        healthData["bloodPressure"] = "${systolic.toInt()}/${diastolic.toInt()}"
                        break // Take the most recent reading
                    }
                }
                HealthDataTypes.TYPE_BLOOD_GLUCOSE -> {
                    for (dataPoint in dataSet.dataPoints.sortedByDescending { it.getEndTime(TimeUnit.MILLISECONDS) }) {
                        healthData["bloodSugar"] = dataPoint.getValue(HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL).asFloat()
                        break // Take the most recent reading
                    }
                }
            }
        }

        healthData["lastUpdated"] = com.google.firebase.Timestamp.now()

        return healthData
    }
}