package com.project.wellnesswise.viewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.viewModels.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val TAG = LoginViewModel::class.simpleName
    var loginUIState = mutableStateOf(LoginUIState())
        private set
    var validationResults = mutableStateOf(emptyMap<String, Boolean>())
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set
    private val _dataSourcePreference = mutableStateOf(
        RegistrationViewModel.DataSourcePreference.MANUAL)

    var logInProgress = mutableStateOf(false)
    var needsGoogleFitPermissions = mutableStateOf(false)
    var isLoggedIn = mutableStateOf(false)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
        .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
        .build()

    fun onEvent(event: LoginUIEvent) {
        when (event) {
            is LoginUIEvent.EmailChangedLogin -> {
                loginUIState.value = loginUIState.value.copy(email = event.email)
                updateValidationResults()
            }
            is LoginUIEvent.PasswordChangedLogin -> {
                loginUIState.value = loginUIState.value.copy(password = event.password)
                updateValidationResults()
            }
            is LoginUIEvent.LoginButtonClicked -> {
                if (Validator.isValidLoginUIState(loginUIState.value)) {
                    login()
                } else {
                    Log.d(TAG, "Validation failed")
                }
            }
        }

    }

    private fun updateValidationResults() {
        validationResults.value = Validator.validateLoginUIState(loginUIState.value)
    }

    private fun login() {
        logInProgress.value = true
        Log.d(TAG, "Inside Login")
        printState()

        auth.signInWithEmailAndPassword(loginUIState.value.email, loginUIState.value.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.reload()?.addOnCompleteListener { reloadTask ->
                        if (reloadTask.isSuccessful) {
                            if (user.isEmailVerified) {
                                fetchUserDataSourcePreference(user.uid)
                            } else {
                                logInProgress.value = false
                                Log.d(TAG, "Email not verified")
                                WellnessWiseAppRouter.navigateTo(Screen.EmailVerificationScreen)
                            }
                        } else {
                            logInProgress.value = false
                            Log.w(TAG, "Error reloading user", reloadTask.exception)
                            errorMessage.value = reloadTask.exception?.message ?: "Error reloading user"
                        }
                    }
                } else {
                    logInProgress.value = false
                    Log.w(TAG, "Login failed", task.exception)
                    errorMessage.value = task.exception?.message ?: "Login failed"
                }
            }
    }

    private fun fetchUserDataSourcePreference(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val preference = document.getString("dataSourcePreference")
                    _dataSourcePreference.value = when (preference) {
                        "GOOGLE_FIT" -> RegistrationViewModel.DataSourcePreference.GOOGLE_FIT
                        else -> RegistrationViewModel.DataSourcePreference.MANUAL
                    }
                    logInProgress.value = false
                    isLoggedIn.value = true
                    Log.d(TAG, "Login successful")
                    if (_dataSourcePreference.value == RegistrationViewModel.DataSourcePreference.GOOGLE_FIT) {
                        needsGoogleFitPermissions.value = true
                    } else {
                        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
                    }
                } else {
                    Log.d(TAG, "No such document")
                    logInProgress.value = false
                    errorMessage.value = "Error fetching user viewModels"
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
                logInProgress.value = false
                errorMessage.value = "Error fetching user viewModels"
            }
    }

    fun checkGoogleFitPermissions(context: Context) {
        viewModelScope.launch {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                needsGoogleFitPermissions.value = true
            } else {
                needsGoogleFitPermissions.value = false
                WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
            }
        }
    }

    fun onGoogleFitPermissionResult(result: Boolean) {
        if (result) {
            needsGoogleFitPermissions.value = false
            WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
        } else {
            errorMessage.value = "Google Fit permissions are required for full functionality"
            needsGoogleFitPermissions.value = false
        }
    }

    fun onGoogleFitPermissionDismissed() {
        needsGoogleFitPermissions.value = false
        errorMessage.value = "Google Fit permissions are required for full functionality"
    }

    private fun printState() {
        Log.d(TAG, "Inside printState")
        Log.d(TAG, loginUIState.value.toString())
    }

    fun resetLoginUIState() {
        errorMessage.value = null
        loginUIState.value = LoginUIState()
        logInProgress.value = false
        needsGoogleFitPermissions.value = false
        isLoggedIn.value = false
    }
}