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

class PredictionsViewModel(private val context: Context) : ViewModel() {
    private val healthDataProcessor = HealthDataProcessor()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var predictions by mutableStateOf<List<Triple<String, Float, String>>?>(null)
    var modelInput by mutableStateOf<List<Float>?>(null)
        private set
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)
    private val _predictionHistory = MutableStateFlow<List<PredictionHistoryItem>>(emptyList())
    val predictionHistory: StateFlow<List<PredictionHistoryItem>> = _predictionHistory.asStateFlow()
    private var tfliteInterpreter: TFLiteInterpreter? = null
    private var isModelLoaded = false

    init {
        viewModelScope.launch {
            loadModel()
            healthDataProcessor.startListeningForChanges()
            healthDataProcessor.addListener { loadPredictions() }
            loadPredictions()
        }
    }
    private suspend fun loadModel() {
        withContext(Dispatchers.Default) {
            tfliteInterpreter = TFLiteInterpreter(context, "enhanced_health_risk_model.tflite")
            isModelLoaded = true
            loadPredictions() // Attempt to load predictions once the model is ready
        }
    }

    private fun savePredictionsToFirestore(predictions: List<Triple<String, Float, String>>) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No user logged in")
            return
        }

        val userId = currentUser.uid
        val timestamp = System.currentTimeMillis()

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
            .add(predictionData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Prediction saved with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding prediction", e)
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

                val input = withContext(Dispatchers.IO) {
                    healthDataProcessor.getUserHealthData()
                }

                if (input == null) {
                    Log.d(TAG, "User health data not available yet")
                    errorMessage = "Health data not available. Please complete your health assessment."
                    isLoading = false
                    return@launch
                }

                input.let { modelInput ->
                    Log.d(TAG, "Raw input values: ${modelInput.values.zip(modelInput.labels)}")

                    val outputData = withContext(Dispatchers.Default) {
                        tfliteInterpreter?.predict(modelInput.values.toFloatArray()) ?: floatArrayOf()
                    }
                    Log.d(TAG, "Raw model output: ${outputData.toList()}")

                    val newPredictions = HealthDataProcessor.riskCategories.zip(outputData.toList()).map { (category, risk) ->
                        val riskLevel = classifyRisk(risk)
                        Log.d(TAG, "$category: $risk ($riskLevel)")
                        val context = getRiskContext(category, risk, modelInput.values[0])
                        Log.d(TAG, "$category: $risk ($riskLevel)")
                        Log.d(TAG, "Context: $context")
                        Triple(category, risk, context)
                    }

                    if (arePredictionsDifferent(predictions, newPredictions)) {
                        predictions = newPredictions
                        savePredictionsToFirestore(newPredictions)
                        Log.d(TAG, "New predictions saved to Firestore")
                    } else {
                        Log.d(TAG, "Predictions unchanged, not saving to Firestore")
                    }
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
        val ageValue = age.toInt() // Convert to Int for comparison
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
        healthDataProcessor.removeListener { loadPredictions() }
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
                .limit(10) // Limit to the 10 most recent predictions
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
                    }.distinctBy { it.predictions } // Keep only unique predictions
                    _predictionHistory.value = history
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting prediction history", exception)
                }
        }
    }

    fun resetPredictions()
    {
        predictions = null
        modelInput = null
        isLoading = false
        errorMessage = null
        _predictionHistory.value = emptyList()
    }

    private fun arePredictionsDifferent(oldPredictions: List<Triple<String, Float, String>>?, newPredictions: List<Triple<String, Float, String>>): Boolean {
        if (oldPredictions == null) return true
        if (oldPredictions.size != newPredictions.size) return true
        return oldPredictions.zip(newPredictions).any { (old, new) ->
            old.first != new.first || old.second != new.second
        }
    }
    data class PredictionHistoryItem(
        val timestamp: Long,
        val predictions: List<Triple<String, Float, String>>
    )
}