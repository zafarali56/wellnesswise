package com.project.wellnesswise.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RecommendationDataUploader {

    suspend fun uploadAllRecommendations(firestore: FirebaseFirestore) {
        val recommendationsCollection = firestore.collection("recommendationTemplates")

        // Create a batch for atomic operation
        val batch = firestore.batch()

        // Diabetes recommendations
        val diabetesDoc = recommendationsCollection.document("Diabetes")
        batch.set(diabetesDoc, mapOf(
            "category" to "Diabetes",
            "thresholds" to listOf(
                mapOf(
                    "min" to 0.0,
                    "max" to 0.2,
                    "message" to "Excellent! Your diabetes risk is stable. Your healthy lifestyle choices are paying off - keep maintaining a balanced diet and regular exercise routine. This is a great foundation for long-term health."
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to "Your diabetes risk is mild. While you're on the right track, consider fine-tuning your diet by reducing refined sugars and increasing fiber intake. Your current habits are already helping you maintain good health."
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to "Your diabetes risk is moderate. Schedule a diabetes screening with your healthcare provider. Consider consulting a nutritionist for a personalized meal plan and increase your physical activity."
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to "Your diabetes risk is severe. Consult your doctor soon for a comprehensive diabetes assessment and management plan. Focus on immediate lifestyle changes including diet, exercise, and possibly medication."
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to "Your diabetes risk is critical. Consult your doctor immediately for an urgent diabetes assessment and management plan. Immediate attention to lifestyle changes and medical intervention may be necessary."
                )
            )
        ))

        // Cardiovascular recommendations
        val cardiovascularDoc = recommendationsCollection.document("Cardiovascular Disease")
        batch.set(cardiovascularDoc, mapOf(
            "category" to "Cardiovascular Disease",
            "thresholds" to listOf(
                mapOf(
                    "min" to 0.0,
                    "max" to 0.2,
                    "message" to "Excellent! Your cardiovascular health is in great shape. Your heart-healthy practices are working well - continue with your balanced diet rich in fruits, vegetables, and whole grains, and maintain your exercise routine."
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to "Your cardiovascular risk is mild. Your heart-healthy habits are showing positive results. Consider regular check-ups and maintain your current lifestyle while looking for ways to further improve."
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to "Your cardiovascular risk is moderate. Schedule a comprehensive cardiovascular health check-up. Consider discussing preventive measures, including lifestyle changes and possibly medication, with your doctor."
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to "Your cardiovascular risk is severe. Consult a doctor soon for a thorough evaluation and personalized heart health plan. Immediate attention to lifestyle changes may be necessary."
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to "Your cardiovascular risk is critical. Consult a cardiologist immediately for a thorough evaluation and personalized heart health plan. This requires urgent attention to lifestyle changes and possible medical intervention."
                )
            )
        ))

        // Hypertension recommendations
        val hypertensionDoc = recommendationsCollection.document("Hypertension")
        batch.set(hypertensionDoc, mapOf(
            "category" to "Hypertension",
            "thresholds" to listOf(
                mapOf(
                    "min" to 0.0,
                    "max" to 0.2,
                    "message" to "Excellent! Your blood pressure risk is stable. Your current lifestyle choices are helping maintain healthy blood pressure levels. Keep up with your low-sodium diet and regular exercise routine."
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to "Your blood pressure risk is mild. Your current habits are working well. Consider optimizing your diet further by increasing potassium-rich foods while maintaining your low-sodium approach."
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to "Your hypertension risk is moderate. Monitor your blood pressure regularly at home. Discuss lifestyle modifications with your doctor and consider medication if recommended."
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to "Your hypertension risk is severe. Consult your healthcare provider soon for a thorough blood pressure assessment and management plan. Immediate lifestyle changes may be necessary."
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to "Your hypertension risk is critical. Seek immediate medical attention for your blood pressure management. Urgent lifestyle changes and medical intervention may be necessary."
                )
            )
        ))

        // Obesity recommendations
        val obesityDoc = recommendationsCollection.document("Obesity")
        batch.set(obesityDoc, mapOf(
            "category" to "Obesity",
            "thresholds" to listOf(
                mapOf(
                    "min" to 0.0,
                    "max" to 0.2,
                    "message" to "Excellent! Your weight management is on track. Your balanced approach to diet and exercise is working well - keep maintaining these healthy habits for continued success."
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to "Your weight-related risk is mild. Your current lifestyle choices are showing positive results. Continue focusing on portion control and regular physical activity while looking for ways to optimize further."
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to "Your obesity risk is moderate. Consider consulting a nutritionist for a personalized meal plan. Aim for at least 150 minutes of moderate exercise per week."
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to "Your obesity risk is severe. Consult a healthcare professional soon for a comprehensive weight management plan. This may include specific dietary changes and structured physical activity."
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to "Your obesity risk is critical. Seek immediate professional help for weight management. This requires urgent attention to diet, exercise, and possible medical interventions."
                )
            )
        ))

        // Cancer recommendations
        val cancerDoc = recommendationsCollection.document("Cancer")
        batch.set(cancerDoc, mapOf(
            "category" to "Cancer",
            "thresholds" to listOf(
                mapOf(
                    "min" to 0.0,
                    "max" to 0.2,
                    "message" to "Excellent! Your cancer risk is stable. Your healthy lifestyle choices are helping maintain low risk levels. Continue with age-appropriate screenings and maintain your healthy habits."
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to "Your cancer risk is mild. Your current lifestyle choices are showing positive results. Stay up-to-date with recommended screenings while maintaining your healthy habits."
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to "Your cancer risk is moderate. Schedule a check-up with your doctor to discuss risk factors and preventive strategies. This may include more frequent screenings and lifestyle modifications."
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to "Your cancer risk is severe. Consult a doctor soon for a thorough risk assessment and prevention plan. This may include genetic testing and frequent screenings."
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to "Your cancer risk is critical. Consult an oncologist immediately for a comprehensive risk assessment and prevention plan. This requires urgent attention to screening and possible interventions."
                )
            )
        ))

        // Summary templates
        val summaryDoc = recommendationsCollection.document("summaryTemplates")
        batch.set(summaryDoc, mapOf(
            "highRisk" to "Important health alert: Due to elevated risk of {conditions}, please schedule a comprehensive health check-up with your healthcare provider as soon as possible. They can help create a personalized plan to address these specific health concerns and monitor your progress regularly.",
            "lowRisk" to "Great job! You're maintaining healthy levels for: {conditions}. Your lifestyle choices are positively impacting multiple aspects of your health. Keep up these excellent habits while staying proactive with regular check-ups and screenings."
        ))

        // Commit the batch
        batch.commit().await()
    }
}