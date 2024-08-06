package com.project.wellnesswise.data

data class RegistrationUIState(
    var email: String = "",
    var fullName: String = "",
    var age: Number = 0,
    var gender: Gender = Gender.UNSPECIFIED,
    var height: Number = 0,
    var weight: Number = 0,
    var habits: List<Habit> = emptyList(),
    var medicalHistory: String = "",// it will chang Map<String, String> = emptyMap()e
    var password: String = ""
)

enum class Gender {
    MALE,
    FEMALE,
    UNSPECIFIED
}

enum class Habit {
    SMOKING,
    DRINKING,
    EXERCISE,
    // Add other habits as needed
}

// Example usage of medical history map
val medicalHistoryExample = mapOf(
    "Do you have any allergies?" to "Yes",
    "Have you had any surgeries?" to "No",
    // Add other medical history questions and answers
)
