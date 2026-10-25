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
import com.example.omsaiventures.ui.dashboard.OfferLetterData
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

    fun generateOfferLetterPdf(context: Context, data: OfferLetterData, company: CompanyProfile) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "OfferLetter_${data.employeeId}"

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
                    .setPageCount(4)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdfDocument = createOfferLetterPdf(context, data, company)
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

    private fun createOfferLetterPdf(context: Context, data: OfferLetterData, company: CompanyProfile): PdfDocument {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        
        // Colors
        val navyColor = Color.parseColor("#1A2B56")
        val goldColor = Color.parseColor("#BF953F")
        val grayColor = Color.parseColor("#F5F5F5")
        val lightGrayColor = Color.parseColor("#DDDDDD")
        val textColor = Color.parseColor("#333333")

        // Paints
        val paintTitle = Paint().apply {
            color = navyColor
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSub = Paint().apply {
            color = textColor
            textSize = 10f
        }
        val paintHeader = Paint().apply {
            color = navyColor
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintCenterHeader = Paint(paintHeader).apply {
            textAlign = Paint.Align.CENTER
        }
        val paintBody = Paint().apply {
            color = textColor
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintBodyBold = Paint(paintBody).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintGoldLine = Paint().apply {
            color = goldColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val paintNavyLine = Paint().apply {
            color = navyColor
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        val paintLightLine = Paint().apply {
            color = lightGrayColor
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val paintFillGray = Paint().apply {
            color = grayColor
            style = Paint.Style.FILL
        }

        val margin = 56f
        val pageWidth = pageInfo.pageWidth.toFloat()
        val pageHeight = pageInfo.pageHeight.toFloat()
        
        // Load Logo
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.omsaiventures.R.drawable.logo)
        val scaledLogo = bitmap?.let { android.graphics.Bitmap.createScaledBitmap(it, 60, 60, true) }

        // Helper: Draw Header
        fun drawHeader(canvas: Canvas) {
            val y = 40f
            scaledLogo?.let {
                canvas.drawBitmap(it, margin, y, null)
            }
            val textX = margin + 80f
            canvas.drawText("OMSAI VENTURES", textX, y + 20f, paintTitle)
            canvas.drawText("Registered Office: First Floor, 65, Shreya Park, Near Ravi Nagar, Gokul Road, Hubli – 580030", textX, y + 35f, paintSub)
            canvas.drawText("Email: omsaiventures9@gmail.com | Phone: 8073874620 / 9739316724", textX, y + 50f, paintSub)
            
            canvas.drawLine(margin, y + 70f, pageWidth - margin, y + 70f, paintGoldLine)
        }

        // Helper: Draw Footer
        fun drawFooter(canvas: Canvas, pageNum: Int) {
            val y = pageHeight - margin
            canvas.drawLine(margin, y - 20f, pageWidth - margin, y - 20f, paintLightLine)
            canvas.drawText("OMSAI Ventures — Offer of Contractual Employment", margin, y, paintSub)
            
            val paintPage = Paint(paintSub).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("Page $pageNum of 4", pageWidth - margin, y, paintPage)
        }

        // Helper: Word Wrap Text
        fun drawTextWrapped(canvas: Canvas, text: String, x: Float, yStart: Float, paint: Paint, maxWidth: Float, lineSpacing: Float = 1.5f): Float {
            var currentY = yStart
            val fontMetrics = paint.fontMetrics
            val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * lineSpacing
            val paragraphs = text.split("\n")
            
            for (paragraph in paragraphs) {
                if (paragraph.isEmpty()) {
                    currentY += lineHeight
                    continue
                }
                val words = paragraph.split(" ")
                var line = ""
                for (word in words) {
                    val testLine = if (line.isEmpty()) word else "$line $word"
                    if (paint.measureText(testLine) <= maxWidth) {
                        line = testLine
                    } else {
                        canvas.drawText(line, x, currentY, paint)
                        currentY += lineHeight
                        line = word
                    }
                }
                if (line.isNotEmpty()) {
                    canvas.drawText(line, x, currentY, paint)
                    currentY += lineHeight
                }
            }
            return currentY
        }
        
        fun drawBullet(canvas: Canvas, text: String, x: Float, yStart: Float, paint: Paint, maxWidth: Float): Float {
            canvas.drawText("•", x, yStart, paint)
            return drawTextWrapped(canvas, text, x + 15f, yStart, paint, maxWidth - 15f)
        }

        // === PAGE 1 ===
        var pageInfoPage1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfoPage1)
        var canvas = page.canvas
        drawHeader(canvas)
        drawFooter(canvas, 1)
        
        var yPos = 150f
        
        if (data.employeeId.isNotBlank()) {
            val paintEmpId = Paint(paintBody).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("Employee Id: ${data.employeeId}", pageWidth - margin, yPos, paintEmpId)
            yPos += 20f
        }
        
        canvas.drawText("OFFER LETTER", pageWidth / 2f, yPos, paintCenterHeader)
        yPos += 40f
        
        canvas.drawText("To,", margin, yPos, paintBody)
        yPos += 16f
        canvas.drawText(data.candidateName, margin, yPos, paintBodyBold)
        yPos += 16f
        yPos = drawTextWrapped(canvas, data.address, margin, yPos, paintBody, 250f)
        
        yPos += 20f
        val subjectBoxTop = yPos
        val subjectStr = "Subject: Offer of Contractual Employment – ${data.designation} (${data.roleDescription})"
        val subjectLinesHeight = 40f // approx
        canvas.drawRect(margin, subjectBoxTop, pageWidth - margin, subjectBoxTop + subjectLinesHeight, paintFillGray)
        canvas.drawLine(margin, subjectBoxTop, margin, subjectBoxTop + subjectLinesHeight, paintNavyLine)
        drawTextWrapped(canvas, subjectStr, margin + 15f, subjectBoxTop + 25f, paintBodyBold, pageWidth - margin * 2 - 30f)
        
        yPos += subjectLinesHeight + 30f
        
        canvas.drawText("Dear ${data.candidateName},", margin, yPos, paintBodyBold)
        yPos += 24f
        
        val introText = "We are pleased to offer you the position of ${data.position} with OMSAI Ventures. Based on your interview and qualifications, we are excited to have you join our team.\n\nPlease note that for this role, you will be deputed to our client, ${data.deputedClient}, at the ${data.workLocation} Branch and will be performing your duties at their designated dark store location."
        yPos = drawTextWrapped(canvas, introText, margin, yPos, paintBody, pageWidth - margin * 2)
        
        yPos += 20f
        canvas.drawText("1. Appointment Details:", margin, yPos, paintHeader)
        yPos += 24f
        
        fun drawIndentRow(k: String, v: String, y: Float) {
            canvas.drawText(k, margin + 20f, y, paintBody)
            canvas.drawText(v, margin + 140f, y, paintBodyBold)
        }
        
        drawIndentRow("Designation:", data.designation, yPos); yPos += 16f
        drawIndentRow("Deputed Client:", data.deputedClient, yPos); yPos += 16f
        drawIndentRow("Work Location:", data.workLocation, yPos); yPos += 16f
        drawIndentRow("Date of Joining:", data.dateOfJoining, yPos); yPos += 16f
        drawIndentRow("Reporting Manager:", "Store Manager", yPos); yPos += 16f
        
        yPos += 20f
        canvas.drawText("2. Term of Engagement:", margin, yPos, paintHeader)
        yPos += 24f
        drawIndentRow("Start Date:", data.startDate, yPos); yPos += 16f
        drawIndentRow("End Date:", data.endDate, yPos); yPos += 16f
        
        val noteText = "Note: This is a fixed-term contract. The engagement will automatically terminate on the End Date unless extended in writing by mutual agreement."
        yPos = drawTextWrapped(canvas, noteText, margin + 20f, yPos + 10f, paintBody, pageWidth - margin * 2 - 20f)
        
        yPos += 20f
        canvas.drawText("3. Roles and Responsibilities:", margin, yPos, paintHeader)
        yPos += 24f
        yPos = drawTextWrapped(canvas, "As a ${data.roleDescription} Associate at a rapid delivery facility, your primary responsibilities will include, but are not limited to:", margin, yPos, paintBody, pageWidth - margin * 2)
        
        pdfDocument.finishPage(page)

        // === PAGE 2 ===
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        drawHeader(canvas)
        drawFooter(canvas, 2)
        yPos = 150f
        
        val responsibilities = listOf(
            "Receiving digital orders and navigating the store aisles efficiently to pick items.",
            "Scanning barcodes to ensure the correct items and quantities are selected.",
            "Packing orders securely and safely to prevent damage during transit.",
            "Handing over packed orders to the delivery executives within the strict turnaround time (SLA).",
            "Assisting with unloading inventory, restocking shelves, and maintaining store cleanliness.",
            "Conducting routine inventory checks and reporting missing or damaged items to the Store Manager.",
            "Responsibility and locations will be revised as per requirement of client."
        )
        
        for (resp in responsibilities) {
            yPos = drawBullet(canvas, resp, margin + 20f, yPos, paintBody, pageWidth - margin * 2 - 20f)
            yPos += 8f
        }
        
        yPos += 20f
        canvas.drawText("4. Compensation and Benefits:", margin, yPos, paintHeader)
        yPos += 24f
        canvas.drawText("The breakdown of your monthly compensation is as follows:", margin, yPos, paintBody)
        yPos += 20f
        
        // Table
        val tableTop = yPos
        canvas.drawRect(margin, tableTop, pageWidth - margin, tableTop + 30f, paintFillGray)
        canvas.drawRect(margin, tableTop, pageWidth - margin, tableTop + 150f, paintLightLine) // Outline
        
        canvas.drawText("Component", margin + 10f, tableTop + 20f, paintBodyBold)
        canvas.drawText("Amount / Month (₹)", margin + 250f, tableTop + 20f, paintBodyBold)
        
        yPos += 30f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLightLine)
        
        fun drawTableRow(k: String, v: String, y: Float, isBold: Boolean = false) {
            val p = if (isBold) paintBodyBold else paintBody
            canvas.drawText(k, margin + 10f, y + 20f, p)
            canvas.drawText("₹$v", margin + 250f, y + 20f, p)
        }
        
        drawTableRow("Basic Salary", data.basicSalary, yPos); yPos += 30f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLightLine)
        drawTableRow("Attendance Bonus", data.attendanceBonus, yPos); yPos += 30f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLightLine)
        drawTableRow("Performance Bonus", data.performanceBonus, yPos); yPos += 30f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paintLightLine)
        drawTableRow("Net Salary", data.netSalary, yPos, isBold = true); yPos += 30f
        
        yPos += 20f
        
        val compConditions = listOf(
            "Performance Bonus, if applicable, will be based on the Associate’s monthly performance in Picking and Putaway activities, as per the Company’s performance evaluation criteria.",
            "You may also be eligible for performance-based incentives and Festival bonuses as per the client's operational policies, which will be communicated to you by the Store Manager.",
            "You will be responsible for any DQ loss recovery from the salary if the threshold of DQ value crosses ₹15,000 of the store in any given month."
        )
        
        for (cond in compConditions) {
            yPos = drawBullet(canvas, cond, margin + 10f, yPos, paintBody, pageWidth - margin * 2 - 10f)
            yPos += 8f
        }
        
        yPos += 20f
        canvas.drawText("5. Working Hours and Shifts:", margin, yPos, paintHeader)
        yPos += 24f
        val hoursText = "You will be required to work in shifts of ${data.workingHours} hours as per Client requirement.\n\nGiven the nature of the quick-commerce industry, you will be working in rotational shifts (including weekends and public holidays) as assigned by the Store Manager. You will be entitled to one weekly off."
        yPos = drawTextWrapped(canvas, hoursText, margin, yPos, paintBody, pageWidth - margin * 2)

        pdfDocument.finishPage(page)

        // === PAGE 3 ===
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        drawHeader(canvas)
        drawFooter(canvas, 3)
        yPos = 150f
        
        canvas.drawText("6. Terms and Conditions:", margin, yPos, paintHeader)
        yPos += 24f
        
        val terms = listOf(
            "Deputation: You are an employee of OMSAI Ventures. Your deputation to ${data.deputedClient} is subject to client requirements and performance.",
            "Probation: You will be on probation for a period of 3 months from your date of joining.",
            "Background Verification: This offer is contingent upon the successful completion of a background and reference check.",
            "Notice Period: Either party may terminate this employment by giving 15 days' written notice or payment of basic salary in lieu thereof.",
            "Company Property: You are responsible for the safe handling of client property (scanners, devices, inventory). Any willful damage or loss may result in disciplinary action or cost recovery."
        )
        
        for (term in terms) {
            val parts = term.split(": ")
            val boldPart = parts[0] + ":"
            val rest = if (parts.size > 1) parts[1] else ""
            
            canvas.drawText("•", margin + 10f, yPos, paintBody)
            canvas.drawText(boldPart, margin + 25f, yPos, paintBodyBold)
            
            val indent = margin + 25f + paintBodyBold.measureText(boldPart) + 5f
            if (rest.isNotEmpty()) {
                yPos = drawTextWrapped(canvas, rest, indent, yPos, paintBody, pageWidth - indent - margin)
            }
            yPos += 8f
        }
        
        yPos += 20f
        canvas.drawText("7. Documents Required at Joining:", margin, yPos, paintHeader)
        yPos += 24f
        
        val docs = listOf(
            "Aadhaar Card and PAN Card.",
            "Bank account details (Cancelled Cheque or Passbook copy).",
            "Two recent passport-sized photographs.",
            "Educational certificates (if applicable)."
        )
        for (doc in docs) {
            yPos = drawBullet(canvas, doc, margin + 10f, yPos, paintBody, pageWidth - margin * 2 - 10f)
            yPos += 8f
        }
        
        yPos += 20f
        val closeText = "We welcome you to OMSAI Ventures and wish you a successful career working with ${data.deputedClient}.\n\nPlease sign and return the duplicate copy of this letter as a token of your acceptance of this offer and the terms mentioned herein."
        yPos = drawTextWrapped(canvas, closeText, margin, yPos, paintBody, pageWidth - margin * 2)
        
        yPos += 40f
        canvas.drawText("Sincerely,", margin, yPos, paintBody)
        yPos += 40f
        canvas.drawText("For OMSAI Ventures", margin, yPos, paintBodyBold)
        yPos += 50f
        canvas.drawLine(margin, yPos, margin + 150f, yPos, paintBody)
        yPos += 15f
        canvas.drawText("Authorized Signatory", margin, yPos, paintBody)

        pdfDocument.finishPage(page)

        // === PAGE 4 ===
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        drawHeader(canvas)
        drawFooter(canvas, 4)
        yPos = 150f
        
        canvas.drawText("Declaration of Acceptance", margin, yPos, paintTitle.apply { textSize = 16f })
        yPos += 40f
        
        val declText = "I, _________________________________________, have read and understood the terms and conditions outlined in this offer letter. I accept the offer for the position of ${data.roleDescription} Associate and agree to join on or before ${data.dateOfJoining}."
        yPos = drawTextWrapped(canvas, declText, margin, yPos, paintBody, pageWidth - margin * 2)
        
        yPos += 80f
        canvas.drawText("Signature: _______________________________", margin, yPos, paintBody)
        yPos += 40f
        canvas.drawText("Date: ____________________________________", margin, yPos, paintBody)
        
        pdfDocument.finishPage(page)
        
        return pdfDocument
    }
}
