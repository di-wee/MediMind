package com.example.medimind.data

data class EditMedRequest(
    val medicationId: String,
    val patientId: String,
    val frequency: Int,
    val times: List<String>
)
