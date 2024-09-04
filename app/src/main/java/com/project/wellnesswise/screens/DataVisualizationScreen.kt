import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataVisualizationScreen(dataVisualizationViewModel: DataVisualizationViewModel) {
    val context = LocalContext.current
    val useDarkIcons = !isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()
    val colorScheme = when {
        useDarkIcons -> dynamicLightColorScheme(context)
        else -> dynamicDarkColorScheme(context)
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.surface,
            darkIcons = useDarkIcons
        )
    }



    val isDarkTheme = isSystemInDarkTheme()

    val chartBackgroundColor =
        if (isDarkTheme) colorScheme.surface.toArgb() else colorScheme.background.toArgb()
    val chartTextColor =
        if (isDarkTheme) colorScheme.onSurface.toArgb() else colorScheme.onBackground.toArgb()

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Data Visualization", color = colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) }) {
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ChartBox(
                        title = "Blood Pressure Line Chart",
                        content = {
                            LineChartComponent(
                                data = dataVisualizationViewModel.bloodPressureData,
                                lineColor = colorScheme.primary.toArgb(),
                                textColor = chartTextColor,
                                backgroundColor = chartBackgroundColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    )

                }
                item {
                    ChartBox(
                        title = "Heart Rate Bar Chart",
                        content = {
                            BarChartComponent(
                                data = dataVisualizationViewModel.heartRateData,
                                barColor = colorScheme.secondary.toArgb(),
                                textColor = chartTextColor,
                                backgroundColor = chartBackgroundColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    )
                }
                item {
                    ChartBox(
                        title = "Overall Health Radar Chart",
                        content = {
                            RadarChartComponent(
                                data = dataVisualizationViewModel.getOverallHealthData(colorScheme.tertiary.toArgb()),
                                textColor = chartTextColor,
                                backgroundColor = chartBackgroundColor,
                                webColor = colorScheme.onSurface.copy(alpha = 0.1f).toArgb(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    )
                }
            }
        }

    }
    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
    }
}


@Composable
fun LineChartComponent(
    data: List<Pair<Entry, Entry>>,
    lineColor: Int,
    textColor: Int,
    backgroundColor: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                val systolicDataSet = LineDataSet(data.map { it.first }, "Systolic").apply {
                    color = lineColor
                    setCircleColor(lineColor)
                    setDrawValues(false)
                }

                val diastolicDataSet = LineDataSet(data.map { it.second }, "Diastolic").apply {
                    color = Color(lineColor).copy(alpha = 0.5f).toArgb()
                    setCircleColor(Color(lineColor).copy(alpha = 0.5f).toArgb())
                    setDrawValues(false)
                }

                setData(LineData(systolicDataSet, diastolicDataSet))
                description.isEnabled = false
                legend.isEnabled = true
                axisLeft.textColor = textColor
                axisRight.isEnabled = false
                xAxis.textColor = textColor
                setBackgroundColor(backgroundColor)
                invalidate()
            }
        },
        modifier = modifier
    )
}


@Composable
fun BarChartComponent(
    data: List<BarEntry>,
    barColor: Int,
    textColor: Int,
    backgroundColor: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                setData(BarData(BarDataSet(data, "Heart Rate").apply {
                    color = barColor
                    setDrawValues(false)
                }))
                description.isEnabled = false
                legend.isEnabled = false
                axisLeft.textColor = textColor
                axisRight.isEnabled = false
                xAxis.textColor = textColor
                setBackgroundColor(backgroundColor)
                invalidate()
            }
        },
        modifier = modifier
    )
}

@Composable
fun RadarChartComponent(
    data: RadarData,
    textColor: Int,
    backgroundColor: Int,
    webColor: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            RadarChart(context).apply {
                setData(data)
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Sleep", "Diet", "Exercise", "Stress", "Hydration"))
                xAxis.textColor = textColor
                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = 100f
                yAxis.textColor = textColor
                this.webColor = webColor
                webLineWidth = 1f
                webColorInner = webColor
                webLineWidthInner = 1f
                setBackgroundColor(backgroundColor)
                invalidate()
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(15.dp)
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}