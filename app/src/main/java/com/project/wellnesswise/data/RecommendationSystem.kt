class HealthRecommendationSystem {
    fun generateRecommendations(predictions: List<Triple<String, Float, String>>): List<String> {
        val recommendations = mutableListOf<String>()

        predictions.forEach { (category, risk, _) ->
            when (category) {
                "Diabetes" -> recommendations.add(generateDiabetesRecommendations(risk))
                "Cardiovascular Disease" -> recommendations.add(generateCardiovascularRecommendations(risk))
                "Hypertension" -> recommendations.add(generateHypertensionRecommendations(risk))
                "Obesity" -> recommendations.add(generateObesityRecommendations(risk))
                "Cancer" -> recommendations.add(generateCancerRecommendations(risk))
            }
        }

        // Add general recommendations based on highest and lowest risks
        val highRiskConditions = predictions.filter { it.second >= 0.6f }
        val lowRiskConditions = predictions.filter { it.second < 0.2f }

        if (highRiskConditions.isNotEmpty()) {
            recommendations.add(generateHighRiskRecommendation(highRiskConditions))
        }

        if (lowRiskConditions.isNotEmpty()) {
            recommendations.add(generateLowRiskRecommendation(lowRiskConditions))
        }

        return recommendations
    }

    private fun generateDiabetesRecommendations(risk: Float): String {
        return when {
            risk < 0.2f -> "Excellent! Your diabetes risk is stable. Your healthy lifestyle choices are paying off - keep maintaining a balanced diet and regular exercise routine. This is a great foundation for long-term health."
            risk < 0.4f -> "Good news! Your diabetes risk is mild. While you're on the right track, consider fine-tuning your diet by reducing refined sugars and increasing fiber intake. Your current habits are already helping you maintain good health."
            risk < 0.6f -> "Your diabetes risk is moderate. Schedule a diabetes screening with your healthcare provider. Consider consulting a nutritionist for a personalized meal plan and increase your physical activity."
            risk < 0.8f -> "Your diabetes risk is severe. Consult your doctor soon for a comprehensive diabetes assessment and management plan. Focus on immediate lifestyle changes including diet, exercise, and possibly medication."
            else -> "Your diabetes risk is critical. Consult your doctor immediately for an urgent diabetes assessment and management plan. Immediate attention to lifestyle changes and medical intervention may be necessary."
        }
    }

    private fun generateCardiovascularRecommendations(risk: Float): String {
        return when {
            risk < 0.2f -> "Excellent! Your cardiovascular health is in great shape. Your heart-healthy practices are working well - continue with your balanced diet rich in fruits, vegetables, and whole grains, and maintain your exercise routine."
            risk < 0.4f -> "Good news! Your cardiovascular risk is mild. Your heart-healthy habits are showing positive results. Consider regular check-ups and maintain your current lifestyle while looking for ways to further improve."
            risk < 0.6f -> "Your cardiovascular risk is moderate. Schedule a comprehensive cardiovascular health check-up. Consider discussing preventive measures, including lifestyle changes and possibly medication, with your doctor."
            risk < 0.8f -> "Your cardiovascular risk is severe. Consult a doctor soon for a thorough evaluation and personalized heart health plan. Immediate attention to lifestyle changes may be necessary."
            else -> "Your cardiovascular risk is critical. Consult a cardiologist immediately for a thorough evaluation and personalized heart health plan. This requires urgent attention to lifestyle changes and possible medical intervention."
        }
    }

    private fun generateHypertensionRecommendations(risk: Float): String {
        return when {
            risk < 0.2f -> "Excellent! Your blood pressure risk is stable. Your current lifestyle choices are helping maintain healthy blood pressure levels. Keep up with your low-sodium diet and regular exercise routine."
            risk < 0.4f -> "Good news! Your blood pressure risk is mild. Your current habits are working well. Consider optimizing your diet further by increasing potassium-rich foods while maintaining your low-sodium approach."
            risk < 0.6f -> "Your hypertension risk is moderate. Monitor your blood pressure regularly at home. Discuss lifestyle modifications with your doctor and consider medication if recommended."
            risk < 0.8f -> "Your hypertension risk is severe. Consult your healthcare provider soon for a thorough blood pressure assessment and management plan. Immediate lifestyle changes may be necessary."
            else -> "Your hypertension risk is critical. Seek immediate medical attention for your blood pressure management. Urgent lifestyle changes and medical intervention may be necessary."
        }
    }

    private fun generateObesityRecommendations(risk: Float): String {
        return when {
            risk < 0.2f -> "Excellent! Your weight management is on track. Your balanced approach to diet and exercise is working well - keep maintaining these healthy habits for continued success."
            risk < 0.4f -> "Good news! Your weight-related risk is mild. Your current lifestyle choices are showing positive results. Continue focusing on portion control and regular physical activity while looking for ways to optimize further."
            risk < 0.6f -> "Your obesity risk is moderate. Consider consulting a nutritionist for a personalized meal plan. Aim for at least 150 minutes of moderate exercise per week."
            risk < 0.8f -> "Your obesity risk is severe. Consult a healthcare professional soon for a comprehensive weight management plan. This may include specific dietary changes and structured physical activity."
            else -> "Your obesity risk is critical. Seek immediate professional help for weight management. This requires urgent attention to diet, exercise, and possible medical interventions."
        }
    }

    private fun generateCancerRecommendations(risk: Float): String {
        return when {
            risk < 0.2f -> "Excellent! Your cancer risk is stable. Your healthy lifestyle choices are helping maintain low risk levels. Continue with age-appropriate screenings and maintain your healthy habits."
            risk < 0.4f -> "Good news! Your cancer risk is mild. Your current lifestyle choices are showing positive results. Stay up-to-date with recommended screenings while maintaining your healthy habits."
            risk < 0.6f -> "Your cancer risk is moderate. Schedule a check-up with your doctor to discuss risk factors and preventive strategies. This may include more frequent screenings and lifestyle modifications."
            risk < 0.8f -> "Your cancer risk is severe. Consult a doctor soon for a thorough risk assessment and prevention plan. This may include genetic testing and frequent screenings."
            else -> "Your cancer risk is critical. Consult an oncologist immediately for a comprehensive risk assessment and prevention plan. This requires urgent attention to screening and possible interventions."
        }
    }

    private fun generateHighRiskRecommendation(highRiskConditions: List<Triple<String, Float, String>>): String {
        val conditions = highRiskConditions.joinToString(", ") { it.first }
        return "Important health alert: Due to elevated risk of $conditions, please schedule a comprehensive health check-up with your healthcare provider as soon as possible. " +
                "They can help create a personalized plan to address these specific health concerns and monitor your progress regularly."
    }

    private fun generateLowRiskRecommendation(lowRiskConditions: List<Triple<String, Float, String>>): String {
        val conditions = lowRiskConditions.joinToString(", ") { it.first }
        return "Great job! You're maintaining healthy levels for: $conditions. " +
                "Your lifestyle choices are positively impacting multiple aspects of your health. Keep up these excellent habits while staying proactive with regular check-ups and screenings."
    }
}