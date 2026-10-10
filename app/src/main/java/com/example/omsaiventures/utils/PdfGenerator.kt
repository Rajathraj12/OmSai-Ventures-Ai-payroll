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
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSub = Paint().apply {
            color = Color.parseColor("#6B7686") // InkSoft
            textSize = 10f
        }
        val paintHeader = Paint().apply {
            color = Color.parseColor("#16243A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val paintLabel = Paint().apply {
            color = Color.parseColor("#6B7686")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintValue = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintBorder = Paint().apply {
            color = Color.parseColor("#DDE3EA") // GlassBorder
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val paintFillDark = Paint().apply {
            color = Color.parseColor("#0F2438") // Ledger
            style = Paint.Style.FILL
        }

        var yPos = 40f
        val margin = 40f
        
        // Load bitmap instead of drawable directly to ensure it renders on PDF Canvas
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.omsaiventures.R.drawable.logo)
        if (bitmap != null) {
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 60, 60, true)
            canvas.drawBitmap(scaledBitmap, margin, yPos, null)
        }

        val textStartX = margin + 80f // Offset for text next to logo
        
        // Draw the Month/Year box on the right
        val boxWidth = 120f
        val boxHeight = 60f
        val boxLeft = pageInfo.pageWidth - margin - boxWidth
        val boxRight = pageInfo.pageWidth - margin
        
        canvas.drawRoundRect(
            boxLeft, 
            yPos, 
            boxRight, 
            yPos + boxHeight, 
            8f, 8f, 
            Paint().apply { color = Color.parseColor("#EEF1F5") } // PaperDim
        )
        canvas.drawRoundRect(
            boxLeft, 
            yPos, 
            boxRight, 
            yPos + boxHeight, 
            8f, 8f, 
            paintBorder
        )

        val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthStr = months.getOrNull(employee.payMonth) ?: "Unknown"
        
        val paintBoxText = Paint(paintValue).apply { textAlign = Paint.Align.CENTER }
        val paintBoxSub = Paint(paintSub).apply { textAlign = Paint.Align.CENTER; textSize = 8f }
        
        val boxCenterX = boxLeft + (boxWidth / 2f)
        canvas.drawText(monthStr, boxCenterX, yPos + 20f, paintBoxText)
        canvas.drawText("${employee.payYear}", boxCenterX, yPos + 35f, paintBoxText.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
        canvas.drawText("Issued 09 Oct 2026", boxCenterX, yPos + 50f, paintBoxSub)


        // Company Details (Center Column)
        canvas.drawText(company.name, textStartX, yPos + 15f, paintTitle)
        var currentY = yPos + 30f
        
        // Handle word wrap for Address Sub (split by commas or newlines if too long)
        val maxTextWidth = boxLeft - textStartX - 20f
        val addressWords = company.sub.replace("\n", ", ").split(" ")
        var line = ""
        addressWords.forEach { word ->
            if (paintSub.measureText("$line $word") < maxTextWidth) {
                line += "$word "
            } else {
                canvas.drawText(line.trim(), textStartX, currentY, paintSub)
                currentY += 15f
                line = "$word "
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line.trim(), textStartX, currentY, paintSub)
        currentY += 15f
        
        // Handle contact wrapping
        canvas.drawText(company.contact.replace("\n", " • "), textStartX, currentY, paintSub)
        currentY += 15f
        canvas.drawText(company.gst, textStartX, currentY, paintLabel)
        
        yPos = maxOf(currentY, yPos + boxHeight) + 30f
        canvas.drawLine(margin, yPos, pageInfo.pageWidth - margin, yPos, paintBorder)
        
        yPos += 30f
        canvas.drawText("PAY SLIP", pageInfo.pageWidth / 2f, yPos, paintHeader)

        yPos += 40f
        
        // Employee Info Grid Helper
        fun drawInfoRow(k1: String, v1: String, k2: String, v2: String, y: Float) {
            val col1 = margin
            val col2 = margin + 250f
            canvas.drawText(k1.uppercase(), col1, y, paintLabel)
            canvas.drawText(v1.ifEmpty { "—" }, col1, y + 15f, paintValue)
            
            canvas.drawText(k2.uppercase(), col2, y, paintLabel)
            canvas.drawText(v2.ifEmpty { "—" }, col2, y + 15f, paintValue)
        }

        drawInfoRow("Employee Name", employee.name, "Emp ID", employee.empid, yPos)
        yPos += 40f
        drawInfoRow("Designation", employee.role, "Date of Joining", employee.doj, yPos)
        yPos += 40f
        drawInfoRow("Deputed At", employee.deputedAt, "Store", employee.store, yPos)
        yPos += 40f
        drawInfoRow("Account No.", employee.bank, "Bank Name", employee.bankname, yPos)
        yPos += 40f
        drawInfoRow("PAN No.", employee.pan, "IFSC Code", employee.ifsc, yPos)
        
        yPos += 40f
        canvas.drawLine(margin, yPos, pageInfo.pageWidth - margin, yPos, paintBorder)
        
        // Attendance
        yPos += 30f
        canvas.drawText("PAID DAYS: ${employee.paidDays}", margin, yPos, paintValue)
        canvas.drawText("LWP: ${employee.lwp}", margin + 150f, yPos, paintValue)
        canvas.drawText("REFUND DAYS: ${employee.refundDays}", margin + 300f, yPos, paintValue)

        yPos += 20f
        canvas.drawLine(margin, yPos, pageInfo.pageWidth - margin, yPos, paintBorder)

        // Earnings and Deductions Header
        yPos += 40f
        val colEarnings = margin
        val colDeductions = margin + 250f
        val valColEarnings = margin + 200f
        val valColDeductions = pageInfo.pageWidth - margin

        val paintAmountRight = Paint(paintValue).apply { textAlign = Paint.Align.RIGHT }

        canvas.drawText("EARNINGS", colEarnings, yPos, paintLabel)
        canvas.drawText("DEDUCTIONS", colDeductions, yPos, paintLabel)
        
        yPos += 20f
        val startY = yPos

        // Earnings List
        fun drawLineItem(label: String, amount: Double, xLabel: Float, xVal: Float, y: Float) {
            canvas.drawText(label, xLabel, y, paintValue.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })
            canvas.drawText(formatCurrency(amount), xVal, y, paintAmountRight)
        }

        var earnY = startY
        drawLineItem("Basic Salary", employee.basic, colEarnings, valColEarnings, earnY); earnY += 25f
        drawLineItem("Attendance Bonus", employee.attendance, colEarnings, valColEarnings, earnY); earnY += 25f
        drawLineItem("Performance Bonus", employee.performance, colEarnings, valColEarnings, earnY); earnY += 25f
        drawLineItem("Festival Bonus", employee.festivalBonus, colEarnings, valColEarnings, earnY); earnY += 25f

        // Deductions List
        var dedY = startY
        if (employee.lwpDeduction > 0) {
            drawLineItem("LWP Deduction", employee.lwpDeduction, colDeductions, valColDeductions, dedY); dedY += 25f
        }
        drawLineItem("DQ Deduction", employee.dq, colDeductions, valColDeductions, dedY); dedY += 25f
        drawLineItem("Other Deduction", employee.otherDed, colDeductions, valColDeductions, dedY); dedY += 25f
        drawLineItem("Group Insurance", employee.groupInsurance, colDeductions, valColDeductions, dedY); dedY += 25f

        yPos = maxOf(earnY, dedY) + 20f
        canvas.drawLine(margin, yPos, pageInfo.pageWidth - margin, yPos, paintBorder)
        
        // Subtotals
        yPos += 20f
        canvas.drawText("Gross Earnings", colEarnings, yPos, paintValue)
        canvas.drawText(formatCurrency(employee.gross), valColEarnings, yPos, paintAmountRight.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })

        canvas.drawText("Total Deductions", colDeductions, yPos, paintValue)
        canvas.drawText(formatCurrency(employee.ded), valColDeductions, yPos, paintAmountRight)

        // Net Pay Bar
        yPos += 40f
        canvas.drawRect(margin, yPos, pageInfo.pageWidth - margin, yPos + 60f, paintFillDark)
        val paintWhiteTitle = Paint(paintTitle).apply { color = Color.WHITE }
        val paintWhiteSub = Paint(paintSub).apply { color = Color.LTGRAY }
        
        canvas.drawText("NET PAY", margin + 20f, yPos + 25f, paintWhiteTitle.apply { textSize = 14f })
        canvas.drawText("For the month", margin + 20f, yPos + 45f, paintWhiteSub)
        canvas.drawText(formatCurrency(employee.net), valColDeductions - 20f, yPos + 40f, paintWhiteTitle.apply { textAlign = Paint.Align.RIGHT; textSize = 24f })

        // Remarks
        yPos += 100f
        canvas.drawText("REMARKS / REASON FOR ADJUSTMENT", margin, yPos, paintLabel)
        yPos += 20f
        canvas.drawText(employee.remarks.ifEmpty { "No remarks." }, margin, yPos, paintValue.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) })

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
