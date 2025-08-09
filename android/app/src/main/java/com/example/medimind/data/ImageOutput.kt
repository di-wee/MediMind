package com.example.medimind.data

data class ImageOutput (
    val medicationName: String,
    val intakeQuantity: String,
    val frequency: Int,
    val instructions: String,
    val notes: String
)
