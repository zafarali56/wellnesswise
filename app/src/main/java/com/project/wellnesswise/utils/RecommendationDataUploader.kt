package com.project.wellnesswise.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RecommendationDataUploader {

    suspend fun uploadAllRecommendations(firestore: FirebaseFirestore) {
        val recommendationsCollection = firestore.collection("recommendationTemplates")

        val batch = firestore.batch()

        // Diabetes recommendations
        val diabetesDoc = recommendationsCollection.document("Diabetes")
        batch.set(diabetesDoc, mapOf(
            "category" to "Diabetes",
            "thresholds" to listOf(
                mapOf(
                    "min" to 0.0,
                    "max" to 0.2,
                    "message" to """
                        Excellent! Your Diabetes risk is stable. Your healthy lifestyle choices are paying off. 
                        Continue with:
                        - Diet: Focus on a balanced diet rich in whole grains, lean proteins, and healthy fats. Include plenty of non-starchy vegetables like spinach, broccoli, and peppers. Limit refined sugars and processed foods.
                        - Exercise: Aim for at least 150 minutes of moderate aerobic activity per week, such as brisk walking or cycling. Include strength training exercises twice a week.
                        - Monitoring: Regularly check your blood sugar levels and maintain a healthy weight.
                        - Hydration: Drink plenty of water and avoid sugary beverages.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to """
                        Your Diabetes risk is mild. While you're on the right track, consider fine-tuning your habits:
                        - Diet: Reduce intake of refined sugars and increase fiber intake. Include more legumes, nuts, and seeds in your diet.
                        - Exercise: Incorporate more physical activity into your daily routine. Try activities like swimming or yoga.
                        - Monitoring: Keep a food diary to track your carbohydrate intake and monitor your blood sugar levels regularly.
                        - Stress Management: Practice stress-reducing techniques such as meditation or deep breathing exercises.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to """
                        Your Diabetes risk is moderate. Take proactive steps:
                        - Diet: Consult a nutritionist for a personalized meal plan. Focus on low-glycemic index foods like oats, lentils, and non-starchy vegetables.
                        - Exercise: Aim for at least 30 minutes of moderate exercise five times a week. Consider activities like jogging or dancing.
                        - Monitoring: Schedule regular check-ups with your healthcare provider and monitor your blood sugar levels closely.
                        - Medication: Discuss with your doctor if medication is necessary to manage your blood sugar levels.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to """
                        Your Diabetes risk is severe. Immediate action is needed:
                        - Diet: Follow a strict low-carb diet. Avoid sugary snacks and beverages. Focus on high-fiber foods and lean proteins.
                        - Exercise: Engage in daily physical activity. Consider supervised exercise programs if necessary.
                        - Monitoring: Regularly monitor your blood sugar levels and keep a detailed log for your doctor.
                        - Medication: Consult your doctor for a comprehensive diabetes management plan, including medication and insulin therapy if needed.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to """
                        Your Diabetes risk is critical. Urgent medical attention is required:
                        - Diet: Strictly adhere to a diabetic meal plan. Avoid all forms of sugar and refined carbohydrates.
                        - Exercise: Engage in light to moderate exercise as recommended by your healthcare provider.
                        - Monitoring: Monitor your blood sugar levels multiple times a day and keep a detailed log.
                        - Medication: Follow your doctor's advice on medication and insulin therapy strictly.
                    """.trimIndent()
                )
            )
        ))

        // Cardiovascular recommendations
        val cardiovascularDoc = recommendationsCollection.document("Cardiovascular")
        batch.set(cardiovascularDoc, mapOf(
            "category" to "cardiovascular Disease",
            "thresholds" to listOf(
                mapOf(
                    "min" to 0.0,
                    "max" to 0.2,
                    "message" to """
                        Excellent! Your Cardiovascular health is in great shape. Maintain your heart-healthy practices:
                        - Diet: Continue with a diet rich in fruits, vegetables, whole grains, and lean proteins. Include omega-3 fatty acids from sources like salmon and flaxseeds.
                        - Exercise: Aim for at least 150 minutes of moderate aerobic activity per week, such as brisk walking or cycling.
                        - Monitoring: Regularly check your blood pressure and cholesterol levels.
                        - Stress Management: Practice stress-reducing techniques such as meditation or yoga.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to """
                        Your Cardiovascular risk is mild. Optimize your heart health:
                        - Diet: Reduce intake of saturated fats and sodium. Include more nuts, seeds, and whole grains in your diet.
                        - Exercise: Incorporate strength training exercises twice a week along with aerobic activities.
                        - Monitoring: Keep track of your blood pressure and cholesterol levels regularly.
                        - Stress Management: Engage in activities that reduce stress, such as reading or gardening.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to """
                        Your Cardiovascular risk is moderate. Take preventive measures:
                        - Diet: Follow a heart-healthy diet plan. Limit red meat and processed foods. Include more plant-based proteins.
                        - Exercise: Aim for at least 30 minutes of moderate exercise five times a week. Consider activities like swimming or dancing.
                        - Monitoring: Schedule regular check-ups with your healthcare provider and monitor your blood pressure and cholesterol levels closely.
                        - Medication: Discuss with your doctor if medication is necessary to manage your cardiovascular health.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to """
                        Your Cardiovascular risk is severe. Immediate action is needed:
                        - Diet: Follow a strict heart-healthy diet. Avoid trans fats and limit sodium intake. Focus on fruits, vegetables, and whole grains.
                        - Exercise: Engage in daily physical activity. Consider supervised exercise programs if necessary.
                        - Monitoring: Regularly monitor your blood pressure and cholesterol levels and keep a detailed log for your doctor.
                        - Medication: Consult your doctor for a comprehensive cardiovascular management plan, including medication if needed.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to """
                        Your Cardiovascular risk is critical. Urgent medical attention is required:
                        - Diet: Strictly adhere to a heart-healthy meal plan. Avoid all forms of trans fats and limit saturated fats.
                        - Exercise: Engage in light to moderate exercise as recommended by your healthcare provider.
                        - Monitoring: Monitor your blood pressure and cholesterol levels multiple times a day and keep a detailed log.
                        - Medication: Follow your doctor's advice on medication strictly.
                    """.trimIndent()
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
                    "message" to """
                        Excellent! Your Hypertension risk is stable. Maintain your healthy habits:
                        - Diet: Continue with a low-sodium diet rich in fruits, vegetables, and whole grains. Include potassium-rich foods like bananas and sweet potatoes.
                        - Exercise: Aim for at least 150 minutes of moderate aerobic activity per week, such as brisk walking or cycling.
                        - Monitoring: Regularly check your blood pressure and maintain a healthy weight.
                        - Stress Management: Practice stress-reducing techniques such as meditation or deep breathing exercises.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to """
                        Your Hypertension risk is mild. Optimize your habits:
                        - Diet: Reduce sodium intake further and increase potassium-rich foods. Avoid processed foods and limit alcohol consumption.
                        - Exercise: Incorporate more physical activity into your daily routine. Try activities like swimming or yoga.
                        - Monitoring: Keep a food diary to track your sodium intake and monitor your blood pressure regularly.
                        - Stress Management: Engage in activities that reduce stress, such as reading or gardening.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to """
                        Your Hypertension risk is moderate. Take proactive steps:
                        - Diet: Consult a nutritionist for a personalized meal plan. Focus on low-sodium foods and include more fruits and vegetables.
                        - Exercise: Aim for at least 30 minutes of moderate exercise five times a week. Consider activities like jogging or dancing.
                        - Monitoring: Schedule regular check-ups with your healthcare provider and monitor your blood pressure closely.
                        - Medication: Discuss with your doctor if medication is necessary to manage your blood pressure.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to """
                        Your Hypertension risk is severe. Immediate action is needed:
                        - Diet: Follow a strict low-sodium diet. Avoid all forms of processed foods and limit alcohol consumption.
                        - Exercise: Engage in daily physical activity. Consider supervised exercise programs if necessary.
                        - Monitoring: Regularly monitor your blood pressure and keep a detailed log for your doctor.
                        - Medication: Consult your doctor for a comprehensive hypertension management plan, including medication if needed.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to """
                        Your Hypertension risk is critical. Urgent medical attention is required:
                        - Diet: Strictly adhere to a low-sodium meal plan. Avoid all forms of processed foods and limit alcohol consumption.
                        - Exercise: Engage in light to moderate exercise as recommended by your healthcare provider.
                        - Monitoring: Monitor your blood pressure multiple times a day and keep a detailed log.
                        - Medication: Follow your doctor's advice on medication strictly.
                    """.trimIndent()
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
                    "message" to """
                        Excellent! Your Obesity management is on track. Maintain your healthy habits:
                        - Diet: Continue with a balanced diet rich in fruits, vegetables, whole grains, and lean proteins. Avoid sugary snacks and beverages.
                        - Exercise: Aim for at least 150 minutes of moderate aerobic activity per week, such as brisk walking or cycling.
                        - Monitoring: Regularly check your weight and maintain a healthy BMI.
                        - Hydration: Drink plenty of water and avoid sugary beverages.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to """
                        Your Obesity risk is mild. Optimize your habits:
                        - Diet: Focus on portion control and include more fiber-rich foods. Avoid processed foods and limit high-calorie snacks.
                        - Exercise: Incorporate more physical activity into your daily routine. Try activities like swimming or yoga.
                        - Monitoring: Keep a food diary to track your calorie intake and monitor your weight regularly.
                        - Stress Management: Practice stress-reducing techniques such as meditation or deep breathing exercises.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to """
                        Your Obesity risk is moderate. Take proactive steps:
                        - Diet: Consult a nutritionist for a personalized meal plan. Focus on low-calorie, nutrient-dense foods.
                        - Exercise: Aim for at least 30 minutes of moderate exercise five times a week. Consider activities like jogging or dancing.
                        - Monitoring: Schedule regular check-ups with your healthcare provider and monitor your weight closely.
                        - Medication: Discuss with your doctor if medication is necessary to manage your weight.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to """
                        Your Obesity risk is severe. Immediate action is needed:
                        - Diet: Follow a strict low-calorie diet. Avoid all forms of processed foods and limit high-calorie snacks.
                        - Exercise: Engage in daily physical activity. Consider supervised exercise programs if necessary.
                        - Monitoring: Regularly monitor your weight and keep a detailed log for your doctor.
                        - Medication: Consult your doctor for a comprehensive weight management plan, including medication if needed.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to """
                        Your Obesity risk is critical. Urgent medical attention is required:
                        - Diet: Strictly adhere to a low-calorie meal plan. Avoid all forms of processed foods and limit high-calorie snacks.
                        - Exercise: Engage in light to moderate exercise as recommended by your healthcare provider.
                        - Monitoring: Monitor your weight multiple times a day and keep a detailed log.
                        - Medication: Follow your doctor's advice on medication strictly.
                    """.trimIndent()
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
                    "message" to """
                        Excellent! Your Cancer risk is stable. Maintain your healthy habits:
                        - Diet: Continue with a diet rich in fruits, vegetables, whole grains, and lean proteins. Include antioxidants from sources like berries and green tea.
                        - Exercise: Aim for at least 150 minutes of moderate aerobic activity per week, such as brisk walking or cycling.
                        - Monitoring: Stay up-to-date with recommended cancer screenings and maintain a healthy weight.
                        - Stress Management: Practice stress-reducing techniques such as meditation or yoga.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.2,
                    "max" to 0.4,
                    "message" to """
                        Your Cancer risk is mild. Optimize your habits:
                        - Diet: Reduce intake of processed meats and include more plant-based foods. Avoid sugary snacks and beverages.
                        - Exercise: Incorporate more physical activity into your daily routine. Try activities like swimming or yoga.
                        - Monitoring: Keep track of your weight and stay up-to-date with recommended cancer screenings.
                        - Stress Management: Engage in activities that reduce stress, such as reading or gardening.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.4,
                    "max" to 0.6,
                    "message" to """
                        Your Cancer risk is moderate. Take preventive measures:
                        - Diet: Follow a cancer-preventive diet plan. Limit red meat and processed foods. Include more cruciferous vegetables like broccoli and cauliflower.
                        - Exercise: Aim for at least 30 minutes of moderate exercise five times a week. Consider activities like jogging or dancing.
                        - Monitoring: Schedule regular check-ups with your healthcare provider and stay up-to-date with recommended cancer screenings.
                        - Medication: Discuss with your doctor if any preventive medications are necessary.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.6,
                    "max" to 0.8,
                    "message" to """
                        Your Cancer risk is severe. Immediate action is needed:
                        - Diet: Follow a strict cancer-preventive diet. Avoid all forms of processed meats and limit red meat consumption.
                        - Exercise: Engage in daily physical activity. Consider supervised exercise programs if necessary.
                        - Monitoring: Regularly monitor your health and stay up-to-date with recommended cancer screenings.
                        - Medication: Consult your doctor for a comprehensive cancer prevention plan, including medication if needed.
                    """.trimIndent()
                ),
                mapOf(
                    "min" to 0.8,
                    "max" to 1.0,
                    "message" to """
                        Your Cancer risk is critical. Urgent medical attention is required:
                        - Diet: Strictly adhere to a cancer-preventive meal plan. Avoid all forms of processed meats and limit red meat consumption.
                        - Exercise: Engage in light to moderate exercise as recommended by your healthcare provider.
                        - Monitoring: Monitor your health closely and stay up-to-date with recommended cancer screenings.
                        - Medication: Follow your doctor's advice on preventive medications strictly.
                    """.trimIndent()
                )
            )
        ))
        val summaryDoc = recommendationsCollection.document("summaryTemplates")
        batch.set(summaryDoc, mapOf(
            "highRisk" to "Important health alert: Due to elevated risk of {conditions}, please schedule a comprehensive health check-up with your healthcare provider as soon as possible. They can help create a personalized plan to address these specific health concerns and monitor your progress regularly.",
            "lowRisk" to "Great job! You're maintaining healthy levels for: {conditions}. Your lifestyle choices are positively impacting multiple aspects of your health. Keep up these excellent habits while staying proactive with regular check-ups and screenings."
        ))

        batch.commit().await()
    }
}