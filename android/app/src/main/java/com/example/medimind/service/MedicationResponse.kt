package com.example.medimind.service

data class MedicationResponse(
    val id: String,
    val medicationName: String,
    val intakeQuantity: String,
    val frequency: Int,
    val timing: String?,
    val instructions: String?,
    val notes:String?,
    val active: Boolean,
    val scheduleIds: List<String>
)
