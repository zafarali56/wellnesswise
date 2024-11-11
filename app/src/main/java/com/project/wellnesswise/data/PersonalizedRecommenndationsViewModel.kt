import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.project.wellnesswise.data.PredictionsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class PersonalizedRecommendationsViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val predictionsViewModel: PredictionsViewModel
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<String>>(emptyList())
    val recommendations: StateFlow<List<String>> = _recommendations

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val recommendationSystem = HealthRecommendationSystem()

    private var userDataListener: ListenerRegistration? = null
    private var updateJob: Job? = null

    init {
        setupDataListeners()
        loadRecommendationsIfLoggedIn()
    }

    private fun setupDataListeners() {
        viewModelScope.launch {
            auth.addAuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    Log.d(TAG, "User logged in, setting up listeners")
                    setupUserDataListener(firebaseAuth.currentUser!!.uid)
                    setupPredictionListener()
                } else {
                    Log.d(TAG, "User logged out, clearing recommendations")
                    _recommendations.value = emptyList()
                    userDataListener?.remove()
                }
            }
        }
    }

    private fun setupUserDataListener(userId: String) {
        userDataListener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "User data changed, triggering recommendation update")
                    triggerRecommendationUpdate()
                }
            }
    }

    private fun setupPredictionListener() {
        viewModelScope.launch {
            predictionsViewModel.predictions.collectLatest { predictions ->
                if (predictions != null) {
                    Log.d(TAG, "Predictions changed, triggering recommendation update")
                    triggerRecommendationUpdate()
                }
            }
        }
    }

    private fun loadRecommendationsIfLoggedIn() {
        if (auth.currentUser != null) {
            Log.d(TAG, "User already logged in, loading recommendations")
            triggerRecommendationUpdate()
        }
    }

    private fun triggerRecommendationUpdate() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(500)
            loadRecommendations()
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUser ?: throw Exception("User not authenticated")


                val latestPrediction = firestore.collection("users")
                    .document(user.uid)
                    .collection("predictions")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                if (latestPrediction.isEmpty) {
                    _recommendations.value = listOf("No predictions available. Please complete your health assessment.")
                    return@launch
                }

                val predictionDoc = latestPrediction.documents[0]
                val predictions = (predictionDoc.get("predictions") as? List<Map<String, Any>>)?.map { pred ->
                    Triple(
                        pred["category"] as String,
                        (pred["risk"] as Number).toFloat(),
                        pred["context"] as String
                    )
                } ?: throw Exception("Invalid prediction data format")

                val newRecommendations = recommendationSystem.generateRecommendations(predictions)
                _recommendations.value = newRecommendations
                Log.d(TAG, "New recommendations generated from latest predictions")
            } catch (e: Exception) {
                _recommendations.value = listOf("Unable to load recommendations. Please try again later.")
                Log.e(TAG, "Error loading recommendations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getUserData(): Map<String, Any> {
        val user = auth.currentUser ?: throw Exception("User not authenticated")
        val snapshot = firestore.collection("users").document(user.uid).get().await()
        return snapshot.data ?: emptyMap()
    }


    override fun onCleared() {
        super.onCleared()
        userDataListener?.remove()
        updateJob?.cancel()
    }

    companion object {
        private const val TAG = "PersonalizedRecommendationsViewModel"
    }
}

class PersonalizedRecommendationsViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val predictionsViewModel: PredictionsViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalizedRecommendationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonalizedRecommendationsViewModel(firestore, auth, predictionsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}