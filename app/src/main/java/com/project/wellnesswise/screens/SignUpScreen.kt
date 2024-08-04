package com.project.wellnesswise.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.R
import com.project.wellnesswise.components.CheckBoxComponent
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.MyPasswordField
import com.project.wellnesswise.components.MyTextField
import com.project.wellnesswise.components.NormalTextComponent
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter


@Composable
fun SignUpScreen (){

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp)

    ) {
 Column (modifier = Modifier.fillMaxSize())
 {
     NormalTextComponent(value = stringResource(id = R.string.hello))
     HeadingTextComponent(value = stringResource(id = R.string.Register))
     Spacer(modifier = Modifier.height(20.dp))
     MyTextField(labelValue = stringResource(id = R.string.Email))
     MyTextField(labelValue = stringResource(id = R.string.FullName))
     MyTextField(labelValue = stringResource(id = R.string.Age))
     MyTextField(labelValue = stringResource(id = R.string.Gender))
     MyTextField(labelValue = stringResource(id = R.string.Height))
     MyTextField(labelValue = stringResource(id = R.string.Weight))
     MyTextField(labelValue = stringResource(id = R.string.Habits))
     MyTextField(labelValue = stringResource(id = R.string.MedicalHistory))
     MyPasswordField(labelValue = stringResource(id = R.string.Password))
     CheckBoxComponent(value = stringResource(id = R.string.Agreement), onTextSelected = {
         WellnessWiseAppRouter.navigateTo(Screen.TermsAndConditionsScreen)
     })
 }
    }
}


@Preview
@Composable
fun DefaultPreviewOfSignUpScreen() {
    SignUpScreen()
}