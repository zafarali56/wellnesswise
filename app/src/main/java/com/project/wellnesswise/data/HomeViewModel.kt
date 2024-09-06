import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val TAG = "HomeViewModel"

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    private val _bloodPressure = MutableStateFlow("N/A")
    val bloodPressure: StateFlow<String> = _bloodPressure

    private val _heartRate = MutableStateFlow("N/A")
    val heartRate: StateFlow<String> = _heartRate

    private val _bloodSugar = MutableStateFlow("N/A")
    val bloodSugar: StateFlow<String> = _bloodSugar

    private val _cholesterol = MutableStateFlow("N/A")
    val cholesterol: StateFlow<String> = _cholesterol

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var firestoreListener: ListenerRegistration? = null
    init {
        checkForActiveSession()
    }
    fun getUserData(callback: (Map<String, Any>?) -> Unit) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                try {
                    val snapshot = firestore.collection("users").document(currentUser.uid).get().await()
                    if (snapshot.exists()) {
                        callback(snapshot.data)
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user data", e)
                    callback(null)
                }
            } else {
                callback(null)
            }
        }
    }
    fun checkForActiveSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Valid session for user: ${currentUser.uid}")
            _isUserLoggedIn.value = true
            setupFirestoreListener(currentUser.uid)
        } else {
            Log.d(TAG, "User is not logged in")
            _isUserLoggedIn.value = false
            removeFirestoreListener()
        }
    }

    private fun setupFirestoreListener(userId: String) {
        removeFirestoreListener()

        firestoreListener =
            firestore
                .collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        try {
                            snapshot.data?.let { data ->
                                viewModelScope.launch {
                                    updateHealthData(data)
                                }
                            }
                            Log.d(
                                TAG,
                                "Data updated: BP: ${_bloodPressure.value}, HR: ${_heartRate.value}, BS: ${_bloodSugar.value}, Chol: ${_cholesterol.value}",
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing Firestore data", e)
                            setDefaultValues()
                        }
                    } else {
                        Log.d(TAG, "Current data: null")
                        setDefaultValues()
                    }
                }
    }

    private fun removeFirestoreListener() {
        firestoreListener?.remove()
        firestoreListener = null
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val snapshot =
                        firestore
                            .collection("users")
                            .document(currentUser.uid)
                            .get()
                            .await()
                    if (snapshot.exists()) {
                        snapshot.data?.let { data ->
                            updateHealthData(data)
                        }
                    } else {
                        setDefaultValues()
                    }
                } else {
                    setDefaultValues()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                setDefaultValues()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun updateHealthData(data: Map<String, Any>) {
        _bloodPressure.value = data["bloodPressure"]?.toString() ?: "N/A"
        _heartRate.value =
            when (val hrData = data["heartRate"]) {
                is Number -> hrData.toString()
                is String -> hrData.toIntOrNull()?.toString() ?: "N/A"
                else -> "N/A"
            }
        _bloodSugar.value = data["bloodSugar"]?.toString() ?: "N/A"
        _cholesterol.value = data["cholesterol"]?.toString() ?: "N/A"
    }

    private fun setDefaultValues() {
        _bloodPressure.value = "N/A"
        _heartRate.value = "N/A"
        _bloodSugar.value = "N/A"
        _cholesterol.value = "N/A"
    }

    override fun onCleared() {
        super.onCleared()
        removeFirestoreListener()
    }
}
