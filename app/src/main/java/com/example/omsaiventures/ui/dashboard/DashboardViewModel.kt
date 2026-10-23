package com.example.omsaiventures.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CompanyProfile(
    val name: String = "",
    val sub: String = "",
    val contact: String = "",
    val gst: String = ""
)

data class Employee(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val empid: String = "",
    val doj: String = "",
    val deputedAt: String = "",
    val bank: String = "",
    val pan: String = "",
    val bankname: String = "",
    val ifsc: String = "",
    val store: String = "",
    val payMonth: Int = 8, // September (0-indexed usually, but let's just use raw)
    val payYear: Int = 2026,
    val customDaysInMonth: Int = 31,
    val paidDays: Int = 30,
    val lwp: Int = 0,
    val refundDays: Int = 0,
    val basic: Double = 0.0,
    val attendance: Double = 0.0,
    val performance: Double = 0.0,
    val festivalBonus: Double = 0.0,
    val dq: Double = 0.0,
    val otherDed: Double = 0.0,
    val groupInsurance: Double = 0.0,
    val remarks: String = ""
) {
    val gross get() = basic + attendance + performance + festivalBonus
    val lwpDeduction get() = if (customDaysInMonth > 0) (basic / customDaysInMonth) * lwp else 0.0
    val ded get() = dq + otherDed + groupInsurance + lwpDeduction
    val net get() = gross - ded
}

data class SalarySlip(
    val id: String = "",
    val empId: String = "",
    val empName: String = "",
    val payMonth: Int = 8,
    val payYear: Int = 2026,
    val timestamp: String = "",
    val gross: Double = 0.0,
    val ded: Double = 0.0,
    val net: Double = 0.0
)

data class DashboardState(
    val isLoading: Boolean = true,
    val company: CompanyProfile = CompanyProfile(),
    val employees: List<Employee> = emptyList(),
    val historicalSlips: List<SalarySlip> = emptyList(),
    val activeEmployeeId: String? = null,
    val activeTab: String = "payroll", // "payroll", "offerLetter", "history"
    val error: String? = null,
    
    // AI Integration States
    val aiIsLoading: Boolean = false,
    val prediction: com.example.omsaiventures.api.PredictionResponse? = null,
    val anomalies: List<com.example.omsaiventures.api.AnomalyItem> = emptyList(),
    val aiError: String? = null,
    
    val successMessage: String? = null
)

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        fetchData()
        fetchAiInsights()
    }

    fun fetchAiInsights() {
        viewModelScope.launch {
            _state.value = _state.value.copy(aiIsLoading = true, aiError = null)
            try {
                // Check health first to see if models are loaded
                val health = com.example.omsaiventures.api.ApiClient.apiService.getHealth()
                if (health.status != "ok") {
                    _state.value = _state.value.copy(
                        aiIsLoading = false,
                        aiError = "AI Services are currently unavailable."
                    )
                    return@launch
                }

                // Fetch Prediction & Anomalies concurrently or sequentially
                val prediction = com.example.omsaiventures.api.ApiClient.apiService.getPayrollPrediction()
                val anomalyResponse = com.example.omsaiventures.api.ApiClient.apiService.getAnomalies()

                _state.value = _state.value.copy(
                    aiIsLoading = false,
                    prediction = prediction,
                    anomalies = anomalyResponse.anomalies,
                    aiError = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    aiIsLoading = false,
                    aiError = "Failed to connect to AI server: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Fetch Company Profile
                val companySnapshot = db.collection("company").document("profile").get().await()
                val company = CompanyProfile(
                    name = companySnapshot.getString("name")?.takeIf { it.isNotBlank() } ?: "OMSAI VENTURES",
                    sub = companySnapshot.getString("sub")?.takeIf { it.isNotBlank() } ?: "Registered Office: First Floor, 65, Shreya Park, Near Ravi Nagar, Gokul Road, Hubli – 580030",
                    contact = companySnapshot.getString("contact")?.takeIf { it.isNotBlank() } ?: "Email: omsaiventures9@gmail.com\nPhone: 8073874620 / 9739316724",
                    gst = companySnapshot.getString("gst")?.takeIf { it.isNotBlank() } ?: "29AAKF01084E1Z6"
                )

                // Fetch Employees safely handling Number formats
                val employeesSnapshot = db.collection("employees").get().await()
                val employeesList = employeesSnapshot.documents.mapNotNull { doc ->
                    try {
                        Employee(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            role = doc.getString("role") ?: "",
                            empid = doc.getString("empid") ?: "",
                            doj = doc.getString("doj") ?: "",
                            deputedAt = doc.getString("deputedAt") ?: "",
                            bank = doc.getString("bank") ?: "",
                            pan = doc.getString("pan") ?: "",
                            bankname = doc.getString("bankname") ?: "",
                            ifsc = doc.getString("ifsc") ?: "",
                            store = doc.getString("store") ?: "",
                            payMonth = (doc.get("payMonth") as? Number)?.toInt() ?: 8,
                            payYear = (doc.get("payYear") as? Number)?.toInt() ?: 2026,
                            customDaysInMonth = (doc.get("customDaysInMonth") as? Number)?.toInt() ?: 31,
                            paidDays = (doc.get("paidDays") as? Number)?.toInt() ?: 30,
                            lwp = (doc.get("lwp") as? Number)?.toInt() ?: 0,
                            refundDays = (doc.get("refundDays") as? Number)?.toInt() ?: 0,
                            basic = (doc.get("basic") as? Number)?.toDouble() ?: 0.0,
                            attendance = (doc.get("attendance") as? Number)?.toDouble() ?: 0.0,
                            performance = (doc.get("performance") as? Number)?.toDouble() ?: 0.0,
                            festivalBonus = (doc.get("festivalBonus") as? Number)?.toDouble() ?: 0.0,
                            dq = (doc.get("dq") as? Number)?.toDouble() ?: 0.0,
                            otherDed = (doc.get("otherDed") as? Number)?.toDouble() ?: 0.0,
                            groupInsurance = (doc.get("groupInsurance") as? Number)?.toDouble() ?: 0.0,
                            remarks = doc.getString("remarks") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // Fetch Historical Slips
                // Note: The collection name in Firebase is "salary_slips" based on the screenshot provided
                val slipsSnapshot = db.collection("salary_slips").get().await()
                val slipsList = slipsSnapshot.documents.mapNotNull { doc ->
                    try {
                        val rawTimestamp = doc.get("timestamp")
                        val timestampStr = when (rawTimestamp) {
                            is String -> rawTimestamp
                            is com.google.firebase.Timestamp -> {
                                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                    .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                    .format(rawTimestamp.toDate())
                            }
                            is java.util.Date -> {
                                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                    .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                    .format(rawTimestamp)
                            }
                            else -> rawTimestamp?.toString() ?: ""
                        }

                        fun parseDouble(field: String): Double {
                            val v = doc.get(field)
                            return when (v) {
                                is Number -> v.toDouble()
                                is String -> v.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                        }

                        fun parseInt(field: String, default: Int): Int {
                            val v = doc.get(field)
                            return when (v) {
                                is Number -> v.toInt()
                                is String -> v.toIntOrNull() ?: default
                                else -> default
                            }
                        }

                        SalarySlip(
                            id = doc.id,
                            empId = doc.getString("empId") ?: doc.getString("empid") ?: "",
                            empName = doc.getString("empName") ?: doc.getString("name") ?: "Unknown Employee",
                            payMonth = parseInt("payMonth", 8),
                            payYear = parseInt("payYear", 2026),
                            timestamp = timestampStr,
                            gross = parseDouble("gross"),
                            ded = parseDouble("ded"),
                            net = parseDouble("net")
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardViewModel", "Failed to parse slip ${doc.id}", e)
                        null
                    }
                }.sortedByDescending { it.timestamp } // Show newest first

                _state.value = _state.value.copy(
                    isLoading = false,
                    company = company,
                    employees = employeesList,
                    historicalSlips = slipsList,
                    activeEmployeeId = employeesList.firstOrNull()?.id // Select first by default
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to fetch dashboard data"
                )
            }
        }
    }

    fun setActiveEmployee(id: String) {
        _state.value = _state.value.copy(activeEmployeeId = id)
    }

    fun setActiveTab(tab: String) {
        _state.value = _state.value.copy(activeTab = tab)
    }

    // Function to handle adding or updating an employee
    fun saveEmployee(employee: Employee) {
        viewModelScope.launch {
            val isNew = employee.id.isBlank()
            val docRef = if (isNew) {
                db.collection("employees").document() // auto-generate ID
            } else {
                db.collection("employees").document(employee.id)
            }

            val employeeToSave = if (isNew) employee.copy(id = docRef.id) else employee

            try {
                docRef.set(employeeToSave).await()
                // Refresh list locally
                val currentList = _state.value.employees.toMutableList()
                if (isNew) {
                    currentList.add(employeeToSave)
                } else {
                    val index = currentList.indexOfFirst { it.id == employee.id }
                    if (index != -1) currentList[index] = employeeToSave
                }

                _state.value = _state.value.copy(
                    employees = currentList,
                    activeEmployeeId = employeeToSave.id
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to save employee: ${e.message}")
            }
        }
    }

    fun deleteEmployee(id: String) {
        viewModelScope.launch {
            try {
                db.collection("employees").document(id).delete().await()
                val currentList = _state.value.employees.filter { it.id != id }
                _state.value = _state.value.copy(
                    employees = currentList,
                    activeEmployeeId = currentList.firstOrNull()?.id,
                    successMessage = "Employee deleted successfully."
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to delete employee: ${e.message}")
            }
        }
    }

    fun saveSlipToHistory(employee: Employee) {
        viewModelScope.launch {
            try {
                val slipData = hashMapOf(
                    "empId" to employee.id,
                    "empName" to employee.name,
                    "payMonth" to employee.payMonth,
                    "payYear" to employee.payYear,
                    "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        .format(java.util.Date()),
                    "basic" to employee.basic,
                    "attendance" to employee.attendance,
                    "performance" to employee.performance,
                    "festivalBonus" to employee.festivalBonus,
                    "dq" to employee.dq,
                    "otherDed" to employee.otherDed,
                    "groupInsurance" to employee.groupInsurance,
                    "lwp" to employee.lwp,
                    "paidDays" to employee.paidDays,
                    "lwpDeduction" to employee.lwpDeduction,
                    "gross" to employee.gross,
                    "ded" to employee.ded,
                    "net" to employee.net,
                    "remarks" to employee.remarks
                )
                val docRef = db.collection("salary_slips").add(slipData).await()
                
                // Add to local state so history tab updates immediately
                val newSlip = SalarySlip(
                    id = docRef.id,
                    empId = employee.id,
                    empName = employee.name,
                    payMonth = employee.payMonth,
                    payYear = employee.payYear,
                    timestamp = slipData["timestamp"] as String,
                    gross = employee.gross,
                    ded = employee.ded,
                    net = employee.net
                )
                val currentHistory = _state.value.historicalSlips.toMutableList()
                currentHistory.add(0, newSlip) // add to top
                
                _state.value = _state.value.copy(
                    historicalSlips = currentHistory,
                    successMessage = "Payslip saved to history!"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to save slip to history: ${e.message}")
            }
        }
    }

    fun deleteHistoricalSlip(id: String) {
        viewModelScope.launch {
            try {
                db.collection("salary_slips").document(id).delete().await()
                val currentHistory = _state.value.historicalSlips.filter { it.id != id }
                _state.value = _state.value.copy(
                    historicalSlips = currentHistory,
                    successMessage = "Payslip deleted from history."
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to delete slip: ${e.message}")
            }
        }
    }

    fun seedSampleSlips() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val targetEmployees = if (_state.value.employees.isNotEmpty()) {
                    _state.value.employees
                } else {
                    listOf(
                        Employee(id = "E001", name = "Rahul Sharma", role = "Store Manager", basic = 25000.0, attendance = 1000.0, performance = 2000.0, payMonth = 9, payYear = 2026),
                        Employee(id = "E002", name = "Priya Desai", role = "Cashier", basic = 18000.0, attendance = 1000.0, performance = 500.0, festivalBonus = 1000.0, payMonth = 8, payYear = 2026),
                        Employee(id = "E003", name = "Amit Kumar", role = "Delivery Executive", basic = 15000.0, attendance = 500.0, lwp = 2, payMonth = 7, payYear = 2026)
                    )
                }

                val newSlips = mutableListOf<SalarySlip>()
                val months = listOf(9, 8, 7) // September, August, July
                val calendar = java.util.Calendar.getInstance()

                for ((index, emp) in targetEmployees.withIndex()) {
                    val monthIdx = months.getOrElse(index % months.size) { 9 }
                    calendar.set(2026, monthIdx, 5 + (index * 2), 10, 30, 0)
                    val timestampStr = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        .format(calendar.time)

                    val slipData = hashMapOf(
                        "empId" to emp.id,
                        "empName" to emp.name,
                        "payMonth" to monthIdx,
                        "payYear" to 2026,
                        "timestamp" to timestampStr,
                        "basic" to emp.basic,
                        "attendance" to emp.attendance,
                        "performance" to emp.performance,
                        "festivalBonus" to emp.festivalBonus,
                        "dq" to emp.dq,
                        "otherDed" to emp.otherDed,
                        "groupInsurance" to emp.groupInsurance,
                        "lwp" to emp.lwp,
                        "paidDays" to emp.paidDays,
                        "lwpDeduction" to emp.lwpDeduction,
                        "gross" to emp.gross,
                        "ded" to emp.ded,
                        "net" to emp.net,
                        "remarks" to (emp.remarks.ifBlank { "Monthly salary slip" })
                    )
                    val docRef = db.collection("slips").add(slipData).await()
                    newSlips.add(
                        SalarySlip(
                            id = docRef.id,
                            empId = emp.id,
                            empName = emp.name,
                            payMonth = monthIdx,
                            payYear = 2026,
                            timestamp = timestampStr,
                            gross = emp.gross,
                            ded = emp.ded,
                            net = emp.net
                        )
                    )
                }

                val updatedHistory = (newSlips + _state.value.historicalSlips).sortedByDescending { it.timestamp }
                _state.value = _state.value.copy(
                    isLoading = false,
                    historicalSlips = updatedHistory,
                    successMessage = "Sample historical slips created!"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to create sample slips: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null, aiError = null)
    }
}
