package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.wellnesswise.R
import com.project.wellnesswise.components.HabitSelection
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.data.LoginViewModel
import com.project.wellnesswise.data.UIEvent
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter


@Composable
fun HabitsScreen (loginViewModel: LoginViewModel = viewModel ()){
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
               HeadingTextComponent(value = stringResource(id = R.string.Habits))
               Spacer(modifier = Modifier.padding(10.dp))
               HabitSelection(onHabitsSelected = { habits ->
                   loginViewModel.onEvent(UIEvent.HabitsChanged(habits))
               })
           }
        }
    }
    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
    }
}

@Composable
@Preview
fun HabitsScreenPreview() {
    HabitsScreen()
}
