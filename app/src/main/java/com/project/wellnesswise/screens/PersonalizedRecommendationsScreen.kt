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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
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
                    title = { Text("Recommendations") },
                    navigationIcon = {
                        IconButton(onClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
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
enum class RiskLevel {
    STABLE, MILD, MODERATE, SEVERE, CRITICAL
}

@Composable
fun RecommendationCard(recommendation: String) {
    val (icon, category, description, riskLevel) = getDetailedCategoryInfo(recommendation)
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(riskLevel) {
                RiskLevel.CRITICAL -> colorScheme.errorContainer
                RiskLevel.SEVERE -> colorScheme.errorContainer.copy(alpha = 0.7f)
                RiskLevel.MODERATE -> colorScheme.tertiaryContainer
                RiskLevel.MILD -> colorScheme.secondaryContainer
                RiskLevel.STABLE -> colorScheme.surfaceVariant
            },
            contentColor = when(riskLevel) {
                RiskLevel.CRITICAL -> colorScheme.onErrorContainer
                RiskLevel.SEVERE -> colorScheme.onErrorContainer
                RiskLevel.MODERATE -> colorScheme.onTertiaryContainer
                RiskLevel.MILD -> colorScheme.onSecondaryContainer
                RiskLevel.STABLE -> colorScheme.onSurfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = when(riskLevel) {
                            RiskLevel.CRITICAL -> colorScheme.error
                            RiskLevel.SEVERE -> colorScheme.error.copy(alpha = 0.7f)
                            RiskLevel.MODERATE -> colorScheme.tertiary
                            RiskLevel.MILD -> colorScheme.secondary
                            RiskLevel.STABLE -> colorScheme.primary
                        },
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalContentColor.current.copy(alpha = 0.7f)
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = when(riskLevel) {
                            RiskLevel.CRITICAL -> colorScheme.error.copy(alpha = 0.1f)
                            RiskLevel.SEVERE -> colorScheme.error.copy(alpha = 0.08f)
                            RiskLevel.MODERATE -> colorScheme.tertiary.copy(alpha = 0.1f)
                            RiskLevel.MILD -> colorScheme.secondary.copy(alpha = 0.1f)
                            RiskLevel.STABLE -> colorScheme.primary.copy(alpha = 0.1f)
                        },
                        contentColor = when(riskLevel) {
                            RiskLevel.CRITICAL -> colorScheme.error
                            RiskLevel.SEVERE -> colorScheme.error.copy(alpha = 0.7f)
                            RiskLevel.MODERATE -> colorScheme.tertiary
                            RiskLevel.MILD -> colorScheme.secondary
                            RiskLevel.STABLE -> colorScheme.primary
                        }
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = riskLevel.name.capitalize(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = LocalContentColor.current.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
@Composable
fun getDetailedCategoryInfo(recommendation: String): Quadruple<ImageVector, String, String, RiskLevel> {
    val riskLevel = when {
        recommendation.contains("critical", ignoreCase = true) -> RiskLevel.CRITICAL
        recommendation.contains("severe", ignoreCase = true) -> RiskLevel.SEVERE
        recommendation.contains("moderate", ignoreCase = true) -> RiskLevel.MODERATE
        recommendation.contains("mild", ignoreCase = true) -> RiskLevel.MILD
        else -> RiskLevel.STABLE
    }

    // For combined health alerts
    if (recommendation.contains("Important health alert", ignoreCase = true) ||
        recommendation.contains("Great job!", ignoreCase = true)) {
        return Quadruple(
            Icons.Default.HealthAndSafety,
            "Health Summary",
            if (recommendation.contains("Great job!", ignoreCase = true))
                "Overall health status and achievements"
            else
                "Multiple health concerns requiring attention",
            riskLevel
        )
    }

    return when {
        recommendation.contains("diabetes", ignoreCase = true) ->
            Quadruple(
                Icons.Default.Bloodtype,
                "Diabetes Risk",
                if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.SEVERE)
                    "Urgent: Blood sugar management required"
                else
                    "Blood sugar management and lifestyle factors",
                riskLevel
            )
        recommendation.contains("cardiovascular", ignoreCase = true) ->
            Quadruple(
                Icons.Default.Favorite,
                "Heart Health",
                if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.SEVERE)
                    "Urgent: Cardiovascular attention needed"
                else
                    "Cardiovascular disease risk and prevention",
                riskLevel
            )
        recommendation.contains("hypertension", ignoreCase = true) ->
            Quadruple(
                Icons.Default.Speed,
                "Blood Pressure",
                if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.SEVERE)
                    "Urgent: Blood pressure management needed"
                else
                    "Hypertension risk and management",
                riskLevel
            )
        recommendation.contains("obesity", ignoreCase = true) ->
            Quadruple(
                Icons.Default.MonitorWeight,
                "Weight Management",
                if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.SEVERE)
                    "Urgent: Weight management attention needed"
                else
                    "BMI and healthy weight strategies",
                riskLevel
            )
        recommendation.contains("cancer", ignoreCase = true) ->
            Quadruple(
                Icons.Default.Biotech,
                "Cancer Risk",
                if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.SEVERE)
                    "Urgent: Cancer risk assessment needed"
                else
                    "Cancer prevention and screening",
                riskLevel
            )
        else ->
            Quadruple(
                Icons.Default.HealthAndSafety,
                "Health Alert",
                "Important health information",
                riskLevel
            )
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

fun String.capitalize() = this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    var currentRotation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(16)
            currentRotation = (currentRotation + 5) % 360
        }
    }

    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Loading",
        modifier = modifier
            .size(64.dp)
            .graphicsLayer(rotationZ = currentRotation),
        tint = colorScheme.primary
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
            tint = colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Unable to load recommendations.",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.error
        )
    }
}