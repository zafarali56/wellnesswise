package com.project.wellnesswise.screens

import PredictionsViewModelFactory
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.data.HealthDataProcessor
import com.project.wellnesswise.data.PredictionsViewModel
import com.project.wellnesswise.ml.TFLiteInterpreter
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun PredictionsScreen(viewModel: PredictionsViewModel = viewModel(factory = PredictionsViewModelFactory(LocalContext.current))) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current

    val colorScheme = if (useDarkIcons) {
        dynamicLightColorScheme(context)
    } else {
        dynamicDarkColorScheme(context)
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = useDarkIcons
        )
    }

    // Load predictions when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.loadPredictions()
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Health Risk Predictions") },
                    navigationIcon = {
                        IconButton(onClick = {
                            WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
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
                when {
                    viewModel.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                        }
                    }
                    viewModel.errorMessage != null -> {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            item {
                                Text(
                                    text = "Your Health Data (Model Input):",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                            items(HealthDataProcessor.inputLabels.zip(viewModel.modelInput ?: emptyList())) { (label, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = value.toString(), style = MaterialTheme.typography.bodyMedium)
                                    Log.d("PredictionsScreen", "Label: $label, Value: $value")
                                }
                            }

                            viewModel.predictions?.let { preds ->
                                items(preds) { (category, risk, context) ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = category, style = MaterialTheme.typography.bodyLarge)
                                            val riskLevel = viewModel.classifyRisk(risk)
                                            Text(
                                                text = "$riskLevel",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = when (riskLevel) {
                                                    "Stable" -> MaterialTheme.colorScheme.primary
                                                    "Mild" -> MaterialTheme.colorScheme.secondary
                                                    "Moderate" -> MaterialTheme.colorScheme.tertiary
                                                    "Severe", "Critical" -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                        Text(
                                            text = context,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
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