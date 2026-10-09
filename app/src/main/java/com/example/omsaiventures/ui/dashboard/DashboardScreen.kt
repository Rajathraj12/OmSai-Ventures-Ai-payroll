package com.example.omsaiventures.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
                .verticalScroll(rememberScrollState())
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
                            onDeleteClicked = {
                                viewModel.deleteEmployee(activeEmployee.id)
                            }
                        )
                    }
                } else if (state.activeTab == "offerLetter") {
                    OfferLetterScreen()
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

            // Row 2: Tabs (Payroll Register, Offer Letter, History)
            Row(
                modifier = Modifier
                    .background(Paper.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
    // Glassmorphism Card matches .panel
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Paper, RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
    ) {
        // .panel-head
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paper.copy(alpha = 0.2f))
                .padding(20.dp)
        ) {
            Text(text = "ROSTER", color = InkSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(text = "Employees", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = GlassBorder)

        // Roster Body (Fixed height on mobile so it scrolls independently, or we just let outer column scroll)
        // Here we'll just list them out so they scroll naturally with the page
        employees.forEach { emp ->
            val isActive = emp.id == activeId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isActive) Paper.copy(alpha = 0.6f) else Color.Transparent)
                    .clickable { onEmployeeSelected(emp.id) }
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Active indicator
                if (isActive) {
                    Box(modifier = Modifier.width(4.dp).height(32.dp).background(Stamp))
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // ID Tab
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(listOf(Ledger, LedgerDark)),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = emp.empid, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Role
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = emp.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = emp.role, color = InkSoft, fontSize = 12.sp)
                }

                // Net Pay
                Text(
                    text = "₹${formatCurrency(emp.net)}",
                    color = Ledger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(start = 16.dp))
        }

        // Add Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Ledger, LedgerDark)))
                .clickable { onAddClicked() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("+ Add Employee", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PayslipDetail(
    employee: Employee, 
    company: CompanyProfile,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Payslip Top Header (Logo + Details + Date)
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.weight(1f)) {
                // Logo
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.omsaiventures.R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(4.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                
                // Company Details
                Column {
                    Text(text = company.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = LedgerDark)
                    Text(text = company.sub, fontSize = 11.sp, color = InkSoft, lineHeight = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = company.contact, fontSize = 11.sp, color = InkSoft, lineHeight = 14.sp)
                    Text(text = company.gst, fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Edit Employee",
                color = InkSoft,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onEditClicked() }.padding(end = 16.dp)
            )
            Text(
                text = "Delete",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onDeleteClicked() }
            )
        }
        
        HorizontalDivider(color = GlassBorder)
        
        Text(
            text = "PAY SLIP",
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ledger,
            letterSpacing = 1.sp
        )

        // Employee Info Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaperDim.copy(alpha = 0.5f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow("Employee Name", employee.name, "Date of Joining", employee.doj)
            InfoRow("Designation", employee.role, "Account No.", employee.bank)
            InfoRow("Emp ID", employee.empid, "PAN No.", employee.pan)
            InfoRow("Deputed At", employee.deputedAt, "Bank Name", employee.bankname)
            InfoRow("Store", employee.store, "IFSC Code", employee.ifsc)
        }

        Divider(
            color = Color.Transparent,
            modifier = Modifier.padding(vertical = 1.dp)
        )
        // Simulated Dashed border
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(GlassBorder, Color.Transparent))))

        // Attendance Block
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AttendanceCard("PAID DAYS", employee.paidDays.toString(), Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            AttendanceCard("LWP", employee.lwp.toString(), Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            AttendanceCard("REFUND DAYS", employee.refundDays.toString(), Modifier.weight(1f))
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(GlassBorder, Color.Transparent))))

        // Earnings and Deductions Columns
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Text("EARNINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ledger, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                LineItem("Basic Salary", employee.basic)
                LineItem("Attendance Bonus", employee.attendance)
                LineItem("Performance Bonus", employee.performance)
                LineItem("Festival Bonus", employee.festivalBonus)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LedgerDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gross Earnings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("₹${formatCurrency(employee.gross)}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
            
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(GlassBorder))

            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Text("DEDUCTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ledger, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                if (employee.lwpDeduction > 0) {
                    LineItem("LWP Deduction", employee.lwpDeduction)
                }
                LineItem("DQ Deduction", employee.dq)
                LineItem("Other Deduction", employee.otherDed)
                LineItem("Group Insurance", employee.groupInsurance)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LedgerDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Deduction", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("₹${formatCurrency(employee.ded)}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }

        // Net Pay Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Ledger, LedgerDark)))
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Net Pay", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("₹${formatCurrency(employee.net)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun InfoRow(k1: String, v1: String, k2: String, v2: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(k1, color = InkSoft, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(v1, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(k2, color = InkSoft, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(v2, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttendanceCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = InkSoft, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun LineItem(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Ink)
        Text(formatCurrency(amount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ledger)
    }
}

// Simple Helper using standard Java format
fun formatCurrency(amount: Double): String {
    val format = java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN"))
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    return format.format(amount)
}