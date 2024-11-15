package com.project.wellnesswise

import com.project.wellnesswise.viewModels.DataVisualizationViewModel
import com.project.wellnesswise.viewModels.HealthAlerts
import com.project.wellnesswise.viewModels.HealthDataSyncWorker
import com.project.wellnesswise.viewModels.HealthDataViewModel
import com.project.wellnesswise.viewModels.HomeViewModel
import com.project.wellnesswise.viewModels.LoginViewModel
import PersonalizedRecommendationsViewModel
import PersonalizedRecommendationsViewModelFactory
import com.project.wellnesswise.viewModels.PredictionsViewModelFactory
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.project.wellnesswise.app.WellnessWiseApp
import com.project.wellnesswise.viewModels.AuthViewModel
import com.project.wellnesswise.viewModels.PredictionsViewModel
import com.project.wellnesswise.viewModels.RegistrationViewModel
import android.Manifest // import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    private lateinit var googleFitPermissionLauncher: ActivityResultLauncher<Intent>
    private var onPermissionGranted: (() -> Unit)? = null
    private lateinit var updateReceiver: BroadcastReceiver
    private val homeViewModel: HomeViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted: Boolean ->
        if (isGranted) {
            HealthAlerts.startPeriodicMonitoring(this)
        } else {
            // Inform the user that the permission was denied
            Toast.makeText(this, "Notification permission denied. You won't receive health alerts.", Toast.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!HealthAlerts.hasNotificationPermission(this)) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            HealthAlerts.startPeriodicMonitoring(this)
        }
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore for real-time updates
        configureFirestore()

        // Schedule periodic health viewModels sync
        scheduleHealthDataSync()

        // Set up Google Fit permission launcher
        setupGoogleFitPermissionLauncher()

        // Register broadcast receiver for health viewModels updates
        registerUpdateReceiver()

        // Set up auth state listener
        setupAuthStateListener()

        setContent {
            val registrationViewModel: RegistrationViewModel = viewModel()
            val loginViewModel: LoginViewModel = viewModel()
            val healthDataViewModel: HealthDataViewModel = viewModel()

            val predictionsViewModel: PredictionsViewModel = viewModel(
                factory = PredictionsViewModelFactory(applicationContext)
            )
            val dataVisualizationViewModel: DataVisualizationViewModel = viewModel()

            val personalizedRecommendationsViewModel: PersonalizedRecommendationsViewModel = viewModel(
                factory = PersonalizedRecommendationsViewModelFactory(
                    FirebaseFirestore.getInstance(),
                    FirebaseAuth.getInstance(),
                    predictionsViewModel
                )
            )
            val authViewModel: AuthViewModel = viewModel {
                AuthViewModel(registrationViewModel, loginViewModel, healthDataViewModel, predictionsViewModel, dataVisualizationViewModel)
            }
            WellnessWiseApp(
                homeViewModel = homeViewModel,
                registrationViewModel = registrationViewModel,
                loginViewModel = loginViewModel,
                authViewModel = authViewModel,
                healthDataViewModel = healthDataViewModel,
                dataVisualizationViewModel = dataVisualizationViewModel,
                personalizedRecommendationsViewModel = personalizedRecommendationsViewModel
            )
        }
    }

    private fun configureFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val settings =
            FirebaseFirestoreSettings
                .Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
        firestore.firestoreSettings = settings
    }

    private fun scheduleHealthDataSync() {
        HealthDataSyncWorker.startPeriodicSync(this)

        // Schedule an immediate sync
        val immediateSync =
            OneTimeWorkRequestBuilder<HealthDataSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
        WorkManager.getInstance(this).enqueue(immediateSync)
    }

    private fun setupGoogleFitPermissionLauncher() {
        googleFitPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    Log.i(TAG, "Google Fit permissions granted")
                    onPermissionGranted?.invoke()
                    scheduleHealthDataSync() // Trigger a sync after permissions are granted
                } else {
                    Log.e(TAG, "Google Fit permissions not granted")
                }
            }
    }

    private fun registerUpdateReceiver() {
        updateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.project.wellnesswise.HEALTH_DATA_UPDATED") {
                    Log.d(TAG, "Received health viewModels update broadcast")
                    homeViewModel.refreshData()
                    HealthAlerts.performImmediateHealthCheck(this@MainActivity)
                }
            }
        }
        registerReceiver(updateReceiver, IntentFilter("com.project.wellnesswise.HEALTH_DATA_UPDATED"))
    }


    private fun setupAuthStateListener() {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            homeViewModel.checkForActiveSession()
            if (firebaseAuth.currentUser != null) {
                scheduleHealthDataSync() // Trigger a sync when user logs in
            }
        }
    }
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            HealthAlerts.NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    HealthAlerts.startPeriodicMonitoring(this)
                } else {
                }
                return
            }
        }
    }
    override fun onResume() {
        super.onResume()
        homeViewModel.checkForActiveSession()

        val immediateSync =
            OneTimeWorkRequestBuilder<HealthDataSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
        WorkManager.getInstance(this).enqueue(immediateSync)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateReceiver)
    }
}
