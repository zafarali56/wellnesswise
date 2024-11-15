package com.project.wellnesswise.components.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

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
                RiskLevel.SEVERE -> colorScheme.errorContainer
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
                            RiskLevel.CRITICAL -> colorScheme.error.copy(alpha = 0.15f)
                            RiskLevel.SEVERE -> colorScheme.error.copy(alpha = 0.15f)
                            RiskLevel.MODERATE -> colorScheme.tertiary.copy(alpha = 0.1f)
                            RiskLevel.MILD -> colorScheme.secondary.copy(alpha = 0.1f)
                            RiskLevel.STABLE -> colorScheme.primary.copy(alpha = 0.1f)
                        },
                        contentColor = when(riskLevel) {
                            RiskLevel.CRITICAL -> colorScheme.error
                            RiskLevel.SEVERE -> colorScheme.error
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
                    HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.1f))
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