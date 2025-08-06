package com.example.medimind.service

data class EditMedResponse(
    val frequency: Int,
    val activeSchedulesTimes: List<String>
)
