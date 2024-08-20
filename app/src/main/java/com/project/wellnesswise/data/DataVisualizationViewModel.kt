import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HealthDataPoint(
    val bloodPressure: String = "120/80",
    val heartRate: Float = 70f,
    val bloodSugar: Float = 100f,
    val cholesterol: String = "100,50,150"
)

sealed class HealthDataState {
    object Loading : HealthDataState()
    data class Success(val data: HealthDataPoint) : HealthDataState()
    data class Error(val message: String) : HealthDataState()
}

class DataVisualizationViewModel : ViewModel() {
    private val _healthDataState = MutableStateFlow<HealthDataState>(HealthDataState.Loading)
    val healthDataState: StateFlow<HealthDataState> = _healthDataState

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun fetchHealthData() {
        viewModelScope.launch {
            _healthDataState.value = HealthDataState.Loading
            val userId = auth.currentUser?.uid
            Log.d("DataVisualizationViewModel", "Fetching health data for user: $userId")

            if (userId == null) {
                Log.e("DataVisualizationViewModel", "User not authenticated")
                _healthDataState.value = HealthDataState.Error("User not authenticated")
                return@launch
            }

            try {
                val snapshot = firestore.collection("users").document(userId)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val data = snapshot.data
                    Log.d("DataVisualizationViewModel", "Document data: $data")
                    if (data != null) {
                        val healthDataPoint = HealthDataPoint(
                            bloodPressure = parseBloodPressure(data["bloodPressure"]),
                            heartRate = (data["heartRate"] as? Number)?.toFloat() ?: 70f,
                            bloodSugar = (data["bloodSugar"] as? Number)?.toFloat() ?: 100f,
                            cholesterol = parseCholesterol(data["cholesterol"])
                        )
                        Log.d("DataVisualizationViewModel", "Parsed HealthDataPoint: $healthDataPoint")
                        _healthDataState.value = HealthDataState.Success(healthDataPoint)
                    } else {
                        Log.w("DataVisualizationViewModel", "Document data is null, using default values")
                        _healthDataState.value = HealthDataState.Success(HealthDataPoint())
                    }
                } else {
                    Log.w("DataVisualizationViewModel", "No health data found in Firestore, using default values")
                    _healthDataState.value = HealthDataState.Success(HealthDataPoint())
                }
            } catch (e: Exception) {
                Log.e("DataVisualizationViewModel", "Error fetching health data", e)
                _healthDataState.value = HealthDataState.Error("Failed to fetch health data: ${e.message}")
            }
        }
    }

    private fun parseBloodPressure(value: Any?): String {
        return when (value) {
            is String -> value
            is Number -> "${value.toInt()}/80"
            else -> "120/80"
        }
    }

    private fun parseCholesterol(value: Any?): String {
        return when (value) {
            is String -> value
            is Number -> "${value.toInt()},50,150"
            else -> "100,50,150"
        }
    }
}