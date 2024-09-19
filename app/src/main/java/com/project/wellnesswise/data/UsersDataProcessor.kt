package com.project.wellnesswise.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.pow

data class ModelInput(
    val values: List<Number>,
    val labels: List<String>
)

class HealthDataProcessor {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserHealthData(): ModelInput? {
        val userId = auth.currentUser?.uid ?: return null
        val docSnapshot = firestore.collection("users").document(userId).get().await()
        val userData = docSnapshot.data ?: return null

        val bmi = calculateBMI(userData["height"] as? Number, userData["weight"] as? Number)
        val (systolicBP, diastolicBP) = parseBloodPressure(userData["bloodPressure"] as? String)

        val inputValues = listOf(
            (userData["age"] as? Number)?.toInt() ?: 0,
            (userData["height"] as? Number)?.toFloat() ?: 0f,
            (userData["weight"] as? Number)?.toFloat() ?: 0f,
            bmi,
            systolicBP,
            diastolicBP,
            (userData["heartRate"] as? Number)?.toInt() ?: 0,
            (userData["bloodSugar"] as? Number)?.toFloat() ?: 0f,
            (userData["cholesterol"] as? Number)?.toFloat() ?: 0f,
            if (userData["smoking"] as? Boolean == true) 1 else 0,
            (userData["alcoholConsumption"] as? Number)?.toInt() ?: 0,
            (userData["physicalActivity"] as? Number)?.toInt() ?: 0,
            (userData["dietQuality"] as? Number)?.toInt() ?: 0,
            (userData["sleepHours"] as? Number)?.toInt() ?: 0,
            (userData["airQualityIndex"] as? Number)?.toInt() ?: 0,
            (userData["stressLevel"] as? Number)?.toInt() ?: 0,
            (userData["exposureToPollutants"] as? Number)?.toInt() ?: 0,
            (userData["accessToHealthcare"] as? Number)?.toInt() ?: 0,
            if (userData["familyDiabetes"] as? String == "Yes") 1 else 0,
            if (userData["familyHeart"] as? String == "Yes") 1 else 0,
            if (userData["familyCancer"] as? String == "Yes") 1 else 0,
            if (userData["previousSurgeries"] as? String == "Yes") 1 else 0,
            if (userData["chronicConditions"] as? String == "Yes") 1 else 0,
            if (userData["gender"] as? String == "FEMALE") 1 else 0,
            if (userData["gender"] as? String == "MALE") 1 else 0
        )

        val inputLabels = listOf(
            "Age", "Height", "Weight", "BMI", "Systolic BP", "Diastolic BP", "Heart Rate",
            "Blood Sugar", "Cholesterol", "Smoking", "Alcohol Consumption", "Physical Activity",
            "Diet Quality", "Sleep Hours", "Air Quality Index", "Stress Level",
            "Exposure to Pollutants", "Access to Healthcare", "Family History: Diabetes",
            "Family History: Heart Disease", "Family History: Cancer", "Previous Surgeries",
            "Chronic Conditions", "Gender: Female", "Gender: Male"
        )

        return ModelInput(inputValues, inputLabels)
    }

    private fun calculateBMI(height: Number?, weight: Number?): Float {
        if (height == null || weight == null) return 0f
        val heightInMeters = height.toFloat() / 100
        return weight.toFloat() / (heightInMeters.pow(2))
    }

    private fun parseBloodPressure(bp: String?): Pair<Int, Int> {
        if (bp == null) return Pair(0, 0)
        val parts = bp.split("/")
        return if (parts.size == 2) {
            Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
        } else {
            Pair(0, 0)
        }
    }
}