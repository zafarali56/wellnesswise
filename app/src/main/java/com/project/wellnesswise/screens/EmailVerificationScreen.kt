package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen() {
    var resendEmail by remember { mutableStateOf(false) }
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Check every 5 seconds
            user?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (user.isEmailVerified) {
                        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
                    }
                } else {
                    // Handle error
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(28.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Verification Email Sent", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Please check your email to verify your account.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(20.dp))
                ButtonComponent(
                    value = "Resend Verification Email", isEnabled = true,
                    onButtonClicked = {
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    resendEmail = true
                                } else {
                                    // Handle error
                                }
                            }
                    }
                )
                if (resendEmail) {
                    Text(text = "Verification email resent", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
@Preview
fun EmailVerificationScreenPreview() {
    EmailVerificationScreen()
}
