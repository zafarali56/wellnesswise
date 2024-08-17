package com.project.wellnesswise.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.launch

class RegistrationViewModel : ViewModel() {
    var registrationUIState = mutableStateOf(RegistrationUIState())
        private set
    var validationResults = mutableStateOf(emptyMap<String, Boolean>())
        private set

    private val _isSyncing = mutableStateOf(false)
    val isSyncing: State<Boolean> = _isSyncing

    private val _syncMessage = mutableStateOf<String?>(null)
    val syncMessage: State<String?> = _syncMessage
    var signUpInProgress = mutableStateOf(false)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    fun setIsSyncing(syncing: Boolean) {
        _isSyncing.value = syncing
    }

    fun setSyncMessage(message: String?) {
        _syncMessage.value = message
    }
    fun updateHealthParameters(
        bloodPressure: String? = null,
        heartRate: String? = null,
        bloodSugar: String? = null,
        cholesterol: String? = null
    ) {
        val currentState = registrationUIState.value
        val newState = currentState.copy(
            bloodPressure = bloodPressure ?: currentState.bloodPressure,
            heartRate = heartRate ?: currentState.heartRate,
            bloodSugar = bloodSugar ?: currentState.bloodSugar,
            cholesterol = cholesterol ?: currentState.cholesterol
        )

        registrationUIState.value = newState
        validateHealthParameters(newState)
    }


    private fun validateHealthParameters(state: RegistrationUIState) {
        val validationResults = Validator.validateHealthParameters(
            state.bloodPressure,
            state.heartRate,
            state.bloodSugar,
            state.cholesterol
        )

        registrationUIState.value = state.copy(
            bloodPressureError = !validationResults["bloodPressure"]!!,
            heartRateError = !validationResults["heartRate"]!!,
            bloodSugarError = !validationResults["bloodSugar"]!!,
            cholesterolError = !validationResults["cholesterol"]!!
        )
    }
    fun sendHealthDataToFirestore(navigateToHome: Boolean = false) {
        setIsSyncing(true)
        setSyncMessage(null)
        val uiState = registrationUIState.value
        val user = auth.currentUser
        if (user != null) {
            val healthData = mapOf(
                "bloodPressure" to (uiState.bloodPressure.takeIf { it.isNotBlank() } ?: "N/A"),
                "heartRate" to (uiState.heartRate.takeIf { it.isNotBlank() } ?: "N/A"),
                "bloodSugar" to (uiState.bloodSugar.takeIf { it.isNotBlank() } ?: "N/A"),
                "cholesterol" to (uiState.cholesterol.takeIf { it.isNotBlank() } ?: "N/A")
            )
            firestore.collection("users").document(user.uid)
                .set(healthData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    setIsSyncing(false)
                    setSyncMessage("Health data updated successfully")
                    if (navigateToHome) {
                        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
                    }
                }
                .addOnFailureListener { e ->
                    setIsSyncing(false)
                    setSyncMessage("Error updating health data: ${e.localizedMessage}")
                }
        } else {
            setIsSyncing(false)
            setSyncMessage("Error: User not signed in")
        }
    }



    fun updateHeartRate(heartRate: Float) {
        registrationUIState.value = registrationUIState.value.copy(
            heartRate = heartRate.toString()
        )
        sendHealthDataToFirestore()
    }
    fun syncWithGoogleFit(heartRate: Float?, bloodPressure: String?, bloodSugar: Float?) {
        viewModelScope.launch {
            setIsSyncing(true)
            setSyncMessage(null)

            var dataUpdated = false

            heartRate?.let {
                updateHealthParameters(heartRate = it.toString())
                dataUpdated = true
            }

            bloodPressure?.let {
                updateHealthParameters(bloodPressure = it)
                dataUpdated = true
            }

            bloodSugar?.let {
                updateHealthParameters(bloodSugar = it.toString())
                dataUpdated = true
            }

            if (dataUpdated) {
                sendHealthDataToFirestore()
                setSyncMessage("Data synced successfully from Google Fit")
            } else {
                setSyncMessage("No new data found in Google Fit")
            }

            setIsSyncing(false)
        }
    }

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.EmailChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    email = event.email
                )
            }
            is UIEvent.FullNameChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    fullName = event.fullName
                )
            }
            is UIEvent.AgeChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    age = event.age
                )
            }
            is UIEvent.GenderChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    gender = event.gender
                )
            }
            is UIEvent.HeightChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    height = event.height
                )
            }
            is UIEvent.WeightChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    weight = event.weight
                )
            }
            is UIEvent.HabitsChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    habits = event.habits
                )
            }
            is UIEvent.MedicalHistoryChanged -> {
                val updatedMedicalHistory = registrationUIState.value.medicalHistory.toMutableMap()
                updatedMedicalHistory[event.question] = event.answer
                registrationUIState.value = registrationUIState.value.copy(medicalHistory = updatedMedicalHistory)
            }
            is UIEvent.PasswordChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    password = event.password
                )
            }
            is UIEvent.PolicyAcceptedChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    isPolicyAccepted = event.isPolicyAccepted
                )
            }
            is UIEvent.RegisterButtonClicked -> {
                updateValidationResults()
                if (Validator.isValidRegistrationUIState(registrationUIState.value)) {
                    createUserInFirebase(
                        email = registrationUIState.value.email,
                        password = registrationUIState.value.password,
                        fullName = registrationUIState.value.fullName,
                        age = registrationUIState.value.age,
                        gender = registrationUIState.value.gender,
                        height = registrationUIState.value.height,
                        weight = registrationUIState.value.weight,
                        habits = registrationUIState.value.habits,
                        medicalHistory = registrationUIState.value.medicalHistory
                    )
                } else {
                    // Show validation errors
                    Log.d(TAG, "Validation failed")
                }
            }
        }
    }

    private fun updateValidationResults() {
        validationResults.value = Validator.validateRegistrationUIState(registrationUIState.value)
    }

    private fun createUserInFirebase(
        email: String,
        password: String,
        fullName: String,
        age: Number,
        gender: Gender,
        height: Number,
        weight: Number,
        habits: List<Habit>,
        medicalHistory: Map<String, String>
    ) {
        signUpInProgress.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful)
                        // Send verification email
                        user.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Log.d(TAG, "Verification email sent")
                                    signUpInProgress.value = false
                                    WellnessWiseAppRouter.navigateTo(Screen.EmailVerificationScreen)
                                } else {
                                    signUpInProgress.value = false
                                    Log.w(TAG, "Error sending verification email", verificationTask.exception)
                                }
                            }
                    }
                } else {
                    signUpInProgress.value = false
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }
            }
    fun checkEmailVerification() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    val userData = mapOf(
                        "fullName" to registrationUIState.value.fullName,
                        "age" to registrationUIState.value.age,
                        "gender" to registrationUIState.value.gender.name,
                        "height" to registrationUIState.value.height,
                        "weight" to registrationUIState.value.weight,
                        "habits" to registrationUIState.value.habits.map { it.name },
                        "medicalHistory" to registrationUIState.value.medicalHistory
                    )
                    firestore.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(TAG, "User data stored successfully")
                            WellnessWiseAppRouter.navigateTo(Screen.HealthDataScreen)
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error storing user data", e)
                        }
                }
            } else {
                Log.w(TAG, "Error reloading user", task.exception)
            }
        }
    }


    fun resetRegistrationUIState() {
        registrationUIState.value = RegistrationUIState()
        validationResults.value = emptyMap()
        signUpInProgress.value = false
    }
}
