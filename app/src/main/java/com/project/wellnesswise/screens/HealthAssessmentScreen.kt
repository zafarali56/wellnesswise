package com.project.wellnesswise.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.components.ui.AdditionalDataSection
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.EnvironmentalFactorsSection
import com.project.wellnesswise.components.ui.HealthAssessmentMode
import com.project.wellnesswise.components.ui.LifestyleHabitsSection
import com.project.wellnesswise.components.ui.MedicalHistorySection
import com.project.wellnesswise.viewModels.RegistrationViewModel
import com.project.wellnesswise.viewModels.UIEvent
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
                    WellnessWiseAppRouter.navigateTo(Screen.WelcomeScreen)

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
                    .padding(horizontal = 16.dp)
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            MedicalHistorySection(registrationViewModel, validationResults)
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            LifestyleHabitsSection(registrationViewModel, validationResults)
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            EnvironmentalFactorsSection(registrationViewModel, validationResults)
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            AdditionalDataSection(registrationViewModel, validationResults)
                        }
                    }
                }
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
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }

            }
        }
    }

    SystemBackButtonHandler {
        onBack()
    }
}
