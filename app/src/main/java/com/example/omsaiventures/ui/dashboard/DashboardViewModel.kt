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

data class DashboardState(
    val isLoading: Boolean = true,
    val company: CompanyProfile = CompanyProfile(),
    val employees: List<Employee> = emptyList(),
    val activeEmployeeId: String? = null,
    val activeTab: String = "payroll", // "payroll", "offerLetter", "history"
    val error: String? = null
)

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Fetch Company Profile
                val companySnapshot = db.collection("company").document("profile").get().await()
                val company = CompanyProfile(
                    name = companySnapshot.getString("name") ?: "",
                    sub = companySnapshot.getString("sub") ?: "",
                    contact = companySnapshot.getString("contact") ?: "",
                    gst = companySnapshot.getString("gst") ?: ""
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

                _state.value = _state.value.copy(
                    isLoading = false,
                    company = company,
                    employees = employeesList,
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
                    activeEmployeeId = currentList.firstOrNull()?.id
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to delete employee: ${e.message}")
            }
        }
    }
}
