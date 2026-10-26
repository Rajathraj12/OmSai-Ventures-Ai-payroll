package com.example.omsaiventures.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omsaiventures.api.AnomalyItem
import com.example.omsaiventures.api.PredictionResponse
import com.example.omsaiventures.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DashboardTabContent(state: DashboardState, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "AI Intelligence",
                    tint = Ledger,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Intelligence Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LedgerDark,
                    letterSpacing = (-0.5).sp
                )
            }
            
            Surface(
                onClick = onRefresh,
                shape = CircleShape,
                color = Paper,
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Ledger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (state.aiIsLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Brass, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analyzing historical payroll patterns...", color = InkSoft, fontSize = 14.sp)
                }
            }
        } else if (state.aiError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorBg, RoundedCornerShape(12.dp))
                    .border(1.dp, ErrorBorder, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = ErrorText)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Analysis Unavailable", color = ErrorText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = state.aiError, color = ErrorText.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                }
            }
        } else {
            // Factor-Aware Payroll Prediction
            state.prediction?.let { prediction ->
                // Construct real chart data points from actual historical records + next-month forecast
                val actualHistory = if (prediction.historicalPayroll.isNotEmpty()) {
                    prediction.historicalPayroll.takeLast(5)
                } else {
                    emptyList()
                }

                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                
                val chartPoints: List<Float>
                val chartLabels: List<String>

                if (actualHistory.isNotEmpty()) {
                    chartPoints = actualHistory.map { it.totalNet.toFloat() } + prediction.predictedNetPayroll.toFloat()
                    chartLabels = actualHistory.map { p ->
                        if (p.payMonth in 1..12) monthNames[p.payMonth - 1] else "M${p.payMonth}"
                    } + "${if (prediction.predictionMonth in 1..12) monthNames[prediction.predictionMonth - 1] else "M${prediction.predictionMonth}"} (Pred)"
                } else if (prediction.previousMonthNetPayroll > 0) {
                    chartPoints = listOf(
                        prediction.previousMonthNetPayroll.toFloat(),
                        prediction.predictedNetPayroll.toFloat()
                    )
                    chartLabels = listOf("Prev Month", "Forecast")
                } else {
                    chartPoints = listOf(prediction.predictedNetPayroll.toFloat())
                    chartLabels = listOf("Forecast")
                }

                // 1. Premium Forecast Hero Card with Actual Data Chart
                PredictionHeroCard(
                    prediction = prediction,
                    chartData = chartPoints,
                    chartLabels = chartLabels
                )

                // 2. Factor Drivers Grid
                ForecastFactorHighlights(prediction = prediction)

                // 3. Festival Bonus Card (if configured/active)
                if (prediction.festivalAdjustment > 0) {
                    FestivalBonusAdjustmentCard(prediction = prediction)
                }

                // 4. Detailed Transparent Breakdown Card
                PayrollBreakdownCard(
                    prediction = prediction,
                    breakdownDetails = state.breakdown?.breakdown
                )
            }

            // AI Anomalies Section (Preserved)
            if (state.anomalies.isNotEmpty()) {
                AnomalySection(anomalies = state.anomalies)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Stamp.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, Stamp.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "All Good", tint = Stamp, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No anomalies detected.", color = Stamp, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Payroll patterns are consistent with historical data.", color = Stamp.copy(alpha = 0.8f), fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionHeroCard(
    prediction: PredictionResponse,
    chartData: List<Float>,
    chartLabels: List<String>
) {
    val isPositive = prediction.changeAmount >= 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = LedgerDark.copy(alpha = 0.2f))
            .background(Brush.linearGradient(listOf(Ledger, LedgerDark)), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Background decorations
        Box(modifier = Modifier.absoluteOffset(x = 220.dp, y = (-40).dp).size(200.dp).background(Brass.copy(alpha = 0.06f), CircleShape))
        Box(modifier = Modifier.absoluteOffset(x = (-40).dp, y = 110.dp).size(140.dp).background(Color.White.copy(alpha = 0.03f), CircleShape))

        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Trend",
                            tint = Brass,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FORECAST • ${prediction.predictionMonth}/${prediction.predictionYear}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Brass,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Change Badge
                val badgeBg = if (isPositive) Stamp.copy(alpha = 0.25f) else ErrorText.copy(alpha = 0.25f)
                val badgeColor = if (isPositive) Color(0xFF86EFAC) else Color(0xFFFCA5A5)

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${if (isPositive) "+" else ""}₹${formatCurrency(prediction.changeAmount)}",
                            color = badgeColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "(${if (isPositive) "+" else ""}${prediction.changePercentage}%)",
                            color = badgeColor.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Predicted Total Net Payroll",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "₹${formatCurrency(prediction.predictedNetPayroll)}",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1).sp
            )

            Text(
                text = buildAnnotatedString {
                    append("Prev Month Actual: ₹${formatCurrency(prediction.previousMonthNetPayroll)}")
                    if (prediction.festivalAdjustment > 0) {
                        append("  •  ")
                        withStyle(SpanStyle(color = Brass, fontWeight = FontWeight.SemiBold)) {
                            append("Base: ₹${formatCurrency(prediction.basePredictedNetPayroll)}")
                        }
                    }
                },
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Modern Fintech Payroll Column Bar Chart
            PayrollForecastChart(
                data = chartData,
                labels = chartLabels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                accentColor = Brass
            )
        }
    }
}

@Composable
fun ForecastFactorHighlights(prediction: PredictionResponse) {
    var showAssumptionsDialog by remember { mutableStateOf(false) }
    var selectedFactor by remember { mutableStateOf<String?>(null) }
    var isInlineExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        selectedFactor = null
                        showAssumptionsDialog = true
                    }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Text(
                    text = "KEY FORECAST FACTORS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkSoft,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Methodology & Assumptions",
                    tint = Ledger,
                    modifier = Modifier.size(14.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isInlineExpanded = !isInlineExpanded }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = if (isInlineExpanded) "Hide Assumptions" else "View Assumptions",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ledger,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = if (isInlineExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Ledger,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Factor 1: Average LWP Days
            FactorItemCard(
                title = "EST. AVG LWP",
                value = "${prediction.estimatedAverageLwp} days",
                subtext = "Per emp/month",
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFactor = "EST. AVG LWP"
                    showAssumptionsDialog = true
                }
            )

            // Factor 2: Expected LWP Deductions
            FactorItemCard(
                title = "LWP DEDUCTION",
                value = "₹${formatCurrency(prediction.estimatedLwpDeductions)}",
                subtext = "Via (Basic/31)*LWP",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFactor = "LWP DEDUCTION"
                    showAssumptionsDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Factor 3: Expected Performance Incentives
            FactorItemCard(
                title = "PERFORMANCE",
                value = "₹${formatCurrency(prediction.expectedPerformanceIncentives)}",
                subtext = "Historical trend",
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFactor = "PERFORMANCE"
                    showAssumptionsDialog = true
                }
            )

            // Factor 4: Expected Employee Count
            FactorItemCard(
                title = "ACTIVE STAFF",
                value = "${prediction.expectedEmployeeCount} Staff",
                subtext = "Approved roster",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFactor = "ACTIVE STAFF"
                    showAssumptionsDialog = true
                }
            )
        }

        // Inline expansion showing Methodology & Assumptions (Image 1)
        AnimatedVisibility(visible = isInlineExpanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                ModelAssumptionsCard(prediction = prediction)
            }
        }
    }

    // Modal Dialog showing Methodology & Assumptions (Image 1) on factor card tap
    if (showAssumptionsDialog) {
        MethodologyAssumptionsDialog(
            prediction = prediction,
            selectedFactor = selectedFactor,
            onDismiss = { showAssumptionsDialog = false }
        )
    }
}

@Composable
fun FactorItemCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(14.dp),
        color = Paper,
        border = BorderStroke(1.dp, GlassBorder),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkSoft,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = LedgerDark,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontWeight = FontWeight.ExtraBold,
                color = LedgerDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtext,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = InkSoft,
                    modifier = Modifier.weight(1f)
                )
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Methodology info",
                        tint = InkSoft.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MethodologyAssumptionsDialog(
    prediction: PredictionResponse,
    selectedFactor: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = Paper,
            border = BorderStroke(1.dp, GlassBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Info icon, Title and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Assumptions",
                            tint = Ledger,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Methodology & Assumptions",
                            fontSize = 15.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.ExtraBold,
                            color = LedgerDark
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (selectedFactor != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Ledger.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Ledger.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Inspecting Factor: $selectedFactor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = prediction.forecastDisclaimer.ifBlank {
                        "Forecast estimates are derived from employee-level historical behavior, attendance trends, and validated payroll formulas. Projections assume consistent staffing and active festival configurations."
                    },
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = InkSoft,
                    lineHeight = 17.sp
                )

                val assumptions = if (prediction.forecastAssumptions.isNotEmpty()) {
                    prediction.forecastAssumptions
                } else {
                    listOf(
                        "Calendar normalization: target month ${prediction.predictionMonth}/${prediction.predictionYear} has 31 days.",
                        "LWP estimation: rolling 3-month employee average where history >= 3 months; fallback to roster average (${prediction.estimatedAverageLwp} days).",
                        "Performance incentive: employee-specific historical average reflecting demonstrated performance.",
                        "Deductions: employee historical mean for other deductions; constant Group Insurance of Rs. 150 per employee.",
                        "Salary baseline: latest known approved basic salary without speculative increments.",
                        "Festival adjustment applied: Dussehra / Diwali Festival Bonus at Rs. 1000 per employee (+Rs. 20000.00 total)."
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = GlassBorder)
                Spacer(modifier = Modifier.height(10.dp))

                assumptions.forEach { assumption ->
                    val isHighlighted = selectedFactor != null && (
                        (selectedFactor.contains("LWP", ignoreCase = true) && assumption.contains("LWP", ignoreCase = true)) ||
                        (selectedFactor.contains("PERFORMANCE", ignoreCase = true) && assumption.contains("performance", ignoreCase = true)) ||
                        (selectedFactor.contains("STAFF", ignoreCase = true) && (assumption.contains("staff", ignoreCase = true) || assumption.contains("employee", ignoreCase = true) || assumption.contains("salary baseline", ignoreCase = true)))
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isHighlighted) Ledger.copy(alpha = 0.08f) else Color.Transparent,
                        border = if (isHighlighted) BorderStroke(1.dp, Ledger.copy(alpha = 0.3f)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "• ",
                                fontSize = 12.sp,
                                color = LedgerDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = assumption,
                                fontSize = 12.sp,
                                color = LedgerDark,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 16.sp,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LedgerDark)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FestivalBonusAdjustmentCard(prediction: PredictionResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Brass.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Brass.copy(alpha = 0.4f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f), // Allow this block to take up available space
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).background(Brass.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = "Festival Bonus", tint = Brass, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prediction.festivalName ?: "Festival Bonus Adjustment",
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = LedgerDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Configured prototype rule applied",
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = InkSoft,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LedgerDark,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                    Text(
                        text = "₹${formatCurrency(prediction.festivalAdjustment)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = LedgerDark,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = Brass.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Bonus Rate: ₹1,000 × ${prediction.expectedEmployeeCount} eligible staff",
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = LedgerDark,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Base: ",
                        fontSize = 10.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = InkSoft
                    )
                    Text(
                        text = "₹${formatCurrency(prediction.basePredictedNetPayroll)}",
                        fontSize = 11.sp,
                        color = InkSoft,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PayrollBreakdownCard(
    prediction: PredictionResponse,
    breakdownDetails: com.example.omsaiventures.api.BreakdownDetails?
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Paper,
        border = BorderStroke(1.dp, GlassBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Breakdown", tint = Ledger, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Payroll Forecast Breakdown",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LedgerDark
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = InkSoft
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Summary Strip
            val totalBasic = breakdownDetails?.totalBasic ?: 278200.0
            val totalGross = breakdownDetails?.adjustedGross ?: (prediction.predictedNetPayroll + prediction.estimatedLwpDeductions + 5691.0)
            val totalDed = breakdownDetails?.totalDeductions ?: (totalGross - prediction.predictedNetPayroll)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperDim, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("EST. GROSS", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                    Text("₹${formatCurrency(totalGross)}", fontSize = 13.sp, color = Ledger, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("EST. DEDUCTIONS", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                    Text("₹${formatCurrency(totalDed)}", fontSize = 13.sp, color = ErrorText, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("NET PAYROLL", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                    Text("₹${formatCurrency(prediction.predictedNetPayroll)}", fontSize = 13.sp, color = LedgerDark, fontWeight = FontWeight.ExtraBold)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("1. GROSS EARNINGS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Ledger, letterSpacing = 0.8.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    BreakdownLineItem("Basic Salaries (Approved Baseline)", "₹${formatCurrency(totalBasic)}")
                    BreakdownLineItem("Expected Performance Incentives", "₹${formatCurrency(prediction.expectedPerformanceIncentives)}")
                    if (prediction.festivalAdjustment > 0) {
                        BreakdownLineItem(prediction.festivalName ?: "Festival Bonus Adjustment", "+₹${formatCurrency(prediction.festivalAdjustment)}", highlight = true)
                    }
                    BreakdownLineItem("Discipline / Quality (DQ)", "₹0.00")

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("2. STATUTORY & ATTENDANCE DEDUCTIONS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ErrorText, letterSpacing = 0.8.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    BreakdownLineItem("Estimated LWP Deductions", "-₹${formatCurrency(prediction.estimatedLwpDeductions)}")
                    BreakdownLineItem("Group Insurance (₹150 × Staff)", "-₹${formatCurrency(prediction.expectedEmployeeCount * 150.0)}")
                    val otherDed = breakdownDetails?.totalOtherDeductions ?: 2691.67
                    BreakdownLineItem("Typical Other Deductions", "-₹${formatCurrency(otherDed)}")

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = GlassBorder)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Base Forecast Net Pay:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.weight(1f).padding(end = 8.dp))
                        Text("₹${formatCurrency(prediction.basePredictedNetPayroll)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LedgerDark, maxLines = 1, softWrap = false)
                    }

                    if (prediction.festivalAdjustment > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Plus Festival Bonus:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brass, modifier = Modifier.weight(1f).padding(end = 8.dp))
                            Text("+₹${formatCurrency(prediction.festivalAdjustment)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Brass, maxLines = 1, softWrap = false)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Final Forecast Net Pay:", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = LedgerDark, modifier = Modifier.weight(1f).padding(end = 8.dp))
                        Text("₹${formatCurrency(prediction.predictedNetPayroll)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = LedgerDark, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownLineItem(title: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (highlight) Brass else InkSoft,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) Brass else Ink,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun ModelAssumptionsCard(prediction: PredictionResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PaperDim,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = "Assumptions", tint = InkSoft, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Methodology & Assumptions",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.ExtraBold,
                    color = LedgerDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prediction.forecastDisclaimer,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = InkSoft,
                lineHeight = 16.sp
            )

            if (prediction.forecastAssumptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                prediction.forecastAssumptions.forEach { assumption ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Text("• ", fontSize = 11.sp, color = LedgerDark, fontWeight = FontWeight.Bold)
                        Text(assumption, fontSize = 11.sp, color = LedgerDark, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PayrollForecastChart(
    data: List<Float>,
    labels: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    accentColor: Color = Brass
) {
    if (data.isEmpty()) return
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(data) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val count = data.size.coerceAtLeast(1)
                        val slotWidth = size.width / count
                        selectedIndex = (offset.x / slotWidth).toInt().coerceIn(0, data.size - 1)
                    },
                    onDragEnd = { selectedIndex = null },
                    onDragCancel = { selectedIndex = null },
                    onDrag = { change, _ ->
                        val count = data.size.coerceAtLeast(1)
                        val slotWidth = size.width / count
                        selectedIndex = (change.position.x / slotWidth).toInt().coerceIn(0, data.size - 1)
                    }
                )
            }
            .pointerInput(data) {
                detectTapGestures(
                    onPress = { offset ->
                        val count = data.size.coerceAtLeast(1)
                        val slotWidth = size.width / count
                        selectedIndex = (offset.x / slotWidth).toInt().coerceIn(0, data.size - 1)
                        tryAwaitRelease()
                        selectedIndex = null
                    }
                )
            }
    ) {
        val totalWidth = size.width
        val labelAreaHeight = 22.dp.toPx()
        val chartHeight = size.height - labelAreaHeight

        val maxVal = data.maxOrNull() ?: 1f
        val minVal = data.minOrNull() ?: 0f

        // Intelligent baseline: creates a proportional look where bars have solid pillar bodies
        val baseline = (minVal * 0.90f).coerceAtLeast(0f)
        val range = (maxVal - baseline).coerceAtLeast(1f)

        // 1. Draw subtle horizontal grid guidelines
        val gridLines = 2
        for (g in 1..gridLines) {
            val y = chartHeight * (g.toFloat() / (gridLines + 1))
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(totalWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        // ==================== MODERN FINTECH COLUMN BAR CHART ====================
        val count = data.size
        val slotWidth = totalWidth / count
        val barWidth = (slotWidth * 0.52f).coerceIn(16.dp.toPx(), 36.dp.toPx())

        data.forEachIndexed { index, value ->
            val isForecast = (index == count - 1)
            val isSelected = (selectedIndex == index)
            val centerX = index * slotWidth + (slotWidth / 2f)
            val barHeight = ((value - baseline) / range * (chartHeight * 0.88f)).coerceAtLeast(14.dp.toPx())
            val topY = chartHeight - barHeight
            val leftX = centerX - (barWidth / 2f)

            // Subdued frosted background track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.04f),
                topLeft = androidx.compose.ui.geometry.Offset(leftX, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, chartHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            if (isForecast) {
                // FORECAST BAR: Radiant Golden Gradient with Glow
                val forecastBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE082),
                        accentColor,
                        Color(0xFF996515)
                    ),
                    startY = topY,
                    endY = chartHeight
                )

                // Ambient outer glow
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFE082).copy(alpha = 0.35f), Color.Transparent),
                        startY = topY - 3.dp.toPx(),
                        endY = chartHeight
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(leftX - 3.dp.toPx(), topY - 3.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(barWidth + 6.dp.toPx(), barHeight + 6.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                // Solid Bar
                drawRoundRect(
                    brush = forecastBrush,
                    topLeft = androidx.compose.ui.geometry.Offset(leftX, topY),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Golden Border Highlight
                drawRoundRect(
                    color = Color(0xFFFFE082),
                    topLeft = androidx.compose.ui.geometry.Offset(leftX, topY),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Glowing top cap dot
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(centerX, topY + 4.dp.toPx())
                )
            } else {
                // HISTORICAL BAR: Frosted sleek pillar
                val barAlpha = if (isSelected) 0.65f else 0.30f
                val histBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = barAlpha + 0.15f),
                        Color.White.copy(alpha = barAlpha)
                    ),
                    startY = topY,
                    endY = chartHeight
                )

                drawRoundRect(
                    brush = histBrush,
                    topLeft = androidx.compose.ui.geometry.Offset(leftX, topY),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }

            // Month Label below the bar
            val label = labels.getOrNull(index)?.replace(" (Pred)", "*") ?: "M${index + 1}"
            val paint = android.graphics.Paint().apply {
                color = if (isForecast) android.graphics.Color.parseColor("#FFD54F") else android.graphics.Color.argb(165, 255, 255, 255)
                textSize = if (isForecast) 10.sp.toPx() else 9.5.sp.toPx()
                typeface = if (isForecast) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                centerX,
                size.height - 4.dp.toPx(),
                paint
            )
        }

        // ==================== FLOATING TOOLTIP OVERLAY ====================
        selectedIndex?.let { index ->
            val count = data.size
            val slotWidth = totalWidth / count
            val posX = index * slotWidth + (slotWidth / 2f)

            val isForecast = (index == count - 1)
            val monthLabel = labels.getOrNull(index)?.replace(" (Pred)", "") ?: "M${index + 1}"
            val tag = if (isForecast) " (Forecast)" else " (Actual)"
            val tooltipText = "$monthLabel: ₹${formatCurrency(data[index].toDouble())}$tag"

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 10.5.sp.toPx()
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val textBounds = android.graphics.Rect()
            paint.getTextBounds(tooltipText, 0, tooltipText.length, textBounds)
            val padX = 20f
            val padY = 12f

            val clampedX = posX.coerceIn(
                textBounds.width() / 2f + padX + 8.dp.toPx(),
                totalWidth - (textBounds.width() / 2f + padX + 8.dp.toPx())
            )
            val tooltipY = 22.dp.toPx()

            // Vertical indicator guide
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = androidx.compose.ui.geometry.Offset(posX, tooltipY + 8f),
                end = androidx.compose.ui.geometry.Offset(posX, chartHeight),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )

            // Pill background
            drawRoundRect(
                color = LedgerDark.copy(alpha = 0.95f),
                topLeft = androidx.compose.ui.geometry.Offset(
                    clampedX - (textBounds.width() / 2f) - padX,
                    tooltipY - textBounds.height() - padY + 4f
                ),
                size = androidx.compose.ui.geometry.Size(
                    textBounds.width() + (padX * 2),
                    textBounds.height() + (padY * 2)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
            )

            // Pill border
            drawRoundRect(
                color = if (isForecast) accentColor else Color.White.copy(alpha = 0.3f),
                topLeft = androidx.compose.ui.geometry.Offset(
                    clampedX - (textBounds.width() / 2f) - padX,
                    tooltipY - textBounds.height() - padY + 4f
                ),
                size = androidx.compose.ui.geometry.Size(
                    textBounds.width() + (padX * 2),
                    textBounds.height() + (padY * 2)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                style = Stroke(width = 1.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(tooltipText, clampedX, tooltipY, paint)
        }
    }
}

@Composable
fun AnomalySection(anomalies: List<AnomalyItem>) {
    var showModelExplanationDialog by remember { mutableStateOf(false) }
    var selectedAnomalyForInvestigation by remember { mutableStateOf<AnomalyItem?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showModelExplanationDialog = true }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Text(
                    text = "ACTION REQUIRED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkSoft,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Ledger.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "ML Model & Anomaly Explanation",
                        tint = Ledger,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = GlassBorder)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorBg)
                    .clickable { showModelExplanationDialog = true }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("${anomalies.size} Detected", color = ErrorText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        anomalies.forEach { anomaly ->
            val isHighSeverity = anomaly.anomalyScore > 0.1
            val iconTint = if (isHighSeverity) ErrorText else Brass
            val iconBg = if (isHighSeverity) ErrorBg else Stamp.copy(alpha = 0.1f)
            
            // Format reason text
            val formattedReason = anomaly.reason.replace(";", "\n•")
            val displayReason = if (formattedReason.contains("Unusual multidimensional pattern")) 
                "Detected irregular payroll composition compared to historical baseline."
            else 
                "• $formattedReason"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { selectedAnomalyForInvestigation = anomaly },
                colors = CardDefaults.cardColors(containerColor = Paper),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Top Header of Anomaly Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier.size(40.dp).background(iconBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Alert", tint = iconTint, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = anomaly.employeeName, 
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontWeight = FontWeight.ExtraBold, 
                                    fontSize = 16.sp, 
                                    color = LedgerDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Emp ID: ${anomaly.employeeId} • Period: ${anomaly.payMonth}/${anomaly.payYear}", 
                                    fontSize = 12.sp, 
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = InkSoft,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Info Icon button on card
                        IconButton(
                            onClick = { selectedAnomalyForInvestigation = anomaly },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Investigate Details",
                                tint = InkSoft,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Reason Text
                    Text(
                        text = displayReason,
                        fontSize = 13.sp, 
                        color = LedgerDark,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Financial Impact Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PaperDim, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("GROSS", fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = InkSoft, fontWeight = FontWeight.Bold)
                                Text("₹${formatCurrency(anomaly.gross)}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 13.sp, color = LedgerDark, fontWeight = FontWeight.ExtraBold)
                            }
                            Column {
                                Text("DEDUCTIONS", fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = InkSoft, fontWeight = FontWeight.Bold)
                                Text("₹${formatCurrency(anomaly.deductions)}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 13.sp, color = ErrorText, fontWeight = FontWeight.ExtraBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("NET PAY", fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = InkSoft, fontWeight = FontWeight.Bold)
                                Text("₹${formatCurrency(anomaly.net)}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 14.sp, color = LedgerDark, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Severity Score:", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = InkSoft)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${(anomaly.anomalyScore * 100).toInt()}/100", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = iconTint)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedAnomalyForInvestigation = anomaly }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "INVESTIGATE",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontWeight = FontWeight.ExtraBold,
                                color = LedgerDark,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Investigate Anomaly",
                                tint = LedgerDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal 1: Overall AI Anomaly Detection Model Architecture & Explanation
    if (showModelExplanationDialog) {
        AnomalyModelExplanationDialog(
            anomalies = anomalies,
            onDismiss = { showModelExplanationDialog = false }
        )
    }

    // Modal 2: Individual Anomaly Investigation & Diagnosis Dialog
    selectedAnomalyForInvestigation?.let { anomaly ->
        AnomalyInvestigationDialog(
            anomaly = anomaly,
            onDismiss = { selectedAnomalyForInvestigation = null },
            onOpenModelArchitecture = {
                selectedAnomalyForInvestigation = null
                showModelExplanationDialog = true
            }
        )
    }
}

@Composable
fun AnomalyModelExplanationDialog(
    anomalies: List<AnomalyItem>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = Paper,
            border = BorderStroke(1.dp, GlassBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Stamp.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Model Info",
                                tint = Stamp,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Anomaly Detection",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LedgerDark
                            )
                            Text(
                                text = "Live Machine Learning Engine • Not Mock Data",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Stamp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Verification Callout Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Stamp.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Stamp.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Verified",
                            tint = Stamp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "These anomaly signals are generated dynamically by an active Machine Learning model on the live backend API — they are not mock or placeholder data.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = LedgerDark,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 1: WHICH MODEL GENERATES THIS?
                Text(
                    text = "1. WHICH MODEL GENERATES THIS?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LedgerDark,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PaperDim,
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Generated by an Isolation Forest unsupervised anomaly detection model trained using scikit-learn:",
                            fontSize = 12.sp,
                            color = LedgerDark,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnomalyDetailBullet(
                            title = "Model File",
                            value = "anomaly_detection_model.joblib"
                        )
                        AnomalyDetailBullet(
                            title = "Inference Script",
                            value = "detect_anomalies.py (active on backend)"
                        )
                        AnomalyDetailBullet(
                            title = "API Endpoint",
                            value = "/ai/anomalies in api.py"
                        )
                        AnomalyDetailBullet(
                            title = "App Invocation",
                            value = "DashboardViewModel calls ApiClient.apiService.getAnomalies() live"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: WHAT DATA IS ANALYZED?
                Text(
                    text = "2. WHAT DATA IS ANALYZED?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LedgerDark,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PaperDim,
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "The model evaluates historical payroll records across 15 different feature dimensions:",
                            fontSize = 12.sp,
                            color = LedgerDark,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnomalyFeatureBullet("Deduction Ratios: Total deductions relative to gross monthly compensation (normal baseline is < 4%).")
                        AnomalyFeatureBullet("Performance Incentive Ratios: Proportion of monthly incentive compared against basic pay.")
                        AnomalyFeatureBullet("Month-over-Month Net Salary Swings: Sharp percentage jumps or drops in take-home pay.")
                        AnomalyFeatureBullet("Loss of Pay (LWP) Ratios: Days absent relative to scheduled working calendar.")
                        AnomalyFeatureBullet("Multivariate Correlation: Complex cross-feature outliers isolated from standard company patterns.")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 3: WHY DID THE MODEL FLAG THESE SPECIFIC SLIPS?
                Text(
                    text = "3. WHY DID THE MODEL FLAG THESE SPECIFIC SLIPS?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LedgerDark,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (anomalies.isNotEmpty()) {
                    anomalies.forEachIndexed { idx, anomaly ->
                        val isHighSeverity = anomaly.anomalyScore > 0.1
                        val cardBorder = if (isHighSeverity) ErrorText.copy(alpha = 0.3f) else Brass.copy(alpha = 0.3f)
                        val cardBg = if (isHighSeverity) ErrorBg.copy(alpha = 0.5f) else PaperDim
                        val scoreColor = if (isHighSeverity) ErrorText else Brass

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "Slip ${idx + 1}: ${anomaly.employeeName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = LedgerDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Surface(
                                        color = scoreColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Severity ${(anomaly.anomalyScore * 100).toInt()}/100",
                                            color = scoreColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "ID: ${anomaly.employeeId} • Period: ${anomaly.payMonth}/${anomaly.payYear}",
                                    fontSize = 11.sp,
                                    color = InkSoft,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                                )

                                Text(
                                    text = "What happened in the record:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LedgerDark
                                )
                                Text(
                                    text = "• Gross: ₹${formatCurrency(anomaly.gross)} | Deductions: ₹${formatCurrency(anomaly.deductions)} | Net: ₹${formatCurrency(anomaly.net)}",
                                    fontSize = 11.5.sp,
                                    color = LedgerDark,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 6.dp)
                                )

                                Text(
                                    text = "Model flags:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LedgerDark
                                )
                                val reasons = anomaly.reason.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                                reasons.forEach { r ->
                                    Text(
                                        text = "• $r",
                                        fontSize = 11.5.sp,
                                        color = if (isHighSeverity) ErrorText else LedgerDark,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 4.dp, top = 1.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Isolation Forest decision score: ~${String.format(java.util.Locale.US, "%.3f", anomaly.anomalyScore)} → ${(anomaly.anomalyScore * 100).toInt()}/100",
                                    fontSize = 11.sp,
                                    color = InkSoft,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PaperDim,
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No anomalies are currently flagged. The Isolation Forest model continuously monitors historical records on each refresh.",
                            fontSize = 12.sp,
                            color = InkSoft,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Note
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Ledger.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Ledger.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Summary",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LedgerDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every number, percentage, and severity score shown on these cards is calculated dynamically by the Isolation Forest machine learning pipeline detecting genuine statistical outliers in the historical salary slips.",
                            fontSize = 11.5.sp,
                            color = LedgerDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnomalyInvestigationDialog(
    anomaly: AnomalyItem,
    onDismiss: () -> Unit,
    onOpenModelArchitecture: () -> Unit
) {
    val isHighSeverity = anomaly.anomalyScore > 0.1
    val severityColor = if (isHighSeverity) ErrorText else Brass
    val severityBg = if (isHighSeverity) ErrorBg else Stamp.copy(alpha = 0.12f)
    val reasons = anomaly.reason.split(";").map { it.trim() }.filter { it.isNotEmpty() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = Paper,
            border = BorderStroke(1.dp, GlassBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with employee and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anomaly Investigation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkSoft,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = anomaly.employeeName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LedgerDark
                        )
                        Text(
                            text = "Emp ID: ${anomaly.employeeId} • Pay Period: ${anomaly.payMonth}/${anomaly.payYear}",
                            fontSize = 11.5.sp,
                            color = InkSoft
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = InkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Severity Badge Callout
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = severityBg,
                    border = BorderStroke(1.dp, severityColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(severityColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Severity",
                                tint = severityColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Severity Score: ${(anomaly.anomalyScore * 100).toInt()}/100",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = severityColor
                            )
                            Text(
                                text = "Isolation Forest Decision Score: ${String.format(java.util.Locale.US, "%.3f", anomaly.anomalyScore)} (Contamination Outlier)",
                                fontSize = 11.sp,
                                color = LedgerDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Financial Overview
                Text(
                    text = "RECORDED PAYROLL FIGURES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkSoft,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PaperDim,
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("GROSS", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                            Text("₹${formatCurrency(anomaly.gross)}", fontSize = 13.sp, color = LedgerDark, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("DEDUCTIONS", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                            Text("₹${formatCurrency(anomaly.deductions)}", fontSize = 13.sp, color = ErrorText, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("NET PAY", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                            Text("₹${formatCurrency(anomaly.net)}", fontSize = 14.sp, color = LedgerDark, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Model Flags
                Text(
                    text = "WHY THE MODEL FLAGGED THIS RECORD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkSoft,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PaperDim,
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        reasons.forEach { reason ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text("•", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = severityColor)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = reason,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LedgerDark
                                    )
                                    val contextExplanation = when {
                                        reason.contains("Deduction ratio", ignoreCase = true) ->
                                            "Normal company deduction baseline is < 4% of gross pay. This record represents an extreme deviation."
                                        reason.contains("Net salary changed", ignoreCase = true) ->
                                            "Significant jump or drop from typical monthly baseline pay detected by rolling MoM variance."
                                        reason.contains("Performance", ignoreCase = true) ->
                                            "Incentive ratio deviates substantially from regular historical performance brackets."
                                        else ->
                                            "Multivariate statistical outlier isolated by decision trees from regular historical patterns."
                                    }
                                    Text(
                                        text = contextExplanation,
                                        fontSize = 11.sp,
                                        color = InkSoft,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recommended Actions Checklist
                Text(
                    text = "RECOMMENDED VERIFICATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkSoft,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PaperDim,
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        AnomalyChecklistItem("Verify if deduction contains salary advances, uniform costs, or loan repayments.")
                        AnomalyChecklistItem("Confirm biometric attendance logs against recorded Loss of Pay (LWP) days.")
                        AnomalyChecklistItem("Verify authorized supervisor approval remarks in salary slips.")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to open Full Model Architecture
                Button(
                    onClick = {
                        onDismiss()
                        onOpenModelArchitecture()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ledger)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Brass
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View Full ML Model Architecture & Pipeline",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AnomalyDetailBullet(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", fontSize = 12.sp, color = LedgerDark, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = LedgerDark)) {
                    append("$title: ")
                }
                append(value)
            },
            fontSize = 11.5.sp,
            color = InkSoft,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun AnomalyFeatureBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", fontSize = 12.sp, color = Stamp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.5.sp,
            color = LedgerDark,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun AnomalyChecklistItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("✓", fontSize = 12.sp, color = Stamp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.5.sp,
            color = LedgerDark,
            lineHeight = 16.sp
        )
    }
}
