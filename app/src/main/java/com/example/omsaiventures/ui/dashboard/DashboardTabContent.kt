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
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

                // 5. Model Methodology & Assumptions Card
                ModelAssumptionsCard(prediction = prediction)
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
    val changeText = "${if (isPositive) "+" else ""}₹${formatCurrency(prediction.changeAmount)} (${if (isPositive) "+" else ""}${prediction.changePercentage}%)"

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

        Column(modifier = Modifier.padding(24.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Trend", tint = Brass, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FORECAST • ${prediction.predictionMonth}/${prediction.predictionYear}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Brass,
                        letterSpacing = 1.4.sp
                    )
                }

                // Change Badge
                val badgeBg = if (isPositive) Stamp.copy(alpha = 0.25f) else ErrorText.copy(alpha = 0.25f)
                val badgeColor = if (isPositive) Color(0xFF86EFAC) else Color(0xFFFCA5A5)

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = changeText, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Prev Month Actual: ₹${formatCurrency(prediction.previousMonthNetPayroll)}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                if (prediction.festivalAdjustment > 0) {
                    Text(
                        text = " • Base: ₹${formatCurrency(prediction.basePredictedNetPayroll)}",
                        fontSize = 12.sp,
                        color = Brass.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Real Data Line Chart
            MiniLineChart(
                data = chartData,
                labels = chartLabels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                lineColor = Brass
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Info, contentDescription = "Engine", tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Engine: ${prediction.modelUsed} (Calendar Normalized)",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun ForecastFactorHighlights(prediction: PredictionResponse) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "KEY FORECAST FACTORS",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = InkSoft,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

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
                modifier = Modifier.weight(1f)
            )

            // Factor 2: Expected LWP Deductions
            FactorItemCard(
                title = "LWP DEDUCTION",
                value = "₹${formatCurrency(prediction.estimatedLwpDeductions)}",
                subtext = "Via (Basic/31)*LWP",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
            )

            // Factor 4: Expected Employee Count
            FactorItemCard(
                title = "ACTIVE STAFF",
                value = "${prediction.expectedEmployeeCount} Staff",
                subtext = "Approved roster",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FactorItemCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
                Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = InkSoft, letterSpacing = 0.5.sp)
                Icon(imageVector = icon, contentDescription = title, tint = Ledger, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = LedgerDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, fontSize = 11.sp, color = InkSoft)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(Brass.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = "Festival Bonus", tint = Brass, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = prediction.festivalName ?: "Festival Bonus Adjustment",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerDark
                        )
                        Text(
                            text = "Configured prototype rule applied",
                            fontSize = 11.sp,
                            color = InkSoft
                        )
                    }
                }

                Text(
                    text = "+₹${formatCurrency(prediction.festivalAdjustment)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LedgerDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = Brass.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Bonus Rate: ₹1,000 × ${prediction.expectedEmployeeCount} eligible staff",
                    fontSize = 12.sp,
                    color = Ink
                )
                Text(
                    text = "Base: ₹${formatCurrency(prediction.basePredictedNetPayroll)}",
                    fontSize = 12.sp,
                    color = InkSoft,
                    fontWeight = FontWeight.Medium
                )
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
                    BreakdownLineItem("Estimated LWP Deductions (Calendar Rule)", "-₹${formatCurrency(prediction.estimatedLwpDeductions)}")
                    BreakdownLineItem("Group Insurance (₹150 × Staff)", "-₹${formatCurrency(prediction.expectedEmployeeCount * 150.0)}")
                    val otherDed = breakdownDetails?.totalOtherDeductions ?: 2691.67
                    BreakdownLineItem("Typical Other Deductions", "-₹${formatCurrency(otherDed)}")

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = GlassBorder)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Forecast Net Pay:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("₹${formatCurrency(prediction.basePredictedNetPayroll)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LedgerDark)
                    }

                    if (prediction.festivalAdjustment > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plus Festival Bonus:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Brass)
                            Text("+₹${formatCurrency(prediction.festivalAdjustment)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Brass)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Final Forecast Net Pay:", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = LedgerDark)
                        Text("₹${formatCurrency(prediction.predictedNetPayroll)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = LedgerDark)
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 12.sp, color = if (highlight) Brass else InkSoft)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (highlight) Brass else Ink)
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
                    fontWeight = FontWeight.Bold,
                    color = LedgerDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prediction.forecastDisclaimer,
                fontSize = 11.sp,
                color = InkSoft,
                lineHeight = 16.sp
            )

            if (prediction.forecastAssumptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                prediction.forecastAssumptions.forEach { assumption ->
                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                        Text("• ", fontSize = 11.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                        Text(assumption, fontSize = 11.sp, color = InkSoft, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniLineChart(
    data: List<Float>,
    labels: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    lineColor: Color
) {
    if (data.isEmpty()) return
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(data) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                        selectedIndex = (offset.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                    },
                    onDragEnd = { selectedIndex = null },
                    onDragCancel = { selectedIndex = null },
                    onDrag = { change, _ ->
                        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                        selectedIndex = (change.position.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                    }
                )
            }
            .pointerInput(data) {
                detectTapGestures(
                    onPress = { offset ->
                        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                        selectedIndex = (offset.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                        tryAwaitRelease()
                        selectedIndex = null
                    }
                )
            }
    ) {
        val max = data.maxOrNull() ?: 1f
        val min = data.minOrNull() ?: 0f
        val range = if (max == min) 1f else max - min
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - ((value - min) / range * size.height)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        // Draw fill gradient
        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = size.height
            ),
            style = Fill
        )

        // Draw Line
        drawPath(
            path = path, 
            color = lineColor, 
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw data points
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - min) / range * size.height)
            val isLast = index == data.size - 1
            val pointRadius = if (isLast) 4.5.dp.toPx() else 3.dp.toPx()
            val pointColor = if (isLast) Color.White else lineColor
            drawCircle(color = pointColor, radius = pointRadius, center = androidx.compose.ui.geometry.Offset(x, y))
        }

        // Draw interaction overlay
        selectedIndex?.let { index ->
            val x = index * stepX
            val y = size.height - ((data[index] - min) / range * size.height)

            // Vertical dashed line
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x, size.height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Circles
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))

            // Tooltip text with label if available
            val labelStr = labels.getOrNull(index)?.let { "$it: " } ?: ""
            val text = labelStr + "₹" + formatCurrency(data[index].toDouble())
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 12.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            // Adjust text position so it doesn't go off-screen
            val textX = x.coerceIn(60.dp.toPx(), size.width - 60.dp.toPx())
            val textY = (y - 12.dp.toPx()).coerceAtLeast(12.sp.toPx())
            
            drawContext.canvas.nativeCanvas.drawText(text, textX, textY, paint)
        }
    }
}

@Composable
fun AnomalySection(anomalies: List<AnomalyItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)) {
            Text(
                text = "ACTION REQUIRED",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = InkSoft,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = GlassBorder)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.background(ErrorBg, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Paper),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Top Header of Anomaly Card
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(40.dp).background(iconBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Alert", tint = iconTint, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = anomaly.employeeName, 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 16.sp, 
                                color = LedgerDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Emp ID: ${anomaly.employeeId} • Period: ${anomaly.payMonth}/${anomaly.payYear}", 
                                fontSize = 12.sp, 
                                color = InkSoft,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Reason Text
                    Text(
                        text = displayReason,
                        fontSize = 13.sp, 
                        color = Ink,
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
                                Text("GROSS", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                                Text("₹${formatCurrency(anomaly.gross)}", fontSize = 13.sp, color = Ledger, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("DEDUCTIONS", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                                Text("₹${formatCurrency(anomaly.deductions)}", fontSize = 13.sp, color = ErrorText, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("NET PAY", fontSize = 10.sp, color = InkSoft, fontWeight = FontWeight.Bold)
                                Text("₹${formatCurrency(anomaly.net)}", fontSize = 14.sp, color = LedgerDark, fontWeight = FontWeight.ExtraBold)
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
                            Text("Severity Score:", fontSize = 12.sp, color = InkSoft)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${(anomaly.anomalyScore * 100).toInt()}/100", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = iconTint)
                        }
                        
                        Text(
                            text = "INVESTIGATE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ledger,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.clickable { /* Handle investigation */ }
                        )
                    }
                }
            }
        }
    }
}
