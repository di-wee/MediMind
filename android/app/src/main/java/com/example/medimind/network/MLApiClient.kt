package com.example.medimind.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MLApiClient {
    // FastAPI endpoints for ML service
    // For running locally
    private const val EMULATOR_URL = "http://10.0.2.2:8000"
    private const val DEVICE_URL = "http://192.168.1.3:8000"  // if using physical android device change to laptop IP on WiFi

    // Change to this for running on EC2. Instance
    //private const val EMULATOR_URL = "http://47.130.114.135:8000" // Works for both emulator and physical device

    private fun isEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT
        return fingerprint.contains("generic") ||
                fingerprint.contains("sdk_gphone") ||
                fingerprint.contains("emulator")
    }

    private fun getBaseUrl(): String {
        return if (isEmulator()) EMULATOR_URL else DEVICE_URL
    }

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val mlApiService: MLApiService by lazy {
        retrofit.create(MLApiService::class.java)
    }
}
