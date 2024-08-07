package com.project.wellnesswise.data.rules

import com.project.wellnesswise.data.Gender
import com.project.wellnesswise.data.Habit
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

    // Habits validation
    fun validateHabits(habits: List<Habit>): Boolean {
        return habits.isNotEmpty()
    }

    // Medical history validation
    fun validateMedicalHistory(medicalHistory: Map<String, String>): Boolean {
        return medicalHistory.isNotEmpty()
    }

    // Password validation
    fun validatePassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
        return password.matches(passwordRegex)
    }

    fun validatePolicyAccepted(isPolicyAccepted: Boolean): Boolean {
        return isPolicyAccepted
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
            "habits" to validateHabits(uiState.habits),
            "medicalHistory" to validateMedicalHistory(uiState.medicalHistory),
            "password" to validatePassword(uiState.password),
            "policyAccepted" to validatePolicyAccepted(uiState.isPolicyAccepted)

        )
    }

    // Check if registration UI state is valid
    fun isValidRegistrationUIState(uiState: RegistrationUIState): Boolean {
        val validationResults = validateRegistrationUIState(uiState)
        return validationResults.all { it.value }
    }

    // Validate login UI state
    fun validateLoginUIState(uiState: LoginUIState): Map<String, Boolean> {
        return mapOf(
            "email" to validateEmail(uiState.email),
            "password" to validatePassword(uiState.password)
        )
    }

    // Check if login UI state is valid
    fun isValidLoginUIState(uiState: LoginUIState): Boolean {
        val validationResults = validateLoginUIState(uiState)
        return validationResults.all { it.value }
    }
}
