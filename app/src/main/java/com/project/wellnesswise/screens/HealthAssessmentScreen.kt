package com.project.wellnesswise.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.components.ui.AdditionalDataSection
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.EnvironmentalFactorsSection
import com.project.wellnesswise.components.ui.HealthAssessmentMode
import com.project.wellnesswise.components.ui.LifestyleHabitsSection
import com.project.wellnesswise.components.ui.MedicalHistorySection
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
                    onSave()
                    onBack()
                }
            }
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
                    .padding(horizontal = 26.dp, vertical = 10.dp),
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
                                    registrationViewModel.onEvent(UIEvent.SaveHealthAssessmentClicked)
                                }
                                HealthAssessmentMode.EDIT -> {
                                    registrationViewModel.onEvent(UIEvent.SaveHealthAssessmentClicked)
                                }
                            }
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
