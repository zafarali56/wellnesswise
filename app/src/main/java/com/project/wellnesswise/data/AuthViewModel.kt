package com.project.wellnesswise.data

import android.util.Log
import androidx.lifecycle.ViewModel

import com.google.firebase.auth.FirebaseAuth
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

class AuthViewModel(
    private val registrationViewModel: RegistrationViewModel,
    private val loginViewModel: LoginViewModel
) : ViewModel() {
    private val TAG = AuthViewModel::class.simpleName
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun logOut() {
        firebaseAuth.signOut()
        firebaseAuth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                Log.d(TAG, "User logged out successfully")
                WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
                registrationViewModel.resetRegistrationUIState()
                loginViewModel.resetLoginUIState()
            } else {
                Log.d(TAG, "Logout failed")
            }
        }
    }
}
