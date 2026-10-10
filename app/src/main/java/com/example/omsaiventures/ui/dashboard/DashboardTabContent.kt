package com.example.omsaiventures.ui.dashboard

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Refresh
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
                    Text("Analyzing payroll patterns...", color = InkSoft, fontSize = 14.sp)
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
            // AI Prediction - Designed as a Premium "Hero" Card
            state.prediction?.let { prediction ->
                PredictionHeroCard(
                    predictedAmount = "₹${formatCurrency(prediction.predictedNetPayroll)}",
                    change = "${if (prediction.changePercentage >= 0) "+" else ""}${prediction.changePercentage}%",
                    isPositiveChange = prediction.changePercentage >= 0,
                    trendData = listOf(
                        prediction.previousMonthNetPayroll.toFloat() * 0.9f,
                        prediction.previousMonthNetPayroll.toFloat(),
                        prediction.predictedNetPayroll.toFloat()
                    )
                )
            }

            // AI Anomalies
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
fun PredictionHeroCard(predictedAmount: String, change: String, isPositiveChange: Boolean, trendData: List<Float>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = LedgerDark.copy(alpha = 0.2f))
            .background(Brush.linearGradient(listOf(Ledger, LedgerDark)), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Subtle background decoration
        Box(modifier = Modifier.absoluteOffset(x = 200.dp, y = (-50).dp).size(200.dp).background(Brass.copy(alpha = 0.05f), CircleShape))
        Box(modifier = Modifier.absoluteOffset(x = (-50).dp, y = 100.dp).size(150.dp).background(Color.White.copy(alpha = 0.03f), CircleShape))

        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Trend", tint = Brass, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "FORECAST",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Brass,
                        letterSpacing = 1.5.sp
                    )
                }
                
                // Change Badge
                val badgeBg = if (isPositiveChange) Stamp.copy(alpha = 0.2f) else ErrorText.copy(alpha = 0.2f)
                val badgeColor = if (isPositiveChange) Color(0xFF86EFAC) else Color(0xFFFCA5A5) // Light green / Light red
                
                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = change, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Predicted Total Net Pay",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = predictedAmount,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Interactive Chart with Gradient Fill
            MiniLineChart(
                data = trendData, 
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                lineColor = Brass
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Based on machine learning trend analysis.", 
                    fontSize = 11.sp, 
                    color = Color.White.copy(alpha = 0.5f), 
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun MiniLineChart(data: List<Float>, modifier: Modifier = Modifier, lineColor: Color) {
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
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
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

            // Tooltip using nativeCanvas
            val text = "₹" + formatCurrency(data[index].toDouble())
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 14.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            // Adjust text position so it doesn't go off-screen
            val textX = x.coerceIn(50.dp.toPx(), size.width - 50.dp.toPx())
            val textY = (y - 14.dp.toPx()).coerceAtLeast(14.sp.toPx())
            
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
