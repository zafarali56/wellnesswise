package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
import com.project.wellnesswise.components.HabitSelection
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.MyPasswordField
import com.project.wellnesswise.components.MyTextField
import com.project.wellnesswise.data.LoginViewModel
import com.project.wellnesswise.data.UIEvent

import com.project.wellnesswise.navigations.Screen

import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun SignUpScreen(loginViewModel: LoginViewModel= viewModel ()) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

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
                MyTextField(labelValue = stringResource(id = R.string.Email), onTextSelected = {
                    loginViewModel.onEvent(UIEvent.EmailChanged(it))
                })
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.FullName), onTextSelected = {
                    loginViewModel.onEvent(UIEvent.FullNameChanged(it))
                })
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Age)

                    ,keyboardType = KeyboardType.Number, onTextSelected = {
                        loginViewModel.onEvent(UIEvent.AgeChanged(it.toInt()))
                    })
            }
            item {
                Spacer(modifier = Modifier.height(6.dp))
                GenderSelection(onGenderSelected = { gender ->
                    loginViewModel.onEvent(UIEvent.GenderChanged(gender))
                })

            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Height), keyboardType = KeyboardType.Number, onTextSelected = {
                    loginViewModel.onEvent(UIEvent.HeightChanged(it.toInt()))
                })
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Weight), keyboardType = KeyboardType.Number, onTextSelected = {
                    loginViewModel.onEvent(UIEvent.WeightChanged(it.toInt()))
                })
            }
            item {
                HabitSelection(onHabitsSelected = { habits ->
                    loginViewModel.onEvent(UIEvent.HabitsChanged(habits))
                })
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.MedicalHistory), onTextSelected = {
                    loginViewModel.onEvent(UIEvent.MedicalHistoryChanged(it))
                })
            }
            item {
                MyPasswordField(labelValue = stringResource(id = R.string.Password), onTextSelected = {
                    loginViewModel.onEvent(UIEvent.PasswordChanged(it))
                })
            }
            item {
                CheckBoxComponent(value = stringResource(id = R.string.Agreement), onTextSelected = {
                    WellnessWiseAppRouter.navigateTo(Screen.TermsAndConditionsScreen)
                })
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
                ButtonComponent(value = stringResource(id = R.string.Register))
                DividerTextComponent(value = "OR")
            }
            item {
                ClickableLoginTextComponent(tryingToLogin = true, onTextSelected = {
                    WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
                })
            }
        }
    }

}

@Preview
@Composable
fun DefaultPreviewOfSignUpScreen() {
    SignUpScreen()
}
