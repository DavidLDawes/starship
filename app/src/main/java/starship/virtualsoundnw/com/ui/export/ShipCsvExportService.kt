/*
 * Copyright (C) 2025 David L. Dawes
 * Notice: As this license may require, be aware this file is new and had been added by David L. Dawes since cloning the original archive from github.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package starship.virtualsoundnw.com.ui.export

import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import androidx.core.content.ContextCompat.getSystemService
import starship.virtualsoundnw.com.data.DetailedShipTableData
import starship.virtualsoundnw.com.data.ShipTableRow
import starship.virtualsoundnw.com.ui.utils.RoundingUtils
import java.io.File
import java.io.FileWriter
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling ship design CSV export functionality
 */
@Singleton
class ShipCsvExportService @Inject constructor() {

    /**
     * Generate CSV content from ship table data
     */
    fun generateCsv(tableData: DetailedShipTableData): String {
        val csvBuilder = StringBuilder()
        
        // Ship header information
        csvBuilder.appendLine("Ship Name,${escapeForCsv(tableData.ship.name)}")
        csvBuilder.appendLine("Description,${escapeForCsv(tableData.shipDescription)}")
        csvBuilder.appendLine("Tonnage,${tableData.ship.tons}")
        csvBuilder.appendLine("Tech Level,${tableData.ship.techLevel}")
        csvBuilder.appendLine("Configuration,${tableData.ship.configuration}")
        csvBuilder.appendLine() // Empty line for separation
        
        // Table headers
        csvBuilder.appendLine("Category,Item,Tons,Cost (MCr),Crew")
        
        // Table rows
        for (row in tableData.rows) {
            val category = if (row.isFirstInCategory) row.category else ""
            csvBuilder.appendLine(
                "${escapeForCsv(category)},${escapeForCsv(row.item)},${RoundingUtils.roundUpTons(row.tons)},${RoundingUtils.roundUpMCr(row.costMCr)},${row.crew}"
            )
        }
        
        // Empty line before totals
        csvBuilder.appendLine()
        
        // Totals row
        csvBuilder.appendLine("TOTALS,,${RoundingUtils.roundUpTons(tableData.totalTons)},${RoundingUtils.roundUpMCr(tableData.totalCostMCr)},${tableData.totalCrew}")
        
        // Remaining tons
        csvBuilder.appendLine("Remaining Tons,,${RoundingUtils.roundUpTons(tableData.remainingTons)},,")
        
        return csvBuilder.toString()
    }

    /**
     * Copy CSV content to system clipboard
     */
    fun copyToClipboard(context: Context, csvContent: String): Result<Unit> {
        return try {
            val clipboardManager = getSystemService(context, ClipboardManager::class.java)
            val clipData = ClipData.newPlainText("Ship Design CSV", csvContent)
            clipboardManager?.setPrimaryClip(clipData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save CSV content to external storage
     */
    fun saveToFile(context: Context, csvContent: String, fileName: String): Result<File> {
        return try {
            val downloadsDir = File(context.getExternalFilesDir(null), "downloads")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val csvFile = File(downloadsDir, fileName)
            FileWriter(csvFile).use { writer ->
                writer.write(csvContent)
            }
            Result.success(csvFile)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Generate a filename for the CSV export
     */
    fun generateFileName(shipName: String): String {
        val sanitizedName = shipName.replace("[^a-zA-Z0-9\\-_]".toRegex(), "_")
        return "${sanitizedName}_design.csv"
    }

    /**
     * Escape special characters for CSV format
     */
    private fun escapeForCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}