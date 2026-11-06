package com.example.expensetracker

import android.content.Context
import android.widget.Toast
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object DataExporter {
    fun exportToCsv(context: Context, expenses: List<MainActivity.Expense>) {
        val file = File(context.filesDir, "expenses.csv")
        try {
            file.bufferedWriter().use { writer ->
                writer.write("Название,Сумма,Способ оплаты\n")
                for (expense in expenses) {
                    writer.write("${expense.name},${expense.amount},${expense.payment}\n")
                }
            }
            Toast.makeText(context, "CSV сохранен", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportToExcel(context: Context, expenses: List<MainActivity.Expense>) {
        val file = File(context.filesDir, "expenses.xlsx")
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Расходы")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Название")
            headerRow.createCell(1).setCellValue("Сумма")
            headerRow.createCell(2).setCellValue("Оплата")

            for (i in expenses.indices) {
                val row = sheet.createRow(i + 1)
                row.createCell(0).setCellValue(expenses[i].name)
                row.createCell(1).setCellValue(expenses[i].amount)
                row.createCell(2).setCellValue(expenses[i].payment)
            }
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            workbook.close()
            outputStream.close()
            Toast.makeText(context, "Excel сохранен", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка Excel: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    fun exportToPdf(context: Context, expenses: List<MainActivity.Expense>) {
        val file = File(context.filesDir, "expenses.pdf")
        try {
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = android.graphics.Paint()
            paint.textSize = 14f
            paint.color = android.graphics.Color.BLACK

            val titlePaint = android.graphics.Paint()
            titlePaint.textSize = 22f
            titlePaint.isFakeBoldText = true
            canvas.drawText("Отчет по расходам", 40f, 60f, titlePaint)

            var yPosition = 100f
            var total = 0.0

            for (expense in expenses) {
                val text = "${expense.name} — ${expense.amount} руб. (${expense.payment})"
                canvas.drawText(text, 40f, yPosition, paint)
                yPosition += 25f
                total += expense.amount
            }

            yPosition += 20f
            canvas.drawText("Итого: $total руб.", 40f, yPosition, titlePaint)

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            Toast.makeText(context, "PDF сохранен", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun textFromPDF(file: File): List<String> {
        val doc = PDDocument.load(file)
        val textStripper = PDFTextStripper()
        val text = textStripper.getText(doc).split("\n").map { it.trim() }
        doc.close()
        return text
    }
    fun importFromCsv(context: Context, dbHelper: DatabaseHelper): Int {
        val file = File(context.filesDir, "expenses.csv")
        if (!file.exists()) return 0
        var count = 0
        try {
            file.readLines().drop(1).forEach { line ->
                val parts = line.split(",")
                val amount = parts[1].toDoubleOrNull()
                if (amount != null) {
                    dbHelper.addExpense(parts[0], amount, if (parts[2] == "Карта") 2 else 1)
                    count++
                }
            }
        } catch (e: Exception) { }
        return count
    }

    fun importFromExcel(context: Context, dbHelper: DatabaseHelper): Int {
        val file = File(context.filesDir, "expenses.xlsx")
        if (!file.exists()) return 0
        var count = 0
        try {
            val workbook = WorkbookFactory.create(FileInputStream(file))
            val sheet = workbook.getSheetAt(0)
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val amount = row.getCell(1)?.numericCellValue
                if (amount != null) {
                    dbHelper.addExpense(
                        row.getCell(0).stringCellValue,
                        amount,
                        if (row.getCell(2).stringCellValue == "Карта") 2 else 1
                    )
                    count++
                }
            }
            workbook.close()
        } catch (e: Exception) { }
        return count
    }

    fun importFromPdf(context: Context, dbHelper: DatabaseHelper): Int {
        val file = File(context.filesDir, "expenses.pdf")
        if (!file.exists()) return 0
        var count = 0
        try {
            textFromPDF(file).forEach { line ->
                if (line.contains("—")) {
                    val name = line.substringBefore("—").trim()
                    val rest = line.substringAfter("—").trim().split(" ")

                    val amount = rest[0].toDoubleOrNull()
                    val payment = rest.last().replace("(", "").replace(")", "")

                    if (amount != null) {
                        dbHelper.addExpense(name, amount, if (payment == "Карта") 2 else 1)
                        count++
                    }
                }
            }
        } catch (e: Exception) { }
        return count
    }
}