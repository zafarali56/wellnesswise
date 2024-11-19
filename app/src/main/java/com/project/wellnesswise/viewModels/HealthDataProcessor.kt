package com.project.wellnesswise.viewModels

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

data class ModelInput(
    val values: List<Float>,
    val labels: List<String>
)

class HealthDataProcessor {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var firestoreListener: ListenerRegistration? = null
    private val listeners = mutableSetOf<() -> Unit>()
    companion object {
        val riskCategories =
            listOf("Diabetes", "Cardiovascular Disease", "Hypertension", "Obesity", "Cancer")
        val inputLabels = listOf(
            "Age", "Height", "Weight", "BMI", "Systolic_BP", "Diastolic_BP", "Heart_Rate",
            "Blood_Sugar", "Cholesterol", "Triglycerides",
            "Waist_Circumference", "Smoking", "Alcohol_Consumption", "Physical_Activity",
            "Diet_Quality", "Sleep_Hours", "Air_Quality_Index", "Stress_Level",
            "Exposure_to_Pollutants", "Access_to_Healthcare", "Family_History_Diabetes",
            "Family_History_Heart_Disease", "Family_History_Cancer", "Previous_Surgeries",
            "Chronic_Conditions",
            "Gender_Female",
            "Gender_Male"
        )

        val featureMeans = mapOf(
            "Age" to 43.6f,
            "Height" to 171.6f,
            "Weight" to 78.6f,
            "BMI" to 26.76f,
            "Systolic_BP" to 132.6f,
            "Diastolic_BP" to 85.8f,
            "Heart_Rate" to 72.8f,
            "Blood_Sugar" to 121.0f,
            "Cholesterol" to 222.0f,
            "Triglycerides" to 160.0f,
            "Waist_Circumference" to 93.0f,
            "Smoking" to 0.6f,
            "Alcohol_Consumption" to 1.8f,
            "Physical_Activity" to 1.6f,
            "Diet_Quality" to 2.0f,
            "Sleep_Hours" to 6.6f,
            "Air_Quality_Index" to 92.0f,
            "Stress_Level" to 3.2f,
            "Exposure_to_Pollutants" to 1.6f,
            "Access_to_Healthcare" to 2.0f,
            "Family_History_Diabetes" to 0.6f,
            "Family_History_Heart_Disease" to 0.6f,
            "Family_History_Cancer" to 0.4f,
            "Previous_Surgeries" to 0.8f,
            "Chronic_Conditions" to 0.8f,
            "Gender_Female" to 0.6f,
            "Gender_Male" to 0.4f
        )

        val featureStds = mapOf(
            "Age" to 18.16039647144302f,
            "Height" to 8.677557259966656f,
            "Weight" to 7.402702209328699f,
            "BMI" to 2.841302518212377f,
            "Systolic_BP" to 13.55728586406586f,
            "Diastolic_BP" to 6.760177512462228f,
            "Heart_Rate" to 4.969909455915671f,
            "Blood_Sugar" to 18.16590212458495f,
            "Cholesterol" to 23.874672772626646f,
            "Triglycerides" to 30.822070014844883f,
            "Waist_Circumference" to 8.366600265340756f,
            "Smoking" to 0.5477225575051662f,
            "Alcohol_Consumption" to 0.8366600265340756f,
            "Physical_Activity" to 1.140175425099138f,
            "Diet_Quality" to 1.224744871391589f,
            "Sleep_Hours" to 1.51657508881031f,
            "Air_Quality_Index" to 37.68288736283355f,
            "Stress_Level" to 0.8366600265340756f,
            "Exposure_to_Pollutants" to 1.140175425099138f,
            "Access_to_Healthcare" to 1.224744871391589f,
            "Family_History_Diabetes" to 0.5477225575051662f,
            "Family_History_Heart_Disease" to 0.5477225575051662f,
            "Family_History_Cancer" to 0.5477225575051662f,
            "Previous_Surgeries" to 0.8366600265340756f,
            "Chronic_Conditions" to 0.8366600265340756f,
            "Gender_Female" to 0.5477225575051662f,
            "Gender_Male" to 0.5477225575051662f



        )


        private val relevantFields = setOf(
            "age", "height", "weight", "bloodPressure", "heartRate", "bloodSugar",
            "cholesterol", "triglycerides", "waistCircumference", "smoking",
            "alcoholConsumption", "physicalActivity", "dietQuality", "sleepHours",
            "airQualityIndex", "stressLevel", "exposureToPollutants", "accessToHealthcare",
            "familyDiabetes", "familyHeart", "familyCancer", "previousSurgeries",
            "chronicConditions", "gender"
        )
    }
    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }
        private fun notifyListeners() {
            listeners.forEach { it() }
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
                    val changedFields = snapshot.data?.filterKeys { it in relevantFields } ?: emptyMap()
                    if (changedFields.isNotEmpty()) {
                        notifyListeners()
                    }
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
                "Triglycerides" -> parseHealthValue(userData["triglycerides"])
                "Waist_Circumference" -> parseHealthValue(userData["waistCircumference"])
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

            // Normalize each feature individually
            val mean = featureMeans[label] ?: 0f
            val std = featureStds[label] ?: 1f
            val normalizedValue = (value - mean) / std
            normalizedValues.add(normalizedValue)

            Log.d("HealthDataProcessor", "Feature: $label, Raw: $value, Normalized: $normalizedValue")
        }

        Log.d("HealthDataProcessor", "Raw input viewModels: ${rawInputValues.zip(inputLabels)}")
        Log.d("HealthDataProcessor", "Normalized input viewModels: ${normalizedValues.zip(inputLabels)}")

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