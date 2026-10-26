package com.example.omsaiventures.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class HealthResponse(
    val status: String,
    @SerializedName("prediction_model_loaded") val predictionModelLoaded: Boolean,
    @SerializedName("anomaly_model_loaded") val anomalyModelLoaded: Boolean
)

data class HistoricalPayrollPoint(
    val payMonth: Int = 0,
    val payYear: Int = 0,
    val employeeCount: Int = 0,
    val totalGross: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val totalNet: Double = 0.0,
    val totalLwp: Int = 0,
    val totalPerformance: Double = 0.0,
    val totalFestivalBonus: Double = 0.0
)

data class PredictionResponse(
    val predictionMonth: Int = 0,
    val predictionYear: Int = 0,
    val predictedNetPayroll: Double = 0.0,
    val previousMonthNetPayroll: Double = 0.0,
    val changeAmount: Double = 0.0,
    val changePercentage: Double = 0.0,
    val modelUsed: String = "",
    val forecastDisclaimer: String = "",
    
    // Factor-aware extensions
    val basePredictedNetPayroll: Double = 0.0,
    val expectedEmployeeCount: Int = 0,
    val estimatedAverageLwp: Double = 0.0,
    val estimatedLwpDeductions: Double = 0.0,
    val expectedPerformanceIncentives: Double = 0.0,
    val festivalName: String? = null,
    val festivalAdjustment: Double = 0.0,
    val adjustedPredictedNetPayroll: Double = 0.0,
    val forecastAssumptions: List<String> = emptyList(),
    val historicalPayroll: List<HistoricalPayrollPoint> = emptyList()
)

data class EmployeePayrollForecast(
    val empId: String = "",
    val empName: String = "",
    val basic: Double = 0.0,
    val expectedPerf: Double = 0.0,
    val expectedLwp: Double = 0.0,
    val expectedLwpDed: Double = 0.0,
    val expectedOtherDed: Double = 0.0,
    val gi: Double = 0.0,
    val expectedGross: Double = 0.0,
    val expectedDed: Double = 0.0,
    val expectedNet: Double = 0.0
)

data class BreakdownDetails(
    val baseGross: Double = 0.0,
    val totalBasic: Double = 0.0,
    val totalPerformance: Double = 0.0,
    val totalLwpDays: Double = 0.0,
    val totalLwpDeductions: Double = 0.0,
    val totalOtherDeductions: Double = 0.0,
    val totalGroupInsurance: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val festivalBonus: Double = 0.0,
    val adjustedGross: Double = 0.0,
    val employeeForecasts: List<EmployeePayrollForecast> = emptyList()
)

data class BreakdownResponse(
    val predictionMonth: Int = 0,
    val predictionYear: Int = 0,
    val basePredictedNetPayroll: Double = 0.0,
    val adjustedPredictedNetPayroll: Double = 0.0,
    val festivalName: String? = null,
    val festivalAdjustment: Double = 0.0,
    val expectedEmployeeCount: Int = 0,
    val modelUsed: String = "",
    val breakdown: BreakdownDetails = BreakdownDetails()
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
    suspend fun getPayrollPrediction(
        @Query("include_festival") includeFestival: Boolean = true
    ): PredictionResponse

    @GET("/ai/payroll-prediction/breakdown")
    suspend fun getPayrollPredictionBreakdown(
        @Query("include_festival") includeFestival: Boolean = true
    ): BreakdownResponse

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
