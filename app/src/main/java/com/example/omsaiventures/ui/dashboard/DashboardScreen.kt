package com.example.omsaiventures.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omsaiventures.ui.theme.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()
    
    // State to handle our Add/Edit Employee Modal
    var showEmployeeModal by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var employeeToDelete by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(state.error) {
        state.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperDim) // Background color matching the web body
    ) {
        // 1. Web-Style Navigation Bar (Masthead)
        Masthead(
            employeeCount = state.employees.size,
            activeTab = state.activeTab,
            onTabChange = { viewModel.setActiveTab(it) }
        )

        // Main Scrollable Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Brass)
                }
            } else {
                if (state.activeTab == "payroll") {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            // Mobile Layout: Show Employee Roster list first
                            EmployeeRegister(
                                employees = state.employees,
                                activeId = state.activeEmployeeId,
                                onEmployeeSelected = { viewModel.setActiveEmployee(it) },
                                onAddClicked = {
                                    employeeToEdit = null
                                    showEmployeeModal = true
                                }
                            )
                        }

                        item {
                            // Then show the active Employee's Payslip
                            val activeEmployee = state.employees.find { it.id == state.activeEmployeeId }
                            if (activeEmployee != null) {
                                PayslipDetail(
                                    employee = activeEmployee,
                                    company = state.company,
                                    onEditClicked = {
                                        employeeToEdit = activeEmployee
                                        showEmployeeModal = true
                                    },
                                    onSaveToHistoryClicked = {
                                        viewModel.saveSlipToHistory(activeEmployee)
                                    },
                                    onPrintClicked = {
                                        com.example.omsaiventures.utils.PdfGenerator.generatePayslipPdf(
                                            context = context,
                                            employee = activeEmployee,
                                            company = state.company
                                        )
                                    },
                                    onDeleteClicked = {
                                        employeeToDelete = activeEmployee.id
                                    }
                                )
                            }
                        }
                    }
                } else if (state.activeTab == "dashboard") {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        DashboardTabContent(state = state, onRefresh = { viewModel.fetchAiInsights() })
                    }
                } else if (state.activeTab == "offerLetter") {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        OfferLetterScreen()
                    }
                } else {
                    // Placeholder for history tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${state.activeTab} coming soon...",
                            color = InkSoft,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }

    if (showEmployeeModal) {
        EmployeeModal(
            employee = employeeToEdit,
            onDismiss = { showEmployeeModal = false },
            onSave = { updatedEmployee ->
                viewModel.saveEmployee(updatedEmployee)
                showEmployeeModal = false
            }
        )
    }

    if (employeeToDelete != null) {
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("Delete Employee") },
            text = { Text("Are you sure you want to delete this employee? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEmployee(employeeToDelete!!)
                    employeeToDelete = null
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

// ----------------------------------------------------
// UI COMPONENTS
// ----------------------------------------------------

@Composable
fun Masthead(employeeCount: Int, activeTab: String, onTabChange: (String) -> Unit) {
    // Top Bar (Deep Navy with Brass bottom border)
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ledger)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Row 1: Logo, Title, and Logout Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo placeholder + Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.omsaiventures.R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .padding(4.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "OmSai ", color = Paper, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Ventures", color = Brass, fontSize = 24.sp, fontStyle = FontStyle.Italic)
                }

                // Logout Button
                Box(
                    modifier = Modifier
                        .border(1.dp, Paper.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Logout", color = Paper.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Tabs (Dashboard, Payroll Register, Offer Letter, History)
            Row(
                modifier = Modifier
                    .background(Paper.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MastheadTab("Dashboard", activeTab == "dashboard") { onTabChange("dashboard") }
                MastheadTab("Payroll Register", activeTab == "payroll") { onTabChange("payroll") }
                MastheadTab("Offer Letters", activeTab == "offerLetter") { onTabChange("offerLetter") }
                MastheadTab("History", activeTab == "history") { onTabChange("history") }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$employeeCount EMPLOYEES ON FILE",
                color = Paper.copy(alpha = 0.55f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        HorizontalDivider(color = Brass, thickness = 3.dp)
    }
}

@Composable
fun MastheadTab(title: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) Brass else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isActive) Color.White else Paper.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmployeeRegister(
    employees: List<Employee>,
    activeId: String?,
    onEmployeeSelected: (String) -> Unit,
    onAddClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
    ) {
        // Roster Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "ROSTER", color = InkSoft, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = "Employees", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
        HorizontalDivider(color = GlassBorder)

        // Roster Body
        Column(modifier = Modifier.fillMaxWidth()) {
            employees.forEach { emp ->
                val isActive = emp.id == activeId
                val rowBg = if (isActive) Color.White else Color.White
                
                Box(modifier = Modifier.fillMaxWidth().background(rowBg).clickable { onEmployeeSelected(emp.id) }) {
                    // Left Active Indicator
                    if (isActive) {
                        Box(modifier = Modifier.width(4.dp).height(48.dp).background(Stamp).align(Alignment.CenterStart))
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ID Badge
                        Box(
                            modifier = Modifier
                                .background(Ledger, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .widthIn(min = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emp.empid, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Name & Role
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = emp.name.ifEmpty { "Unnamed" },
                                color = Ink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = emp.role.ifEmpty { "—" }, color = InkSoft, fontSize = 10.sp)
                        }

                        // Net Pay
                        Text(
                            text = "₹${formatCurrency(emp.net)}",
                            color = Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        
        // Add Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ledger)
                .clickable { onAddClicked() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("+ Add Employee", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PayslipDetail(
    employee: Employee, 
    company: CompanyProfile,
    onEditClicked: () -> Unit,
    onSaveToHistoryClicked: () -> Unit,
    onPrintClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
    ) {
        // Actions Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onEditClicked, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, GlassBorder)) {
                Text("Edit Employee", color = InkSoft, fontSize = 12.sp)
            }
            OutlinedButton(onClick = onSaveToHistoryClicked, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, GlassBorder)) {
                Text("Save to History", color = InkSoft, fontSize = 12.sp)
            }
            OutlinedButton(onClick = onPrintClicked, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, GlassBorder)) {
                Text("Print PDF", color = InkSoft, fontSize = 12.sp)
            }
            OutlinedButton(onClick = onDeleteClicked, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, GlassBorder)) {
                Text("Delete", color = InkSoft, fontSize = 12.sp)
            }
        }

        HorizontalDivider(color = GlassBorder)

        // Payslip Top Header (Company Details)
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                // Logo
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.omsaiventures.R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                // Company Details
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(text = company.name, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = LedgerDark, letterSpacing = (-0.5).sp)
                    Text(text = company.sub.replace("\n", ", "), fontSize = 9.sp, color = InkSoft, lineHeight = 12.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = company.contact.replace("\n", " • "), fontSize = 8.sp, color = InkSoft, fontWeight = FontWeight.Medium)
                    Text(text = company.gst, fontSize = 8.sp, color = InkSoft, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Month Year Box
            Box(
                modifier = Modifier
                    .background(PaperDim, RoundedCornerShape(4.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                    Text("${months.getOrNull(employee.payMonth) ?: "Unknown"}", color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${employee.payYear}", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Issued 09 Oct 2026", color = InkSoft, fontSize = 7.sp)
                }
            }
        }
        
        HorizontalDivider(color = GlassBorder)
        
        Text(
            text = "PAY SLIP",
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            letterSpacing = 1.sp
        )
        
        HorizontalDivider(color = GlassBorder)

        // Employee Info List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Employee Name", employee.name, Modifier.weight(1f))
                InfoItem("Date of Joining", employee.doj, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Designation", employee.role, Modifier.weight(1f))
                InfoItem("Account No.", employee.bank, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Emp ID", employee.empid, Modifier.weight(1f))
                InfoItem("PAN No.", employee.pan, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Deputed At", employee.deputedAt, Modifier.weight(1f))
                InfoItem("Bank Name", employee.bankname, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Store", employee.store, Modifier.weight(1f))
                InfoItem("IFSC Code", employee.ifsc, Modifier.weight(1f))
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder))

        // Attendance Block
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(4.dp)).padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PAID DAYS", color = InkSoft, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text(employee.paidDays.toString(), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Box(
                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(4.dp)).padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LWP", color = InkSoft, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text(if (employee.lwp == 0) "—" else employee.lwp.toString(), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Box(
                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(4.dp)).padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REFUND DAYS", color = InkSoft, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text(if (employee.refundDays == 0) "—" else employee.refundDays.toString(), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder))

        // Earnings and Deductions Columns
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Text("EARNINGS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Ink, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                LineItemWeb("Basic Salary", employee.basic)
                LineItemWeb("Attendance Bonus", employee.attendance)
                LineItemWeb("Performance Bonus", employee.performance)
                LineItemWeb("Festival Bonus", employee.festivalBonus)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Ink)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Gross Earnings", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Ink)
                    Text("₹${formatCurrency(employee.gross)}", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Ink)
                }
            }
            
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(GlassBorder))

            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Text("DEDUCTIONS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Ink, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                if (employee.lwpDeduction > 0) {
                    LineItemWeb("LWP Deduction", employee.lwpDeduction)
                }
                LineItemWeb("DQ Deduction", employee.dq)
                LineItemWeb("Other Deduction", employee.otherDed)
                LineItemWeb("Group Insurance", employee.groupInsurance)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Ink)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total Deduction", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Ink)
                    Text("₹${formatCurrency(employee.ded)}", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Ink)
                }
            }
        }

        // Net Pay Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ledger)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Net Pay", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("₹${formatCurrency(employee.net)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
        
        // Net pay in words
        Box(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)
        ) {
            Text(
                text = "Net pay in words: Rupees (Implementation specific) only",
                color = InkSoft,
                fontSize = 10.sp,
                fontStyle = FontStyle.Italic
            )
        }
        
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder.copy(alpha=0.5f)))
        
        // Remarks Section
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("REMARKS / REASON FOR ADJUSTMENT", color = Brass, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
                    .padding(12.dp)
                    .height(60.dp)
            ) {
                Text(
                    text = employee.remarks.ifEmpty { "State the reason for any deduction, bonus, or pay adjustment for this period." },
                    color = if (employee.remarks.isEmpty()) InkSoft.copy(alpha=0.5f) else Ink,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label.uppercase(), color = InkSoft, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(value.ifEmpty { "—" }, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun LineItemWeb(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 9.sp, color = Ink)
        Box(
            modifier = Modifier.background(PaperDim, RoundedCornerShape(2.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(formatCurrency(amount), fontSize = 9.sp, color = Ink)
        }
    }
}

// Simple Helper using standard Java format
fun formatCurrency(amount: Double): String {
    val format = java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN"))
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    return format.format(amount)
}