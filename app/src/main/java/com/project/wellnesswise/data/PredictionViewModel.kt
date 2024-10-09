package com.project.wellnesswise.data

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.wellnesswise.ml.TFLiteInterpreter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PredictionsViewModel(private val context: Context) : ViewModel() {
    private val healthDataProcessor = HealthDataProcessor()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _predictions = MutableStateFlow<List<Triple<String, Float, String>>?>(null)
    val predictions: StateFlow<List<Triple<String, Float, String>>?> = _predictions.asStateFlow()

    private val _modelInput = MutableStateFlow<List<Float>?>(null)
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)
    private val _predictionHistory = MutableStateFlow<List<PredictionHistoryItem>>(emptyList())
    val predictionHistory: StateFlow<List<PredictionHistoryItem>> = _predictionHistory.asStateFlow()
    private var tfliteInterpreter: TFLiteInterpreter? = null
    private var isModelLoaded = false
    private var lastSavedPredictions: List<Triple<String, Float, String>>? = null
    private var lastSavedTimestamp: Long = 0
    private var lastHealthData: ModelInput? = null
    @Serializable
    private data class PredictionTriple(val first: String, val second: Float, val third: String)
    init {
        viewModelScope.launch {
            loadLastSavedPredictions()
            loadModel()
            healthDataProcessor.startListeningForChanges()
            healthDataProcessor.addListener { checkAndLoadPredictions() }
        }
    }
    private fun checkAndLoadPredictions() {
        viewModelScope.launch {
            val currentHealthData = healthDataProcessor.getUserHealthData()
            if (currentHealthData != lastHealthData) {
                lastHealthData = currentHealthData
                loadPredictions()
            }
        }
    }
    private suspend fun loadLastSavedPredictions() {
        withContext(Dispatchers.IO) {
            val sharedPrefs = context.getSharedPreferences("PredictionsPrefs", Context.MODE_PRIVATE)
            lastSavedTimestamp = sharedPrefs.getLong("last_saved_timestamp", 0)
            lastSavedPredictions = sharedPrefs.getString("last_saved_predictions", null)?.let {
                Json.decodeFromString<List<PredictionTriple>>(it).map { triple ->
                    Triple(triple.first, triple.second, triple.third)
                }
            }
        }
    }   private suspend fun saveLastPredictions(predictions: List<Triple<String, Float, String>>, timestamp: Long) {
        withContext(Dispatchers.IO) {
            val sharedPrefs = context.getSharedPreferences("PredictionsPrefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putLong("last_saved_timestamp", timestamp)
                putString("last_saved_predictions", Json.encodeToString(predictions.map { PredictionTriple(it.first, it.second, it.third) }))
                apply()
            }
        }
    }

    private suspend fun loadModel() {
        withContext(Dispatchers.Default) {
            tfliteInterpreter = TFLiteInterpreter(context, "enhanced_health_risk_model.tflite")
            isModelLoaded = true
            loadPredictions()
        }
    }

    fun loadPredictions() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                if (!isModelLoaded) {
                    Log.d(TAG, "Model not loaded yet, skipping predictions")
                    return@launch
                }

                val input = lastHealthData
                if (input == null) {
                    Log.d(TAG, "User health data not available yet")
                    errorMessage = "Health data not available. Please complete your health assessment."
                    isLoading = false
                    return@launch
                }

                _modelInput.value = input.values

                val outputData = withContext(Dispatchers.Default) {
                    tfliteInterpreter?.predict(input.values.toFloatArray()) ?: floatArrayOf()
                }

                val newPredictions = HealthDataProcessor.riskCategories.zip(outputData.toList()).map { (category, risk) ->
                    val context = getRiskContext(category, risk, input.values[0])
                    Triple(category, risk, context)
                }

                if (arePredictionsDifferent(lastSavedPredictions, newPredictions)) {
                    _predictions.value = newPredictions
                    val currentTimestamp = System.currentTimeMillis()
                    savePredictionsToFirestore(newPredictions, currentTimestamp)
                    lastSavedPredictions = newPredictions
                    lastSavedTimestamp = currentTimestamp
                    saveLastPredictions(newPredictions, currentTimestamp)
                    Log.d(TAG, "New predictions saved to Firestore")
                } else {
                    _predictions.value = newPredictions
                    Log.d(TAG, "Predictions unchanged, not saving to Firestore")
                }
            } catch (e: Exception) {
                errorMessage = "An error occurred: ${e.message}"
                Log.e(TAG, "Error: ", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun classifyRisk(prediction: Float): String {
        return when {
            prediction < 0.2f -> "Stable"
            prediction < 0.4f -> "Mild"
            prediction < 0.6f -> "Moderate"
            prediction < 0.8f -> "Severe"
            else -> "Critical"
        }
    }

    private fun getRiskContext(category: String, risk: Float, age: Number): String {
        val riskLevel = classifyRisk(risk)
        val ageValue = age.toInt()
        return when (category) {
            "Diabetes" -> when {
                ageValue < 40 && riskLevel == "Moderate" -> "Consider lifestyle changes to reduce risk."
                ageValue >= 40 && riskLevel == "Moderate" -> "Regular check-ups recommended."
                riskLevel == "Severe" || riskLevel == "Critical" -> "Consult a healthcare professional soon."
                else -> "Maintain a healthy lifestyle to keep risk low."
            }
            "Cardiovascular Disease" -> when {
                ageValue < 50 && riskLevel == "Moderate" -> "Focus on heart-healthy habits."
                ageValue >= 50 && riskLevel == "Moderate" -> "Regular cardiovascular check-ups advised."
                riskLevel == "Severe" || riskLevel == "Critical" -> "Seek medical advice for heart health."
                else -> "Continue heart-healthy practices."
            }
            "Hypertension" -> when {
                riskLevel == "Moderate" -> "Monitor blood pressure regularly."
                riskLevel == "Severe" || riskLevel == "Critical" -> "Consult a doctor for blood pressure management."
                else -> "Maintain a healthy lifestyle to control blood pressure."
            }
            "Obesity" -> when {
                riskLevel == "Moderate" -> "Consider adjusting diet and exercise habits."
                riskLevel == "Severe" || riskLevel == "Critical" -> "Consult a nutritionist or weight management specialist."
                else -> "Maintain a balanced diet and regular physical activity."
            }
            "Cancer" -> when {
                riskLevel == "Moderate" -> "Stay up-to-date with recommended cancer screenings."
                riskLevel == "Severe" || riskLevel == "Critical" -> "Discuss cancer prevention strategies with your doctor."
                else -> "Maintain a healthy lifestyle to reduce cancer risk."
            }
            else -> "Consult with a healthcare professional for personalized advice."
        }
    }

    override fun onCleared() {
        super.onCleared()
        healthDataProcessor.removeListener { checkAndLoadPredictions() }
        healthDataProcessor.stopListeningForChanges()
        tfliteInterpreter?.close()
    }

    fun fetchPredictionHistory() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No user logged in")
                return@launch
            }

            val userId = currentUser.uid
            firestore.collection("users").document(userId)
                .collection("predictions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener { result ->
                    val history = result.mapNotNull { document ->
                        val timestamp = document.getLong("timestamp") ?: return@mapNotNull null
                        val predictions = document.get("predictions") as? List<Map<String, Any>> ?: return@mapNotNull null
                        PredictionHistoryItem(
                            timestamp = timestamp,
                            predictions = predictions.map { pred ->
                                Triple(
                                    pred["category"] as String,
                                    (pred["risk"] as Number).toFloat(),
                                    pred["context"] as String
                                )
                            }
                        )
                    }.distinctBy { it.predictions }
                    _predictionHistory.value = history
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting prediction history", exception)
                }
        }
    }

    fun resetPredictions() {
        _predictions.value = null
        _modelInput.value = null
        isLoading = false
        errorMessage = null
        _predictionHistory.value = emptyList()
    }

    private fun arePredictionsDifferent(oldPredictions: List<Triple<String, Float, String>>?, newPredictions: List<Triple<String, Float, String>>): Boolean {
        if (oldPredictions == null) return true
        if (oldPredictions.size != newPredictions.size) return true
        return oldPredictions.zip(newPredictions).any { (old, new) ->
            old.first != new.first || kotlin.math.abs(old.second - new.second) > 0.001f || old.third != new.third
        }
    }
    private fun savePredictionsToFirestore(predictions: List<Triple<String, Float, String>>, timestamp: Long) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No user logged in")
            return
        }

        val userId = currentUser.uid
        val predictionData = hashMapOf(
            "timestamp" to timestamp,
            "predictions" to predictions.map { (category, risk, context) ->
                hashMapOf(
                    "category" to category,
                    "risk" to risk,
                    "context" to context
                )
            }
        )

        firestore.collection("users").document(userId)
            .collection("predictions")
            .document(timestamp.toString())
            .set(predictionData)
            .addOnSuccessListener {
                Log.d(TAG, "Prediction saved with timestamp: $timestamp")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding prediction", e)
            }
    }

    data class PredictionHistoryItem(
        val timestamp: Long,
        val predictions: List<Triple<String, Float, String>>
    )
}