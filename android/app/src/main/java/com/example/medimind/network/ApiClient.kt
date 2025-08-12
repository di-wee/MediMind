package com.example.medimind.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Lewis: update of ApiClient Code to run app on emulator and physical device.
object ApiClient {

    private const val EMULATOR_URL = "http://10.0.2.2:8080"
    private const val DEVICE_URL = "http://192.168.1.3:8080" // Local IP 172.20.10.2 (Hotspot) & 192.168.1.3 (Network)

    private fun isEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT
        return fingerprint.contains("generic") ||
                fingerprint.contains("sdk_gphone") ||
                fingerprint.contains("emulator")
    }

    private fun getBaseUrl(): String {
        return if (isEmulator()) EMULATOR_URL else DEVICE_URL
    }

    // Logging interceptor for debugging API requests
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient with logging
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Single retrofitService definition
    val retrofitService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}