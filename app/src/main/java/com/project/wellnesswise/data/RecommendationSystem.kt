import kotlin.math.pow

class HealthRecommendationSystem {
    fun generateRecommendations(userData: Map<String, Any>, predictions: List<Triple<String, Float, String>>): List<String> {
        val recommendations = mutableListOf<String>()

        // General lifestyle recommendations
        recommendations.addAll(generateLifestyleRecommendations(userData))

        predictions.forEach { (category, risk, _) ->
            when (category) {
                "Diabetes" -> recommendations.add(generateDiabetesRecommendations(risk, userData))
                "Cardiovascular Disease" -> recommendations.add(generateCardiovascularRecommendations(risk, userData))
                "Hypertension" -> recommendations.add(generateHypertensionRecommendations(risk, userData))
                "Obesity" -> recommendations.add(generateObesityRecommendations(risk, userData))
                "Cancer" -> recommendations.add(generateCancerRecommendations(risk, userData))
            }
        }

        recommendations.add(generateEnvironmentalRecommendations(userData))

        recommendations.add(generateMentalHealthRecommendations(userData))

        return recommendations
    }

    private fun generateLifestyleRecommendations(userData: Map<String, Any>): List<String> {
        val recommendations = mutableListOf<String>()

        // Smoking
        when (userData["smoking"] as? Boolean) {
            true -> recommendations.add("Consider quitting smoking or seek support to reduce tobacco use. This can significantly improve your overall health and reduce risks of various diseases.")
            false -> recommendations.add("Great job on not smoking! Continue to avoid tobacco products to maintain your health.")
            null -> recommendations.add("If you smoke, consider quitting. If you don't, continue to avoid tobacco products for optimal health.")
        }

        // Alcohol consumption
        when (userData["alcoholConsumption"] as? Int ?: 0) {
            0 -> recommendations.add("Maintaining abstinence from alcohol is beneficial for your health. Keep it up!")
            in 1..2 -> recommendations.add("Your moderate alcohol consumption is within recommended limits. Remember, less is always better for your health.")
            in 3..4 -> recommendations.add("Consider reducing your alcohol intake to no more than 1-2 drinks per day. This can improve your overall health and reduce risks of various diseases.")
            else -> recommendations.add("Your alcohol consumption is high. It's strongly recommended to reduce your intake significantly or consider quitting. Consult a healthcare professional for support if needed.")
        }

        // Physical activity
        when (userData["physicalActivity"] as? Int ?: 0) {
            0 -> recommendations.add("Start incorporating physical activity into your daily routine. Begin with short walks and gradually increase duration and intensity.")
            in 1..2 -> recommendations.add("Aim to increase your physical activity. Try to achieve at least 150 minutes of moderate-intensity or 75 minutes of vigorous-intensity aerobic activity per week.")
            3 -> recommendations.add("Good job on maintaining regular physical activity. Consider adding variety to your exercises and gradually increasing intensity for optimal health benefits.")
            4 -> recommendations.add("Excellent work on maintaining a high level of physical activity. Keep up the good work and ensure you're also including strength training exercises in your routine.")
        }

        // Diet quality
        when (userData["dietQuality"] as? Int ?: 0) {
            0, 1 -> recommendations.add("Focus on improving your diet. Incorporate more fruits, vegetables, whole grains, and lean proteins. Reduce processed foods and sugary drinks.")
            2 -> recommendations.add("Your diet is on the right track. Continue to increase your intake of fruits, vegetables, and whole grains while reducing processed foods.")
            3 -> recommendations.add("You're maintaining a good diet. Consider fine-tuning it by ensuring a variety of nutrient-rich foods and staying hydrated.")
            4 -> recommendations.add("Excellent dietary habits! Maintain your balanced diet and consider consulting a nutritionist for further optimization if desired.")
        }

        // Sleep hours
        when (userData["sleepHours"] as? Int ?: 0) {
            in 0..5 -> recommendations.add("You're not getting enough sleep. Aim for 7-9 hours of sleep per night. Establish a consistent sleep schedule and create a relaxing bedtime routine.")
            6 -> recommendations.add("You're close to the recommended amount of sleep. Try to increase your sleep duration by 30-60 minutes for optimal health benefits.")
            in 7..9 -> recommendations.add("Great job on maintaining a healthy sleep schedule. Continue prioritizing your sleep for optimal health and well-being.")
            in 10..24 -> recommendations.add("You might be oversleeping. While sleep is crucial, too much can be detrimental. Try to adjust your sleep schedule to 7-9 hours per night.")
        }

        // Combination recommendations
        val alcoholConsumption = userData["alcoholConsumption"] as? Int ?: 0
        val physicalActivity = userData["physicalActivity"] as? Int ?: 0
        val dietQuality = userData["dietQuality"] as? Int ?: 0
        val sleepHours = userData["sleepHours"] as? Int ?: 0

        if (alcoholConsumption > 2 && physicalActivity < 2) {
            recommendations.add("Consider reducing alcohol intake and increasing physical activity. This combination can significantly improve your overall health and reduce disease risks.")
        }

        if (dietQuality < 2 && physicalActivity < 2) {
            recommendations.add("Improving both your diet and physical activity levels can have synergistic health benefits. Start with small, sustainable changes in both areas.")
        }

        if (sleepHours < 6 && (alcoholConsumption > 2 || dietQuality < 2)) {
            recommendations.add("Poor sleep combined with high alcohol intake or poor diet can negatively impact your health. Focus on improving your sleep habits and consider how your diet or alcohol consumption might be affecting your sleep quality.")
        }

        if (physicalActivity > 2 && dietQuality < 2) {
            recommendations.add("While your physical activity level is good, pairing it with an improved diet can enhance your overall health and fitness results.")
        }

        if (sleepHours > 9 && physicalActivity < 2) {
            recommendations.add("Excessive sleep coupled with low physical activity may indicate underlying health issues. Consider reducing sleep slightly and increasing physical activity for better overall health.")
        }

        return recommendations
    }


    private fun generateDiabetesRecommendations(risk: Float, userData: Map<String, Any>): String {
        val age = userData["age"] as? Int ?: 0
        val bmi = calculateBMI(userData)

        return when {
            risk < 0.2f -> "Your diabetes risk is low. Maintain a healthy lifestyle with regular exercise and a balanced diet to keep it that way."
            risk < 0.4f -> {
                when {
                    age > 45 -> "Consider getting your blood sugar levels checked annually, as age increases diabetes risk. Focus on a diet low in refined sugars and high in fiber."
                    bmi > 25 -> "Your BMI suggests increased diabetes risk. Consider weight management strategies and get your blood sugar checked regularly."
                    else -> "While your diabetes risk is moderate, maintaining a healthy diet and regular exercise can help prevent its onset."
                }
            }
            risk < 0.6f -> "Your diabetes risk is elevated. Schedule a diabetes screening with your healthcare provider. Consider consulting a nutritionist for a personalized meal plan and increase your physical activity."
            else -> "Your diabetes risk is high. Consult your doctor immediately for a comprehensive diabetes assessment and management plan. Focus on lifestyle changes including diet, exercise, and possibly medication."
        }
    }

    private fun generateCardiovascularRecommendations(risk: Float, userData: Map<String, Any>): String {
        val age = userData["age"] as? Int ?: 0
        val cholesterol = userData["cholesterol"] as? Int ?: 0

        return when {
            risk < 0.2f -> "Your cardiovascular risk is low. Continue heart-healthy practices like regular exercise and a balanced diet rich in fruits, vegetables, and whole grains."
            risk < 0.4f -> {
                when {
                    age > 40 -> "Consider getting your cholesterol and blood pressure checked regularly. Increase cardiovascular exercises in your routine and maintain a heart-healthy diet."
                    cholesterol > 200 -> "Your cholesterol levels are elevated. Focus on a diet low in saturated fats and high in fiber. Consider discussing cholesterol management with your doctor."
                    else -> "While your cardiovascular risk is moderate, regular exercise and a heart-healthy diet can help maintain good heart health."
                }
            }
            risk < 0.6f -> "Your cardiovascular risk is concerning. Schedule a comprehensive cardiovascular health check-up. Consider discussing preventive measures, including lifestyle changes and possibly medication, with your doctor."
            else -> "Your cardiovascular risk is high. Consult a cardiologist for a thorough evaluation and personalized heart health plan. This may include lifestyle changes, medication, and regular monitoring."
        }
    }

    private fun generateHypertensionRecommendations(risk: Float, userData: Map<String, Any>): String {
        val bloodPressure = userData["bloodPressure"] as? String ?: ""
        val (systolic, diastolic) = bloodPressure.split("/").map { it.toIntOrNull() ?: 0 }

        return when {
            risk < 0.2f -> "Your hypertension risk is low. Continue monitoring your blood pressure periodically and maintain a low-sodium diet."
            risk < 0.4f -> {
                when {
                    systolic in 120..129 && diastolic < 80 -> "Your blood pressure is elevated. Focus on reducing sodium intake, increasing potassium-rich foods in your diet, and regular exercise."
                    else -> "Consider reducing sodium intake and increasing potassium-rich foods in your diet. Regular exercise and stress management can also help control blood pressure."
                }
            }
            risk < 0.6f -> "Your hypertension risk is significant. Monitor your blood pressure regularly at home. Discuss lifestyle modifications with your doctor and consider medication if recommended."
            else -> "Your hypertension risk is high. Consult your healthcare provider for a thorough blood pressure assessment and management plan. This may include lifestyle changes, medication, and regular monitoring."
        }
    }

    private fun generateObesityRecommendations(risk: Float, userData: Map<String, Any>): String {
        val bmi = calculateBMI(userData)

        return when {
            risk < 0.2f -> "Your obesity risk is low. Maintain a balanced diet and regular exercise routine to keep a healthy weight."
            risk < 0.4f -> {
                when {
                    bmi in 25.0..29.9 -> "You're in the overweight range. Focus on portion control and increase your daily physical activity to achieve a healthier weight."
                    else -> "While your obesity risk is moderate, maintaining a balanced diet and regular exercise can help prevent weight gain."
                }
            }
            risk < 0.6f -> "Your obesity risk is significant. Consider consulting a nutritionist for a personalized meal plan. Aim for at least 150 minutes of moderate exercise per week and focus on both diet and physical activity for weight management."
            else -> "Your obesity risk is high. Consult a healthcare professional for a comprehensive weight management plan. This may include dietary changes, increased physical activity, and possibly medical interventions."
        }
    }

    private fun generateCancerRecommendations(risk: Float, userData: Map<String, Any>): String {
        val familyCancer = userData["familyCancer"] as? String == "Yes"
        val smoking = userData["smoking"] as? Boolean ?: false

        return when {
            risk < 0.2f -> "Your cancer risk is low. Continue with regular cancer screenings as recommended for your age and gender. Maintain a healthy lifestyle to keep your risk low."
            risk < 0.4f -> {
                when {
                    familyCancer -> "Due to your family history, ensure you're up to date with all recommended cancer screenings. Discuss your family history with your doctor for personalized prevention strategies."
                    smoking -> "Quitting smoking is one of the best ways to reduce your cancer risk. Consider reducing processed food intake and increasing fruits and vegetables in your diet."
                    else -> "Ensure you're up to date with all recommended cancer screenings. Consider reducing processed food intake and increasing your consumption of fruits, vegetables, and whole grains."
                }
            }
            risk < 0.6f -> "Your cancer risk is elevated. Schedule a check-up with your doctor to discuss your risk factors and preventive strategies. This may include more frequent screenings and lifestyle modifications."
            else -> "Your cancer risk is high. Consult an oncologist for a thorough risk assessment and personalized prevention plan. This may include genetic testing, frequent screenings, and significant lifestyle changes."
        }
    }

    private fun generateEnvironmentalRecommendations(userData: Map<String, Any>): String {
        val airQualityIndex = userData["airQualityIndex"] as? Int ?: 0
        val exposureToPollutants = userData["exposureToPollutants"] as? Int ?: 0

        return when {
            airQualityIndex > 100 || exposureToPollutants > 2 ->
                "Your environmental exposure is concerning. Consider using air purifiers at home, wearing masks when outdoor air quality is poor, and minimizing exposure to pollutants. Check local air quality reports regularly."
            airQualityIndex in 51..100 || exposureToPollutants == 2 ->
                "Your environmental exposure is moderate. On days with poorer air quality, consider reducing outdoor activities. Ensure good ventilation in your living spaces."
            else ->
                "Your environmental exposure seems low. Continue to stay informed about local air quality and take precautions on days when pollution levels are higher."
        }
    }

    private fun generateMentalHealthRecommendations(userData: Map<String, Any>): String {
        val stressLevel = userData["stressLevel"] as? Int ?: 0

        return when (stressLevel) {
            0, 1 -> "Your stress levels appear low. Continue practicing good mental health habits like regular exercise, adequate sleep, and engaging in activities you enjoy."
            2 -> "You're experiencing moderate stress. Consider incorporating stress-reduction techniques like meditation, deep breathing exercises, or yoga into your daily routine."
            3 -> "Your stress levels are high. Prioritize stress management through regular exercise, adequate sleep, and relaxation techniques. Consider talking to a mental health professional for additional support."
            4 -> "You're experiencing very high stress levels. It's crucial to address this. Consider seeking professional help from a mental health expert. In the meantime, focus on stress-reduction techniques, ensure adequate sleep, and engage in regular physical activity."
            else -> "Stress management is important for everyone. Regular exercise, adequate sleep, and relaxation techniques can help maintain good mental health."
        }
    }

    private fun calculateBMI(userData: Map<String, Any>): Double {
        val height = (userData["height"] as? Number)?.toDouble() ?: return 0.0
        val weight = (userData["weight"] as? Number)?.toDouble() ?: return 0.0
        return weight / (height / 100).pow(2)
    }
}