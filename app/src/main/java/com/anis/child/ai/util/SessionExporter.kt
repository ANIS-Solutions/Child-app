package com.anis.child.ai.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.data.local.SessionEntity
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SessionExporter {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun exportSession(
        context: Context,
        session: SessionEntity,
        results: List<AnalysisResultEntity>
    ): Uri {
        val exportDir = File(context.cacheDir, "exports")
        exportDir.mkdirs()
        val baseName = "session_${session.id}_${dateFormat.format(Date(session.startTime))}"
        val xlsxFile = File(exportDir, "$baseName.xlsx")
        val zipFile = File(exportDir, "$baseName.zip")

        FileOutputStream(xlsxFile).use { outputStream ->
            val workbook = Workbook(outputStream, "SessionExporter", "1.0")
            val sessionSheet = workbook.newWorksheet("Session Info")
            writeSessionSheet(sessionSheet, session)

            val resultsSheet = workbook.newWorksheet("Analysis Results")
            writeResultsSheet(resultsSheet, results)

            workbook.finish()
        }

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("$baseName.xlsx"))
            xlsxFile.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()

            results.forEachIndexed { index, result ->
                val imagePath = result.imagePath ?: return@forEachIndexed
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    val entryName = "images/${index + 1}_${imageFile.name}"
                    zos.putNextEntry(ZipEntry(entryName))
                    imageFile.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }

        xlsxFile.delete()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zipFile)
    }

    fun exportAllSessions(
        context: Context,
        sessions: List<SessionEntity>,
        allResults: Map<Long, List<AnalysisResultEntity>>
    ): Uri {
        val exportDir = File(context.cacheDir, "exports")
        exportDir.mkdirs()
        val fileName = "all_sessions_${dateFormat.format(Date())}.xlsx"
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { outputStream ->
            val workbook = Workbook(outputStream, "SessionExporter", "1.0")

            val allSheet = workbook.newWorksheet("All Sessions")
            writeAllSessionsSheet(allSheet, sessions)

            sessions.forEach { session ->
                val sResults = allResults[session.id] ?: return@forEach
                if (sResults.isEmpty()) return@forEach
                val sheet = workbook.newWorksheet("Session ${session.id}")
                writeResultsSheet(sheet, sResults)
            }

            workbook.finish()
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun writeSessionSheet(sheet: Worksheet, session: SessionEntity) {
        val headers = arrayOf(
            "Session ID", "Start Time", "End Time", "Status",
            "Interval (ms)", "Total Captures", "Blocked", "Safe",
            "Battery Start (%)", "Battery End (%)", "Charging",
            "CPU Time (ms)", "CPU Usage (%)", "RAM PSS (MB)"
        )
        headers.forEachIndexed { i, h -> sheet.value(0, i, h) }
        sheet.range(0, 0, 0, headers.size - 1).style().bold().set()

        val row = 1
        sheet.value(row, 0, session.id)
        sheet.value(row, 1, dateTimeFormat.format(Date(session.startTime)))
        sheet.value(row, 2, session.endTime?.let { dateTimeFormat.format(Date(it)) } ?: "ACTIVE")
        sheet.value(row, 3, session.status)
        sheet.value(row, 4, session.intervalMs)
        sheet.value(row, 5, session.totalCaptures)
        sheet.value(row, 6, session.blockedCount)
        sheet.value(row, 7, session.safeCount)
        sheet.value(row, 8, session.batteryStart)
        sheet.value(row, 9, session.batteryEnd)
        sheet.value(row, 10, if (session.batteryCharging) "Yes" else "No")
        sheet.value(row, 11, session.cpuTimeMs)
        sheet.value(row, 12, session.cpuUsagePercent)
        sheet.value(row, 13, session.ramPssMb)
    }

    private fun writeResultsSheet(sheet: Worksheet, results: List<AnalysisResultEntity>) {
        val headers = arrayOf(
            "#", "Timestamp", "Result", "Total Time (ms)", "OCR Time (ms)",
            "ONNX Time (ms)", "Threat Details", "Image", "Analysis Report"
        )
        headers.forEachIndexed { i, h -> sheet.value(0, i, h) }
        sheet.range(0, 0, 0, headers.size - 1).style().bold().set()

        results.forEachIndexed { index, result ->
            val row = index + 1
            val totalTime = result.ocrTimeMs + result.onnxTimeMs
            val imageName = if (result.imagePath != null) {
                "images/${index + 1}_${File(result.imagePath).name}"
            } else {
                "-"
            }
            sheet.value(row, 0, index + 1)
            sheet.value(row, 1, dateTimeFormat.format(Date(result.timestamp)))
            sheet.value(row, 2, result.decision)
            sheet.value(row, 3, totalTime)
            sheet.value(row, 4, result.ocrTimeMs)
            sheet.value(row, 5, result.onnxTimeMs)
            sheet.value(row, 6, result.threatDetails)
            sheet.value(row, 7, imageName)
            sheet.value(row, 8, result.analysisResult)
        }
    }

    private fun writeAllSessionsSheet(sheet: Worksheet, sessions: List<SessionEntity>) {
        val headers = arrayOf(
            "Session ID", "Start Time", "End Time", "Status",
            "Interval (ms)", "Total Captures", "Blocked", "Safe",
            "Battery Start (%)", "Battery End (%)", "Charging",
            "CPU Time (ms)", "CPU Usage (%)", "RAM PSS (MB)"
        )
        headers.forEachIndexed { i, h -> sheet.value(0, i, h) }
        sheet.range(0, 0, 0, headers.size - 1).style().bold().set()

        sessions.forEachIndexed { index, session ->
            val row = index + 1
            sheet.value(row, 0, session.id)
            sheet.value(row, 1, dateTimeFormat.format(Date(session.startTime)))
            sheet.value(row, 2, session.endTime?.let { dateTimeFormat.format(Date(it)) } ?: "ACTIVE")
            sheet.value(row, 3, session.status)
            sheet.value(row, 4, session.intervalMs)
            sheet.value(row, 5, session.totalCaptures)
            sheet.value(row, 6, session.blockedCount)
            sheet.value(row, 7, session.safeCount)
            sheet.value(row, 8, session.batteryStart)
            sheet.value(row, 9, session.batteryEnd)
            sheet.value(row, 10, if (session.batteryCharging) "Yes" else "No")
            sheet.value(row, 11, session.cpuTimeMs)
            sheet.value(row, 12, session.cpuUsagePercent)
            sheet.value(row, 13, session.ramPssMb)
        }
    }
}
