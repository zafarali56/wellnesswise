import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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

import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel

import com.project.wellnesswise.components.ui.BloodPressureChart
import com.project.wellnesswise.components.ui.BloodSugarChart
import com.project.wellnesswise.components.ui.CholesterolChart
import com.project.wellnesswise.components.ui.HeartRateChart
import com.project.wellnesswise.components.ui.NormalTextComponent
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter


@Composable
fun DataVisualizationScreen(
    dataVisualizationViewModel: DataVisualizationViewModel = viewModel()
) {


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

