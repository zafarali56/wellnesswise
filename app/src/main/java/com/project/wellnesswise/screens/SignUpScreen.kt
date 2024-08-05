package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ButtonComponent
import com.project.wellnesswise.components.CheckBoxComponent
import com.project.wellnesswise.components.ClickableLoginTextComponent
import com.project.wellnesswise.components.DividerTextComponent
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.MyPasswordField
import com.project.wellnesswise.components.MyTextField

import com.project.wellnesswise.navigations.Screen

import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun SignUpScreen() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {

                HeadingTextComponent(value = stringResource(id = R.string.Create_an_account))
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Email))
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.FullName))
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Age))
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Gender))
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Height))
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Weight))
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.Habits))
            }
            item {
                MyTextField(labelValue = stringResource(id = R.string.MedicalHistory))
            }
            item {
                MyPasswordField(labelValue = stringResource(id = R.string.Password))
            }
            item {
                CheckBoxComponent(value = stringResource(id = R.string.Agreement), onTextSelected = {
                    WellnessWiseAppRouter.navigateTo(Screen.TermsAndConditionsScreen)
                })
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
                ButtonComponent(value = stringResource(id = R.string.Register))
                DividerTextComponent(value = "OR")
            }
            item {
                ClickableLoginTextComponent(tryingToLogin = true, onTextSelected = {
                    WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
                })
            }
        }
    }

}

@Preview
@Composable
fun DefaultPreviewOfSignUpScreen() {
    SignUpScreen()
}
