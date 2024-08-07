package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.components.CheckBoxComponent
import com.project.wellnesswise.components.ClickableLoginTextComponent
import com.project.wellnesswise.components.DividerTextComponent
import com.project.wellnesswise.components.GenderSelection
import com.project.wellnesswise.components.HabbitAndMedHistoryButton
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.MyNumberField
import com.project.wellnesswise.components.MyPasswordField
import com.project.wellnesswise.components.MyTextField
import com.project.wellnesswise.data.LoginViewModel
import com.project.wellnesswise.data.UIEvent
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun SignUpScreen(loginViewModel: LoginViewModel) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    val validationResults = loginViewModel.validationResults.value
    val registrationUIState = loginViewModel.registrationUIState.value

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                HeadingTextComponent(value = stringResource(id = R.string.Create_an_account))
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                MyTextField(
                    labelValue = stringResource(id = R.string.Email),
                    initialValue = registrationUIState.email,
                    onTextSelected = {
                        loginViewModel.onEvent(UIEvent.EmailChanged(it))
                    }
                )
                if (validationResults["email"] == false) {
                    Text(text = "Invalid email", color = Color.Red)
                }
            }
            item {
                MyTextField(
                    labelValue = stringResource(id = R.string.FullName),
                    initialValue = registrationUIState.fullName,
                    onTextSelected = {
                        loginViewModel.onEvent(UIEvent.FullNameChanged(it))
                    }
                )
                if (validationResults["fullName"] == false) {
                    Text(text = "Invalid full name", color = Color.Red)
                }
            }
            item {
                MyNumberField(
                    labelValue = stringResource(id = R.string.Age),
                    initialValue = registrationUIState.age.toString(),
                    onTextSelected = {
                        loginViewModel.onEvent(UIEvent.AgeChanged(it ?: 0))
                    }
                )
                if (validationResults["age"] == false) {
                    Text(text = "Invalid age", color = Color.Red)
                }
            }
            item {
                Spacer(modifier = Modifier.height(6.dp))
                GenderSelection(
                    initialGender = registrationUIState.gender,
                    onGenderSelected = { gender ->
                        loginViewModel.onEvent(UIEvent.GenderChanged(gender))
                    }
                )
                if (validationResults["gender"] == false) {
                    Text(text = "Invalid gender", color = Color.Red)
                }
            }
            item {
                MyNumberField(
                    labelValue = stringResource(id = R.string.Height),
                    initialValue = registrationUIState.height.toString(),
                    onTextSelected = {
                        loginViewModel.onEvent(UIEvent.HeightChanged(it ?: 0))
                    }
                )
                if (validationResults["height"] == false) {
                    Text(text = "Invalid height", color = Color.Red)
                }
            }
            item {
                MyNumberField(
                    labelValue = stringResource(id = R.string.Weight),
                    initialValue = registrationUIState.weight.toString(),
                    onTextSelected = {
                        loginViewModel.onEvent(UIEvent.WeightChanged(it ?: 0))
                    }
                )
                if (validationResults["weight"] == false) {
                    Text(text = "Invalid weight", color = Color.Red)
                }
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
                HabbitAndMedHistoryButton(
                    text = stringResource(id = R.string.Habits),
                    onClick = { WellnessWiseAppRouter.navigateTo(Screen.HabitsScreen) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                HabbitAndMedHistoryButton(
                    text = stringResource(id = R.string.MedicalHistory),
                    onClick = { WellnessWiseAppRouter.navigateTo(Screen.MedicalHistoryScreen) }
                )
                if (validationResults["habits"] == false) {
                    Text(text = "Select at least one habit", color = Color.Red)
                }
                if (validationResults["medicalHistory"] == false) {
                    Text(text = "Provide medical history", color = Color.Red)
                }
            }
            item {
                MyPasswordField(
                    labelValue = stringResource(id = R.string.Password),
                    initialValue = registrationUIState.password,
                    onTextSelected = {
                        loginViewModel.onEvent(UIEvent.PasswordChanged(it))
                    }
                )
                if (validationResults["password"] == false) {
                    Text(text = "Invalid password", color = Color.Red)
                }
            }
            item {
                CheckBoxComponent(
                    value = stringResource(id = R.string.Agreement),
                    onTextSelected = {
                        WellnessWiseAppRouter.navigateTo(Screen.TermsAndConditionsScreen)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
                ButtonComponent(
                    value = stringResource(id = R.string.Register),
                    onButtonClicked = {
                        loginViewModel.onEvent(UIEvent.RegisterButtonClicked)
                    },
                    isEnabled = Validator.isValidRegistrationUIState(registrationUIState)
                )
                DividerTextComponent(value = "OR")
            }
            item {
                ClickableLoginTextComponent(
                    tryingToLogin = true,
                    onTextSelected = {
                        WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
                    }
                )
            }
        }
    }
}