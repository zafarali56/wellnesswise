package com.project.wellnesswise.screens

import LoginViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.NavigationDrawer
import com.project.wellnesswise.components.UserImage
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable

fun UserProfileScreen(authViewModel: AuthViewModel) {
    NavigationDrawer(

        content = {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .background(color = Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { HeadingTextComponent(value = "User Profile") }
                    item { UserImage() }
                }
            }

        },
        onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) },
        onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) },
        onLogoutClick =  {authViewModel.logOut()}

    )

    SystemBackButtonHandler {
       WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
    }
}


@Composable
@Preview
fun UserProfilePreview ()
{
    UserProfileScreen(AuthViewModel(RegistrationViewModel(), LoginViewModel()))
}