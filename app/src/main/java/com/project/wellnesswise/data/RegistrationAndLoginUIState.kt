package com.project.wellnesswise.data

data class RegistrationUIState(
    var email: String = "",
    var fullName: String = "",
    var age: Number = 0,
    var gender: Gender = Gender.MALE,
    var height: Number = 0,
    var weight: Number = 0,
    var habits: List<Habit> = emptyList(),
    var medicalHistory:  Map<String, String> = emptyMap(),
    var password: String = "",
    var isPolicyAccepted: Boolean = false
)



data class LoginUIState(
    var email: String = "",
    var password: String = "",

    var emailError: Boolean = false,
    var passwordError: Boolean = false,
)


enum class Gender {
    MALE,
    FEMALE,

}

enum class Habit {
    Smoking,
    Drinking,
    Exercise,
    HealthyEating,
    SleepPatterns,
    StressManagement,
    Meditation,
    AlcoholConsumption,
    DrugUse,
    ScreenTime,
    SocialInteraction,
    PhysicalActivity,
    MentalHealthPractices,
    HygienePractices,

}
