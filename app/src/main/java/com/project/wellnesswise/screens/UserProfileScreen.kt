package com.project.wellnesswise.screens

import HealthDataViewModel
import LoginViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.HeadingTextComponent
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.components.ui.UserImage
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable

fun UserProfileScreen(authViewModel: AuthViewModel) {
    val user = FirebaseAuth.getInstance().currentUser
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
                    item {
                       UserImage()
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = user?.displayName ?: "User Name",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .padding(20.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row{
                               Text(text = "Email: ", fontWeight = FontWeight.W500,fontSize = 16.sp)
                                Text(
                                    text = user?.email ?: "User Email",
                                    fontWeight = FontWeight.W500,
                                    fontSize = 16.sp
                                )
                            }

                        }
                    }
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
    UserProfileScreen(AuthViewModel(RegistrationViewModel(), LoginViewModel(), HealthDataViewModel()))
}