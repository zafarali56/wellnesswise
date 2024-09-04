import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.ClickableLoginTextComponent
import com.project.wellnesswise.components.ui.DividerTextComponent
import com.project.wellnesswise.components.ui.GoogleFitPermissionRequest
import com.project.wellnesswise.components.ui.LoadingAnimation
import com.project.wellnesswise.components.ui.MyPasswordField
import com.project.wellnesswise.components.ui.MyTextField
import com.project.wellnesswise.components.ui.UnderLinedTextComponent
import com.project.wellnesswise.data.LoginUIEvent
import com.project.wellnesswise.data.rules.Validator
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun LoginScreen(loginViewModel: LoginViewModel) {
    val loginUIState = loginViewModel.loginUIState.value
    val errorMessage = loginViewModel.errorMessage.value
    val context = LocalContext.current
    val validationResults = loginViewModel.validationResults.value

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

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

    LaunchedEffect(Unit) {
        loginViewModel.resetLoginUIState()
    }

    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(horizontal = 26.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (isSystemInDarkTheme()) {
                                        R.drawable.blacktheme
                                    } else {
                                        R.drawable.whitetheme
                                    }
                                ),
                                contentDescription = "Logo",
                                modifier = Modifier.size(width = 400.dp, height = 300.dp)
                            )
                        }
                        Text(text = "Login to your account",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                            )
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
                        Spacer(modifier = Modifier.height(10.dp))
                        if (loginViewModel.logInProgress.value) {

                            Box (modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){LoadingAnimation()}
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        UnderLinedTextComponent(value = stringResource(id = R.string.Forgot_password))
                        DividerTextComponent(value = "OR")

                        if (errorMessage != null) {
                            val context = LocalContext.current
                            LaunchedEffect(errorMessage) {
                                Toast.makeText(
                                    context,
                                    "ERROR:${errorMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    item() {
                        Spacer(modifier = Modifier.height(10.dp))
                        ClickableLoginTextComponent(tryingToLogin = false, onTextSelected = {
                            WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
                        })
                    }

                }
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
}
@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(LoginViewModel())
}


