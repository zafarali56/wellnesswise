package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.MedicalHistorySection
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.data.medicalHistoryQuestions
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.R

@Composable
fun MedicalHistoryScreen(registrationViewModel: RegistrationViewModel) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(16.dp)
    ) {
        LazyColumn {
            item {
                HeadingTextComponent(value = stringResource(id = R.string.MedicalHistory))
                MedicalHistorySection(registrationViewModel = registrationViewModel, questions = medicalHistoryQuestions)
            }
        }
    }
    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
    }
}

@Composable
@Preview
fun MedicalHistoryScreenPreview() {
    MedicalHistoryScreen(viewModel())
}
