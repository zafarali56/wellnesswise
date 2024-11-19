package com.project.wellnesswise.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.components.ui.HealthAssessmentMode
import com.project.wellnesswise.viewModels.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

class RegistrationViewModel : ViewModel() {
    var registrationUIState = mutableStateOf(RegistrationUIState())
        private set
    var validationResults = mutableStateOf(emptyMap<String, Boolean>())
        private set

    private val _emailAlreadyInUse = mutableStateOf(false)
    val emailAlreadyInUse: State<Boolean> = _emailAlreadyInUse

    var signUpInProgress = mutableStateOf(false)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    var currentMode = mutableStateOf(HealthAssessmentMode.SIGNUP)

    private val _healthAssessmentValidated = mutableStateOf(false)
    val healthAssessmentValidated: State<Boolean> = _healthAssessmentValidated

    fun setMode(mode: HealthAssessmentMode) {
        currentMode.value = mode
        _healthAssessmentValidated.value = false
    }

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.EmailChanged -> {
                registrationUIState.value = registrationUIState.value.copy(email = event.email)
                _emailAlreadyInUse.value = false
                validateField("email")
            }
            is UIEvent.FullNameChanged -> {
                registrationUIState.value = registrationUIState.value.copy(fullName = event.fullName)
                validateField("fullName")
            }
            is UIEvent.AgeChanged -> {
                registrationUIState.value = registrationUIState.value.copy(age = event.age)
                validateField("age")
            }
            is UIEvent.GenderChanged -> {
                registrationUIState.value = registrationUIState.value.copy(gender = event.gender)
                validateField("gender")
            }
            is UIEvent.HeightChanged -> {
                registrationUIState.value = registrationUIState.value.copy(height = event.height)
                validateField("height")
            }
            is UIEvent.WeightChanged -> {
                registrationUIState.value = registrationUIState.value.copy(weight = event.weight)
                validateField("weight")
            }

            is UIEvent.PasswordChanged -> {
                registrationUIState.value = registrationUIState.value.copy(password = event.password)
                validateField("password")
            }
            is UIEvent.PolicyAcceptedChanged -> {
                registrationUIState.value = registrationUIState.value.copy(isPolicyAccepted = event.isPolicyAccepted)
                validateField("policyAccepted")
            }

            is UIEvent.FamilyDiabetesChanged -> {
                registrationUIState.value = registrationUIState.value.copy(familyDiabetes = event.value)
                validateField("familyDiabetes")
            }
            is UIEvent.FamilyHeartChanged -> {
                registrationUIState.value = registrationUIState.value.copy(familyHeart = event.value)
                validateField("familyHeart")
            }
            is UIEvent.FamilyCancerChanged -> {
                registrationUIState.value = registrationUIState.value.copy(familyCancer = event.value)
                validateField("familyCancer")
            }
            is UIEvent.PreviousSurgeriesChanged -> {
                registrationUIState.value = registrationUIState.value.copy(previousSurgeries = event.value)
                validateField("previousSurgeries")
            }
            is UIEvent.ChronicConditionsChanged -> {
                registrationUIState.value = registrationUIState.value.copy(chronicConditions = event.value)
                validateField("chronicConditions")
            }
            is UIEvent.SmokingChanged -> {
                registrationUIState.value = registrationUIState.value.copy(smoking = event.value)
                validateField("smoking")
            }
            is UIEvent.AlcoholConsumptionChanged -> {
                registrationUIState.value = registrationUIState.value.copy(alcoholConsumption = event.value)
                validateField("alcoholConsumption")
            }
            is UIEvent.PhysicalActivityChanged -> {
                registrationUIState.value = registrationUIState.value.copy(physicalActivity = event.value)
                validateField("physicalActivity")
            }
            is UIEvent.DietQualityChanged -> {
                registrationUIState.value = registrationUIState.value.copy(dietQuality = event.value)
                validateField("dietQuality")
            }
            is UIEvent.SleepHoursChanged -> {
                registrationUIState.value = registrationUIState.value.copy(sleepHours = event.value)
                validateField("sleepHours")
            }
            is UIEvent.AirQualityIndexChanged -> {
                registrationUIState.value = registrationUIState.value.copy(airQualityIndex = event.value)
                validateField("airQualityIndex")
            }
            is UIEvent.ExposureToPollutantsChanged -> {
                registrationUIState.value = registrationUIState.value.copy(exposureToPollutants = event.value)
                validateField("exposureToPollutants")
            }
            is UIEvent.StressLevelChanged -> {
                registrationUIState.value = registrationUIState.value.copy(stressLevel = event.value)
                validateField("stressLevel")
            }
            is UIEvent.AccessToHealthcareChanged -> {
                registrationUIState.value = registrationUIState.value.copy(accessToHealthcare = event.value)
                validateField("accessToHealthcare")
            }
            is UIEvent.SaveHealthAssessmentClicked -> {
                val isValid = validateHealthAssessment()
                if (isValid) {
                    _healthAssessmentValidated.value = true
                } else {
                    Log.d(TAG, "Health assessment validation failed")
                }
            }



            is UIEvent.RegisterButtonClicked -> {
                updateValidationResults()
                if (Validator.isValidRegistrationUIState(registrationUIState.value)) {
                    createUserInFirebase(
                        email = registrationUIState.value.email,
                        password = registrationUIState.value.password,

                    )
                } else {
                    Log.d(TAG, "Validation failed")
                }
            }
        }
    }

    fun resetHealthAssessmentValidation() {
        _healthAssessmentValidated.value = false
    }
    private fun validateHealthAssessment(): Boolean {
        val validationResults = Validator.validateRegistrationUIState(registrationUIState.value)
        return validationResults.filterKeys {
            it in listOf("familyDiabetes", "familyHeart", "familyCancer", "previousSurgeries",
                "chronicConditions", "smoking", "alcoholConsumption", "physicalActivity",
                "dietQuality", "sleepHours", "airQualityIndex", "exposureToPollutants",
                "stressLevel", "accessToHealthcare")
        }.all { it.value }
    }

    private fun validateField(fieldName: String) {
        val currentValidationResults = validationResults.value.toMutableMap()
        currentValidationResults[fieldName] =
            when (fieldName) {
                "email" -> Validator.validateEmail(registrationUIState.value.email)
                "fullName" -> Validator.validateFullName(registrationUIState.value.fullName)
                "age" -> Validator.validateAge(registrationUIState.value.age)
                "gender" -> Validator.validateGender(registrationUIState.value.gender)
                "height" -> Validator.validateHeight(registrationUIState.value.height)
                "weight" -> Validator.validateWeight(registrationUIState.value.weight)
                "password" -> Validator.validatePassword(registrationUIState.value.password)
                "policyAccepted" -> Validator.validatePolicyAccepted(registrationUIState.value.isPolicyAccepted)
                "familyDiabetes" -> Validator.validateYesNoAnswer(registrationUIState.value.familyDiabetes)
                "familyHeart" -> Validator.validateYesNoAnswer(registrationUIState.value.familyHeart)
                "familyCancer" -> Validator.validateYesNoAnswer(registrationUIState.value.familyCancer)
                "previousSurgeries" -> Validator.validateYesNoAnswer(registrationUIState.value.previousSurgeries)
                "chronicConditions" -> Validator.validateYesNoAnswer(registrationUIState.value.chronicConditions)
                "alcoholConsumption" -> Validator.validateNumericScale(registrationUIState.value.alcoholConsumption, 0, 4)
                "physicalActivity" -> Validator.validateNumericScale(registrationUIState.value.physicalActivity, 0, 4)
                "dietQuality" -> Validator.validateNumericScale(registrationUIState.value.dietQuality, 0, 4)
                "sleepHours" -> Validator.validateNumericScale(registrationUIState.value.sleepHours, 4, 12)
                "airQualityIndex" -> Validator.validateNumericScale(registrationUIState.value.airQualityIndex, 0, 500)
                "exposureToPollutants" -> Validator.validateNumericScale(registrationUIState.value.exposureToPollutants, 0, 3)
                "stressLevel" -> Validator.validateNumericScale(registrationUIState.value.stressLevel, 0, 4)
                "accessToHealthcare" -> Validator.validateNumericScale(registrationUIState.value.accessToHealthcare, 0, 4)
                else -> true
            }
        validationResults.value = currentValidationResults
    }

    private fun updateValidationResults() {
        validationResults.value = Validator.validateRegistrationUIState(registrationUIState.value)
    }

    private fun createUserInFirebase(
        email: String,
        password: String,
    ) {
        signUpInProgress.value = true
        auth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val profileUpdates =
                            UserProfileChangeRequest
                                .Builder()
                                .build()
                        user
                            .updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    user
                                        .sendEmailVerification()
                                        .addOnCompleteListener { verificationTask ->
                                            if (verificationTask.isSuccessful) {
                                                Log.d(TAG, "Verification email sent")
                                                signUpInProgress.value = false
                                                WellnessWiseAppRouter.navigateTo(Screen.EmailVerificationScreen)
                                            } else {
                                                signUpInProgress.value = false
                                                Log.w(
                                                    TAG,
                                                    "Error sending verification email",
                                                    verificationTask.exception,
                                                )
                                            }
                                        }
                                }
                            }
                    } else {
                        signUpInProgress.value = false
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    }
                } else {
                    signUpInProgress.value = false
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        _emailAlreadyInUse.value = true
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    }
                }
            }
    }

    fun checkEmailVerification() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful && user.isEmailVerified) {
                firestore
                    .collection("users")
                    .document(user.uid)
                    .set(getUserData())
                    .addOnSuccessListener {
                        Log.d(TAG, "User viewModels stored successfully")
                        WellnessWiseAppRouter.navigateTo(Screen.HealthDataScreen)
                    }.addOnFailureListener { e ->
                        Log.w(TAG, "Error storing user viewModels", e)
                    }
            } else {
                Log.w(TAG, "Error reloading user or email not verified", task.exception)
            }
        }
    }

    fun getUserData(): Map<String, Any> {
        return mapOf(
            "fullName" to registrationUIState.value.fullName,
            "age" to registrationUIState.value.age,
            "gender" to registrationUIState.value.gender.name,
            "height" to registrationUIState.value.height,
            "weight" to registrationUIState.value.weight,
            "familyDiabetes" to registrationUIState.value.familyDiabetes,
            "familyHeart" to registrationUIState.value.familyHeart,
            "familyCancer" to registrationUIState.value.familyCancer,
            "previousSurgeries" to registrationUIState.value.previousSurgeries,
            "chronicConditions" to registrationUIState.value.chronicConditions,
            "smoking" to registrationUIState.value.smoking,
            "alcoholConsumption" to registrationUIState.value.alcoholConsumption,
            "physicalActivity" to registrationUIState.value.physicalActivity,
            "dietQuality" to registrationUIState.value.dietQuality,
            "sleepHours" to registrationUIState.value.sleepHours,
            "airQualityIndex" to registrationUIState.value.airQualityIndex,
            "exposureToPollutants" to registrationUIState.value.exposureToPollutants,
            "stressLevel" to registrationUIState.value.stressLevel,
            "accessToHealthcare" to registrationUIState.value.accessToHealthcare,
            "bloodPressure" to registrationUIState.value.bloodPressure,
            "heartRate" to registrationUIState.value.heartRate,
            "bloodSugar" to registrationUIState.value.bloodSugar,
            "cholesterol" to registrationUIState.value.cholesterol
        )
    }

    fun getHealthAssessmentData(): Map<String, Any> {
        return mapOf(
            "familyDiabetes" to registrationUIState.value.familyDiabetes,
            "familyHeart" to registrationUIState.value.familyHeart,
            "familyCancer" to registrationUIState.value.familyCancer,
            "previousSurgeries" to registrationUIState.value.previousSurgeries,
            "chronicConditions" to registrationUIState.value.chronicConditions,
            "smoking" to registrationUIState.value.smoking,
            "alcoholConsumption" to registrationUIState.value.alcoholConsumption,
            "physicalActivity" to registrationUIState.value.physicalActivity,
            "dietQuality" to registrationUIState.value.dietQuality,
            "sleepHours" to registrationUIState.value.sleepHours,
            "airQualityIndex" to registrationUIState.value.airQualityIndex,
            "exposureToPollutants" to registrationUIState.value.exposureToPollutants,
            "stressLevel" to registrationUIState.value.stressLevel,
            "accessToHealthcare" to registrationUIState.value.accessToHealthcare
        )
    }
    fun loadExistingHealthAssessmentData(userData: Map<String, Any>?) {
        userData?.let { data ->
            registrationUIState.value = registrationUIState.value.copy(
                familyDiabetes = data["familyDiabetes"] as? String ?: registrationUIState.value.familyDiabetes,
                familyHeart = data["familyHeart"] as? String ?: registrationUIState.value.familyHeart,
                familyCancer = data["familyCancer"] as? String ?: registrationUIState.value.familyCancer,
                previousSurgeries = data["previousSurgeries"] as? String ?: registrationUIState.value.previousSurgeries,
                chronicConditions = data["chronicConditions"] as? String ?: registrationUIState.value.chronicConditions,
                smoking = data["smoking"] as? Boolean ?: registrationUIState.value.smoking,
                alcoholConsumption = (data["alcoholConsumption"] as? Number)?.toInt() ?: registrationUIState.value.alcoholConsumption,
                physicalActivity = (data["physicalActivity"] as? Number)?.toInt() ?: registrationUIState.value.physicalActivity,
                dietQuality = (data["dietQuality"] as? Number)?.toInt() ?: registrationUIState.value.dietQuality,
                sleepHours = (data["sleepHours"] as? Number)?.toInt() ?: registrationUIState.value.sleepHours,
                airQualityIndex = (data["airQualityIndex"] as? Number)?.toInt() ?: registrationUIState.value.airQualityIndex,
                exposureToPollutants = (data["exposureToPollutants"] as? Number)?.toInt() ?: registrationUIState.value.exposureToPollutants,
                stressLevel = (data["stressLevel"] as? Number)?.toInt() ?: registrationUIState.value.stressLevel,
                accessToHealthcare = (data["accessToHealthcare"] as? Number)?.toInt() ?: registrationUIState.value.accessToHealthcare
            )
        }
    }

    fun resetRegistrationUIState() {
        registrationUIState.value = RegistrationUIState()
        validationResults.value = emptyMap()
        signUpInProgress.value = false
        _emailAlreadyInUse.value = false
    }

    enum class DataSourcePreference {
        MANUAL,
        GOOGLE_FIT,
    }
}