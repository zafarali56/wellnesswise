package com.project.wellnesswise

import HealthDataSyncWorker
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.project.wellnesswise.app.WellnessWiseApp
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.RegistrationViewModel

class MainActivity : ComponentActivity() {

    private lateinit var googleFitPermissionLauncher: ActivityResultLauncher<Intent>
    private var onPermissionGranted: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore
        configureFirestore()

        // Schedule periodic health data sync
        HealthDataSyncWorker.startPeriodicSync(this)

        // Set up Google Fit permission launcher
        googleFitPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.i(TAG, "Google Fit permissions granted")
                onPermissionGranted?.invoke()
            } else {
                Log.e(TAG, "Google Fit permissions not granted")
            }
        }

        setContent {
            val registrationViewModel: RegistrationViewModel = viewModel()
            WellnessWiseApp(
                homeViewModel = viewModel(),
                registrationViewModel = registrationViewModel,
                loginViewModel = viewModel(),
                authViewModel = AuthViewModel(),
                onRequestGoogleFitPermission = {
                    requestGoogleFitPermissions()
                }
            )
        }
    }

    private fun configureFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firestore.firestoreSettings = settings
    }

    private fun requestGoogleFitPermissions() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            val signInOptions = GoogleSignInOptions.Builder()
                .addExtension(fitnessOptions)
                .build()
            googleFitPermissionLauncher.launch(GoogleSignIn.getClient(this, signInOptions).signInIntent)
        } else {
            Log.i(TAG, "Google Fit permissions already granted")
            onPermissionGranted?.invoke()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}