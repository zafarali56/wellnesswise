import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.components.data.HealthDataUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HealthDataViewModel : ViewModel() {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage

    private val _healthData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val healthData: StateFlow<Map<String, Any>> = _healthData

    private val _cholesterol = MutableStateFlow<String>("")
    val cholesterol: StateFlow<String> = _cholesterol

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    init {
        loadUserHealthData()
    }
    private fun loadUserHealthData() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                try {
                    val document = firestore.collection("users").document(user.uid).get().await()
                    if (document.exists()) {
                        val data = document.data ?: emptyMap()
                        _healthData.value = data.filter { it.key != "cholesterol" }
                        _cholesterol.value = data["cholesterol"] as? String ?: ""
                    } else {
                        _healthData.value = emptyMap()
                        _cholesterol.value = ""
                    }
                } catch (e: Exception) {
                    Log.e("HealthDataViewModel", "Error loading user health data", e)
                }
            } else {
                _healthData.value = emptyMap()
                _cholesterol.value = ""
            }
        }
    }

    fun handleGoogleFitSync(
        context: Context,
        fitnessOptions: FitnessOptions,
        googleSignInLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) {
        viewModelScope.launch {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                try {
                    _isSyncing.value = true
                    setSyncMessage("Requesting Google Fit permissions...")

                    val signInOptions = GoogleSignInOptions.Builder()
                        .addExtension(fitnessOptions)
                        .build()
                    val intent = GoogleSignIn.getClient(context, signInOptions).signInIntent
                    googleSignInLauncher.launch(intent)
                } catch (e: Exception) {
                    setSyncMessage("Error requesting Google Fit permissions: ${e.message}")
                } finally {
                    _isSyncing.value = false
                }
            } else {
                syncWithGoogleFit(context, fitnessOptions)
            }
        }
    }

    fun updateManualHealthData(
        bloodPressure: String? = null,
        heartRate: String? = null,
        bloodSugar: String? = null,
    ) {
        val currentData = _healthData.value.toMutableMap()
        bloodPressure?.let {
            currentData["bloodPressure"] = it
            currentData["bloodPressureSource"] = "MANUAL"
        }
        heartRate?.let {
            currentData["heartRate"] = it
            currentData["heartRateSource"] = "MANUAL"
        }
        bloodSugar?.let {
            currentData["bloodSugar"] = it
            currentData["bloodSugarSource"] = "MANUAL"
        }

        currentData["dataSourcePreference"] = "MANUAL"
        _healthData.value = currentData
    }

    fun updateCholesterol(cholesterol: String) {
        _cholesterol.value = cholesterol
    }

    suspend fun syncWithGoogleFit(
        context: Context,
        fitnessOptions: FitnessOptions
    ) {
        try {
            _isSyncing.value = true
            setSyncMessage("Syncing with Google Fit...")

            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            val googleFitData = HealthDataUtils.fetchHealthData(context, account)

            val updatedData = googleFitData.flatMap { (key, value) ->
                listOf(
                    key to value,
                    "${key}Source" to "GOOGLE_FIT"
                )
            }.toMap().toMutableMap()
            updatedData["dataSourcePreference"] = "GOOGLE_FIT"

            _healthData.value = updatedData

            updateFirestore(updatedData)

            setSyncMessage("Sync completed successfully")
        } catch (e: Exception) {
            Log.e("HealthDataViewModel", "Error syncing health data", e)
            setSyncMessage("Error syncing with Google Fit: ${e.message}")
        } finally {
            _isSyncing.value = false
        }
    }
    fun sendHealthDataToFirestore() {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                setSyncMessage("Updating health data...")
                val combinedData = _healthData.value.toMutableMap()
                combinedData["cholesterol"] = _cholesterol.value
                updateFirestore(combinedData)
                setSyncMessage("Health data updated successfully")
            } catch (e: Exception) {
                Log.e("HealthDataViewModel", "Error updating health data", e)
                setSyncMessage("Error updating health data: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }
    private suspend fun updateFirestore(healthData: Map<String, Any>) {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid)
                .set(healthData, com.google.firebase.firestore.SetOptions.merge())
                .await()
            Log.d("HealthDataViewModel", "Health data updated successfully: $healthData")
        } else {
            throw Exception("User not authenticated")
        }
    }

    fun setSyncMessage(message: String?) {
        _syncMessage.value = message
    }

    fun resetHealthData() {
        _healthData.value = emptyMap()
        _syncMessage.value = null
        _isSyncing.value = false
        _cholesterol.value = ""
    }
}