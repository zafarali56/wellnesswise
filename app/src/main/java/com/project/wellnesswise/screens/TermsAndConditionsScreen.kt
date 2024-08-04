package com.project.wellnesswise.screens

import android.hardware.camera2.params.ColorSpaceProfiles
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.R
import com.project.wellnesswise.components.HeadingTextComponent
import com.project.wellnesswise.components.NormalTextComponent


@Composable
fun TermsAndConditionsScreen ()
{
    Surface (modifier = Modifier.fillMaxSize().background(color = Color.White).padding(16.dp)) {
    HeadingTextComponent(value = stringResource(id = R.string.TermsAndConditions))
    }
}


@Preview
@Composable
fun TermsAndConditionsPreview (){
    TermsAndConditionsScreen()
}