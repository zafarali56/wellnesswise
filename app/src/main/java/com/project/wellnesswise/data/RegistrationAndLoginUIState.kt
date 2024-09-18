package com.project.wellnesswise.data



data class RegistrationUIState(
    var email: String = "",
    var fullName: String = "",
    var age: Number = 0,
    var gender: Gender = Gender.MALE,
    var height: Number = 0,
    var weight: Number = 0,
    var password: String = "",
    var isPolicyAccepted: Boolean = false,

    var familyDiabetes: String = "",
    var familyHeart: String = "",
    var familyCancer: String = "",
    var previousSurgeries: String = "",
    var chronicConditions: String = "",
    var smoking: Boolean = false,
    var alcoholConsumption: Int = 0,
    var physicalActivity: Int = 0,
    var dietQuality: Int = 0,
    var sleepHours: Int = 0,
    var airQualityIndex: Int = 0,
    var exposureToPollutants: Int = 0,
    var stressLevel: Int = 0,
    var accessToHealthcare: Int = 0,

    // Health parameters
    var bloodPressure: String = "",
    var heartRate: String = "",
    var bloodSugar: String = "",
    var cholesterol: String = "",

)
data class LoginUIState(
    var email: String = "",
    var password: String = "",


)


enum class Gender {
    MALE,
    FEMALE,

}
