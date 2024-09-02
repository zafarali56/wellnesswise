package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.CheckBoxComponent
import com.project.wellnesswise.components.ui.ClickableLoginTextComponent
import com.project.wellnesswise.components.ui.DividerTextComponent
import com.project.wellnesswise.components.ui.GenderSelection
import com.project.wellnesswise.components.ui.HabbitAndMedHistoryButton
import com.project.wellnesswise.components.ui.HeadingTextComponent
import com.project.wellnesswise.components.ui.MyNumberField
import com.project.wellnesswise.components.ui.MyPasswordField
import com.project.wellnesswise.components.ui.MyTextField
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.data.UIEvent
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter


@Composable
fun SignUpScreen(registrationViewModel: RegistrationViewModel) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    // Use dynamic color scheme
    val colorScheme = when {
        useDarkIcons -> dynamicLightColorScheme(LocalContext.current)
        else -> dynamicDarkColorScheme(LocalContext.current)
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = useDarkIcons
        )
    }

    val validationResults = registrationViewModel.validationResults.value
    val registrationUIState = registrationViewModel.registrationUIState.value

    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()

                ) {
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        HeadingTextComponent(value = stringResource(id = R.string.Create_an_account))
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                    item {

                        Text(
                            text = "Enter a valid email address",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MyTextField(
                            labelValue = stringResource(id = R.string.Email),
                            initialValue = registrationUIState.email,
                            onTextSelected = {
                                registrationViewModel.onEvent(UIEvent.EmailChanged(it))
                            },
                            isError = validationResults["email"] == false
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (registrationViewModel.emailAlreadyInUse.value) {
                            Text(text = "Email already in use", color = Color.Red)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Provide your full name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MyTextField(
                            labelValue = stringResource(id = R.string.FullName),
                            initialValue = registrationUIState.fullName,
                            onTextSelected = {
                                registrationViewModel.onEvent(UIEvent.FullNameChanged(it))
                            },
                            isError = validationResults["fullName"] == false
                        )

                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Enter an age between 1-120",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MyNumberField(
                            labelValue = stringResource(id = R.string.Age),
                            initialValue = registrationUIState.age.toString(),
                            onTextSelected = {
                                registrationViewModel.onEvent(UIEvent.AgeChanged(it ?: 0))
                            },
                            isError = validationResults["age"] == false
                        )

                    }
                    item {

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Select your gender",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        GenderSelection(
                            initialGender = registrationUIState.gender,
                            onGenderSelected = { gender ->
                                registrationViewModel.onEvent(UIEvent.GenderChanged(gender))
                            }
                        )

                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))


                        Text(
                            text = "Enter a height between 50-300 cm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MyNumberField(
                            labelValue = stringResource(id = R.string.Height),
                            initialValue = registrationUIState.height.toString(),
                            onTextSelected = {
                                registrationViewModel.onEvent(UIEvent.HeightChanged(it ?: 0))
                            },
                            isError = validationResults["height"] == false
                        )

                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Enter a weight between 30-500 kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MyNumberField(
                            labelValue = stringResource(id = R.string.Weight),
                            initialValue = registrationUIState.weight.toString(),
                            onTextSelected = {
                                registrationViewModel.onEvent(UIEvent.WeightChanged(it ?: 0))
                            }, isError = validationResults["weight"] == false

                        )


                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Select at least one habit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HabbitAndMedHistoryButton(
                            text = stringResource(id = R.string.Habits),
                            onClick = { WellnessWiseAppRouter.navigateTo(Screen.HabitsScreen) }
                        )


                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Please provide medical history",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
                        Spacer(modifier = Modifier.height(10.dp))


                        MyPasswordField(
                            labelValue = stringResource(id = R.string.Password),
                            initialValue = registrationUIState.password,
                            onTextSelected = {
                                registrationViewModel.onEvent(UIEvent.PasswordChanged(it))
                            },
                            isError = validationResults["password"] == false
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create a strong password (8+ chars, uppercase, lowercase, number, special char)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        CheckBoxComponent(
                            checked = registrationUIState.isPolicyAccepted,
                            onCheckedChange = {
                                registrationViewModel.onEvent(UIEvent.PolicyAcceptedChanged(it))
                            },
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
                                registrationViewModel.onEvent(UIEvent.RegisterButtonClicked)
                            },
                            isEnabled = Validator.isValidRegistrationUIState(registrationViewModel.registrationUIState.value)
                        )
                        DividerTextComponent(value = "OR")
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        ClickableLoginTextComponent(
                            tryingToLogin = true,
                            onTextSelected = {
                                WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
                            }
                        )
                    }
                }
            }
            if (registrationViewModel.signUpInProgress.value) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.primary)
                )
            }

        }


    }
}
@Composable
@Preview
fun SignUpScreenPreview() {
    SignUpScreen(viewModel())
}
