package com.example.omsaiventures.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.style.TextAlign
import com.example.omsaiventures.ui.theme.*
import java.util.*

@Composable
fun HistoryScreen(
    state: DashboardState,
    context: android.content.Context,
    onDeleteSlip: (String) -> Unit,
    onSeedSampleSlips: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Additional filters matching the web mockup
    var filterMonth by remember { mutableStateOf(9) } // 9 represents October (0-indexed)
    var filterYear by remember { mutableStateOf("2026") }
    
    var showMonthDropdown by remember { mutableStateOf(false) }
    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    // Filter by both search query AND selected month/year
    val filteredHistory = state.historicalSlips.filter { slip ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            slip.empName.contains(searchQuery, ignoreCase = true) || 
            slip.empId.contains(searchQuery, ignoreCase = true)
        }
        val matchesMonth = slip.payMonth == filterMonth
        val matchesYear = slip.payYear.toString() == filterYear
        
        matchesSearch && matchesMonth && matchesYear
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mobile-Optimized Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name or ID...", color = InkSoft) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = GlassBorder,
                focusedBorderColor = Brass
            ),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Text("X", color = InkSoft, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Month Dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = months[filterMonth],
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showMonthDropdown = true },
                    enabled = false,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Ink,
                        disabledBorderColor = GlassBorder,
                        disabledContainerColor = Color.White
                    )
                )
                DropdownMenu(
                    expanded = showMonthDropdown,
                    onDismissRequest = { showMonthDropdown = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    months.forEachIndexed { index, month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                filterMonth = index
                                showMonthDropdown = false
                            }
                        )
                    }
                }
            }
            // Year Input
            OutlinedTextField(
                value = filterYear,
                onValueChange = { filterYear = it },
                modifier = Modifier.weight(0.5f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = GlassBorder,
                    focusedBorderColor = Brass
                )
            )
        }

        // Title
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Salary Slips for ${months[filterMonth]} $filterYear",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LedgerDark
            )
            Text(
                text = "${filteredHistory.size} records",
                fontSize = 12.sp,
                color = InkSoft,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = "No slips match \"$searchQuery\"",
                        color = InkSoft,
                        fontSize = 14.sp
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Brass.copy(alpha = 0.15f), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = LedgerDark,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "No Historical Slips Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = LedgerDark
                        )
                        Text(
                            text = "Salary slips saved from the Payroll tab will appear here. You can also generate sample slips below to preview this section.",
                            color = InkSoft,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onSeedSampleSlips,
                            colors = ButtonDefaults.buttonColors(containerColor = LedgerDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Sample Slips", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Table Layout container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                    .horizontalScroll(rememberScrollState()) // Allow horizontal scrolling for table
            ) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Emp ID", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LedgerDark, modifier = Modifier.width(100.dp))
                    Text("Employee Name", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LedgerDark, modifier = Modifier.width(180.dp))
                    Text("Gross", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LedgerDark, modifier = Modifier.width(100.dp))
                    Text("Deductions", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LedgerDark, modifier = Modifier.width(100.dp))
                    Text("Net Pay", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LedgerDark, modifier = Modifier.width(100.dp))
                    Text("Action", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LedgerDark, modifier = Modifier.width(160.dp))
                }

                HorizontalDivider(color = GlassBorder)

                // Table Rows
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp) // Constrain height if needed
                ) {
                    items(filteredHistory) { slip ->
                        HistoryTableRow(
                            slip = slip,
                            onDelete = { onDeleteSlip(slip.id) },
                            onPrintSlip = {
                                val employee = state.employees.find { it.id == slip.empId }
                                if (employee != null) {
                                    com.example.omsaiventures.utils.PdfGenerator.generatePayslipPdf(
                                        context = context,
                                        employee = employee,
                                        company = state.company
                                    )
                                } else {
                                    android.widget.Toast.makeText(context, "Cannot find employee data to generate PDF.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        HorizontalDivider(color = GlassBorder.copy(alpha=0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTableRow(slip: SalarySlip, onDelete: () -> Unit, onPrintSlip: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(slip.empId, fontSize = 12.sp, color = Ink, modifier = Modifier.width(100.dp))
        Text(slip.empName, fontSize = 12.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(180.dp))
        Text("₹${formatCurrency(slip.gross)}", fontSize = 12.sp, color = InkSoft, modifier = Modifier.width(100.dp))
        Text("₹${formatCurrency(slip.ded)}", fontSize = 12.sp, color = InkSoft, modifier = Modifier.width(100.dp))
        Text("₹${formatCurrency(slip.net)}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = LedgerDark, modifier = Modifier.width(100.dp))
        
        // Actions
        Row(modifier = Modifier.width(160.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "View PDF",
                fontSize = 10.sp,
                color = InkSoft,
                modifier = Modifier
                    .border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
                    .clickable { onPrintSlip() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
            Text(
                text = "Delete",
                fontSize = 10.sp,
                color = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier
                    .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .clickable { showDeleteConfirm = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Payslip Record") },
            text = { Text("Are you sure you want to delete this historical payslip? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}