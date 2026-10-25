package com.example.omsaiventures.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.omsaiventures.ui.dashboard.CompanyProfile
import com.example.omsaiventures.ui.dashboard.Employee
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object PdfGenerator {
    fun generatePayslipPdf(context: Context, employee: Employee, company: CompanyProfile) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Payslip_${employee.empid}_${employee.payMonth}_${employee.payYear}"

        printManager.print(jobName, object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdfDocument = createPdf(context, employee, company)
                try {
                    pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.toString())
                } finally {
                    pdfDocument.close()
                }
            }
        }, null)
    }

    private fun createPdf(context: Context, employee: Employee, company: CompanyProfile): PdfDocument {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Paints
        val paintTitle = Paint().apply {
            color = Color.parseColor("#16243A") // LedgerDark
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSub = Paint().apply {
            color = Color.parseColor("#6B7686") // InkSoft
            textSize = 9f
        }
        val paintHeader = Paint().apply {
            color = Color.parseColor("#16243A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.1f
        }
        val paintLabel = Paint().apply {
            color = Color.parseColor("#6B7686")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintValue = Paint().apply {
            color = Color.parseColor("#16243A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintValueRight = Paint(paintValue).apply { textAlign = Paint.Align.RIGHT }
        val paintBorder = Paint().apply {
            color = Color.parseColor("#DDE3EA") // GlassBorder
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val paintDashed = Paint().apply {
            color = Color.parseColor("#DDE3EA") // GlassBorder
            strokeWidth = 1f
            style = Paint.Style.STROKE
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f)
        }
        val paintThickLine = Paint().apply {
            color = Color.parseColor("#16243A")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val paintFillGray = Paint().apply {
            color = Color.parseColor("#EEF1F5") // PaperDim
            style = Paint.Style.FILL
        }
        val paintFillLightGray = Paint().apply {
            color = Color.parseColor("#F9F9F9") // Very faint gray for amount boxes
            style = Paint.Style.FILL
        }
        val paintFillDarkGray = Paint().apply {
            color = Color.parseColor("#DCDFE4") // Net Pay bar
            style = Paint.Style.FILL
        }

        var yPos = 40f
        val margin = 40f

        // Draw the Logo
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.omsaiventures.R.drawable.logo)
        if (bitmap != null) {
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 60, 60, true)
            // Logo background and border
            canvas.drawRoundRect(margin, yPos, margin + 80f, yPos + 80f, 8f, 8f, paintFillGray)
            canvas.drawBitmap(scaledBitmap, margin + 10f, yPos + 10f, null)
            canvas.drawRoundRect(margin, yPos, margin + 80f, yPos + 80f, 8f, 8f, paintBorder)
        }

        val textStartX = margin + 100f // Offset for text next to logo

        // Draw the Month/Year box on the right
        val boxWidth = 100f
        val boxHeight = 70f
        val boxLeft = pageInfo.pageWidth - margin - boxWidth
        val boxRight = pageInfo.pageWidth - margin

        canvas.drawRoundRect(boxLeft, yPos, boxRight, yPos + boxHeight, 8f, 8f, paintFillGray)
        canvas.drawRoundRect(boxLeft, yPos, boxRight, yPos + boxHeight, 8f, 8f, paintBorder)

        val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthStr = months.getOrNull(employee.payMonth) ?: "Unknown"

        val paintBoxText = Paint(paintValue).apply { textAlign = Paint.Align.CENTER; textSize = 11f }
        val paintBoxSub = Paint(paintSub).apply { textAlign = Paint.Align.CENTER; textSize = 8f }

        val boxCenterX = boxLeft + (boxWidth / 2f)
        canvas.drawText(monthStr, boxCenterX, yPos + 25f, paintBoxText)
        canvas.drawText("${employee.payYear}", boxCenterX, yPos + 40f, paintBoxText.apply { textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
        canvas.drawText("Issued 24 Oct", boxCenterX, yPos + 55f, paintBoxSub)
        canvas.drawText("2026", boxCenterX, yPos + 65f, paintBoxSub)

        // Company Details (Center Column)
        canvas.drawText(company.name, textStartX, yPos + 25f, paintTitle)
        var currentY = yPos + 40f

        val maxTextWidth = boxLeft - textStartX - 20f
        val addressWords = company.sub.replace("\n", ", ").split(" ")
        var line = ""
        addressWords.forEach { word ->
            if (paintSub.measureText("$line $word") < maxTextWidth) {
                line += "$word "
            } else {
                canvas.drawText(line.trim(), textStartX, currentY, paintSub)
                currentY += 12f
                line = "$word "
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line.trim(), textStartX, currentY, paintSub)
        currentY += 12f
        canvas.drawText(company.contact.replace("\n", "   Phone: "), textStartX, currentY, paintSub)
        currentY += 12f
        canvas.drawText("GSTIN: ${company.gst}", textStartX, currentY, paintLabel)

        yPos = maxOf(currentY, yPos + boxHeight) + 30f
        
        canvas.drawLine(margin, yPos, pageInfo.pageWidth - margin, yPos, paintBorder)
        
        yPos += 30f
        canvas.drawText("PAY SLIP", pageInfo.pageWidth / 2f, yPos, paintHeader)
        yPos += 15f
        
        canvas.drawLine(margin, yPos, pageInfo.pageWidth - margin, yPos, paintBorder)

        yPos += 25f

        // Employee Info Grid
        val midPoint = pageInfo.pageWidth / 2f
        val col1Left = margin
        val col1Right = midPoint - 15f
        val col2Left = midPoint + 15f
        val col2Right = pageInfo.pageWidth - margin

        fun drawInfoRow(k1: String, v1: String, k2: String, v2: String, y: Float) {
            canvas.drawText(k1, col1Left, y, paintLabel)
            
            // basic wrapping for long values
            if (paintValueRight.measureText(v1) > 110f && v1.contains(" ")) {
                val parts = v1.split(" ")
                val mid = parts.size / 2
                canvas.drawText(parts.take(mid).joinToString(" "), col1Right, y - 6f, paintValueRight)
                canvas.drawText(parts.drop(mid).joinToString(" "), col1Right, y + 6f, paintValueRight)
            } else {
                canvas.drawText(v1.ifEmpty { "—" }, col1Right, y, paintValueRight)
            }

            canvas.drawText(k2, col2Left, y, paintLabel)
            canvas.drawText(v2.ifEmpty { "—" }, col2Right, y, paintValueRight)
        }

        drawInfoRow("Employee Name", employee.name, "Date of Joining", employee.doj, yPos); yPos += 22f
        drawInfoRow("Designation", employee.role, "Account No.", employee.bank, yPos); yPos += 22f
        drawInfoRow("Emp ID", employee.empid, "PAN No.", employee.pan, yPos); yPos += 22f
        drawInfoRow("Deputed At", employee.deputedAt, "Bank Name", employee.bankname, yPos); yPos += 22f
        drawInfoRow("Store", employee.store, "IFSC Code", employee.ifsc, yPos); yPos += 22f

        yPos += 10f
        canvas.drawLine(margin, yPos, pageInfo.pageWidth - margin, yPos, paintDashed)

        // Attendance Boxes
        yPos += 15f
        val boxW = (pageInfo.pageWidth - (margin * 2) - 30f) / 3f
        
        fun drawAttendanceBox(label: String, value: String, xStart: Float) {
            val rect = android.graphics.RectF(xStart, yPos, xStart + boxW, yPos + 45f)
            canvas.drawRoundRect(rect, 6f, 6f, paintBorder)
            canvas.drawText(label, xStart + (boxW/2f), yPos + 18f, paintBoxSub.apply { color = Color.parseColor("#6B7686") })
            canvas.drawText(value, xStart + (boxW/2f), yPos + 35f, paintHeader.apply { textSize = 14f })
        }
        
        drawAttendanceBox("PAID DAYS", employee.paidDays.toString(), margin)
        drawAttendanceBox("LWP", if (employee.lwp == 0) "—" else employee.lwp.toString(), margin + boxW + 15f)
        drawAttendanceBox("REFUND DAYS", if (employee.refundDays == 0) "—" else employee.refundDays.toString(), margin + (boxW*2) + 30f)

        yPos += 60f
        canvas.drawLine(margin, yPos, pageInfo.pageWidth - margin, yPos, paintDashed)
        yPos += 25f

        // Earnings and Deductions
        canvas.drawText("EARNINGS", col1Left, yPos, paintValue)
        canvas.drawText("DEDUCTIONS", col2Left, yPos, paintValue)
        
        yPos += 30f
        val startY = yPos

        fun drawLineItem(label: String, amount: Double, xLabel: Float, xValRight: Float, y: Float) {
            canvas.drawText(label, xLabel, y, paintValue.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })
            
            // Faint gray box behind value
            val boxR = android.graphics.RectF(xValRight - 75f, y - 12f, xValRight, y + 4f)
            canvas.drawRoundRect(boxR, 4f, 4f, paintFillLightGray)
            
            canvas.drawText(formatCurrency(amount), xValRight - 4f, y, paintValueRight.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })
        }

        var earnY = startY
        drawLineItem("Basic Salary", employee.basic, col1Left, col1Right, earnY); earnY += 30f
        drawLineItem("Attendance Bonus", employee.attendance, col1Left, col1Right, earnY); earnY += 30f
        drawLineItem("Performance Bonus", employee.performance, col1Left, col1Right, earnY); earnY += 30f
        drawLineItem("Festival Bonus", employee.festivalBonus, col1Left, col1Right, earnY); earnY += 30f

        var dedY = startY
        if (employee.lwpDeduction > 0) {
            drawLineItem("LWP Deduction", employee.lwpDeduction, col2Left, col2Right, dedY); dedY += 30f
        }
        drawLineItem("DQ Deduction", employee.dq, col2Left, col2Right, dedY); dedY += 30f
        drawLineItem("Other Deduction", employee.otherDed, col2Left, col2Right, dedY); dedY += 30f
        drawLineItem("Group Insurance", employee.groupInsurance, col2Left, col2Right, dedY); dedY += 30f

        yPos = maxOf(earnY, dedY) + 10f
        
        // Thick black line above totals
        canvas.drawLine(col1Left, yPos, col1Right, yPos, paintThickLine)
        canvas.drawLine(col2Left, yPos, col2Right, yPos, paintThickLine)
        
        yPos += 20f
        
        canvas.drawText("Gross Earnings", col1Left, yPos, paintValue)
        canvas.drawText("₹${formatCurrency(employee.gross)}", col1Right, yPos, paintValueRight)

        canvas.drawText("Total Deduction", col2Left, yPos, paintValue)
        canvas.drawText("₹${formatCurrency(employee.ded)}", col2Right, yPos, paintValueRight)

        // Net Pay Bar
        yPos += 30f
        canvas.drawRect(margin, yPos, pageInfo.pageWidth - margin, yPos + 40f, paintFillDarkGray)
        
        canvas.drawText("Net Pay", margin + 15f, yPos + 25f, paintValue.apply { textSize = 14f })
        canvas.drawText("₹${formatCurrency(employee.net)}", pageInfo.pageWidth - margin - 15f, yPos + 25f, paintValueRight.apply { textSize = 16f; color = Color.BLACK })

        // Words
        yPos += 60f
        val paintItalic = Paint(paintSub).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
        canvas.drawText("Net pay in words: Rupees (Implementation specific) only", margin, yPos, paintItalic)

        // Footer
        yPos = pageInfo.pageHeight - 50f
        val footerRect = android.graphics.RectF(margin, yPos, pageInfo.pageWidth - margin, yPos + 30f)
        canvas.drawRoundRect(footerRect, 6f, 6f, paintFillGray)
        canvas.drawText("This is a system-generated payslip.", margin + 10f, yPos + 18f, paintSub)
        canvas.drawText("Page 1 of 1", pageInfo.pageWidth - margin - 10f, yPos + 18f, Paint(paintSub).apply { textAlign = Paint.Align.RIGHT })

        pdfDocument.finishPage(page)
        return pdfDocument
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale("en", "IN"))
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        return format.format(amount)
    }
}
