package com.example.medimind.data

data class MedicationResponse(
    val id: String,
    val medicationName: String,
    val intakeQuantity: String,
    val frequency: Int,
    val timing: String?,
    val instructions: String?,
    val notes:String?,
    val isActive: Boolean,
    val missedDose:Boolean
)
