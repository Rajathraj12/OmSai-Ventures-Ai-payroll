package com.example.omsaiventures.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omsaiventures.ui.theme.*

data class OfferLetterData(
    val candidateName: String,
    val employeeId: String,
    val address: String,
    val designation: String,
    val position: String,
    val roleDescription: String,
    val deputedClient: String,
    val workLocation: String,
    val dateOfJoining: String,
    val startDate: String,
    val endDate: String,
    val workingHours: String,
    val basicSalary: String,
    val attendanceBonus: String,
    val performanceBonus: String,
    val netSalary: String
)

@Composable
fun OfferLetterScreen(state: DashboardState) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var candidateName by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var roleDescription by remember { mutableStateOf("") }
    var deputedClient by remember { mutableStateOf("") }
    var workLocation by remember { mutableStateOf("") }
    var dateOfJoining by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("") }
    var basicSalary by remember { mutableStateOf("") }
    var attendanceBonus by remember { mutableStateOf("") }
    var performanceBonus by remember { mutableStateOf("") }
    var netSalary by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Paper, RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paper.copy(alpha = 0.2f))
                .padding(20.dp)
        ) {
            Text(
                text = "Offer Letter Details",
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider(color = GlassBorder)

        // Form Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = candidateName,
                onValueChange = { candidateName = it },
                label = { Text("Candidate Name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. John Doe") }
            )
            
            OutlinedTextField(
                value = employeeId,
                onValueChange = { employeeId = it },
                label = { Text("Employee ID") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. HR012") }
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Candidate Address") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 71/A Alur, Bagalkot") },
                minLines = 2
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Position") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = roleDescription,
                    onValueChange = { roleDescription = it },
                    label = { Text("Role Category") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = deputedClient,
                    onValueChange = { deputedClient = it },
                    label = { Text("Deputed Client") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = workLocation,
                onValueChange = { workLocation = it },
                label = { Text("Work Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                var showJoiningDatePicker by remember { mutableStateOf(false) }
                var showStartDatePicker by remember { mutableStateOf(false) }

                if (showJoiningDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showJoiningDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showJoiningDatePicker = false
                                datePickerState.selectedDateMillis?.let {
                                    dateOfJoining = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it))
                                }
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showJoiningDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                if (showStartDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showStartDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showStartDatePicker = false
                                datePickerState.selectedDateMillis?.let {
                                    startDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it))
                                }
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = dateOfJoining,
                        onValueChange = {},
                        label = { Text("Date of Joining") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("YYYY-MM-DD") }
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showJoiningDatePicker = true }.background(Color.Transparent))
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = {},
                        label = { Text("Term Start Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("YYYY-MM-DD") }
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true }.background(Color.Transparent))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                var showEndDatePicker by remember { mutableStateOf(false) }

                if (showEndDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showEndDatePicker = false
                                datePickerState.selectedDateMillis?.let {
                                    endDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it))
                                }
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = {},
                        label = { Text("Term End Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("YYYY-MM-DD") }
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true }.background(Color.Transparent))
                }
                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text("Working Hours / Shift") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Compensation (Monthly)", color = Brass, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
            HorizontalDivider(color = GlassBorder)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = basicSalary,
                    onValueChange = { basicSalary = it },
                    label = { Text("Basic Salary") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = attendanceBonus,
                    onValueChange = { attendanceBonus = it },
                    label = { Text("Attendance Bonus") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = performanceBonus,
                    onValueChange = { performanceBonus = it },
                    label = { Text("Performance Bonus") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                val basic = basicSalary.toDoubleOrNull() ?: 0.0
                val attBonus = attendanceBonus.toDoubleOrNull() ?: 0.0
                val perfBonus = performanceBonus.toDoubleOrNull() ?: 0.0
                netSalary = (basic + attBonus + perfBonus).toString()

                OutlinedTextField(
                    value = netSalary,
                    onValueChange = { }, // Read Only
                    readOnly = true,
                    label = { Text("Net Salary (Auto)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paper.copy(alpha = 0.5f))
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    val data = OfferLetterData(
                        candidateName, employeeId, address, designation, position,
                        roleDescription, deputedClient, workLocation, dateOfJoining,
                        startDate, endDate, workingHours, basicSalary, attendanceBonus,
                        performanceBonus, netSalary
                    )
                    com.example.omsaiventures.utils.PdfGenerator.generateOfferLetterPdf(
                        context = context,
                        data = data,
                        company = state.company
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ledger)
            ) {
                Text("Generate PDF", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}