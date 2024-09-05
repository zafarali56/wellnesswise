package com.project.wellnesswise.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.HabitSelection
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.data.UIEvent
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(registrationViewModel: RegistrationViewModel) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current

    // Use dynamic color scheme
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
    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.Habits)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(

                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = colorScheme.background
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = colorScheme.background
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp)
                ) {
                    item {
                        HabitSelection(
                            registrationViewModel = registrationViewModel,
                            onHabitsSelected = { habits ->
                                registrationViewModel.onEvent(UIEvent.HabitsChanged(habits))
                            }
                        )
                    }
                }
            }
        }
        SystemBackButtonHandler {
            WellnessWiseAppRouter.navigateTo(Screen.SignUpScreen)
        }
    }
}
@Composable
@Preview
fun HabitsScreenPreview() {
    HabitsScreen(viewModel())
}