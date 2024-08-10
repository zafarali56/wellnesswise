package com.project.wellnesswise.screens

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
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.data.LoginViewModel


@Composable
fun HomeScreen(loginViewModel: LoginViewModel) {

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
                HeadingTextComponent(value = stringResource(id = R.string.Home))

                ButtonComponent(value = stringResource(id = R.string.Logout), isEnabled = true, onButtonClicked = {
                    loginViewModel.logOut()
                })
            }
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    HomeScreen(LoginViewModel ())
}
