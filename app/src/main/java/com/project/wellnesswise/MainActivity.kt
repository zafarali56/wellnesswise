package com.project.wellnesswise

import DataVisualizationViewModel
import HealthDataSyncWorker
import HealthDataViewModel
import HomeViewModel
import LoginViewModel
import PersonalizedRecommendationsViewModel
import PersonalizedRecommendationsViewModelFactory
import PredictionsViewModelFactory
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.project.wellnesswise.app.WellnessWiseApp
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.PredictionsViewModel
import com.project.wellnesswise.data.RegistrationViewModel

class MainActivity : ComponentActivity() {
    private lateinit var googleFitPermissionLauncher: ActivityResultLauncher<Intent>
    private var onPermissionGranted: (() -> Unit)? = null
    private lateinit var updateReceiver: BroadcastReceiver
    private val homeViewModel: HomeViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore for real-time updates
        configureFirestore()

        // Schedule periodic health data sync
        scheduleHealthDataSync()

        // Set up Google Fit permission launcher
        setupGoogleFitPermissionLauncher()

        // Register broadcast receiver for health data updates
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
            val authViewModel: AuthViewModel = viewModel {
                AuthViewModel(registrationViewModel, loginViewModel, healthDataViewModel, predictionsViewModel, dataVisualizationViewModel)
            }
            val personalizedRecommendationsViewModel: PersonalizedRecommendationsViewModel = viewModel(
                factory = PersonalizedRecommendationsViewModelFactory(
                    FirebaseFirestore.getInstance(),
                    FirebaseAuth.getInstance(),
                    predictionsViewModel
                )
            )

            WellnessWiseApp(
                homeViewModel = homeViewModel,
                registrationViewModel = registrationViewModel,
                loginViewModel = loginViewModel,
                authViewModel = authViewModel,
                healthDataViewModel = healthDataViewModel,
                onRequestGoogleFitPermission = {
                    requestGoogleFitPermissions()
                },
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

    private fun requestGoogleFitPermissions() {
        val fitnessOptions =
            FitnessOptions
                .builder()
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
                .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
                .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            val signInOptions =
                GoogleSignInOptions
                    .Builder()
                    .addExtension(fitnessOptions)
                    .build()
            googleFitPermissionLauncher.launch(GoogleSignIn.getClient(this, signInOptions).signInIntent)
        } else {
            Log.i(TAG, "Google Fit permissions already granted")
            onPermissionGranted?.invoke()
            scheduleHealthDataSync() // Trigger a sync if permissions are already granted
        }
    }

    private fun registerUpdateReceiver() {
        updateReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    if (intent?.action == "com.project.wellnesswise.HEALTH_DATA_UPDATED") {
                        Log.d(TAG, "Received health data update broadcast")
                        homeViewModel.refreshData()
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

    override fun onResume() {
        super.onResume()
        homeViewModel.checkForActiveSession()

        // Trigger an immediate sync when the app comes to the foreground
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
