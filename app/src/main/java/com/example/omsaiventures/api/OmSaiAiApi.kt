package com.example.omsaiventures.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class HealthResponse(
    val status: String,
    @SerializedName("prediction_model_loaded") val predictionModelLoaded: Boolean,
    @SerializedName("anomaly_model_loaded") val anomalyModelLoaded: Boolean
)

data class PredictionResponse(
    val predictionMonth: Int,
    val predictionYear: Int,
    val predictedNetPayroll: Double,
    val previousMonthNetPayroll: Double,
    val changeAmount: Double,
    val changePercentage: Double,
    val modelUsed: String,
    val forecastDisclaimer: String
)

data class AnomalyItem(
    val employeeId: String,
    val employeeName: String,
    val payMonth: Int,
    val payYear: Int,
    val net: Double,
    val gross: Double,
    val deductions: Double,
    val anomalyScore: Double,
    val reason: String
)

data class AnomalyResponse(
    val totalRecordsAnalyzed: Int,
    val anomalyCount: Int,
    val anomalies: List<AnomalyItem>
)

interface OmSaiAiApiService {
    @GET("/health")
    suspend fun getHealth(): HealthResponse

    @GET("/ai/payroll-prediction")
    suspend fun getPayrollPrediction(): PredictionResponse

    @GET("/ai/anomalies")
    suspend fun getAnomalies(): AnomalyResponse
}

object ApiClient {
    // Hosted Python FastAPI server
    private const val BASE_URL = "https://omsai-ventures-ai-payroll.onrender.com/"

    val apiService: OmSaiAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OmSaiAiApiService::class.java)
    }
}
