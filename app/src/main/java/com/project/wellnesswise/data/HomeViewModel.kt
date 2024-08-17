package com.project.wellnesswise.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeViewModel : ViewModel() {
    private val TAG = HomeViewModel::class.simpleName

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    private val _bloodPressure = MutableStateFlow("N/A")
    val bloodPressure: StateFlow<String> = _bloodPressure

    private val _heartRate = MutableStateFlow("N/A")
    val heartRate: StateFlow<String> = _heartRate

    private val _bloodSugar = MutableStateFlow("N/A")
    val bloodSugar: StateFlow<String> = _bloodSugar

    private val _cholesterol = MutableStateFlow("N/A")
    val cholesterol: StateFlow<String> = _cholesterol

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var firestoreListener: ListenerRegistration? = null

    init {
        checkForActiveSession()
    }

    fun checkForActiveSession() {
        if (auth.currentUser != null) {
            Log.d(TAG, "Valid session")
            _isUserLoggedIn.value = true
            setupFirestoreListener()
        } else {
            Log.d(TAG, "User is not logged in")
            _isUserLoggedIn.value = false
        }
    }

    private fun setupFirestoreListener() {
        val user = auth.currentUser
        if (user != null) {
            firestoreListener?.remove() // Remove any existing listener
            firestoreListener = firestore.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        try {
                            snapshot.data?.let { data ->
                                viewModelScope.launch {
                                    _bloodPressure.value = data["bloodPressure"]?.toString() ?: "N/A"
                                    _heartRate.value = when (val hrData = data["heartRate"]) {
                                        is Number -> hrData.toFloat().roundToInt().toString()
                                        is String -> hrData.toIntOrNull()?.toString().toString() // Attempt to convert string to int
                                        else -> "N/A"
                                    }
                                    _bloodSugar.value = data["bloodSugar"]?.toString() ?: "N/A"
                                    _cholesterol.value = data["cholesterol"]?.toString() ?: "N/A"
                                    _isRefreshing.value = false
                                }
                            }
                            Log.d(TAG, "Current data: ${snapshot.data}")
                            Log.d(TAG, "BP: ${_bloodPressure.value}, HR: ${_heartRate.value}, BS: ${_bloodSugar.value}, Chol: ${_cholesterol.value}")
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

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            setupFirestoreListener() // This will re-fetch the data
        }
    }

    private fun setDefaultValues() {
        viewModelScope.launch {
            _bloodPressure.value = "N/A"
            _heartRate.value = "N/A"
            _bloodSugar.value = "N/A"
            _cholesterol.value = "N/A"
            _isRefreshing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }
}