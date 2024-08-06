package com.project.wellnesswise.data

sealed class UIEvent {
    data class EmailChanged(val email: String) : UIEvent()
    data class FullNameChanged(val fullName: String) : UIEvent()
    data class AgeChanged(val age: Number) : UIEvent()
    data class GenderChanged(val gender: Gender) : UIEvent()
    data class HeightChanged(val height: Number) : UIEvent()
    data class WeightChanged(val weight: Number) : UIEvent()
    data class HabitsChanged(val habits: List<Habit>) : UIEvent()
    data class MedicalHistoryChanged(val medicalHistory: String) : UIEvent()
    data class PasswordChanged(val password: String) : UIEvent()
}
