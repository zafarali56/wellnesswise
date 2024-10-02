import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Health Risk Trends", color = colorScheme.onSurface) },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                            text = "No prediction data available",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
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
                    WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
                }
            }
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
                legend.isEnabled = false // Disable legend for cleaner look
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

                // Customize the appearance of the line and circles
                data.dataSets.forEach { set ->
                    if (set is LineDataSet) {
                        set.setDrawFilled(true)
                        set.fillAlpha = 50 // Semi-transparent fill
                        set.setDrawCircles(true)
                        set.setDrawCircleHole(true)
                        set.circleRadius = 4f
                        set.circleHoleRadius = 2f
                        set.setDrawValues(false)
                        set.highLightColor = androidx.compose.ui.graphics.Color.Red.toArgb()// Highlight color
                        set.highlightLineWidth = 2f
                    }
                }

                // Enable highlighting for interaction
                data.isHighlightEnabled = true

                // Set up value formatter for highlighted values
                val formatter = PercentFormatter()
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let {
                            val formattedValue = formatter.getFormattedValue(it.y)
                            Log.d("ChartValue", "Selected value: $formattedValue")
                            // You can use this formattedValue to display in a tooltip or some other UI element
                        }
                    }

                    override fun onNothingSelected() {}
                })

                // Set visible range
                val visibleRange = 20f
                val totalEntries = data.entryCount.toFloat()
                if (totalEntries > visibleRange) {
                    val endX = totalEntries - 1
                    setVisibleXRangeMaximum(visibleRange)
                    moveViewToX(endX)
                }

                // Animate the chart
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
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


