import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.ClickableLoginTextComponent
import com.project.wellnesswise.components.ui.GoogleFitPermissionRequest
import com.project.wellnesswise.components.ui.HeadingTextComponent
import com.project.wellnesswise.components.ui.MyPasswordField
import com.project.wellnesswise.components.ui.MyTextField
import com.project.wellnesswise.components.ui.UnderLinedTextComponent
import com.project.wellnesswise.data.LoginUIEvent
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun LoginScreen(loginViewModel: LoginViewModel) {
    val loginUIState = loginViewModel.loginUIState.value
    val errorMessage = loginViewModel.errorMessage.value
    val context = LocalContext.current
    val validationResults = loginViewModel.validationResults.value

    LaunchedEffect(Unit) {
        loginViewModel.resetLoginUIState()
    }

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
                    .imePadding()
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.img),
                            contentDescription = "Logo",
                            modifier = Modifier.size(250.dp)
                        )
                    }
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
                        },
                        isError = validationResults["email"] == false
                    )
                    MyPasswordField(
                        labelValue = stringResource(id = R.string.Password),
                        initialValue = loginUIState.password,
                        onTextSelected = {
                            loginViewModel.onEvent(LoginUIEvent.PasswordChangedLogin(it))
                        },
                        isError = validationResults["password"] == false
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    ButtonComponent(
                        value = stringResource(id = R.string.Login),
                        onButtonClicked = {
                            loginViewModel.onEvent(LoginUIEvent.LoginButtonClicked)
                        },
                        isEnabled = Validator.isValidLoginUIState(loginUIState)
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    UnderLinedTextComponent(value = stringResource(id = R.string.Forgot_password))
                    ClickableLoginTextComponent(tryingToLogin = false, onTextSelected = {
                        WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
                    })
                    if (errorMessage != null) {
                        val context = LocalContext.current
                        LaunchedEffect(errorMessage) {
                            Toast.makeText(context, "Invalid credentials! Please try again", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        if (loginViewModel.logInProgress.value) {
            CircularProgressIndicator(
                color = colorResource(id = R.color.primary)
            )
        }

        if (loginViewModel.needsGoogleFitPermissions.value) {
            GoogleFitPermissionRequest(
                onPermissionResult = { permissionGranted ->
                    loginViewModel.onGoogleFitPermissionResult(permissionGranted)
                },
                onDismissRequest = {
                    loginViewModel.onGoogleFitPermissionDismissed()
                }
            )
        }
    }

    LaunchedEffect(loginViewModel.isLoggedIn.value) {
        if (loginViewModel.isLoggedIn.value) {
            loginViewModel.checkGoogleFitPermissions(context)
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(LoginViewModel())
}