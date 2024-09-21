package com.project.wellnesswise.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlin.math.pow
import kotlin.math.sqrt

data class ModelInput(
    val values: List<Number>,
    val labels: List<String>
)
class HealthDataProcessor(private val onDataChanged: () -> Unit) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var firestoreListener: ListenerRegistration? = null

    companion object {
        private val listeners = mutableSetOf<() -> Unit>()
        val riskCategories = listOf("Diabetes", "Cardiovascular Disease", "Hypertension", "Obesity", "Cancer")
        fun addDataChangeListener(listener: () -> Unit) {
            listeners.add(listener)
        }

        fun removeDataChangeListener(listener: () -> Unit) {
            listeners.remove(listener)
        }

        fun notifyDataChanged() {
            listeners.forEach { it() }
        }
        val inputLabels = listOf(
            "Age", "Height", "Weight", "BMI", "Systolic_BP", "Diastolic_BP", "Heart_Rate",
            "Blood_Sugar", "Cholesterol", "Smoking", "Alcohol_Consumption", "Physical_Activity",
            "Diet_Quality", "Sleep_Hours", "Air_Quality_Index", "Stress_Level",
            "Exposure_to_Pollutants", "Access_to_Healthcare", "Family_History_Diabetes",
            "Family_History_Heart_Disease", "Family_History_Cancer", "Previous_Surgeries",
            "Chronic_Conditions", "Gender_Female", "Gender_Male"
        )
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

        val inputValues = listOf(
            (userData["age"] as? Number)?.toFloat() ?: 0f,
            (userData["height"] as? Number)?.toFloat() ?: 0f,
            (userData["weight"] as? Number)?.toFloat() ?: 0f,
            calculateBMI(userData["height"] as? Number, userData["weight"] as? Number),
            parseBloodPressure(userData["bloodPressure"] as? String).first,  // Systolic
            parseBloodPressure(userData["bloodPressure"] as? String).second, // Diastolic
            (userData["heartRate"] as? Number)?.toFloat() ?: 0f,
            (userData["bloodSugar"] as? Number)?.toFloat() ?: 0f,
            (userData["cholesterol"] as? Number)?.toFloat() ?: 0f,
            if (userData["smoking"] as? Boolean == true) 1f else 0f,
            (userData["alcoholConsumption"] as? Number)?.toFloat() ?: 0f,
            (userData["physicalActivity"] as? Number)?.toFloat() ?: 0f,
            (userData["dietQuality"] as? Number)?.toFloat() ?: 0f,
            (userData["sleepHours"] as? Number)?.toFloat() ?: 0f,
            (userData["airQualityIndex"] as? Number)?.toFloat() ?: 0f,
            (userData["stressLevel"] as? Number)?.toFloat() ?: 0f,
            (userData["exposureToPollutants"] as? Number)?.toFloat() ?: 0f,
            (userData["accessToHealthcare"] as? Number)?.toFloat() ?: 0f,
            if (userData["familyDiabetes"] as? String == "Yes") 1f else 0f,
            if (userData["familyHeart"] as? String == "Yes") 1f else 0f,
            if (userData["familyCancer"] as? String == "Yes") 1f else 0f,
            if (userData["previousSurgeries"] as? String == "Yes") 1f else 0f,
            if (userData["chronicConditions"] as? String == "Yes") 1f else 0f,
            if (userData["gender"] as? String == "FEMALE") 1f else 0f,
            if (userData["gender"] as? String == "MALE") 1f else 0f
        )

        Log.d("HealthDataProcessor", "Raw input data: ${inputValues.zip(inputLabels)}")
        val normalizedInput = normalizeInput(inputValues)
        Log.d("HealthDataProcessor", "Normalized input: ${normalizedInput.zip(inputLabels)}")

        return ModelInput(normalizedInput, inputLabels)
    }

    private fun normalizeInput(input: List<Float>): List<Float> {
        val mean = input.average().toFloat()
        val std = sqrt(input.map { (it - mean).pow(2) }.average()).toFloat()
        return input.map { (it - mean) / (std + 1e-8f) }
    }

    val featureRanges = mapOf(
        "Age" to Pair(18f, 100f),
        "Height" to Pair(140f, 220f),
        "Weight" to Pair(40f, 200f),
        "BMI" to Pair(15f, 50f),
        "Systolic_BP" to Pair(80f, 200f),
        "Diastolic_BP" to Pair(50f, 130f),
        "Heart_Rate" to Pair(40f, 120f),
        "Blood_Sugar" to Pair(70f, 300f),
        "Cholesterol" to Pair(100f, 300f),
        "Smoking" to Pair(0f, 3f),
        "Alcohol_Consumption" to Pair(0f, 4f),
        "Physical_Activity" to Pair(0f, 4f),
        "Diet_Quality" to Pair(0f, 4f),
        "Sleep_Hours" to Pair(4f, 12f),
        "Air_Quality_Index" to Pair(0f, 500f),
        "Stress_Level" to Pair(0f, 4f),
        "Exposure_to_Pollutants" to Pair(0f, 3f),
        "Access_to_Healthcare" to Pair(0f, 4f),
        "Family_History_Diabetes" to Pair(0f, 1f),
        "Family_History_Heart_Disease" to Pair(0f, 1f),
        "Family_History_Cancer" to Pair(0f, 1f),
        "Previous_Surgeries" to Pair(0f, 3f),
        "Chronic_Conditions" to Pair(0f, 3f),
        "Gender_Female" to Pair(0f, 1f),
        "Gender_Male" to Pair(0f, 1f)
    )



    fun stopListeningForChanges() {
        firestoreListener?.remove()
    }

    private fun calculateBMI(height: Number?, weight: Number?): Float {
        if (height == null || weight == null) return 0f
        val heightInMeters = height.toFloat() / 100
        return weight.toFloat() / (heightInMeters * heightInMeters)
    }

    private fun parseBloodPressure(bp: String?): Pair<Float, Float> {
        if (bp == null) return Pair(0f, 0f)
        val parts = bp.split("/")
        return if (parts.size == 2) {
            Pair(parts[0].toFloatOrNull() ?: 0f, parts[1].toFloatOrNull() ?: 0f)
        } else {
            Pair(0f, 0f)
        }
    }

}