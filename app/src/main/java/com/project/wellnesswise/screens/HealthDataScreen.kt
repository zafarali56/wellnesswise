package com.project.wellnesswise.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.MyTextField
import com.project.wellnesswise.data.RegistrationViewModel

@Composable
fun HealthDataScreen(registrationViewModel: RegistrationViewModel) {
    var bloodPressure by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var bloodSugar by remember { mutableStateOf("") }
    var cholesterol by remember { mutableStateOf("") }

    val uiState by registrationViewModel.registrationUIState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
HeadingTextComponent(value = stringResource(id = R.string.HealthData))

        MyTextField(
            labelValue = "Blood Pressure (e.g. 80/120)",
            initialValue = bloodPressure,
            onTextSelected = { bloodPressure = it },
        )
        if (uiState.bloodPressureError) {
            Text(text = "Invalid blood pressure format", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(8.dp))

        MyTextField(
            labelValue = "Heart Rate (e.g. 70)",
            initialValue = heartRate,
            onTextSelected = { heartRate = it },
        )
        if (uiState.heartRateError) {
            Text(text = "Invalid heart rate", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(8.dp))

        MyTextField(
            labelValue = "Blood Sugar Levels (e.g. 100)",
            initialValue = bloodSugar,
            onTextSelected = { bloodSugar = it },
        )
        if (uiState.bloodSugarError) {
            Text(text = "Invalid blood sugar level", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(8.dp))

        MyTextField(
            labelValue = "Cholesterol Levels (e.g. 200)",
            initialValue = cholesterol,
            onTextSelected = { cholesterol = it },
        )
        if (uiState.cholesterolError) {
            Text(text = "Invalid cholesterol level", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))

        ButtonComponent(
            value = "Submit Health Data",
            onButtonClicked = {
                registrationViewModel.updateHealthParameters(
                    bloodPressure = bloodPressure,
                    heartRate = heartRate,
                    bloodSugar = bloodSugar,
                    cholesterol = cholesterol
                )
                if (!uiState.bloodPressureError && !uiState.heartRateError && !uiState.bloodSugarError && !uiState.cholesterolError) {
                    registrationViewModel.sendHealthDataToFirestore()
                }
            },
            isEnabled = true
        )
    }
}

@Composable
@Preview
fun HealthDataScreenPreview() {
    HealthDataScreen(RegistrationViewModel())
}
