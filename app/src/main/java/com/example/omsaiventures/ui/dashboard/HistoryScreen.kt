package com.example.omsaiventures.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.omsaiventures.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    state: DashboardState,
    onDeleteSlip: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Sort and filter the history
    val filteredHistory = state.historicalSlips.filter {
        if (searchQuery.isBlank()) return@filter true
        it.empName.contains(searchQuery, ignoreCase = true) || 
        it.empId.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Employee Name or ID...", color = InkSoft, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = GlassBorder,
                focusedBorderColor = Brass
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // Title
        Text(
            text = "Generated Salary Slips",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LedgerDark,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No slips match your search." else "No historical slips found.",
                    color = InkSoft,
                    fontSize = 14.sp
                )
            }
        } else {
            // List of Slips
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredHistory) { slip ->
                    HistoryCard(slip = slip, onDelete = { onDeleteSlip(slip.id) })
                }
            }
        }
    }
}

@Composable
fun HistoryCard(slip: SalarySlip, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val formattedDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = parser.parse(slip.timestamp)
        if (date != null) formatter.format(date) else slip.timestamp
    } catch (e: Exception) {
        slip.timestamp
    }

    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthName = months.getOrNull(slip.payMonth) ?: slip.payMonth.toString()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slip.empName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LedgerDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "Pay Period: $monthName ${slip.payYear}", fontSize = 12.sp, color = InkSoft, modifier = Modifier.padding(top = 4.dp))
                Text(text = "Generated: $formattedDate", fontSize = 12.sp, color = InkSoft)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "₹${formatCurrency(slip.net)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Ledger)
                Text(text = "Net Pay", fontSize = 10.sp, color = InkSoft)
            }
        }
        
        HorizontalDivider(color = GlassBorder)
        
        Row(
            modifier = Modifier.fillMaxWidth().background(PaperDim.copy(alpha=0.3f)).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DELETE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier
                    .clickable { showDeleteConfirm = true }
                    .padding(8.dp)
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