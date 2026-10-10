package com.example.omsaiventures.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.omsaiventures.ui.theme.*

@Composable
fun EmployeeModal(
    employee: Employee?,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    // Local state for the form mapped strictly to the screenshot
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var role by remember { mutableStateOf(employee?.role ?: "") }
    var empid by remember { mutableStateOf(employee?.empid ?: "") }
    var doj by remember { mutableStateOf(employee?.doj ?: "") }
    var deputedAt by remember { mutableStateOf(employee?.deputedAt ?: "") }
    var bank by remember { mutableStateOf(employee?.bank ?: "") }
    var pan by remember { mutableStateOf(employee?.pan ?: "") }
    var bankname by remember { mutableStateOf(employee?.bankname ?: "") }
    var ifsc by remember { mutableStateOf(employee?.ifsc ?: "") }
    var store by remember { mutableStateOf(employee?.store ?: "") }
    
    var payMonth by remember { mutableStateOf(employee?.payMonth?.toString() ?: "9") }
    var payYear by remember { mutableStateOf(employee?.payYear?.toString() ?: "2026") }
    var customDaysInMonth by remember { mutableStateOf(employee?.customDaysInMonth?.toString() ?: "31") }
    var paidDays by remember { mutableStateOf(employee?.paidDays?.toString() ?: "30") }
    var lwp by remember { mutableStateOf(employee?.lwp?.toString() ?: "0") }
    var refundDays by remember { mutableStateOf(employee?.refundDays?.toString() ?: "0") }
    
    var basic by remember { mutableStateOf(employee?.basic?.toString() ?: "0") }
    var attendance by remember { mutableStateOf(employee?.attendance?.toString() ?: "0") }
    var performance by remember { mutableStateOf(employee?.performance?.toString() ?: "0") }
    var festivalBonus by remember { mutableStateOf(employee?.festivalBonus?.toString() ?: "0") }
    
    var dq by remember { mutableStateOf(employee?.dq?.toString() ?: "0") }
    var otherDed by remember { mutableStateOf(employee?.otherDed?.toString() ?: "0") }
    var groupInsurance by remember { mutableStateOf(employee?.groupInsurance?.toString() ?: "0") }
    
    var remarks by remember { mutableStateOf(employee?.remarks ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(12.dp),
            color = Paper
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = if (employee == null) "Add Employee" else "Edit Employee",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LedgerDark
                    )
                    TextButton(onClick = onDismiss, modifier = Modifier.background(PaperDim, RoundedCornerShape(20.dp)).size(36.dp), contentPadding = PaddingValues(0.dp)) {
                        Text("X", color = InkSoft, fontWeight = FontWeight.Bold)
                    }
                }
                
                HorizontalDivider(color = GlassBorder)

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    SectionHeader("EMPLOYEE DETAILS")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "EMPLOYEE NAME", value = name, onValueChange = { name = it }, modifier = Modifier.weight(1f))
                        Field(label = "DESIGNATION", value = role, onValueChange = { role = it }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "EMP ID", value = empid, onValueChange = { empid = it }, modifier = Modifier.weight(1f))
                        Field(label = "DATE OF JOINING", value = doj, onValueChange = { doj = it }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "DEPUTED AT", value = deputedAt, onValueChange = { deputedAt = it }, modifier = Modifier.weight(1f))
                        Field(label = "ACCOUNT NO.", value = bank, onValueChange = { bank = it }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "PAN NO.", value = pan, onValueChange = { pan = it }, modifier = Modifier.weight(1f))
                        Field(label = "BANK NAME", value = bankname, onValueChange = { bankname = it }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "IFSC CODE", value = ifsc, onValueChange = { ifsc = it }, modifier = Modifier.weight(1f))
                        Field(label = "STORE", value = store, onValueChange = { store = it }, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("PAY PERIOD & ATTENDANCE")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "PAY MONTH", value = payMonth, onValueChange = { payMonth = it }, modifier = Modifier.weight(1f), isNumber = true)
                        Field(label = "PAY YEAR", value = payYear, onValueChange = { payYear = it }, modifier = Modifier.weight(1f), isNumber = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "DAYS IN MONTH (FOR CALC)", value = customDaysInMonth, onValueChange = { customDaysInMonth = it }, modifier = Modifier.weight(1f), isNumber = true)
                        Field(label = "NO. OF PAID DAYS", value = paidDays, onValueChange = { paidDays = it }, modifier = Modifier.weight(1f), isNumber = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "LWP (LEAVE WITHOUT PAY)", value = lwp, onValueChange = { lwp = it }, modifier = Modifier.weight(1f), isNumber = true)
                        Field(label = "REFUND DAYS", value = refundDays, onValueChange = { refundDays = it }, modifier = Modifier.weight(1f), isNumber = true)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("EARNINGS (MONTHLY)")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "BASIC SALARY", value = basic, onValueChange = { basic = it }, modifier = Modifier.weight(1f), isNumber = true)
                        Field(label = "ATTENDANCE BONUS", value = attendance, onValueChange = { attendance = it }, modifier = Modifier.weight(1f), isNumber = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "PERFORMANCE BONUS", value = performance, onValueChange = { performance = it }, modifier = Modifier.weight(1f), isNumber = true)
                        Field(label = "FESTIVAL BONUS", value = festivalBonus, onValueChange = { festivalBonus = it }, modifier = Modifier.weight(1f), isNumber = true)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("DEDUCTIONS (MONTHLY)")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "DQ DEDUCTION", value = dq, onValueChange = { dq = it }, modifier = Modifier.weight(1f), isNumber = true)
                        Field(label = "OTHER DEDUCTION", value = otherDed, onValueChange = { otherDed = it }, modifier = Modifier.weight(1f), isNumber = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Field(label = "GROUP INSURANCE", value = groupInsurance, onValueChange = { groupInsurance = it }, modifier = Modifier.weight(1f), isNumber = true)
                        Spacer(modifier = Modifier.weight(1f)) // Empty spacer for grid alignment
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("REMARKS")
                    Column {
                        Text("REMARKS / REASON FOR ADJUSTMENT", color = InkSoft, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = GlassBorder,
                                focusedBorderColor = Brass
                            )
                        )
                    }
                }

                HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(bottom = 16.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text("Cancel", color = Ink)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val updated = (employee ?: Employee()).copy(
                                name = name,
                                role = role,
                                empid = empid,
                                doj = doj,
                                deputedAt = deputedAt,
                                bank = bank,
                                pan = pan,
                                bankname = bankname,
                                ifsc = ifsc,
                                store = store,
                                payMonth = payMonth.toIntOrNull() ?: 8,
                                payYear = payYear.toIntOrNull() ?: 2026,
                                customDaysInMonth = customDaysInMonth.toIntOrNull() ?: 31,
                                paidDays = paidDays.toIntOrNull() ?: 30,
                                lwp = lwp.toIntOrNull() ?: 0,
                                refundDays = refundDays.toIntOrNull() ?: 0,
                                basic = basic.toDoubleOrNull() ?: 0.0,
                                attendance = attendance.toDoubleOrNull() ?: 0.0,
                                performance = performance.toDoubleOrNull() ?: 0.0,
                                festivalBonus = festivalBonus.toDoubleOrNull() ?: 0.0,
                                dq = dq.toDoubleOrNull() ?: 0.0,
                                otherDed = otherDed.toDoubleOrNull() ?: 0.0,
                                groupInsurance = groupInsurance.toDoubleOrNull() ?: 0.0,
                                remarks = remarks
                            )
                            onSave(updated)
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LedgerDark)
                    ) {
                        Text("Save Employee", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column {
        Text(text = title, color = Brass, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        HorizontalDivider(color = GlassBorder.copy(alpha=0.5f), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun Field(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, isNumber: Boolean = false) {
    Column(modifier = modifier) {
        Text(text = label, color = InkSoft, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = GlassBorder,
                focusedBorderColor = Brass
            )
        )
    }
}
