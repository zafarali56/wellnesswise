package com.project.wellnesswise.screens

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.data.HealthDataProcessor
import com.project.wellnesswise.ml.TFLiteInterpreter
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionsScreen() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var predictions by remember { mutableStateOf<List<Triple<String, Float, String>>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State to track when data was last updated
    var lastDataUpdateTimestamp by remember { mutableStateOf(0L) }

    val colorScheme = if (useDarkIcons) {
        dynamicLightColorScheme(context)
    } else {
        dynamicDarkColorScheme(context)
    }



    fun classifyRisk(prediction: Float): String {
        return when {
            prediction < 0.2f -> "Very Low"
            prediction < 0.4f -> "Low"
            prediction < 0.6f -> "Moderate"
            prediction < 0.8f -> "High"
            else -> "Very High"
        }
    }

    fun getRiskContext(category: String, risk: Float, age: Int): String {
        return when (category) {
            "Diabetes" -> when {
                risk < 0.05f -> "Your diabetes risk is very low. Keep up your healthy lifestyle."
                risk < 0.15f -> "Your diabetes risk is low. Maintain a healthy diet and exercise routine."
                risk < 0.3f -> "Consider discussing diabetes prevention with your doctor."
                else -> "Schedule a check-up to assess your diabetes risk factors."
            }
            "Cardiovascular Disease" -> when {
                risk < 0.05f -> "Your heart health looks excellent. Maintain your healthy habits."
                risk < 0.15f -> "Your heart health is good. Keep up the healthy lifestyle."
                risk < 0.3f -> "Some cardiovascular risk factors present. Focus on heart-healthy choices."
                else -> "Discuss your heart health with a healthcare professional."
            }
            "Hypertension" -> when {
                risk < 0.05f -> "Your blood pressure appears to be in a very healthy range."
                risk < 0.15f -> "Your blood pressure appears to be in a healthy range."
                risk < 0.3f -> "Monitor your blood pressure regularly and maintain a healthy lifestyle."
                else -> "Consider discussing blood pressure management with your doctor."
            }
            "Obesity" -> when {
                risk < 0.05f -> "Your weight appears to be in a very healthy range."
                risk < 0.15f -> "Your weight appears to be in a healthy range."
                risk < 0.3f -> "Focus on maintaining a balanced diet and regular exercise."
                else -> "Consider discussing weight management strategies with a healthcare professional."
            }
            "Cancer" -> when {
                risk < 0.05f -> "Your cancer risk factors appear to be very low."
                risk < 0.15f -> "Your cancer risk factors appear to be low."
                age < 40 -> "Stay informed about cancer screening recommendations for your age group."
                age < 60 -> "Discuss age-appropriate cancer screenings with your doctor."
                else -> "Regular check-ups and screenings are important at your age."
            }
            else -> "Consult with a healthcare professional for personalized advice."
        }
    }
    fun adjustModelOutput(output: List<Float>): List<Float> {
        val adjustments = listOf(1.2f, 1.2f, 1.1f, 1.2f, 1.0f)
        val intercepts = listOf(-0.1f, -0.1f, -0.05f, -0.1f, -0.05f)
        return output.zip(adjustments.zip(intercepts)).map { (value, adjustment) ->
            val (slope, intercept) = adjustment
            (value * slope + intercept).coerceIn(0f, 1f)
        }
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = useDarkIcons
        )
    }

    fun loadPredictions() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val healthDataProcessor = HealthDataProcessor()
                val modelInput = withContext(Dispatchers.IO) {
                    healthDataProcessor.getUserHealthData()
                }

                modelInput?.let { input ->
                    Log.d("PredictionsScreen", "Model input shape: ${input.values.size}")
                    val tfliteInterpreter = TFLiteInterpreter(context, "enhanced_health_risk_model.tflite")

                    // Actual prediction
                    val outputData = withContext(Dispatchers.Default) {
                        tfliteInterpreter.predict(input.values.map { it.toFloat() })
                    }
                    val adjustedOutput = adjustModelOutput(outputData)
                    Log.d("PredictionsScreen", "Raw model output: $outputData")
                    Log.d("PredictionsScreen", "Adjusted model output: $adjustedOutput")

                    val userAge = input.values.first().toInt() // Assuming age is the first input value

                    predictions = HealthDataProcessor.riskCategories.zip(adjustedOutput).map { (category, risk) ->
                        val riskLevel = classifyRisk(risk)
                        val context = getRiskContext(category, risk, userAge)
                        Log.d("PredictionsScreen", "$category: $risk ($riskLevel)")
                        Log.d("PredictionsScreen", "Context: $context")
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
                Log.e("PredictionsScreen", "Error: ", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Load predictions when the screen is first composed or when data changes
    LaunchedEffect(lastDataUpdateTimestamp) {
        loadPredictions()
    }

    // Set up a listener for data changes
    DisposableEffect(Unit) {
        val listener: () -> Unit = {
            lastDataUpdateTimestamp = System.currentTimeMillis()
        }
        HealthDataProcessor.addDataChangeListener(listener)
        onDispose {
            HealthDataProcessor.removeDataChangeListener(listener)
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Health Risk Predictions") },
                    navigationIcon = {
                        IconButton(onClick = {
                            WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = colorScheme.background
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = colorScheme.background
            ) {
                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                        }
                    }
                    errorMessage != null -> {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            item {
                                Text(
                                    text = "Your Health Risk Predictions:",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                            predictions?.let { preds ->
                                items(preds) { (category, risk, context) ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = category, style = MaterialTheme.typography.bodyLarge)
                                            val riskLevel = classifyRisk(risk)
                                            Text(
                                                text = "$riskLevel (${(risk * 100).toInt()}%)",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = when (riskLevel) {
                                                    "Very Low", "Low" -> MaterialTheme.colorScheme.primary
                                                    "Moderate" -> MaterialTheme.colorScheme.secondary
                                                    "High", "Very High" -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                        Text(
                                            text = context,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
    }
}