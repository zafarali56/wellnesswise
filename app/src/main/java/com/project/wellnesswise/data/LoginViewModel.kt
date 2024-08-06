package com.project.wellnesswise.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    private val TAG = LoginViewModel::class.simpleName
    var registrationUIState = mutableStateOf(RegistrationUIState())
        private set

    fun onEvent(event: UIEvent) {
        when (event) {
            is UIEvent.EmailChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    email = event.email
                )
                printState()
            }
            is UIEvent.FullNameChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    fullName = event.fullName
                )
                printState()
            }
            is UIEvent.AgeChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    age = event.age
                )
                printState()
            }
            is UIEvent.GenderChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    gender = event.gender
                )
                printState()
            }
            is UIEvent.HeightChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    height = event.height
                )
                printState()
            }
            is UIEvent.WeightChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    weight = event.weight
                )
                printState()
            }
            is UIEvent.HabitsChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    habits = event.habits
                )
                printState()
            }
            is UIEvent.MedicalHistoryChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    medicalHistory = event.medicalHistory
                )
                printState()
            }
            is UIEvent.PasswordChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    password = event.password
                )
                printState()
            }
        }
    }

    private fun printState() {
        Log.d(TAG, "Inside printState")
        Log.d(TAG, registrationUIState.value.toString())
    }
}
