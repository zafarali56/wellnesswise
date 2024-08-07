package com.project.wellnesswise.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.project.wellnesswise.data.rules.Validator

class LoginViewModel : ViewModel() {
    private val TAG = LoginViewModel::class.simpleName
    var loginUIState = mutableStateOf(LoginUIState())
        private set
    var validationResults = mutableStateOf(emptyMap<String, Boolean>())
        private set

    fun onEvent(event: LoginUIEvent) {
        when (event) {
            is LoginUIEvent.EmailChangedLogin -> {
                loginUIState.value = loginUIState.value.copy(
                    email = event.email
                )
            }
            is LoginUIEvent.PasswordChangedLogin -> {
                loginUIState.value = loginUIState.value.copy(
                    password = event.password
                )
            }
            is LoginUIEvent.LoginButtonClicked -> {
                updateValidationResults()
                if (Validator.isValidLoginUIState(loginUIState.value)) {
                    login()
                } else {
                    // Show validation errors
                    Log.d(TAG, "Validation failed")
                }
            }



        }
    }

    private fun updateValidationResults() {
        validationResults.value = Validator.validateLoginUIState(loginUIState.value)
    }

    private fun login() {
        Log.d(TAG, "Inside Login")
        printState()
    }

    private fun printState() {
        Log.d(TAG, "Inside printState")
        Log.d(TAG, loginUIState.value.toString())
    }
}
