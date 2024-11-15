package com.project.wellnesswise.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.utils.RecommendationDataUploader
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationSetupScreen() {
    var isUploading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
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
            darkIcons = useDarkIcons)

    }
    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            "Setup Recommendations",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = colorScheme.surface,
                        titleContentColor = colorScheme.onSurface,
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item{
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "Recommendation System",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Text(
                            "Setup the health recommendation system. This includes recommendations for:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant
                        )

                        // List of conditions
                        Column(
                            modifier = Modifier.padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text("Diabetes Management") },
                                leadingContent = {
                                    Icon(Icons.Default.Bloodtype, contentDescription = null)
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Cardiovascular Health") },
                                leadingContent = {
                                    Icon(Icons.Default.Favorite, contentDescription = null)
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Blood Pressure") },
                                leadingContent = {
                                    Icon(Icons.Default.MonitorHeart, contentDescription = null)
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Weight Management") },
                                leadingContent = {
                                    Icon(Icons.Default.MonitorWeight, contentDescription = null)
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Cancer Risk Assessment") },
                                leadingContent = {
                                    Icon(Icons.Default.Biotech, contentDescription = null)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    isUploading = true
                                    try {
                                        RecommendationDataUploader.uploadAllRecommendations(
                                            FirebaseFirestore.getInstance()
                                        )
                                        showSuccessMessage = true
                                        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Unknown error occurred"
                                        showErrorMessage = true
                                    } finally {
                                        isUploading = false
                                    }
                                }
                            },
                            enabled = !isUploading,
                            modifier = Modifier.fillMaxWidth()

                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = colorScheme.onPrimary
                                )
                            } else {
                                Text("Initialize System")
                            }
                        }
                    }
                }
            }
            }

            if (showSuccessMessage) {
                AlertDialog(
                    onDismissRequest = { showSuccessMessage = false },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    title = { Text("Success", textAlign = TextAlign.Center) },
                    text = {
                        Text(
                            "Recommendations setup completed successfully!",
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showSuccessMessage = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showErrorMessage) {
                AlertDialog(
                    onDismissRequest = { showErrorMessage = false },
                    icon = { Icon(Icons.Default.Error, contentDescription = null) },
                    title = { Text("Error", textAlign = TextAlign.Center) },
                    text = {
                        Text(
                            errorMessage,
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showErrorMessage = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}


@Composable
@Preview
fun RecommendationSetupScreenPreview() {
    RecommendationSetupScreen()
}