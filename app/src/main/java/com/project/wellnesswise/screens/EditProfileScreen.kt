package com.project.wellnesswise.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.CustomBloodPressureInput
import com.project.wellnesswise.components.ui.HealthDataTextField
import com.project.wellnesswise.components.ui.MyNumberField
import com.project.wellnesswise.components.ui.MyTextField
import com.project.wellnesswise.components.ui.formatHabitName
import com.project.wellnesswise.data.Habit
import com.project.wellnesswise.data.medicalHistoryQuestions
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userData: Map<String, Any>?,
    onSave: (Map<String, Any>) -> Unit,
    onBack: () -> Unit,
    onEditMedicalHistory: () -> Unit,
    onEditHabits: () -> Unit,
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
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    ButtonComponent(
                        value = "Edit Medical History",
                        onButtonClicked = onEditMedicalHistory,
                        isEnabled = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    ButtonComponent(
                        value = "Edit Habits",
                        onButtonClicked = onEditHabits,
                        isEnabled = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ButtonComponent(
                        value = "Save Changes",
                        onButtonClicked = {
                            val updatedData = mutableMapOf<String, Any>(
                                "fullName" to fullName,
                                "age" to (age.toIntOrNull() ?: 0),
                                "weight" to (weight.toDoubleOrNull() ?: 0.0),
                                "height" to (height.toDoubleOrNull() ?: 0.0),
                                "cholesterol" to (cholesterol.toDoubleOrNull() ?: 0.0)
                            )
                            if (dataSourcePreference == "MANUAL") {
                                updatedData["bloodPressure"] = bloodPressure
                                updatedData["heartRate"] = (heartRate.toIntOrNull() ?: 0)
                                updatedData["bloodSugar"] = (bloodSugar.toDoubleOrNull() ?: 0.0)
                            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicalHistoryScreen(
    medicalHistory: Map<String, String>,
    onSave: (Map<String, String>) -> Unit,
    onBack: () -> Unit,
    colorScheme: ColorScheme
) {
    var updatedMedicalHistory by remember { mutableStateOf(medicalHistory) }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit Medical History", color = colorScheme.onSurface) },
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
                    Spacer(modifier = Modifier.height(16.dp))
                    medicalHistoryQuestions.forEach { question ->

                        Text(text = question.question, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        question.suggestedAnswers.forEach { answer ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = updatedMedicalHistory[question.question] == answer,
                                    onClick = {
                                        updatedMedicalHistory =
                                            updatedMedicalHistory.toMutableMap().apply {
                                                put(question.question, answer)
                                            }
                                    },
                                    colors =
                                    RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = Color.Gray,
                                    ),
                                )
                                Text(text = answer)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ButtonComponent(
                        value = "Save Medical History",
                        onButtonClicked = {
                            onSave(updatedMedicalHistory)
                            showToast(context, "Medical history updated successfully")
                        },
                        isEnabled = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }


            }
        }
    }
    SystemBackButtonHandler {
        onBack()
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitsScreen(
    habits: List<Habit>,
    onSave: (List<Habit>) -> Unit,
    onBack: () -> Unit,
    colorScheme: ColorScheme
) {
    var selectedHabits by remember { mutableStateOf(habits.toSet()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit Habits", color = colorScheme.onSurface) },
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

                    Spacer(modifier = Modifier.height(16.dp))


                    Habit.entries.forEach { habit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedHabits.contains(habit),
                                onCheckedChange = { isChecked ->
                                    selectedHabits = if (isChecked) {
                                        selectedHabits + habit
                                    } else {
                                        selectedHabits - habit
                                    }
                                },
                                colors =
                                CheckboxDefaults.colors(
                                    checkedColor = colorScheme.primary ,
                                    uncheckedColor = Color.Gray,
                                ),
                            )
                            Text(text = formatHabitName(habit.name))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ButtonComponent(
                        value = "Save Habits",
                        onButtonClicked = {
                            onSave(selectedHabits.toList())
                            showToast(context, "Habits updated successfully")
                        },
                        isEnabled = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

            }
        }
    }
    SystemBackButtonHandler {
        onBack()
    }
}