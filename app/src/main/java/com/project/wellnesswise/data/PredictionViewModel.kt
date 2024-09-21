import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.wellnesswise.data.HealthDataProcessor
import com.project.wellnesswise.ml.TFLiteInterpreter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PredictionsViewModel(private val context: Context) : ViewModel() {
    private val healthDataProcessor = HealthDataProcessor { updateLastDataTimestamp() }

    var predictions by mutableStateOf<List<Triple<String, Float, String>>?>(null)
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        healthDataProcessor.startListeningForChanges()
    }

    private fun updateLastDataTimestamp() {
        loadPredictions()
    }

    fun loadPredictions() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val modelInput = withContext(Dispatchers.IO) {
                    healthDataProcessor.getUserHealthData()
                }

                modelInput?.let { input ->
                    Log.d("PredictionsViewModel", "Model input shape: ${input.values.size}")
                    Log.d("PredictionsViewModel", "Model input values: ${input.values}")

                    val tfliteInterpreter = TFLiteInterpreter(context, "enhanced_health_risk_model.tflite")

                    val outputData = withContext(Dispatchers.Default) {
                        tfliteInterpreter.predict(input.values.map { it.toFloat() })
                    }
                    Log.d("PredictionsViewModel", "Raw model output: $outputData")

                    val adjustedOutput = adjustModelOutput(outputData)
                    Log.d("PredictionsViewModel", "Adjusted model output: $adjustedOutput")


                    val userAge = input.values.first().toInt()

                    predictions = HealthDataProcessor.riskCategories.zip(adjustedOutput).map { (category, risk) ->
                        val riskLevel = classifyRisk(risk)
                        val context = getRiskContext(category, risk, userAge)
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
                    is IllegalArgumentException -> "Data normalization error: ${e.message}"
                    else -> "An error occurred: ${e.message}"
                }
                Log.e("PredictionsViewModel", "Error: ", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun adjustModelOutput(output: List<Float>): List<Float> {
        val adjustments = listOf(1.2f, 1.2f, 1.1f, 1.2f, 1.0f)
        val intercepts = listOf(-0.1f, -0.1f, -0.05f, -0.1f, -0.05f)
        return output.zip(adjustments.zip(intercepts)).map { (value, adjustment) ->
            val (slope, intercept) = adjustment
            (value * slope + intercept).coerceIn(0f, 1f)
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

    private fun getRiskContext(category: String, risk: Float, age: Int): String {
        // Implement the risk context logic here
        // This should be the same as the function in your PredictionsScreen
        return "Risk context for $category"
    }

    override fun onCleared() {
        super.onCleared()
        healthDataProcessor.stopListeningForChanges()
    }
}