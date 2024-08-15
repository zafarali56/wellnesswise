package com.project.wellnesswise.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {
    private val Tag = HomeViewModel::class.simpleName
    val isUserLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val bloodPressure: MutableLiveData<String?> = MutableLiveData()
    val heartRate: MutableLiveData<String?> = MutableLiveData()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun checkForActiveSession() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.d(TAG, "Valid session")
            isUserLoggedIn.value = true
            fetchHealthData()
        } else {
            Log.d(TAG, "User is not logged in")
            isUserLoggedIn.value = false
        }
    }

    private fun fetchHealthData() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val bp = document.getString("bloodPressure")
                        val hr = document.getString("heartRate")
                        Log.d(TAG, "Fetched BP: $bp, HR: $hr")
                        bloodPressure.value = bp
                        heartRate.value = hr
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error fetching health data", e)
                }
        }
    }
}
