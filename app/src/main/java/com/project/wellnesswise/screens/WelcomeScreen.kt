package com.project.wellnesswise.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.wellnesswise.components.ui.ActionButton
import com.project.wellnesswise.ui.theme.WellnessWiseTheme
import kotlinx.coroutines.delay // Add this import

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit // Callback for button click to navigate to the next screen
) {
    // Animation states for the welcome text and icons
    var iconVisible by remember { mutableStateOf(false) }
    var welcomeTextVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    // Trigger the animations with delays
    LaunchedEffect(Unit) {
        iconVisible = true
        delay(200) // Delay for the welcome text
        welcomeTextVisible = true
        delay(200) // Delay for the subtitle
        subtitleVisible = true
        delay(200) // Delay for the button
        buttonVisible = true
    }
    WellnessWiseTheme {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Animated medical-themed icons
                AnimatedVisibility(
                    visible = iconVisible,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = "Medical Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Animated welcome text
                AnimatedVisibility(
                    visible = welcomeTextVisible,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    Text(
                        text = "Welcome to WellnessWise!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated subtitle text
                AnimatedVisibility(
                    visible = subtitleVisible,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    Text(
                        text = "Please answer the following questions to create your health profile.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Animated start button
                AnimatedVisibility(
                    visible = buttonVisible,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    ActionButton(
                        text = "Start",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onStartClick,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}