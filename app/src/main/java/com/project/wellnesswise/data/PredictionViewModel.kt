package com.project.wellnesswise.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.wellnesswise.ml.TFLiteInterpreter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PredictionsViewModel(private val context: Context) : ViewModel() {
    private val healthDataProcessor = HealthDataProcessor { updateLastDataTimestamp() }

    var predictions by mutableStateOf<List<Triple<String, Float, String>>?>(null)
    var modelInput by mutableStateOf<List<Float>?>(null)
        private set
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        healthDataProcessor.startListeningForChanges()
    }

    private fun updateLastDataTimestamp() {
        loadPredictions()
    }

    fun loadPredictions() {
        viewModelScope.launch {
            try {
                val input = withContext(Dispatchers.IO) {
                    healthDataProcessor.getUserHealthData()
                }

                input?.let { modelInput ->
                    Log.d("PredictionsViewModel", "Model input shape: ${modelInput.values.size}")
                    Log.d("PredictionsViewModel", "Normalized model input values: ${modelInput.values}")

                    this@PredictionsViewModel.modelInput = modelInput.values

                    val tfliteInterpreter = TFLiteInterpreter(context, "enhanced_health_risk_model.tflite")

                    val outputData = withContext(Dispatchers.Default) {
                        tfliteInterpreter.predict(modelInput.values.toFloatArray())
                    }
                    Log.d("PredictionsViewModel", "Raw model output: ${outputData.toList()}")

                    predictions = HealthDataProcessor.riskCategories.zip(outputData.toList()).map { (category, risk) ->
                        val riskLevel = classifyRisk(risk)
                        val context = getRiskContext(category, risk, modelInput.values[0]) // Assuming age is the first input
                        Log.d("PredictionsViewModel", "$category: $risk ($riskLevel)")
                        Log.d("PredictionsViewModel", "Context: $context")
                        Triple(category, risk, context)
                    }

                    tfliteInterpreter.close()
                } ?: run {
                    errorMessage = "Failed to retrieve user health data"
                }
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is IllegalArgumentException -> "Data processing error: ${e.message}"
                    else -> "An error occurred: ${e.message}"
                }
                Log.e("PredictionsViewModel", "Error: ", e)
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
        healthDataProcessor.stopListeningForChanges()
    }
}