package com.example.omsaiventures.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun OfferLetterScreen() {
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
                OutlinedTextField(
                    value = dateOfJoining,
                    onValueChange = { dateOfJoining = it },
                    label = { Text("Date of Joining") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("YYYY-MM-DD") }
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Term Start Date") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("YYYY-MM-DD") }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Term End Date") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("YYYY-MM-DD") }
                )
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
                OutlinedTextField(
                    value = netSalary,
                    onValueChange = { netSalary = it },
                    label = { Text("Net Salary") },
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
                onClick = { /* Generate PDF Action */ },
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