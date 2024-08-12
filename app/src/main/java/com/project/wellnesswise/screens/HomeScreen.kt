import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.components.NavigationDrawer
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.HomeViewModel
import com.project.wellnesswise.data.LoginViewModel
import com.project.wellnesswise.data.RegistrationViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    registrationViewModel: RegistrationViewModel,
    loginViewModel: LoginViewModel
) {
    NavigationDrawer(
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(28.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding() // Add this modifier to handle keyboard padding
                ) {
                    item {
                        ButtonComponent(
                            value = stringResource(id = R.string.Logout),
                            isEnabled = true,
                            onButtonClicked = {
                                authViewModel.logOut(registrationViewModel, loginViewModel)
                            }
                        )
                    }
                }
            }
        },
        onLogoutClick = { authViewModel.logOut(registrationViewModel, loginViewModel) }
    )
}

@Composable
@Preview
fun HomeScreenPreview() {
    HomeScreen(HomeViewModel(), AuthViewModel(), RegistrationViewModel(), LoginViewModel())
}
