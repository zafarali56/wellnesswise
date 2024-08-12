package com.project.wellnesswise.data

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

class AuthViewModel : ViewModel() {
    private val TAG = AuthViewModel::class.simpleName

    fun logOut(registrationViewModel: RegistrationViewModel, loginViewModel: LoginViewModel) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()
        val authStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                Log.d(ContentValues.TAG, "Inside signOut")
                WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
                registrationViewModel.resetRegistrationUIState()
                loginViewModel.resetLoginUIState()
            } else {
                Log.d(ContentValues.TAG, "Inside signOut else")
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)
    }
}