package com.example.medimind.data

import com.google.gson.annotations.SerializedName

data class MedicationResponse(
    val id: String,
    val medicationName: String,
    val intakeQuantity: String,
    val frequency: Int,
    val timing: String?,
    val instructions: String?,
    val notes:String?,
    @SerializedName("active")
    val isActive: Boolean,
    @SerializedName("missedDose")
    val missedDose:Boolean
)
