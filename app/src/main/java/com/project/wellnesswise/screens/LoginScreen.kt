package com.project.wellnesswise.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.components.ClickableLoginTextComponent
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.MyPasswordField
import com.project.wellnesswise.components.MyTextField
import com.project.wellnesswise.components.UnderLinedTextComponent
import com.project.wellnesswise.data.LoginUIEvent
import com.project.wellnesswise.data.LoginViewModel
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun LoginScreen(loginViewModel: LoginViewModel) {

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    val loginUIState = loginViewModel.loginUIState.value
    val errorMessage = loginViewModel.errorMessage.value

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(28.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .imePadding() // Add this modifier to handle keyboard padding
            ) {
                item {
                    Image(
                        painter = painterResource(id = R.drawable.img),
                        contentDescription = "Logo",
                        modifier = Modifier.size(310.dp)
                    )
                    HeadingTextComponent(value = stringResource(id = R.string.Login))
                    Spacer(modifier = Modifier.height(30.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = colorResource(id = R.color.gray_100)
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    MyTextField(
                        labelValue = stringResource(id = R.string.Email),
                        initialValue = loginUIState.email,
                        onTextSelected = {
                            loginViewModel.onEvent(LoginUIEvent.EmailChangedLogin(it))
                        }
                    )
                    MyPasswordField(
                        labelValue = stringResource(id = R.string.Password),
                        initialValue = loginUIState.password,
                        onTextSelected = {
                            loginViewModel.onEvent(LoginUIEvent.PasswordChangedLogin(it))
                        }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    ButtonComponent(
                        value = stringResource(id = R.string.Login),
                        onButtonClicked = {
                            loginViewModel.onEvent(LoginUIEvent.LoginButtonClicked)
                            // Handle login logic here
                        },
                        isEnabled = Validator.isValidLoginUIState(loginUIState)
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    UnderLinedTextComponent(value = stringResource(id = R.string.Forgot_password))
                    ClickableLoginTextComponent(tryingToLogin = false, onTextSelected = {
                        WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
                    })
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }

        if (loginViewModel.logInProgress.value) {
            CircularProgressIndicator(
                color = colorResource(id = R.color.primary)
            )
        }
    }

    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreen(viewModel())
}
