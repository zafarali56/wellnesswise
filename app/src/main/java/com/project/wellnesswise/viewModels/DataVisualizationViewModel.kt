import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DataVisualizationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _diseaseRiskData = MutableStateFlow<Map<String, List<Entry>>>(emptyMap())
    val diseaseRiskData = _diseaseRiskData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _overallHealthScore = MutableStateFlow<Float?>(null)
    val overallHealthScore = _overallHealthScore.asStateFlow()

    private var predictionListener: ListenerRegistration? = null
    private var userDataListener: ListenerRegistration? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                setupPredictionListener(user.uid)
            } else {
                clearData()
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    private fun setupPredictionListener(userId: String) {
        predictionListener?.remove()

        _isLoading.value = true
        _error.value = null

        predictionListener = firestore.collection("users").document(userId)
            .collection("predictions")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Failed to listen for prediction updates: ${e.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    viewModelScope.launch {
                        processPredictionData(snapshot.documents)
                    }
                } else {
                    _diseaseRiskData.value = emptyMap()
                    _isLoading.value = false
                }
            }
    }

    fun getLineData(disease: String, color: Int): LineData? {
        val entries = _diseaseRiskData.value[disease] ?: return null
        val dataSet = LineDataSet(entries, disease).apply {
            this.color = color
            setCircleColor(color)
            setDrawValues(true)
            lineWidth = 2f
            circleRadius = 4f
            highLightColor = color
            setDrawHighlightIndicators(true)
            valueFormatter = PercentageValueFormatter()
            valueTextColor = color
            valueTextSize = 10f
        }
        return LineData(dataSet)
    }

    private inner class PercentageValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return String.format("%.1f%%", value)
        }
    }
    private fun calculateOverallHealthScore(userData: Map<String, Any>?, latestPredictions: List<Map<String, Any>>? = null) {
        var score = 100f

        // Deduct points based on user viewModels
        userData?.let {
            val age = (it["age"] as? Number)?.toInt() ?: 0
            score -= (age / 100f) * 10

            val bmi = calculateBMI(it["height"] as? Number, it["weight"] as? Number)
            score -= when {
                bmi < 18.5f || bmi > 30f -> 10f
                bmi < 25f -> 0f
                else -> 5f
            }

            val bloodPressure = it["bloodPressure"] as? String
            bloodPressure?.split("/")?.let { bp ->
                val systolic = bp.getOrNull(0)?.toIntOrNull() ?: 0
                val diastolic = bp.getOrNull(1)?.toIntOrNull() ?: 0
                score -= when {
                    systolic > 140 || diastolic > 90 -> 10f
                    systolic > 120 || diastolic > 80 -> 5f
                    else -> 0f
                }
            }

            val heartRate = (it["heartRate"] as? Number)?.toInt() ?: 0
            score -= when {
                heartRate < 60 || heartRate > 100 -> 5f
                else -> 0f
            }

            val bloodSugar = (it["bloodSugar"] as? Number)?.toInt() ?: 0
            score -= when {
                bloodSugar > 200 -> 10f
                bloodSugar > 140 -> 5f
                else -> 0f
            }

            val cholesterol = (it["cholesterol"] as? Number)?.toInt() ?: 0
            score -= when {
                cholesterol > 240 -> 10f
                cholesterol > 200 -> 5f
                else -> 0f
            }

            // Lifestyle factors
            val smoking = it["smoking"] as? Boolean ?: false
            if (smoking) score -= 10f

            val alcoholConsumption = (it["alcoholConsumption"] as? Number)?.toInt() ?: 0
            score -= alcoholConsumption * 2f

            val physicalActivity = (it["physicalActivity"] as? Number)?.toInt() ?: 0
            score += physicalActivity * 2f

            val dietQuality = (it["dietQuality"] as? Number)?.toInt() ?: 0
            score += dietQuality * 2f

            val sleepHours = (it["sleepHours"] as? Number)?.toInt() ?: 0
            score -= when {
                sleepHours < 6 || sleepHours > 9 -> 5f
                else -> 0f
            }

            // Environmental factors
            val airQualityIndex = (it["airQualityIndex"] as? Number)?.toInt() ?: 0
            score -= when {
                airQualityIndex > 150 -> 5f
                airQualityIndex > 100 -> 2f
                else -> 0f
            }

            val stressLevel = (it["stressLevel"] as? Number)?.toInt() ?: 0
            score -= stressLevel * 2f
        }

        // Deduct points based on latest predictions
        latestPredictions?.forEach { prediction ->
            val risk = (prediction["risk"] as? Number)?.toFloat() ?: 0f
            score -= risk * 20 // Deduct up to 20 points per high-risk prediction
        }

        _overallHealthScore.value = score.coerceIn(0f, 100f)
    }
    private fun processPredictionData(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val diseaseData = mutableMapOf<String, MutableList<Entry>>()
        documents.forEachIndexed { index, document ->
            val predictions = document.get("predictions") as? List<Map<String, Any>> ?: return@forEachIndexed
            predictions.forEach { prediction ->
                val category = prediction["category"] as? String ?: return@forEach
                val risk = (prediction["risk"] as? Number)?.toFloat() ?: return@forEach
                diseaseData.getOrPut(category) { mutableListOf() }.add(Entry(index.toFloat(), risk * 100))
            }
        }
        _diseaseRiskData.value = diseaseData
        _isLoading.value = false

        // Use the latest prediction for overall health score calculation
        val latestPredictions = documents.lastOrNull()?.get("predictions") as? List<Map<String, Any>>
        latestPredictions?.let { calculateOverallHealthScore(null, it) }
    }

    private fun calculateBMI(height: Number?, weight: Number?): Float {
        if (height == null || weight == null) return 0f
        val heightInMeters = height.toFloat() / 100
        return weight.toFloat() / (heightInMeters * heightInMeters)
    }

    private fun clearData() {
        _diseaseRiskData.value = emptyMap()
        _overallHealthScore.value = null
        _isLoading.value = false
        _error.value = "Please log in to view your health viewModels"
    }
    override fun onCleared() {
        super.onCleared()
        predictionListener?.remove()
        userDataListener?.remove()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }

    fun resetVisualizationData() {
        _diseaseRiskData.value = emptyMap()
        _overallHealthScore.value = null
        _isLoading.value = false
        _error.value = null
        predictionListener?.remove()
        userDataListener?.remove()
        predictionListener = null
        userDataListener = null
    }
}