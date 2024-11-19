package com.project.wellnesswise.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PersonalizedRecommendationsViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val predictionsViewModel: PredictionsViewModel
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<String>>(emptyList())
    val recommendations: StateFlow<List<String>> = _recommendations

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading


    private var userDataListener: ListenerRegistration? = null
    private var updateJob: Job? = null

    init {
        setupDataListeners()
        loadRecommendationsIfLoggedIn()
    }

    private fun setupDataListeners() {
        viewModelScope.launch {
            auth.addAuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    Log.d(TAG, "User logged in, setting up listeners")
                    setupUserDataListener(firebaseAuth.currentUser!!.uid)
                    setupPredictionListener()
                } else {
                    Log.d(TAG, "User logged out, clearing recommendations")
                    _recommendations.value = emptyList()
                    userDataListener?.remove()
                }
            }
        }
    }

    private fun setupUserDataListener(userId: String) {
        userDataListener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "User viewModels changed, triggering recommendation update")
                    triggerRecommendationUpdate()
                }
            }
    }

    private fun setupPredictionListener() {
        viewModelScope.launch {
            predictionsViewModel.predictions.collectLatest { predictions ->
                if (predictions != null) {
                    Log.d(TAG, "Predictions changed, triggering recommendation update")
                    triggerRecommendationUpdate()
                }
            }
        }
    }
    private suspend fun generateRecommendationFromTemplate(
        category: String,
        risk: Float
    ): String? {
        return try {
            val template = firestore.collection("recommendationTemplates")
                .document(category)
                .get()
                .await()

            val thresholds = template.get("thresholds") as? List<Map<String, Any>> ?: return null

            thresholds.find { threshold ->
                val min = (threshold["min"] as Number).toFloat()
                val max = (threshold["max"] as Number).toFloat()
                risk >= min && risk < max
            }?.get("message") as? String
        } catch (e: Exception) {
            Log.e(TAG, "Error generating recommendation for $category", e)
            null
        }
    }



    private fun loadRecommendationsIfLoggedIn() {
        if (auth.currentUser != null) {
            Log.d(TAG, "User already logged in, loading recommendations")
            triggerRecommendationUpdate()
        }
    }



    private fun triggerRecommendationUpdate() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(500)
            loadRecommendations()
        }
    }
    private suspend fun generateSummaryRecommendation(
        conditions: List<Triple<String, Float, String>>,
        type: String
    ): String? {
        return try {
            val summaryTemplate = firestore.collection("recommendationTemplates")
                .document("summaryTemplates")
                .get()
                .await()

            val template = summaryTemplate.getString(type) ?: return null
            val conditionNames = conditions.joinToString(", ") { it.first }
            template.replace("{conditions}", conditionNames)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating summary recommendation", e)
            null
        }
    }
    private fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUser ?: throw Exception("User not authenticated")

                val latestPrediction = firestore.collection("users")
                    .document(user.uid)
                    .collection("predictions")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                if (latestPrediction.isEmpty) {
                    _recommendations.value = listOf("No predictions available. Please complete your health assessment.")
                    return@launch
                }

                val predictionDoc = latestPrediction.documents[0]
                val predictions = (predictionDoc.get("predictions") as? List<Map<String, Any>>)?.map { pred ->
                    Triple(
                        pred["category"] as String,
                        (pred["risk"] as Number).toFloat(),
                        pred["context"] as String
                    )
                } ?: throw Exception("Invalid prediction data format")

                val recommendations = mutableListOf<String>()
                predictions.forEach { (category, risk, _) ->
                    generateRecommendationFromTemplate(category, risk)?.let {
                        recommendations.add(it)
                    }
                }

                val highRiskConditions = predictions.filter { it.second >= 0.6f }
                val lowRiskConditions = predictions.filter { it.second < 0.2f }

                if (highRiskConditions.isNotEmpty()) {
                    generateSummaryRecommendation(highRiskConditions, "highRisk")?.let {
                        recommendations.add(it)
                    }
                }

                if (lowRiskConditions.isNotEmpty()) {
                    generateSummaryRecommendation(lowRiskConditions, "lowRisk")?.let {
                        recommendations.add(it)
                    }
                }

                _recommendations.value = recommendations
                Log.d(TAG, "New recommendations generated from Firestore templates")
            } catch (e: Exception) {
                _recommendations.value = listOf("Unable to load recommendations. Please try again later.")
                Log.e(TAG, "Error loading recommendations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        userDataListener?.remove()
        updateJob?.cancel()
    }

    companion object {
        private const val TAG = "com.project.wellnesswise.viewModels.PersonalizedRecommendationsViewModel"
    }
}

class PersonalizedRecommendationsViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val predictionsViewModel: PredictionsViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalizedRecommendationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonalizedRecommendationsViewModel(firestore, auth, predictionsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}