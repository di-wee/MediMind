package com.example.medimind.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MLApiClient {
    // FastAPI endpoints for ML service
<<<<<<< Updated upstream
    private const val EMULATOR_URL = "http://47.130.114.135:8000"
    private const val DEVICE_URL = "http://192.168.1.3:8000" // Update this to your local IP address
    
=======
    private const val EMULATOR_URL = "http://10.0.2.2:8000" //run locally
    //private const val EMULATOR_URL = "http://10.0.2.2:8001/" //run ec2 model
    //private const val DEVICE_URL = "http://172.20.10.3:8000" //for physical android phone (when model is run LOCALLY on pris' com)
    private const val DEVICE_URL = "http://10.0.2.2:8000" //for emulator when connecting to model running locally on pris com
    //private const val DEVICE_URL = "http://47.130.114.135:8001/" //connecting to model hosted on ec2 instance
    //10.0.2.2:8001

>>>>>>> Stashed changes
    // For production/EC2 (commented out)
    // private const val PRODUCTION_URL = "http://54.255.65.62:8000"

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
