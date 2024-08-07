package com.project.wellnesswise.data

data class MedicalHistoryQuestion(
    val question: String,
    val suggestedAnswers: List<String>
)

val medicalHistoryQuestions = listOf(
    MedicalHistoryQuestion(
        question = "Do you have any allergies?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Have you had any surgeries?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have any chronic diseases?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have diabetes?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have high blood pressure?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have asthma?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have heart disease?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have arthritis?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have kidney disease?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have a thyroid condition?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have chronic pain?",
        suggestedAnswers = listOf("Yes", "No")
    ),
    MedicalHistoryQuestion(
        question = "Do you have any mental health conditions?",
        suggestedAnswers = listOf("Yes", "No")
    )
)
