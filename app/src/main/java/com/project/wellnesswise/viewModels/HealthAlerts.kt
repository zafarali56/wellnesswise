package com.project.wellnesswise.viewModels

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.MainActivity
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class HealthAlerts(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result {
        createNotificationChannel()
        return performHealthCheck()
    }


    private suspend fun performHealthCheck(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()

        try {
            Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "Starting health check for user: $userId")
            val userData = firestore.collection("users").document(userId).get().await().data
            if (userData != null) {
                Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "User viewModels retrieved: $userData")
                checkHealthParameters(userData)
            } else {
                Log.w("com.project.wellnesswise.viewModels.HealthAlerts", "No user viewModels found for user: $userId")
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("com.project.wellnesswise.viewModels.HealthAlerts", "Error during health check: ${e.message}")
            return Result.retry()
        }
    }

    private fun checkHealthParameters(userData: Map<String, Any>) {
        // Blood Pressure Check
        val bloodPressure = userData["bloodPressure"] as? String
        if (bloodPressure != null) {
            val (systolic, diastolic) = bloodPressure.split("/").map { it.toInt() }
            Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "Checking blood pressure: $systolic/$diastolic")
            if (systolic >= 140 || diastolic >= 90) {
                sendNotification(
                    "blood_pressure",
                    "High Blood Pressure Alert",
                    "Your blood pressure ($bloodPressure) is above normal range.",
                    "Please consult your healthcare provider if this persists."
                )
            }
        }

        // Heart Rate Check
        val heartRateValue = when (val heartRate = userData["heartRate"]) {
            is Number -> heartRate.toInt()
            is String -> heartRate.toIntOrNull()
            else -> null
        }
        Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "Checking heart rate: $heartRateValue")
        if (heartRateValue != null && (heartRateValue >= 100 || heartRateValue < 60)) {
            sendNotification(
                "heart_rate",
                "Abnormal Heart Rate Alert",
                "Your heart rate ($heartRateValue BPM) needs attention.",
                "Consider checking your heart rate again in a calm state."
            )
        }

        // Blood Sugar Check
        val bloodSugarValue = when (val bloodSugar = userData["bloodSugar"]) {
            is Number -> bloodSugar.toDouble()
            is String -> bloodSugar.toDoubleOrNull()
            else -> null
        }
        Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "Checking blood sugar: $bloodSugarValue")
        if (bloodSugarValue != null && bloodSugarValue > 200.0) {
            sendNotification(
                "blood_sugar",
                "High Blood Sugar Alert",
                "Your blood sugar ($bloodSugarValue mg/dL) is elevated.",
                "Check your insulin levels and follow your diabetes management plan."
            )
        }

        // Cholesterol Check
        val cholesterolValue = when (val cholesterol = userData["cholesterol"]) {
            is Number -> cholesterol.toDouble()
            is String -> cholesterol.toDoubleOrNull()
            else -> null
        }
        Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "Checking cholesterol: $cholesterolValue")
        if (cholesterolValue != null && cholesterolValue > 240.0) {
            sendNotification(
                "cholesterol",
                "High Cholesterol Alert",
                "Your cholesterol ($cholesterolValue mg/dL) is above recommended levels.",
                "Consider dietary changes and consult your healthcare provider."
            )
        }
    }

    private fun sendNotification(
        id: String,
        title: String,
        shortContent: String,
        expandedContent: String
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = when(id) {
            "blood_pressure" -> 1001
            "heart_rate" -> 1002
            "blood_sugar" -> 1003
            "cholesterol" -> 1004
            else -> System.currentTimeMillis().toInt()
        }

        // Create action buttons for the notification
        val actionIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification with system icon
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // System alert icon
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$shortContent\n\n$expandedContent"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(applicationContext, android.R.color.holo_red_light))
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "Notification sent: $title - $shortContent")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Health Alerts"
            val descriptionText = "Important alerts about your health parameters"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "health_alerts_channel"
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123

        fun startPeriodicMonitoring(context: Context) {
            if (!hasNotificationPermission(context)) {
                Log.w("com.project.wellnesswise.viewModels.HealthAlerts", "Notification permission not granted")
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val monitoringWork = PeriodicWorkRequestBuilder<HealthAlerts>(
                15, TimeUnit.MINUTES  // Using 15 minutes for testing
            ).setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "health_monitoring",
                ExistingPeriodicWorkPolicy.UPDATE,
                monitoringWork
            )
            Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "Periodic monitoring started")
        }

        fun performImmediateHealthCheck(context: Context) {
            if (!hasNotificationPermission(context)) {
                Log.w("com.project.wellnesswise.viewModels.HealthAlerts", "Notification permission not granted")
                return
            }

            val workRequest = OneTimeWorkRequestBuilder<HealthAlerts>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d("com.project.wellnesswise.viewModels.HealthAlerts", "Immediate health check enqueued")
        }

        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }
}