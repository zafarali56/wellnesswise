package com.project.wellnesswise.data.rules

import com.project.wellnesswise.data.Gender
import com.project.wellnesswise.data.Habit
import com.project.wellnesswise.data.RegistrationUIState

object Validator {

    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
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

    fun validateHabits(habits: List<Habit>): Boolean {
        return habits.isNotEmpty()
    }

    fun validateMedicalHistory(medicalHistory: Map<String, String>): Boolean {
        return medicalHistory.isNotEmpty()
    }

    fun validatePassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
        return password.matches(passwordRegex)
    }

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
            "password" to validatePassword(uiState.password)
        )
    }

    fun isValidRegistrationUIState(uiState: RegistrationUIState): Boolean {
        val validationResults = validateRegistrationUIState(uiState)
        return validationResults.all { it.value }
    }
}
