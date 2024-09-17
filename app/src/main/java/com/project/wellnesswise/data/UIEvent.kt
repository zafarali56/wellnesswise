package com.project.wellnesswise.data

sealed class UIEvent {
    data class EmailChanged(val email: String) : UIEvent()
    data class FullNameChanged(val fullName: String) : UIEvent()
    data class AgeChanged(val age: Number) : UIEvent()
    data class GenderChanged(val gender: Gender) : UIEvent()
    data class HeightChanged(val height: Number) : UIEvent()
    data class WeightChanged(val weight: Number) : UIEvent()
    data class PasswordChanged(val password: String) : UIEvent()
    data class PolicyAcceptedChanged(val isPolicyAccepted: Boolean) : UIEvent()
    data class FamilyDiabetesChanged(val value: String) : UIEvent()
    data class FamilyHeartChanged(val value: String) : UIEvent()
    data class FamilyCancerChanged(val value: String) : UIEvent()
    data class PreviousSurgeriesChanged(val value: String) : UIEvent()
    data class ChronicConditionsChanged(val value: String) : UIEvent()
    data class SmokingChanged(val value: Boolean) : UIEvent()  // Changed to Boolean
    data class AlcoholConsumptionChanged(val value: Int) : UIEvent()  // Changed to Int
    data class PhysicalActivityChanged(val value: Int) : UIEvent()  // Changed to Int
    data class DietQualityChanged(val value: Int) : UIEvent()  // Changed to Int
    data class SleepHoursChanged(val value: Int) : UIEvent()  // Changed to Int
    data class AirQualityIndexChanged(val value: Int) : UIEvent()  // Changed to Int
    data class ExposureToPollutantsChanged(val value: Int) : UIEvent()  // Changed to Int
    data class StressLevelChanged(val value: Int) : UIEvent()  // Changed to Int
    data class AccessToHealthcareChanged(val value: Int) : UIEvent()  // Changed to Int
    object SaveHealthAssessmentClicked : UIEvent()

    object RegisterButtonClicked : UIEvent()




}


sealed class LoginUIEvent {
    data class EmailChangedLogin(val email: String) : LoginUIEvent()
    data class PasswordChangedLogin(val password: String) : LoginUIEvent()


    object LoginButtonClicked : LoginUIEvent()
}