import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.components.data.HealthDataUtils
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class HealthDataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "HealthDataSyncWorker"
        private const val WORK_NAME = "healthDataSync"

        fun startPeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<HealthDataSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
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

        return try {
            val googleFitData = HealthDataUtils.fetchHealthData(applicationContext, account)
            val existingData = fetchExistingHealthData()
            val mergedData = mergeHealthData(existingData, googleFitData)
            updateFirestore(mergedData)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing health data", e)
            Result.retry()
        }
    }

    private suspend fun fetchExistingHealthData(): Map<String, Any> {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) {
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get()
                .await()
            document.data ?: emptyMap()
        } else {
            emptyMap()
        }
    }

    private fun mergeHealthData(existingData: Map<String, Any>, googleFitData: Map<String, Any>): Map<String, Any> {
        val mergedData = existingData.toMutableMap()

        // Check global data source preference
        val globalDataSource = mergedData["dataSourcePreference"] as? String ?: "MANUAL"

        if (globalDataSource == "GOOGLE_FIT") {
            // If global preference is Google Fit, update all data from Google Fit
            for ((key, value) in googleFitData) {
                mergedData[key] = value
                mergedData["${key}Source"] = "GOOGLE_FIT"
            }
        } else {
            // If global preference is manual, only update Google Fit sourced data
            for ((key, value) in googleFitData) {
                val sourceKey = "${key}Source"
                if (sourceKey !in mergedData || mergedData[sourceKey] == "GOOGLE_FIT") {
                    mergedData[key] = value
                    mergedData[sourceKey] = "GOOGLE_FIT"
                }
            }
        }

        return mergedData
    }

    private suspend fun updateFirestore(healthData: Map<String, Any>) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .set(healthData, com.google.firebase.firestore.SetOptions.merge())
                .await()
            Log.d(TAG, "Health data updated successfully: $healthData")
        } else {
            throw Exception("User not authenticated")
        }
    }
}