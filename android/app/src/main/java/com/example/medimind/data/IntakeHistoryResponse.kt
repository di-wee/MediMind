package com.example.medimind.data

data class IntakeHistoryResponse(
    val medicationName: String,
    val scheduledTime: String, // ISO format e.g., "2025-08-01T08:00:00"
    val takenTime: String?,    // can be null
    val status: String         // "TAKEN" or "NOT_TAKEN"
)
