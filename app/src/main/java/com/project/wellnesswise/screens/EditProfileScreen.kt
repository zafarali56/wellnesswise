package com.project.wellnesswise.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.CustomBloodPressureInput
import com.project.wellnesswise.components.ui.HealthDataTextField
import com.project.wellnesswise.components.ui.MyNumberField
import com.project.wellnesswise.components.ui.MyTextField
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userData: Map<String, Any>?,
    onSave: (Map<String, Any>) -> Unit,
    onBack: () -> Unit,
    onEditHealthAssessment: () -> Unit,
    colorScheme: ColorScheme
) {
    var fullName by remember { mutableStateOf(userData?.get("fullName") as? String ?: "") }
    var age by remember { mutableStateOf(userData?.get("age")?.toString() ?: "") }
    var weight by remember { mutableStateOf(userData?.get("weight")?.toString() ?: "") }
    var height by remember { mutableStateOf(userData?.get("height")?.toString() ?: "") }

    var heartRate by remember { mutableStateOf(userData?.get("heartRate")?.toString() ?: "") }
    var bloodSugar by remember { mutableStateOf(userData?.get("bloodSugar")?.toString() ?: "") }
    var cholesterol by remember { mutableStateOf(userData?.get("cholesterol")?.toString() ?: "") }
    val context = LocalContext.current
    val dataSourcePreference = userData?.get("dataSourcePreference") as? String ?: "MANUAL"

    var bloodPressure by remember {
        mutableStateOf(userData?.get("bloodPressure") as? String ?: "")
    }
    var systolic by remember { mutableStateOf(bloodPressure.split("/").firstOrNull() ?: "") }
    var diastolic by remember { mutableStateOf(bloodPressure.split("/").lastOrNull() ?: "") }

    fun getAllUserData(): Map<String, Any> {
        val updatedData = userData?.toMutableMap() ?: mutableMapOf()

        // Update only the fields that can be edited in this screen
        updatedData["fullName"] = fullName
        updatedData["age"] = age.toIntOrNull() ?: updatedData["age"] ?: 0
        updatedData["weight"] = weight.toDoubleOrNull() ?: updatedData["weight"] ?: 0.0
        updatedData["height"] = height.toDoubleOrNull() ?: updatedData["height"] ?: 0.0
        updatedData["cholesterol"] = cholesterol.toDoubleOrNull() ?: updatedData["cholesterol"] ?: 0.0

        // Only update these fields if the data source is MANUAL
        if (dataSourcePreference == "MANUAL") {
            updatedData["bloodPressure"] = bloodPressure
            updatedData["heartRate"] = heartRate.toIntOrNull() ?: updatedData["heartRate"] ?: 0
            updatedData["bloodSugar"] = bloodSugar.toDoubleOrNull() ?: updatedData["bloodSugar"] ?: 0.0
        }

        return updatedData
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit User Profile", color = colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
            ) {
                item {
                    MyTextField(
                        labelValue = "Full Name",
                        initialValue = fullName,
                        onTextSelected = { fullName = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    MyNumberField(
                        labelValue = "Age",
                        initialValue = age,
                        onTextSelected = { age = it?.toString() ?: "" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    MyNumberField(
                        labelValue = "Weight (kg)",
                        initialValue = weight,
                        onTextSelected = { weight = it?.toString() ?: "" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    MyNumberField(
                        labelValue = "Height (cm)",
                        initialValue = height,
                        onTextSelected = { height = it?.toString() ?: "" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    HealthDataTextField(
                        value = cholesterol,
                        onValueChange = { cholesterol = it },
                        label = "Cholesterol (mg/dL)",
                        isError = false,
                        errorMessage = "Invalid cholesterol value",
                        enabled = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    if (dataSourcePreference == "MANUAL") {
                        CustomBloodPressureInput(
                            systolic = systolic,
                            diastolic = diastolic,
                            onSystolicChange = { newSystolic ->
                                systolic = newSystolic
                                bloodPressure = "$newSystolic/$diastolic"
                            },
                            onDiastolicChange = { newDiastolic ->
                                diastolic = newDiastolic
                                bloodPressure = "$systolic/$newDiastolic"
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        HealthDataTextField(
                            value = heartRate,
                            onValueChange = { heartRate = it },
                            label = "Heart Rate (bpm)",
                            isError = false,
                            errorMessage = "Invalid heart rate",
                            enabled = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        HealthDataTextField(
                            value = bloodSugar,
                            onValueChange = { bloodSugar = it },
                            label = "Blood Sugar (mg/dL)",
                            isError = false,
                            errorMessage = "Invalid blood sugar value",
                            enabled = true
                        )
                    } else {
                        // Display read-only fields for Google Fit data
                        Text("Blood Pressure: ${userData?.get("bloodPressure") ?: "N/A"}")
                        Text("Heart Rate: ${userData?.get("heartRate") ?: "N/A"}")
                        Text("Blood Sugar: ${userData?.get("bloodSugar") ?: "N/A"}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ButtonComponent(
                        value = "Edit Health Assessment",
                        onButtonClicked = onEditHealthAssessment,
                        isEnabled = true
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ButtonComponent(
                        value = "Save Changes",
                        onButtonClicked = {
                            val updatedData = getAllUserData()
                            onSave(updatedData)
                            showToast(context, "Profile updated successfully")
                        },
                        isEnabled = true
                    )
                }
            }
        }
    }
    SystemBackButtonHandler {
        onBack()
    }
}