import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.project.wellnesswise.data.PredictionsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant

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

    init {
        setupDataListeners()
    }

    private fun setupDataListeners() {
        viewModelScope.launch {
            auth.currentUser?.let { user ->
                userDataListener = firestore.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            // Handle error
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            loadRecommendations()
                        }
                    }
            }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userData = getUserData()
                val predictions = predictionsViewModel.predictions ?: emptyList()
                val newRecommendations = recommendationSystem.generateRecommendations(userData, predictions)
                _recommendations.value = newRecommendations
            } catch (e: Exception) {
                _recommendations.value = listOf("Unable to load recommendations. Please try again later.")
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

    fun refreshRecommendations() {
        loadRecommendations()
    }

    override fun onCleared() {
        super.onCleared()
        userDataListener?.remove()
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