package com.project.wellnesswise.screens

import PredictionsViewModelFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.components.ui.LoadingAnimation
import com.project.wellnesswise.data.HealthDataProcessor
import com.project.wellnesswise.data.PredictionsViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)


@Composable

fun PredictionsScreen(viewModel: PredictionsViewModel = viewModel(factory = PredictionsViewModelFactory(LocalContext.current))) {    val predictions by viewModel.predictions.collectAsState()
    val modelInput by viewModel.modelInput.collectAsState()

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current
    // Use dynamic color scheme
    val colorScheme = when {
        useDarkIcons -> dynamicLightColorScheme(context)
        else -> dynamicDarkColorScheme(context)
    }
    LaunchedEffect(Unit) {
        viewModel.loadPredictions()
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
                    title = { Text("Health Risk Predictions") },
                    navigationIcon = {
                        IconButton(onClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { WellnessWiseAppRouter.navigateTo(Screen.PredictionHistoryScreen)  }) {
                            Icon(Icons.Default.History, "Prediction History")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
        when {
            viewModel.isLoading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingAnimation()

                }
            }
            viewModel.errorMessage != null -> {
                Text(
                    text = viewModel.errorMessage!!,
                    color = colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    modelInput?.let { input ->
                        items(HealthDataProcessor.inputLabels.zip(input)) { (label, value) ->
                            HealthDataItem(label, value.toString())
                        }
                    }
                    predictions?.let { preds ->
                        items(preds) { (category, risk, context) ->
                            PredictionCard(category, risk, context, viewModel)
                        }
                    }
                }
            }
        }
        }
    }
    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
    }
}

@Composable
fun HealthDataItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PredictionCard(category: String, risk: Float, context: String, viewModel: PredictionsViewModel) {
    val riskLevel = viewModel.classifyRisk(risk)
    val (icon, color) = getRiskIconAndColor(riskLevel)

    Card(

        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer
        )) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = color)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = riskLevel,
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = context, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun getRiskIconAndColor(riskLevel: String): Pair<ImageVector, Color> {
    return when (riskLevel) {
        "Stable" -> Icons.Default.CheckCircle to colorScheme.primary
        "Mild" -> Icons.Default.Info to colorScheme.secondary
        "Moderate" -> Icons.Default.Warning to colorScheme.tertiary
        "Severe" -> Icons.Default.Error to colorScheme.error
        "Critical" -> Icons.Default.Dangerous to colorScheme.error
        else -> Icons.AutoMirrored.Filled.Help to colorScheme.outline
    }
}