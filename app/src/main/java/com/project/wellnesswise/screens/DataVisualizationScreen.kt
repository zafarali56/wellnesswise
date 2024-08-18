import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.project.wellnesswise.components.NormalTextComponent
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.ui.theme.Primary

@Composable
fun DataVisualizationScreen(
    dataVisualizationViewModel: DataVisualizationViewModel = viewModel()
) {

    val Primary = Color(0xFF4ABFA4)
    val Secondary = Color(0xFF8BBF40)
    val healthDataState by dataVisualizationViewModel.healthDataState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        Log.d("DataVisualizationScreen", "LaunchedEffect triggered")
        dataVisualizationViewModel.fetchHealthData()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            NormalTextComponent(value = "Health Data Visualization")

            when (val state = healthDataState) {
                is HealthDataState.Loading -> {
                    Log.d("DataVisualizationScreen", "Loading state")
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is HealthDataState.Success -> {
                    Log.d("DataVisualizationScreen", "Success state: ${state.data}")
                    val healthData = state.data

                    BloodPressureChart(
                        title = "Blood Pressure",
                        value = healthData.bloodPressure,
                        unit = "mmHg"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HeartRateChart(
                        title = "Heart Rate",
                        value = healthData.heartRate,
                        unit = "bpm"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BloodSugarChart(
                        title = "Blood Sugar",
                        value = healthData.bloodSugar,
                        unit = "mg/dL"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CholesterolChart(
                        title = "Cholesterol",
                        value = healthData.cholesterol,
                        unit = "mg/dL"
                    )
                }
                is HealthDataState.Error -> {
                    Log.e("DataVisualizationScreen", "Error state: ${state.message}")
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                else -> {}
            }
        }
    }

    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
    }
}

@Composable
fun BloodPressureChart(title: String, value: String, unit: String) {
    ChartCard(
        title = title,
        value = value,
        unit = unit
    ) {
        val (systolic, diastolic) = value.split("/").mapNotNull { it.toFloatOrNull() }.takeIf { it.size == 2 } ?: listOf(120f, 80f)

        val chartEntryModel = entryModelOf(
            listOf(
                FloatEntry(x = 0f, y = systolic),
                FloatEntry(x = 1f, y = diastolic)
            )
        )

        Chart(
            chart = columnChart(),
            model = chartEntryModel,
            startAxis = startAxis(
                title = "mmHg",
                titleComponent = textComponent(
                    color = MaterialTheme.colorScheme.onSurface,
                    textSize = 12.sp
                )
            ),
            bottomAxis = bottomAxis(
                valueFormatter = { value, _ ->
                    when (value) {
                        0f -> "Systolic"
                        1f -> "Diastolic"
                        else -> ""
                    }
                }
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun HeartRateChart(title: String, value: Float, unit: String) {
    ChartCard(
        title = title,
        value = value.toString(),
        unit = unit
    ) {
        val chartEntryModel = entryModelOf(
            listOf(
                FloatEntry(x = 0f, y = value * 0.9f),
                FloatEntry(x = 1f, y = value * 0.95f),
                FloatEntry(x = 2f, y = value),
                FloatEntry(x = 3f, y = value * 1.05f),
                FloatEntry(x = 4f, y = value * 1.1f)
            )
        )

        Chart(
            chart = lineChart(),
            model = chartEntryModel,
            startAxis = startAxis(
                title = "bpm",
                titleComponent = textComponent(
                    color = MaterialTheme.colorScheme.onSurface,
                    textSize = 12.sp
                )
            ),
            bottomAxis = bottomAxis(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BloodSugarChart(title: String, value: Float, unit: String) {
    ChartCard(
        title = title,
        value = value.toString(),
        unit = unit
    ) {
        val chartEntryModel = entryModelOf(
            listOf(
                FloatEntry(x = 0f, y = value * 0.9f),
                FloatEntry(x = 1f, y = value * 0.95f),
                FloatEntry(x = 2f, y = value),
                FloatEntry(x = 3f, y = value * 1.05f),
                FloatEntry(x = 4f, y = value * 1.1f)
            )
        )

        Chart(
            chart = lineChart(),
            model = chartEntryModel,
            startAxis = startAxis(
                title = "mg/dL",
                titleComponent = textComponent(
                    color = Primary,
                    textSize = 12.sp
                )
            ),
            bottomAxis = bottomAxis(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CholesterolChart(title: String, value: String, unit: String) {
    ChartCard(
        title = title,
        value = value,
        unit = unit
    ) {
        val cholesterolValues = value.split(",").mapNotNull { it.toFloatOrNull() }
        val (ldl, hdl, triglycerides) = when (cholesterolValues.size) {
            3 -> cholesterolValues
            1 -> listOf(cholesterolValues[0], 50f, 150f)
            else -> listOf(100f, 50f, 150f)
        }

        val chartEntryModel = entryModelOf(
            listOf(
                FloatEntry(x = 0f, y = ldl),
                FloatEntry(x = 1f, y = hdl),
                FloatEntry(x = 2f, y = triglycerides)
            )
        )

        Chart(
            chart = columnChart(),
            model = chartEntryModel,
            startAxis = startAxis(
                title = "mg/dL",
                titleComponent = textComponent(
                    color = MaterialTheme.colorScheme.onSurface,
                    textSize = 12.sp
                )
            ),
            bottomAxis = bottomAxis(
                valueFormatter = { value, _ ->
                    when (value) {
                        0f -> "LDL"
                        1f -> "HDL"
                        2f -> "Triglycerides"
                        else -> ""
                    }
                }
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ChartCard(
    title: String,
    value: String,
    unit: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Current: $value $unit",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            content()
        }
    }
}