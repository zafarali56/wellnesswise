package com.project.wellnesswise.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.roundToInt

class HomeViewModel : ViewModel() {
    private val TAG = HomeViewModel::class.simpleName
    val isUserLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    val bloodPressure: MutableLiveData<String> = MutableLiveData("N/A")
    val heartRate: MutableLiveData<String> = MutableLiveData("N/A")
    val bloodSugar: MutableLiveData<String> = MutableLiveData("N/A")
    val cholesterol: MutableLiveData<String> = MutableLiveData("N/A")

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var firestoreListener: ListenerRegistration? = null

    fun checkForActiveSession() {
        if (auth.currentUser != null) {
            Log.d(TAG, "Valid session")
            isUserLoggedIn.value = true
            setupFirestoreListener()
        } else {
            Log.d(TAG, "User is not logged in")
            isUserLoggedIn.value = false
        }
    }

    private fun setupFirestoreListener() {
        val user = auth.currentUser
        if (user != null) {
            firestoreListener = firestore.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        try {
                            snapshot.data?.let { data ->
                                bloodPressure.value = data["bloodPressure"]?.toString() ?: "N/A"
                                heartRate.value = (data["heartRate"] as? Number)?.toFloat()?.roundToInt()?.toString() ?: "N/A"
                                bloodSugar.value = data["bloodSugar"]?.toString() ?: "N/A"
                                cholesterol.value = data["cholesterol"]?.toString() ?: "N/A"
                            }
                            Log.d(TAG, "Current data: ${snapshot.data}")
                            Log.d(TAG, "BP: ${bloodPressure.value}, HR: ${heartRate.value}, BS: ${bloodSugar.value}, Chol: ${cholesterol.value}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing Firestore data", e)
                            setDefaultValues()
                        }
                    } else {
                        Log.d(TAG, "Current data: null")
                        setDefaultValues()
                    }
                }
        }
    }

    private fun setDefaultValues() {
        bloodPressure.value = "N/A"
        heartRate.value = "N/A"
        bloodSugar.value = "N/A"
        cholesterol.value = "N/A"
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }
}