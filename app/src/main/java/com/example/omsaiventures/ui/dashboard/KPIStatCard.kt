package com.example.omsaiventures.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omsaiventures.ui.theme.*

@Composable
fun KPIStatCard(
    title: String,
    value: String,
    eyebrow: String,
    modifier: Modifier = Modifier
) {
    // Matches the .panel class from CSS
    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp, // Approximation of multi-layered CSS shadow
                shape = RoundedCornerShape(12.dp),
                ambientColor = LedgerDark.copy(alpha = 0.08f),
                spotColor = LedgerDark.copy(alpha = 0.04f)
            )
            .background(Paper, RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Matches the .panel-head class
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paper.copy(alpha = 0.2f)) // Light glass-like header
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            // .panel-eyebrow
            Text(
                text = eyebrow.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkSoft,
                letterSpacing = 1.1.sp
            )
            // .panel-title
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.padding(top = 4.dp),
                letterSpacing = (-0.2).sp
            )
        }
        
        // Matches the bottom border of .panel-head
        HorizontalDivider(color = GlassBorder, thickness = 1.dp)

        // Main stat body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 32.dp)
        ) {
            Text(
                text = value,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ledger
            )
        }
    }
}
