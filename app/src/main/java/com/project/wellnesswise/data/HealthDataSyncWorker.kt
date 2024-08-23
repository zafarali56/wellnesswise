import android.content.Context
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
                .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "healthDataSync",
                ExistingPeriodicWorkPolicy.REPLACE,
                syncRequest
            )

            // Schedule an immediate sync
            val immediateSync = OneTimeWorkRequestBuilder<HealthDataSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(immediateSync)
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
            val healthData = fetchHealthData(account)
            updateFirestore(healthData)
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing health data", e)
            return Result.retry()
        }
    }

    private suspend fun fetchHealthData(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount): Map<String, Any> {
        val healthData = mutableMapOf<String, Any>()

        // Read heart rate
        val heartRateResult = Fitness.getHistoryClient(applicationContext, account)
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .await()
        if (!heartRateResult.isEmpty) {
            healthData["heartRate"] = heartRateResult.dataPoints.firstOrNull()
                ?.getValue(Field.FIELD_AVERAGE)?.asFloat() ?: 0f
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 15 * 60 * 1000 // 15 minutes ago
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

    private suspend fun updateFirestore(healthData: Map<String, Any>) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            if (healthData.isNotEmpty()) {
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .set(healthData, com.google.firebase.firestore.SetOptions.merge())
                    .await()
                Log.d(TAG, "Health data updated successfully: $healthData")
            } else {
                Log.d(TAG, "No new health data to update")
            }
        } else {
            Log.e(TAG, "User not authenticated")
            throw Exception("User not authenticated")
        }
    }
}