package com.project.wellnesswise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.project.wellnesswise.app.WellnessWiseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            WellnessWiseApp(
                registrationViewModel = viewModel(),
                loginViewModel = viewModel()
            )
        }
    }
}
