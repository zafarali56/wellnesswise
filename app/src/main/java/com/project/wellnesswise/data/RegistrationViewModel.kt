package com.project.wellnesswise.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

class RegistrationViewModel : ViewModel() {
    var registrationUIState = mutableStateOf(RegistrationUIState())
        private set
    var validationResults = mutableStateOf(emptyMap<String, Boolean>())
        private set
    var signUpInProgress = mutableStateOf(false)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun updateHealthParameters(
        bloodPressure: String,
        heartRate: String,
        bloodSugar: String,
        cholesterol: String
    ) {
        val validationResults = Validator.validateHealthParameters(bloodPressure, heartRate, bloodSugar, cholesterol)

        registrationUIState.value = registrationUIState.value.copy(
            bloodPressure = bloodPressure,
            heartRate = heartRate,
            bloodSugar = bloodSugar,
            cholesterol = cholesterol,
            bloodPressureError = !validationResults["bloodPressure"]!!,
            heartRateError = !validationResults["heartRate"]!!,
            bloodSugarError = !validationResults["bloodSugar"]!!,
            cholesterolError = !validationResults["cholesterol"]!!
        )
    }

    fun sendHealthDataToFirestore() {
        val uiState = registrationUIState.value
        val user = auth.currentUser
        if (user != null) {
            val healthData = mapOf(
                "bloodPressure" to uiState.bloodPressure,
                "heartRate" to uiState.heartRate,
                "bloodSugar" to uiState.bloodSugar,
                "cholesterol" to uiState.cholesterol
            )
            firestore.collection("users").document(user.uid)
                .update(healthData)
                .addOnSuccessListener {
                    Log.d(TAG, "Health data updated successfully")
                    WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating health data", e)
                }
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
