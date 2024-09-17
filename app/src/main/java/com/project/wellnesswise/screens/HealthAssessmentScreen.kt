package com.project.wellnesswise.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.components.ui.*
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.data.UIEvent
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAssessmentScreen    (
    registrationViewModel: RegistrationViewModel,
    mode: HealthAssessmentMode,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current

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

    val healthAssessmentValidated by registrationViewModel.healthAssessmentValidated

    LaunchedEffect(healthAssessmentValidated) {
        if (healthAssessmentValidated) {
            when (mode) {
                HealthAssessmentMode.SIGNUP -> {
                    WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
                }
                HealthAssessmentMode.EDIT -> {
                    // Handle edit mode completion
                    onSave()
                    onBack()
                }
            }
            // Reset the validation state
            registrationViewModel.resetHealthAssessmentValidation()
        }
    }
    LaunchedEffect(Unit) {
        if (mode == HealthAssessmentMode.EDIT) {
            registrationViewModel.loadExistingHealthAssessmentData(registrationViewModel.getUserData())
        }
    }

    val validationResults = registrationViewModel.validationResults.value
    val registrationUIState = registrationViewModel.registrationUIState.value
    var showToast by remember { mutableStateOf(false) }
    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (mode) {
                                HealthAssessmentMode.SIGNUP -> "Health Assessment"
                                HealthAssessmentMode.EDIT -> "Edit Health Assessment"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { MedicalHistorySection(registrationViewModel, validationResults) }
                item { LifestyleHabitsSection(registrationViewModel, validationResults) }
                item { EnvironmentalFactorsSection(registrationViewModel, validationResults) }
                item { AdditionalDataSection(registrationViewModel, validationResults) }
                item {
                    ButtonComponent(
                        value = when (mode) {
                            HealthAssessmentMode.SIGNUP -> "Save and Continue"
                            HealthAssessmentMode.EDIT -> "Save Changes"
                        },
                        onButtonClicked = {
                            when (mode) {
                                HealthAssessmentMode.SIGNUP -> {
                                    if (Validator.isValidRegistrationUIState(registrationUIState)) {
                                        registrationViewModel.onEvent(UIEvent.SaveHealthAssessmentClicked)
                                    } else {
                                        showToast = true
                                    }
                                }
                                HealthAssessmentMode.EDIT -> {
                                    // In edit mode, we don't check the validator
                                    registrationViewModel.onEvent(UIEvent.SaveHealthAssessmentClicked)
                                }
                            }
                        },
                        isEnabled = true
                    )

                }
                item {
                    CustomToast(
                        message = "Please fill out all fields correctly",
                        isVisible = showToast,
                        onDismiss = { showToast = false }
                    )
                }
            }
        }
    }

    SystemBackButtonHandler {
        onBack()
    }
}

@Composable
fun MedicalHistorySection(viewModel: RegistrationViewModel, validationResults: Map<String, Boolean>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Medical History",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        YesNoQuestion(
            question = "Family History of Diabetes",
            answer = viewModel.registrationUIState.value.familyDiabetes,
            onAnswerSelected = { viewModel.onEvent(UIEvent.FamilyDiabetesChanged(it)) },
            isError = validationResults["familyDiabetes"] == false
        )
        YesNoQuestion(
            question = "Family History of Heart Disease",
            answer = viewModel.registrationUIState.value.familyHeart,
            onAnswerSelected = { viewModel.onEvent(UIEvent.FamilyHeartChanged(it)) },
            isError = validationResults["familyHeart"] == false
        )
        YesNoQuestion(
            question = "Family History of Cancer",
            answer = viewModel.registrationUIState.value.familyCancer,
            onAnswerSelected = { viewModel.onEvent(UIEvent.FamilyCancerChanged(it)) },
            isError = validationResults["familyCancer"] == false
        )
        YesNoQuestion(
            question = "Previous Surgeries",
            answer = viewModel.registrationUIState.value.previousSurgeries,
            onAnswerSelected = { viewModel.onEvent(UIEvent.PreviousSurgeriesChanged(it)) },
            isError = validationResults["previousSurgeries"] == false
        )
        YesNoQuestion(
            question = "Chronic Conditions",
            answer = viewModel.registrationUIState.value.chronicConditions,
            onAnswerSelected = { viewModel.onEvent(UIEvent.ChronicConditionsChanged(it)) },
            isError = validationResults["chronicConditions"] == false
        )
    }
}
@Composable
fun LifestyleHabitsSection(viewModel: RegistrationViewModel, validationResults: Map<String, Boolean>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Lifestyle Habits",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        YesNoQuestion(
            question = "Do you smoke?",
            answer = if (viewModel.registrationUIState.value.smoking) "Yes" else "No",
            onAnswerSelected = { viewModel.onEvent(UIEvent.SmokingChanged(it == "Yes")) },
            isError = validationResults["smoking"] == false
        )
        ScaleInput(
            value = viewModel.registrationUIState.value.alcoholConsumption.toString(),
            onValueChange = { viewModel.onEvent(UIEvent.AlcoholConsumptionChanged(it.toIntOrNull() ?: 0)) },
            label = "Alcohol consumption level",
            range = 1..5,
            isError = validationResults["physicalActivity"] == false
        )
        ScaleInput(
            value = viewModel.registrationUIState.value.physicalActivity.toString(),
            onValueChange = { viewModel.onEvent(UIEvent.PhysicalActivityChanged(it.toIntOrNull() ?: 0)) },
            label = "Physical Activity Level",
            range = 1..5,
            isError = validationResults["physicalActivity"] == false
        )
        ScaleInput(
            value = viewModel.registrationUIState.value.dietQuality.toString(),
            onValueChange = { viewModel.onEvent(UIEvent.DietQualityChanged(it.toIntOrNull() ?: 0)) },
            label = "Diet Quality",
            range = 1..5,
            isError = validationResults["dietQuality"] == false
        )
        NumberField(
            labelValue = "Sleep Hours (per night)",
            initialValue = viewModel.registrationUIState.value.sleepHours.toString(),
            onTextSelected = { viewModel.onEvent(UIEvent.SleepHoursChanged(it?.toIntOrNull() ?: 0)) },
            isError = validationResults["sleepHours"] == false
        )
    }
}

@Composable
fun EnvironmentalFactorsSection(viewModel: RegistrationViewModel, validationResults: Map<String, Boolean>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Environmental Factors",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        NumberField(
            labelValue = "Air Quality Index",
            initialValue = viewModel.registrationUIState.value.airQualityIndex.toString(),
            onTextSelected = { viewModel.onEvent(UIEvent.AirQualityIndexChanged(it?.toIntOrNull() ?: 0)) },
            isError = validationResults["airQualityIndex"] == false
        )
        ScaleInput(
            value = viewModel.registrationUIState.value.exposureToPollutants.toString(),
            onValueChange = { viewModel.onEvent(UIEvent.ExposureToPollutantsChanged(it.toIntOrNull() ?: 0)) },
            label = "Exposure to Pollutants",
            range = 1..5,
            isError = validationResults["exposureToPollutants"] == false
        )
    }
}

@Composable
fun AdditionalDataSection(viewModel: RegistrationViewModel, validationResults: Map<String, Boolean>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Additional Information",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        ScaleInput(
            value = viewModel.registrationUIState.value.stressLevel.toString(),
            onValueChange = { viewModel.onEvent(UIEvent.StressLevelChanged(it.toIntOrNull() ?: 0)) },
            label = "Stress Level",
            range = 1..5,
            isError = validationResults["stressLevel"] == false
        )
        ScaleInput(
            value = viewModel.registrationUIState.value.accessToHealthcare.toString(),
            onValueChange = { viewModel.onEvent(UIEvent.AccessToHealthcareChanged(it.toIntOrNull() ?: 0)) },
            label = "Access to Healthcare",
            range = 1..5,
            isError = validationResults["accessToHealthcare"] == false
        )
    }
}
@Composable
fun YesNoQuestion(
    question: String,
    answer: String,
    onAnswerSelected: (String) -> Unit,
    isError: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnswerButton(
                text = "Yes",
                isSelected = answer == "Yes",
                onClick = { onAnswerSelected("Yes") },
                modifier = Modifier.weight(1f)
            )
            AnswerButton(
                text = "No",
                isSelected = answer == "No",
                onClick = { onAnswerSelected("No") },
                modifier = Modifier.weight(1f)
            )
        }
        if (isError) {
            Text(
                text = "Please select an answer",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AnswerButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Text(text)
    }
}

@Composable
fun NumberField(
    labelValue: String,
    initialValue: String,
    keyboardType: KeyboardType = KeyboardType.Number,
    onTextSelected: (String) -> Unit,
    isError: Boolean = false,
) {
    var textValue by remember { mutableStateOf(initialValue) }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.small,
        label = { Text(text = labelValue) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            focusedLabelColor = colorScheme.primary,
            cursorColor = colorScheme.primary,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        keyboardActions = KeyboardActions.Default,
        value = textValue,
        onValueChange = {
            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                textValue = it
                onTextSelected(it)
            }
        },
        isError = isError,
        singleLine = true,
    )
}

@Composable
fun ScaleInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    range: IntRange,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            range.forEach { number ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = value == number.toString(),
                        onClick = { onValueChange(number.toString()) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colorScheme.primary,
                            unselectedColor = colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (value == number.toString()) colorScheme.primary else colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (isError) {
            Text(
                text = "Please select a value",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

enum class HealthAssessmentMode {
    SIGNUP, EDIT
}

@Composable
fun CustomToast(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = MaterialTheme.colorScheme.error,
                shape = MaterialTheme.shapes.small,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}
