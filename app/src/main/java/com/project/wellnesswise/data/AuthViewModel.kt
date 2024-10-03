package com.project.wellnesswise.data

import HealthDataViewModel
import LoginViewModel
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val registrationViewModel: RegistrationViewModel,
    private val loginViewModel: LoginViewModel,
    private val healthDataViewModel: HealthDataViewModel,
    private val predictionsViewModel: PredictionsViewModel
) : ViewModel() {
    private val TAG = AuthViewModel::class.simpleName
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun logOut() {
        firebaseAuth.signOut()
        firebaseAuth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                Log.d(TAG, "User logged out successfully")
                WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
                resetAllViewModels()
            } else {
                Log.d(TAG, "Logout failed")
            }
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("No user is currently signed in")

            // Re-authenticate the user
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential).await()

            // Delete predictions subcollection
            val userDocRef = firestore.collection("users").document(user.uid)
            val predictionsRef = userDocRef.collection("predictions")
            val predictionsSnapshot = predictionsRef.get().await()
            for (doc in predictionsSnapshot.documents) {
                doc.reference.delete().await()
            }
            Log.d(TAG, "User prediction history deleted successfully")

            // Delete the main user document
            userDocRef.delete().await()
            Log.d(TAG, "User Firestore data deleted successfully")

            // Delete the authentication account
            user.delete().await()
            Log.d(TAG, "User authentication account deleted successfully")

            // Reset view models and navigate to login screen
            withContext(Dispatchers.Main) {
                resetAllViewModels()
                WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user account", e)
            Result.failure(e)
        }
    }

    private fun resetAllViewModels() {
        registrationViewModel.resetRegistrationUIState()
        loginViewModel.resetLoginUIState()
        healthDataViewModel.resetHealthData()
        predictionsViewModel.resetPredictions()
    }
}