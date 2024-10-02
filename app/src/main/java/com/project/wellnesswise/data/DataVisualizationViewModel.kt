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

    private var predictionListener: ListenerRegistration? = null
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
        predictionListener?.remove() // Remove any existing listener

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

    private fun clearData() {
        _diseaseRiskData.value = emptyMap()
        _isLoading.value = false
        _error.value = "Please log in to view your prediction history"
    }

    override fun onCleared() {
        super.onCleared()
        predictionListener?.remove()
        authStateListener?.let { auth.removeAuthStateListener(it) }
    }
}