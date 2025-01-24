package com.project.wellnesswise.screens

import com.project.wellnesswise.viewModels.HealthDataViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.CustomBloodPressureInput
import com.project.wellnesswise.components.ui.HeadingTextComponent
import com.project.wellnesswise.components.ui.HealthDataTextField
import com.project.wellnesswise.components.ui.LoadingAnimation
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.utils.RecommendationDataUploader
import kotlinx.coroutines.launch

@Composable
fun HealthDataScreen(
    healthDataViewModel: HealthDataViewModel,
) {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current
    // Use dynamic color scheme
    val colorScheme = when {
        useDarkIcons -> dynamicLightColorScheme(context)
        else -> dynamicDarkColorScheme(context)
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = useDarkIcons
        )
    }

    colorResource(id = R.color.primary)
    val healthData by healthDataViewModel.healthData.collectAsState()
    val isSyncing by healthDataViewModel.isSyncing.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var isManualInput by remember { mutableStateOf(healthData["dataSourcePreference"] != "GOOGLE_FIT") }

    val fitnessOptions = remember {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
            .build()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            coroutineScope.launch {
                healthDataViewModel.syncWithGoogleFit(context, fitnessOptions)
                isManualInput = false
            }
        } else {
            healthDataViewModel.setSyncMessage("Failed to obtain Google Fit permissions")
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
             item {
                 Spacer(modifier = Modifier.height(20.dp))
                 HeadingTextComponent(value = "Health Data")
                 Spacer(modifier = Modifier.height(16.dp))

                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween
                 ) {
                     Button(
                         onClick = { isManualInput = true },
                         colors = ButtonDefaults.buttonColors(
                             containerColor = if (isManualInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                         )
                     ) {
                         Text("Manual Input", fontWeight = FontWeight.Bold)
                     }
                     Button(
                         onClick = {
                             isManualInput = false
                             coroutineScope.launch {
                                 healthDataViewModel.handleGoogleFitSync(
                                     context,
                                     fitnessOptions,
                                     googleSignInLauncher
                                 )
                             }
                         },
                         colors = ButtonDefaults.buttonColors(
                             containerColor = if (!isManualInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                         )
                     ) {
                         Text("Google Fit Sync", fontWeight = FontWeight.Bold)
                     }
                 }

                 Spacer(modifier = Modifier.height(16.dp))
             }
                if (isManualInput) {
                    item {
                        CustomBloodPressureInput(
                            systolic = systolic,
                            diastolic = diastolic,
                            onSystolicChange = { newSystolic ->
                                systolic = newSystolic
                                updateBloodPressure(newSystolic, diastolic, healthDataViewModel)
                            },
                            onDiastolicChange = { newDiastolic ->
                                diastolic = newDiastolic
                                updateBloodPressure(systolic, newDiastolic, healthDataViewModel)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HealthDataTextField(
                            value = healthData["heartRate"] as? String ?: "",
                            onValueChange = { healthDataViewModel.updateManualHealthData(heartRate = it) },
                            label = "Heart Rate (bpm)",
                            isError = false,
                            errorMessage = "Invalid heart rate",
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HealthDataTextField(
                            value = healthData["bloodSugar"] as? String ?: "",
                            onValueChange = { healthDataViewModel.updateManualHealthData(bloodSugar = it) },
                            label = "Blood Sugar (mg/dL)",
                            isError = false,
                            errorMessage = "Invalid blood sugar value",
                            enabled = true
                        )
                    }
                } else {
                    if (isSyncing) {
                    item{    LoadingAnimation()}
                    } else {
                        item {
                            Text("Data synced from Google Fit:")
                            Text("Blood Pressure: ${healthData["bloodPressure"] ?: "N/A"}")
                            Text("Heart Rate: ${healthData["heartRate"] ?: "N/A"}")
                            Text("Blood Sugar: ${healthData["bloodSugar"] ?: "N/A"}")
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Additional Health Information",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    HealthDataTextField(
                        value = healthDataViewModel.cholesterol.collectAsState().value,
                        onValueChange = { healthDataViewModel.updateCholesterol(it) },
                        label = "Cholesterol (mg/dL)",
                        isError = false,
                        errorMessage = "Invalid cholesterol value",
                        enabled = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HealthDataTextField(
                        value = healthDataViewModel.triglycerides.collectAsState().value,
                        onValueChange = { healthDataViewModel.updateTriglycerides(it) },
                        label = "Triglycerides (mg/dL)",
                        isError = false,
                        errorMessage = "Invalid triglycerides value",
                        enabled = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HealthDataTextField(
                        value = healthDataViewModel.waistCircumference.collectAsState().value,
                        onValueChange = { healthDataViewModel.updateWaistCircumference(it) },
                        label = "Waist Circumference (cm)",
                        isError = false,
                        errorMessage = "Invalid waist circumference",
                        enabled = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ButtonComponent(
                        value = "Submit",
                        onButtonClicked = {
                            coroutineScope.launch {
                                try {
                                    // First, send health data to Firestore
                                    healthDataViewModel.sendHealthDataToFirestore()

                                    // After health data is saved, navigate to recommendation setup screen
                                    WellnessWiseAppRouter.navigateTo(Screen.HealthAssessmentScreen)

                                } catch (e: Exception) {
                                    println("Error saving health data: ${e.message}")
                                }
                            }
                        },
                        isEnabled = true
                    )
                }
            }
        }
    }
}
@Composable
@Preview
fun HealthDataScreenPreview() {
    HealthDataScreen(HealthDataViewModel())
}
private fun updateBloodPressure(systolic: String, diastolic: String, viewModel: HealthDataViewModel) {
    if (systolic.isNotEmpty() && diastolic.isNotEmpty()) {
        viewModel.updateManualHealthData(bloodPressure = "$systolic/$diastolic")
    }
}