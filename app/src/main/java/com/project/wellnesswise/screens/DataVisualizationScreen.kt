package com.project.wellnesswise.screens

import androidx.compose.foundation.Image
import com.project.wellnesswise.viewModels.DataVisualizationViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.R
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataVisualizationScreen(dataVisualizationViewModel: DataVisualizationViewModel = viewModel()) {
    val context = LocalContext.current
    val useDarkIcons = !isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()
    val colorScheme = if (useDarkIcons) {
        dynamicLightColorScheme(context)
    } else {
        dynamicDarkColorScheme(context)
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.surface,
            darkIcons = useDarkIcons
        )
    }

    val chartBackgroundColor = if (useDarkIcons) colorScheme.surface.toArgb() else colorScheme.background.toArgb()
    val chartTextColor = if (useDarkIcons) colorScheme.onSurface.toArgb() else colorScheme.onBackground.toArgb()

    val diseaseRiskData by dataVisualizationViewModel.diseaseRiskData.collectAsState()
    val isLoading by dataVisualizationViewModel.isLoading.collectAsState()
    val error by dataVisualizationViewModel.error.collectAsState()
    val overallHealthScore by dataVisualizationViewModel.overallHealthScore.collectAsState()

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Data Visualization", color = colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = { WellnessWiseAppRouter.navigateTo(Screen.PredictionsScreen) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.surface,
                        titleContentColor = colorScheme.onSurface,
                        navigationIconContentColor = colorScheme.onSurface
                    )
                )
            },
            containerColor = colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    error != null -> {
                        Text(
                            text = error ?: "An unknown error occurred",
                            color = colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    diseaseRiskData.isEmpty() -> {
                        Text(
                            text = "No health viewModels available",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            item {
                                OverallHealthScoreCard(overallHealthScore, colorScheme)
                            }


                            diseaseRiskData.forEach { (disease, _) ->
                                item {
                                    ChartBox(
                                        title = "$disease Risk Trend",

                                        content = {
                                            dataVisualizationViewModel.getLineData(disease, colorScheme.primary.toArgb())?.let { lineData ->
                                                LineChartComponent(
                                                    data = lineData,
                                                    textColor = chartTextColor,
                                                    backgroundColor = chartBackgroundColor,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(300.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                SystemBackButtonHandler {
                    WellnessWiseAppRouter.navigateTo(Screen.PredictionsScreen)
                }
            }
        }
    }
}

@Composable
fun OverallHealthScoreCard(score: Float?, colorScheme: ColorScheme) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.score),
                contentDescription = "Health Icon",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Overall Health Score",
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = score?.let { "%.1f".format(it) } ?: "N/A",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { score?.div(100f) ?: 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    score == null -> colorScheme.onSecondaryContainer
                    score >= 80 -> colorScheme.primary
                    score >= 60 -> colorScheme.tertiary
                    else -> colorScheme.error
                },
                trackColor = colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
            )
        }
    }
}
@Composable
fun LineChartComponent(
    data: LineData,
    textColor: Int,
    backgroundColor: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                this.data = data
                description.isEnabled = false
                legend.isEnabled = false
                axisLeft.textColor = textColor
                axisLeft.setDrawGridLines(false)
                axisLeft.valueFormatter = PercentFormatter()
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = 100f
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = textColor
                xAxis.setDrawGridLines(false)
                setBackgroundColor(backgroundColor)
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                data.dataSets.forEach { set ->
                    if (set is com.github.mikephil.charting.data.LineDataSet) {
                        set.setDrawFilled(true)
                        set.fillAlpha = 50
                        set.setDrawCircles(true)
                        set.setDrawCircleHole(true)
                        set.circleRadius = 4f
                        set.circleHoleRadius = 2f
                        set.setDrawValues(false)
                        set.highLightColor = Color.Red.toArgb()
                        set.highlightLineWidth = 2f
                    }
                }

                data.isHighlightEnabled = true

                val visibleRange = 20f
                val totalEntries = data.entryCount.toFloat()
                if (totalEntries > visibleRange) {
                    val endX = totalEntries - 1
                    setVisibleXRangeMaximum(visibleRange)
                    moveViewToX(endX)
                }

                animateXY(1500, 1500)
            }
        },
        modifier = modifier
    )
}

@Composable
fun ChartBox(
    title: String,
    content: @Composable () -> Unit
) {
    // Map disease names to their respective drawable resources
    val iconResId = when {
        title.contains("Diabetes", ignoreCase = true) -> R.drawable.blood
        title.contains("Cardiovascular", ignoreCase = true) -> R.drawable.cardiology
        title.contains("Hypertension", ignoreCase = true) -> R.drawable.arm
        title.contains("Obesity", ignoreCase = true) -> R.drawable.obesity
        title.contains("Cancer", ignoreCase = true) -> R.drawable.tumor
        else -> R.drawable.report // Default icon
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp)
    ) {
        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {

                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            content()
        }
    }
}