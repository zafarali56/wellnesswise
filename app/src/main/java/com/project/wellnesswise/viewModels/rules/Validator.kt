package com.project.wellnesswise.viewModels.rules

import com.project.wellnesswise.viewModels.Gender
import com.project.wellnesswise.viewModels.RegistrationUIState
import com.project.wellnesswise.viewModels.LoginUIState

object Validator {


    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return email.matches(emailRegex)
    }

    fun validateFullName(fullName: String): Boolean {
        return fullName.isNotBlank()
    }

    fun validateAge(age: Number): Boolean {
        return age is Int && age in 1..120
    }

    fun validateGender(gender: Gender): Boolean {
        return gender == Gender.MALE || gender == Gender.FEMALE
    }

    fun validateHeight(height: Number): Boolean {
        return height is Int && height in 50..300
    }

    fun validateWeight(weight: Number): Boolean {
        return weight is Int && weight in 30..500
    }


    fun validatePassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
        return password.matches(passwordRegex)
    }

    fun validatePolicyAccepted(isPolicyAccepted: Boolean): Boolean {
        return isPolicyAccepted
    }

    fun validateTriglycerides(triglycerides: String): Boolean {
        return triglycerides.toFloatOrNull()?.let { it in 50f..500f } ?: false
    }

    fun validateWaistCircumference(waistCircumference: String): Boolean {
        return waistCircumference.toFloatOrNull()?.let { it in 50f..200f } ?: false
    }
    fun validateYesNoAnswer(answer: String): Boolean {
        return answer == "Yes" || answer == "No"
    }

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
//            "familyDiabetes" to validateYesNoAnswer(uiState.familyDiabetes),
//            "familyHeart" to validateYesNoAnswer(uiState.familyHeart),
//            "familyCancer" to validateYesNoAnswer(uiState.familyCancer),
//            "previousSurgeries" to validateYesNoAnswer(uiState.previousSurgeries),
//            "chronicConditions" to validateYesNoAnswer(uiState.chronicConditions),
//            "smoking" to validateSmoking(uiState.smoking),
//            "alcoholConsumption" to validateNumericScale(uiState.alcoholConsumption, 0, 4),
//            "physicalActivity" to validateNumericScale(uiState.physicalActivity, 0, 4),
//            "dietQuality" to validateNumericScale(uiState.dietQuality, 0, 4),
//            "sleepHours" to validateNumericScale(uiState.sleepHours, 4, 12),
//            "airQualityIndex" to validateNumericScale(uiState.airQualityIndex, 0, 500),
//            "exposureToPollutants" to validateNumericScale(uiState.exposureToPollutants, 0, 3),
//            "stressLevel" to validateNumericScale(uiState.stressLevel, 0, 4),
//            "accessToHealthcare" to validateNumericScale(uiState.accessToHealthcare, 0, 4)
        )
    }

    fun isValidRegistrationUIState(uiState: RegistrationUIState): Boolean {
        val validationResults = validateRegistrationUIState(uiState)
        return validationResults.all { it.value }
    }
    fun validateSmoking(smoking: Boolean): Boolean {
        return true
    }

    fun validateNumericScale(value: Int, min: Int, max: Int): Boolean {
        return value in min..max
    }

    fun validateLoginUIState(loginUIState: LoginUIState): Map<String, Boolean> {
        return mapOf(
            "email" to validateEmail(loginUIState.email),
            "password" to validatePassword(loginUIState.password)
        )
    }

    fun isValidLoginUIState(uiState: LoginUIState): Boolean {
        val validationResults = validateLoginUIState(uiState)
        return validationResults.all { it.value }
    }


    fun validateBloodPressure(bloodPressure: String): Boolean {
        val bloodPressureRegex = Regex("^\\d+(\\.\\d+)?/\\d+(\\.\\d+)?$")
        return bloodPressure.matches(bloodPressureRegex)
    }

    fun validateHeartRate(heartRate: String): Boolean {
        return heartRate.toFloatOrNull()?.let { it in 40f..200f } ?: false
    }

    fun validateBloodSugar(bloodSugar: String): Boolean {
        return bloodSugar.toFloatOrNull()?.let { it in 70f..300f } ?: false
    }

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


