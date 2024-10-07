import android.app.Activity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.work.*
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit

class HealthAlerts(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result {
        return performHealthCheck()
    }

    private suspend fun performHealthCheck(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()

        try {
            Log.d("HealthAlerts", "Starting health check for user: $userId")
            val userData = firestore.collection("users").document(userId).get().await().data
            if (userData != null) {
                Log.d("HealthAlerts", "User data retrieved: $userData")
                checkHealthParameters(userData)
            } else {
                Log.w("HealthAlerts", "No user data found for user: $userId")
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("HealthAlerts", "Error during health check: ${e.message}")
            return Result.retry()
        }
    }

    private fun checkHealthParameters(userData: Map<String, Any>) {
        // Check blood pressure
        val bloodPressure = userData["bloodPressure"] as? String
        if (bloodPressure != null) {
            val (systolic, diastolic) = bloodPressure.split("/").map { it.toInt() }
            Log.d("HealthAlerts", "Blood pressure: $systolic/$diastolic")
            if (systolic >= 140 || diastolic >= 90) {
                Log.d("HealthAlerts", "High blood pressure detected. Sending notification.")
                sendNotification("High Blood Pressure Alert", "Your blood pressure is high ($bloodPressure). Please consult your doctor.")
            }
        }


        // Check heart rate
        val heartRate = userData["heartRate"] as? Long
        if (heartRate != null) {
            if (heartRate > 100 || heartRate < 60) {
                sendNotification("Abnormal Heart Rate Alert", "Your heart rate is outside the normal range. Please monitor closely.")
            }
        }

        // Check blood sugar
        val bloodSugar = userData["bloodSugar"] as? Long
        if (bloodSugar != null) {
            if (bloodSugar > 200) {
                sendNotification("High Blood Sugar Alert", "Your blood sugar level is high. Please check your insulin.")
            }
        }

        // Check cholesterol
        val cholesterol = userData["cholesterol"] as? Long
        if (cholesterol != null) {
            if (cholesterol > 240) {
                sendNotification("High Cholesterol Alert", "Your cholesterol level is high. Consider dietary changes and consult your doctor.")
            }
        }

    }

    private fun sendNotification(title: String, content: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "health_alerts",
                "Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "health_alerts")
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Built-in alert icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d("HealthAlerts", "Notification sent: $title - $content")
    }

    companion object {
        fun startPeriodicMonitoring(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val monitoringWork = PeriodicWorkRequestBuilder<HealthAlerts>(
                1, TimeUnit.HOURS
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "health_monitoring",
                ExistingPeriodicWorkPolicy.REPLACE,
                monitoringWork
            )
            Log.d("HealthAlerts", "Periodic monitoring started")
        }

        fun performImmediateHealthCheck(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<HealthAlerts>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d("HealthAlerts", "Immediate health check enqueued")
        }



        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Permission is granted by default for Android 12 and below
            }
        }

        fun requestNotificationPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasNotificationPermission(activity)) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123



    }


}