import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.data.HealthFields
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class HealthDataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "HealthDataSyncWorker"

        fun startPeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<HealthDataSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "healthDataSync",
                ExistingPeriodicWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getAccountForExtension(applicationContext, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.e(TAG, "Google Fit permissions not granted")
            return Result.failure()
        }

        try {
            var heartRate: Float? = null
            var bloodPressure: String? = null
            var bloodSugar: Float? = null

            // Read heart rate
            val heartRateResult = Fitness.getHistoryClient(applicationContext, account)
                .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
                .await()
            if (!heartRateResult.isEmpty) {
                heartRate = heartRateResult.dataPoints.firstOrNull()
                    ?.getValue(Field.FIELD_AVERAGE)?.asFloat()
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - 24 * 60 * 60 * 1000 // 24 hours ago
            val readRequest = com.google.android.gms.fitness.request.DataReadRequest.Builder()
                .read(HealthDataTypes.TYPE_BLOOD_PRESSURE)
                .read(HealthDataTypes.TYPE_BLOOD_GLUCOSE)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val dataResponse = Fitness.getHistoryClient(applicationContext, account)
                .readData(readRequest)
                .await()

            for (dataSet in dataResponse.dataSets) {
                when (dataSet.dataType) {
                    HealthDataTypes.TYPE_BLOOD_PRESSURE -> {
                        for (dataPoint in dataSet.dataPoints.sortedByDescending { it.getEndTime(TimeUnit.MILLISECONDS) }) {
                            val systolic = dataPoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC).asFloat()
                            val diastolic = dataPoint.getValue(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC).asFloat()
                            bloodPressure = "${systolic.toInt()}/${diastolic.toInt()}"
                            break // Take the most recent reading
                        }
                    }
                    HealthDataTypes.TYPE_BLOOD_GLUCOSE -> {
                        for (dataPoint in dataSet.dataPoints.sortedByDescending { it.getEndTime(TimeUnit.MILLISECONDS) }) {
                            bloodSugar = dataPoint.getValue(HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL).asFloat()
                            break // Take the most recent reading
                        }
                    }
                }
            }

            // Update Firestore
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val healthData = mutableMapOf<String, Any>()
                heartRate?.let { healthData["heartRate"] = it }
                bloodPressure?.let { healthData["bloodPressure"] = it }
                bloodSugar?.let { healthData["bloodSugar"] = it }

                if (healthData.isNotEmpty()) {
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .update(healthData)
                        .await()
                    Log.d(TAG, "Health data updated successfully: $healthData")

                    // Notify the app about the update
                    val intent = Intent("com.project.wellnesswise.HEALTH_DATA_UPDATED")
                    applicationContext.sendBroadcast(intent)
                } else {
                    Log.d(TAG, "No new health data to update")
                }
            } else {
                Log.e(TAG, "User not authenticated")
                return Result.failure()
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing health data", e)
            return Result.retry()
        }
    }
}