package com.project.wellnesswise.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlin.math.pow
import kotlin.math.sqrt

data class ModelInput(
    val values: List<Float>,
    val labels: List<String>
)

class HealthDataProcessor(private val onDataChanged: () -> Unit) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var firestoreListener: ListenerRegistration? = null

    companion object {
        private val listeners = mutableSetOf<() -> Unit>()
        val riskCategories = listOf("Diabetes", "Cardiovascular Disease", "Hypertension", "Obesity", "Cancer")
        val inputLabels = listOf(
            "Age", "Height", "Weight", "BMI", "Systolic_BP", "Diastolic_BP", "Heart_Rate",
            "Blood_Sugar", "Cholesterol", "Smoking", "Alcohol_Consumption", "Physical_Activity",
            "Diet_Quality", "Sleep_Hours", "Air_Quality_Index", "Stress_Level",
            "Exposure_to_Pollutants", "Access_to_Healthcare", "Family_History_Diabetes",
            "Family_History_Heart_Disease", "Family_History_Cancer", "Previous_Surgeries",
            "Chronic_Conditions", "Gender_Female", "Gender_Male"
        )

        // Mean values for z-score normalization
        val featureMeans = mapOf(
            "Age" to 45f, "Height" to 170f, "Weight" to 70f, "BMI" to 24f,
            "Systolic_BP" to 120f, "Diastolic_BP" to 80f, "Heart_Rate" to 70f,
            "Blood_Sugar" to 100f, "Cholesterol" to 180f, "Smoking" to 0.5f,
            "Alcohol_Consumption" to 2f, "Physical_Activity" to 2f, "Diet_Quality" to 2f,
            "Sleep_Hours" to 7f, "Air_Quality_Index" to 50f, "Stress_Level" to 2f,
            "Exposure_to_Pollutants" to 2f, "Access_to_Healthcare" to 2f,
            "Family_History_Diabetes" to 0.5f, "Family_History_Heart_Disease" to 0.5f,
            "Family_History_Cancer" to 0.5f, "Previous_Surgeries" to 0.5f,
            "Chronic_Conditions" to 0.5f, "Gender_Female" to 0.5f, "Gender_Male" to 0.5f
        )

        // Standard deviation values for z-score normalization
        val featureStds = mapOf(
            "Age" to 15f, "Height" to 10f, "Weight" to 15f, "BMI" to 5f,
            "Systolic_BP" to 15f, "Diastolic_BP" to 10f, "Heart_Rate" to 10f,
            "Blood_Sugar" to 20f, "Cholesterol" to 30f, "Smoking" to 0.5f,
            "Alcohol_Consumption" to 1f, "Physical_Activity" to 1f, "Diet_Quality" to 1f,
            "Sleep_Hours" to 1f, "Air_Quality_Index" to 25f, "Stress_Level" to 1f,
            "Exposure_to_Pollutants" to 1f, "Access_to_Healthcare" to 1f,
            "Family_History_Diabetes" to 0.5f, "Family_History_Heart_Disease" to 0.5f,
            "Family_History_Cancer" to 0.5f, "Previous_Surgeries" to 0.5f,
            "Chronic_Conditions" to 0.5f, "Gender_Female" to 0.5f, "Gender_Male" to 0.5f
        )

        fun addDataChangeListener(listener: () -> Unit) {
            listeners.add(listener)
        }

        fun removeDataChangeListener(listener: () -> Unit) {
            listeners.remove(listener)
        }

        fun notifyDataChanged() {
            listeners.forEach { it() }
        }
    }

    fun startListeningForChanges() {
        val userId = auth.currentUser?.uid ?: return
        firestoreListener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("HealthDataProcessor", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    notifyDataChanged()
                }
            }
    }

    suspend fun getUserHealthData(): ModelInput? {
        val userId = auth.currentUser?.uid ?: return null
        val docSnapshot = firestore.collection("users").document(userId).get().await()
        val userData = docSnapshot.data ?: return null

        val rawInputValues = mutableListOf<Float>()
        val normalizedValues = mutableListOf<Float>()

        inputLabels.forEach { label ->
            val value = when (label) {
                "Age" -> (userData["age"] as? Number)?.toFloat() ?: 0f
                "Height" -> (userData["height"] as? Number)?.toFloat() ?: 0f
                "Weight" -> (userData["weight"] as? Number)?.toFloat() ?: 0f
                "BMI" -> calculateBMI(userData["height"] as? Number, userData["weight"] as? Number)
                "Systolic_BP" -> parseBloodPressure(userData["bloodPressure"] as? String).first
                "Diastolic_BP" -> parseBloodPressure(userData["bloodPressure"] as? String).second
                "Heart_Rate" -> parseHealthValue(userData["heartRate"])
                "Blood_Sugar" -> parseHealthValue(userData["bloodSugar"])
                "Cholesterol" -> parseHealthValue(userData["cholesterol"])
                "Smoking" -> if (userData["smoking"] as? Boolean == true) 1f else 0f
                "Alcohol_Consumption" -> (userData["alcoholConsumption"] as? Number)?.toFloat() ?: 0f
                "Physical_Activity" -> (userData["physicalActivity"] as? Number)?.toFloat() ?: 0f
                "Diet_Quality" -> (userData["dietQuality"] as? Number)?.toFloat() ?: 0f
                "Sleep_Hours" -> (userData["sleepHours"] as? Number)?.toFloat() ?: 0f
                "Air_Quality_Index" -> (userData["airQualityIndex"] as? Number)?.toFloat() ?: 0f
                "Stress_Level" -> (userData["stressLevel"] as? Number)?.toFloat() ?: 0f
                "Exposure_to_Pollutants" -> (userData["exposureToPollutants"] as? Number)?.toFloat() ?: 0f
                "Access_to_Healthcare" -> (userData["accessToHealthcare"] as? Number)?.toFloat() ?: 0f
                "Family_History_Diabetes" -> if (userData["familyDiabetes"] as? String == "Yes") 1f else 0f
                "Family_History_Heart_Disease" -> if (userData["familyHeart"] as? String == "Yes") 1f else 0f
                "Family_History_Cancer" -> if (userData["familyCancer"] as? String == "Yes") 1f else 0f
                "Previous_Surgeries" -> if (userData["previousSurgeries"] as? String == "Yes") 1f else 0f
                "Chronic_Conditions" -> if (userData["chronicConditions"] as? String == "Yes") 1f else 0f
                "Gender_Female" -> if (userData["gender"] as? String == "FEMALE") 1f else 0f
                "Gender_Male" -> if (userData["gender"] as? String == "MALE") 1f else 0f
                else -> 0f
            }
            rawInputValues.add(value)

            // Z-score normalization
            val mean = featureMeans[label] ?: 0f
            val std = featureStds[label] ?: 1f
            val normalizedValue = (value - mean) / std
            normalizedValues.add(normalizedValue)
        }

        Log.d("HealthDataProcessor", "Raw input data: ${rawInputValues.zip(inputLabels)}")
        Log.d("HealthDataProcessor", "Normalized input data: ${normalizedValues.zip(inputLabels)}")

        return ModelInput(normalizedValues, inputLabels)
    }

    private fun calculateBMI(height: Number?, weight: Number?): Float {
        if (height == null || weight == null) return 0f
        val heightInMeters = height.toFloat() / 100
        return weight.toFloat() / (heightInMeters * heightInMeters)
    }

    private fun parseHealthValue(value: Any?): Float {
        return when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: 0f
            else -> 0f
        }
    }

    private fun parseBloodPressure(bp: String?): Pair<Float, Float> {
        if (bp == null) return Pair(0f, 0f)
        val parts = bp.split("/")
        return if (parts.size == 2) {
            Pair(
                parts[0].toFloatOrNull() ?: 0f,
                parts[1].toFloatOrNull() ?: 0f
            )
        } else {
            Pair(0f, 0f)
        }
    }

    fun stopListeningForChanges() {
        firestoreListener?.remove()
    }
}