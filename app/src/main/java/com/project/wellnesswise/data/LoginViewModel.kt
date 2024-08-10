package com.project.wellnesswise.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

class LoginViewModel : ViewModel() {
    private val TAG = LoginViewModel::class.simpleName
    var loginUIState = mutableStateOf(LoginUIState())
        private set
    var validationResults = mutableStateOf(emptyMap<String, Boolean>())
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set

    var logInProgress = mutableStateOf(false)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
                                logInProgress.value = false
                                Log.d(TAG, "Login successful")
                                WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
                            } else {
                                logInProgress.value = false
                                Log.d(TAG, "Email not verified")
                                WellnessWiseAppRouter.navigateTo(Screen.EmailVerificationScreen)
                            }
                        } else {
                            Log.w(TAG, "Error reloading user", reloadTask.exception)
                            errorMessage.value = reloadTask.exception?.message ?: "Error reloading user"
                            logInProgress.value = false
                        }
                    }
                } else {
                    Log.w(TAG, "Login failed", task.exception)
                    errorMessage.value = task.exception?.message ?: "Login failed"
                    logInProgress.value = false
                }
            }
    }

    private fun printState() {
        Log.d(TAG, "Inside printState")
        Log.d(TAG, loginUIState.value.toString())
    }

    fun logOut() {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()
        val authStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                Log.d(TAG, "Inside signOut")
                WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
            } else {
                Log.d(TAG, "Inside signOut else")
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)
    }
}
