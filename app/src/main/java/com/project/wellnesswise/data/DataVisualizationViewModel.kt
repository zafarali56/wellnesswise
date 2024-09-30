import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.*
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

    init {
        setupPredictionListener()
    }

    private fun setupPredictionListener() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in"
            _isLoading.value = false
            return
        }

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
                    processPredictionData(snapshot.documents)
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
            setDrawValues(false)
            lineWidth = 2f
            circleRadius = 4f
            highLightColor = color
            setDrawHighlightIndicators(true)
        }
        return LineData(dataSet)
    }

    override fun onCleared() {
        super.onCleared()
        predictionListener?.remove()
    }
}