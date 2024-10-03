package com.project.wellnesswise.data.rules

import com.project.wellnesswise.data.Gender
import com.project.wellnesswise.data.RegistrationUIState
import com.project.wellnesswise.data.LoginUIState

object Validator {

    // Email validation
    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return email.matches(emailRegex)
    }

    // Full name validation
    fun validateFullName(fullName: String): Boolean {
        return fullName.isNotBlank()
    }

    // Age validation
    fun validateAge(age: Number): Boolean {
        return age is Int && age in 1..120
    }

    // Gender validation
    fun validateGender(gender: Gender): Boolean {
        return gender == Gender.MALE || gender == Gender.FEMALE
    }

    // Height validation
    fun validateHeight(height: Number): Boolean {
        return height is Int && height in 50..300
    }

    // Weight validation
    fun validateWeight(weight: Number): Boolean {
        return weight is Int && weight in 30..500
    }


    // Password validation
    fun validatePassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
        return password.matches(passwordRegex)
    }

    fun validatePolicyAccepted(isPolicyAccepted: Boolean): Boolean {
        return isPolicyAccepted
    }

    // Triglycerides validation
    fun validateTriglycerides(triglycerides: String): Boolean {
        return triglycerides.toFloatOrNull()?.let { it in 50f..500f } ?: false
    }

    // Waist Circumference validation
    fun validateWaistCircumference(waistCircumference: String): Boolean {
        return waistCircumference.toFloatOrNull()?.let { it in 50f..200f } ?: false
    }
    // New validation methods for health assessment fields
    fun validateYesNoAnswer(answer: String): Boolean {
        return answer == "Yes" || answer == "No"
    }

    // Validate registration UI state
    fun validateRegistrationUIState(uiState: RegistrationUIState): Map<String, Boolean> {
        return mapOf(
            "email" to validateEmail(uiState.email),
            "fullName" to validateFullName(uiState.fullName),
            "age" to validateAge(uiState.age),
            "gender" to validateGender(uiState.gender),
            "height" to validateHeight(uiState.height),
            "weight" to validateWeight(uiState.weight),
            "password" to validatePassword(uiState.password),
            "policyAccepted" to validatePolicyAccepted(uiState.isPolicyAccepted),
            "familyDiabetes" to validateYesNoAnswer(uiState.familyDiabetes),
            "familyHeart" to validateYesNoAnswer(uiState.familyHeart),
            "familyCancer" to validateYesNoAnswer(uiState.familyCancer),
            "previousSurgeries" to validateYesNoAnswer(uiState.previousSurgeries),
            "chronicConditions" to validateYesNoAnswer(uiState.chronicConditions),
            "smoking" to validateSmoking(uiState.smoking),
            "alcoholConsumption" to validateNumericScale(uiState.alcoholConsumption, 0, 4),
            "physicalActivity" to validateNumericScale(uiState.physicalActivity, 0, 4),
            "dietQuality" to validateNumericScale(uiState.dietQuality, 0, 4),
            "sleepHours" to validateNumericScale(uiState.sleepHours, 4, 12),
            "airQualityIndex" to validateNumericScale(uiState.airQualityIndex, 0, 500),
            "exposureToPollutants" to validateNumericScale(uiState.exposureToPollutants, 0, 3),
            "stressLevel" to validateNumericScale(uiState.stressLevel, 0, 4),
            "accessToHealthcare" to validateNumericScale(uiState.accessToHealthcare, 0, 4)
        )
    }

    // Check if registration UI state is valid
    fun isValidRegistrationUIState(uiState: RegistrationUIState): Boolean {
        val validationResults = validateRegistrationUIState(uiState)
        return validationResults.all { it.value }
    }
    // Update this method to handle Boolean
    fun validateSmoking(smoking: Boolean): Boolean {
        return true  // Always valid as it's a Boolean
    }

    // Update this method to handle Int
    fun validateNumericScale(value: Int, min: Int, max: Int): Boolean {
        return value in min..max
    }

    // Validate login UI state
    fun validateLoginUIState(loginUIState: LoginUIState): Map<String, Boolean> {
        return mapOf(
            "email" to validateEmail(loginUIState.email),
            "password" to validatePassword(loginUIState.password)
        )
    }

    // Check if login UI state is valid
    fun isValidLoginUIState(uiState: LoginUIState): Boolean {
        val validationResults = validateLoginUIState(uiState)
        return validationResults.all { it.value }
    }


//Health data validations from now here
    fun validateBloodPressure(bloodPressure: String): Boolean {
        val bloodPressureRegex = Regex("^\\d+(\\.\\d+)?/\\d+(\\.\\d+)?$")
        return bloodPressure.matches(bloodPressureRegex)
    }

    // Heart Rate validation
    fun validateHeartRate(heartRate: String): Boolean {
        return heartRate.toFloatOrNull()?.let { it in 40f..200f } ?: false
    }

    // Blood Sugar validation
    fun validateBloodSugar(bloodSugar: String): Boolean {
        return bloodSugar.toFloatOrNull()?.let { it in 70f..300f } ?: false
    }

    // Cholesterol validation
    fun validateCholesterol(cholesterol: String): Boolean {
        return cholesterol.toFloatOrNull()?.let { it in 100f..300f } ?: false
    }

    fun validateHealthParameters(
        bloodPressure: String,
        heartRate: String,
        bloodSugar: String,
        cholesterol: String,
        triglycerides: String,
        waistCircumference: String
    ): Map<String, Boolean> {
        return mapOf(
            "bloodPressure" to validateBloodPressure(bloodPressure),
            "heartRate" to validateHeartRate(heartRate),
            "bloodSugar" to validateBloodSugar(bloodSugar),
            "cholesterol" to validateCholesterol(cholesterol),
            "triglycerides" to validateTriglycerides(triglycerides),
            "waistCircumference" to validateWaistCircumference(waistCircumference)
        )
    }

    // Update isValidHealthParameters to include new parameters
    fun isValidHealthParameters(
        bloodPressure: String,
        heartRate: String,
        bloodSugar: String,
        cholesterol: String,
        triglycerides: String,
        waistCircumference: String
    ): Boolean {
        val validationResults = validateHealthParameters(bloodPressure, heartRate, bloodSugar, cholesterol, triglycerides, waistCircumference)
        return validationResults.all { it.value }
    }
}


