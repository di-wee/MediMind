package com.example.medimind.network

import com.example.medimind.data.ImageOutput
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MLApiService {
    @Multipart
    @POST("/api/medication/predict_image")
    suspend fun predictImage(@Part file: MultipartBody.Part): Response<ImageOutput>
}