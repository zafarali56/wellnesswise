package com.project.wellnesswise.data

data class MedicalHistoryQuestion(
    val question: String,
    val suggestedAnswers: List<String>,
    val mlModelField: String
)

val medicalHistoryQuestions = listOf(
    MedicalHistoryQuestion(
        question = "Do you smoke?",
        suggestedAnswers = listOf("No", "Yes"),
        mlModelField = "smoking"
    ),
    MedicalHistoryQuestion(
        question = "How many alcoholic drinks do you consume per week on average?",
        suggestedAnswers = listOf("0", "1-3", "4-7", "8-14", "15+"),
        mlModelField = "alcohol"
    ),
    MedicalHistoryQuestion(
        question = "How would you rate your physical activity level?",
        suggestedAnswers = listOf("1 (Very Low)", "2 (Low)", "3 (Moderate)", "4 (High)", "5 (Very High)"),
        mlModelField = "physical_activity"
    ),
    MedicalHistoryQuestion(
        question = "Do you have a family history of diabetes (parents or siblings)?",
        suggestedAnswers = listOf("No", "Yes"),
        mlModelField = "family_diabetes"
    ),
    MedicalHistoryQuestion(
        question = "Do you have a family history of heart disease (parents or siblings)?",
        suggestedAnswers = listOf("No", "Yes"),
        mlModelField = "family_heart"
    ),
    MedicalHistoryQuestion(
        question = "Do you have a family history of cancer (parents or siblings)?",
        suggestedAnswers = listOf("No", "Yes"),
        mlModelField = "family_cancer"
    ),
    MedicalHistoryQuestion(
        question = "How would you rate your overall diet quality?",
        suggestedAnswers = listOf("1 (Poor)", "2 (Fair)", "3 (Average)", "4 (Good)", "5 (Excellent)"),
        mlModelField = "diet_quality"
    ),
    MedicalHistoryQuestion(
        question = "On average, how many hours of sleep do you get per night?",
        suggestedAnswers = listOf("<6", "6-7", "7-8", "8-9", ">9"),
        mlModelField = "sleep_hours"
    ),
    MedicalHistoryQuestion(
        question = "How would you rate your overall stress level in the past month?",
        suggestedAnswers = listOf("1 (Very Low)", "2 (Low)", "3 (Moderate)", "4 (High)", "5 (Very High)"),
        mlModelField = "stress_level"
    )
)
