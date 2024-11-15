package com.project.wellnesswise.viewModels

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
    data class SmokingChanged(val value: Boolean) : UIEvent()
    data class AlcoholConsumptionChanged(val value: Int) : UIEvent()
    data class PhysicalActivityChanged(val value: Int) : UIEvent()
    data class DietQualityChanged(val value: Int) : UIEvent()
    data class SleepHoursChanged(val value: Int) : UIEvent()
    data class AirQualityIndexChanged(val value: Int) : UIEvent()
    data class ExposureToPollutantsChanged(val value: Int) : UIEvent()
    data class StressLevelChanged(val value: Int) : UIEvent()
    data class AccessToHealthcareChanged(val value: Int) : UIEvent()
    data object SaveHealthAssessmentClicked : UIEvent()

    data object RegisterButtonClicked : UIEvent()

}


sealed class LoginUIEvent {
    data class EmailChangedLogin(val email: String) : LoginUIEvent()
    data class PasswordChangedLogin(val password: String) : LoginUIEvent()


    data object LoginButtonClicked : LoginUIEvent()
}