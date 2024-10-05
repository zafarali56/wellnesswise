package com.project.wellnesswise.screens

import PersonalizedRecommendationsViewModel
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizedRecommendationsScreen(
    viewModel: PersonalizedRecommendationsViewModel
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current

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

    val recommendations by viewModel.recommendations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Your Health Insights") },
                    navigationIcon = {
                        IconButton(onClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.primaryContainer,
                        titleContentColor = colorScheme.onPrimaryContainer,
                        navigationIconContentColor = colorScheme.onPrimaryContainer
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
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isLoading),
                    onRefresh = { viewModel.refreshRecommendations() }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            isLoading -> LoadingAnimation(modifier = Modifier.align(Alignment.Center))
                            recommendations.isEmpty() -> ErrorState(modifier = Modifier.align(Alignment.Center))
                            else -> RecommendationsList(recommendations = recommendations)
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
fun RecommendationsList(recommendations: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recommendations) { recommendation ->
            RecommendationCard(recommendation = recommendation)
        }
    }
}


@Composable
fun RecommendationCard(recommendation: String) {
    val (icon, category, description) = getDetailedCategoryInfo(recommendation)
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun getDetailedCategoryInfo(recommendation: String): Triple<ImageVector, String, String> {
    return when {
        recommendation.contains("diabetes", ignoreCase = true) ->
            Triple(Icons.Default.Bloodtype, "Diabetes Risk", "Blood sugar management and lifestyle factors")
        recommendation.contains("cardiovascular", ignoreCase = true) ->
            Triple(Icons.Default.Favorite, "Heart Health", "Cardiovascular disease risk and prevention")
        recommendation.contains("hypertension", ignoreCase = true) ->
            Triple(Icons.Default.Speed, "Blood Pressure", "Hypertension risk and management")
        recommendation.contains("obesity", ignoreCase = true) ->
            Triple(Icons.Default.MonitorWeight, "Weight Management", "BMI and healthy weight strategies")
        recommendation.contains("cancer", ignoreCase = true) ->
            Triple(Icons.Default.Biotech, "Cancer Prevention", "Risk factors and screening recommendations")
        recommendation.contains("diet", ignoreCase = true) ->
            Triple(Icons.Default.Restaurant, "Nutrition", "Dietary habits and nutritional advice")
        recommendation.contains("exercise", ignoreCase = true) ->
            Triple(Icons.Default.FitnessCenter, "Physical Activity", "Exercise routines and benefits")
        recommendation.contains("sleep", ignoreCase = true) ->
            Triple(Icons.Default.Bedtime, "Sleep Health", "Sleep patterns and quality improvement")
        recommendation.contains("stress", ignoreCase = true) ->
            Triple(Icons.Default.SelfImprovement, "Mental Wellbeing", "Stress management and mental health")
        recommendation.contains("smoking", ignoreCase = true) ->
            Triple(Icons.Default.SmokeFree, "Smoking Cessation", "Tobacco use and quitting strategies")
        recommendation.contains("alcohol", ignoreCase = true) ->
            Triple(Icons.Default.LocalBar, "Alcohol Consumption", "Drinking habits and moderation advice")
        recommendation.contains("check", ignoreCase = true) ->
            Triple(Icons.Default.HealthAndSafety, "Health Check-ups", "Regular screenings and preventive care")
        recommendation.contains("environmental", ignoreCase = true) ->
            Triple(Icons.Default.Eco, "Environmental Health", "Air quality and pollution exposure")
        else -> Triple(Icons.Default.Lightbulb, "General Health", "Overall wellness and lifestyle tips")
    }
}


@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    var currentRotation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(16) // approximately 60 FPS
            currentRotation = (currentRotation + 5) % 360
        }
    }

    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Loading",
        modifier = modifier
            .size(64.dp)
            .graphicsLayer(rotationZ = currentRotation),
        tint = MaterialTheme.colorScheme.primary
    )
}
@Composable
fun ErrorState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Unable to load recommendations.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}