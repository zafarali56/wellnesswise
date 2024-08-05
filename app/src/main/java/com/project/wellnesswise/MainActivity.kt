package com.project.wellnesswise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.runtime.Composable

import androidx.compose.ui.tooling.preview.Preview
import com.project.wellnesswise.app.WellnessWiseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
        WellnessWiseApp()
        }
    }
}

@Preview
@Composable
fun DefaultPreview () {
    WellnessWiseApp()
}