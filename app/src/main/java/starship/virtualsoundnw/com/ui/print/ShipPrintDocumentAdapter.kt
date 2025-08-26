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

package starship.virtualsoundnw.com.ui.print

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import starship.virtualsoundnw.com.data.DetailedShipTableData
import starship.virtualsoundnw.com.data.ShipTableRow
import java.io.FileOutputStream
import java.io.IOException

/**
 * Print document adapter for ship design detailed tables
 */
class ShipPrintDocumentAdapter(
    private val context: Context,
    private val tableData: DetailedShipTableData
) : PrintDocumentAdapter() {

    private var pdfDocument: PrintedPdfDocument? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        // Create a new PdfDocument with the requested page attributes
        pdfDocument = PrintedPdfDocument(context, newAttributes)

        // Respond to cancellation requests
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        // Compute the expected number of printed pages
        val pages = 1 // Single page for ship design

        if (pages > 0) {
            // Return print information to print framework
            val info = PrintDocumentInfo.Builder("ship_design.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pages)
                .build()
            
            // Content layout reflow is complete
            callback.onLayoutFinished(info, true)
        } else {
            // Otherwise report an error to the print framework
            callback.onLayoutFailed("Page count calculation failed.")
        }
    }

    override fun onWrite(
        pageRanges: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        // Iterate over each page of the document,
        // check if it's in a page range and then
        // write the page
        var totalPages = 0
        
        try {
            val pdfDoc = pdfDocument ?: throw IllegalStateException("PdfDocument is null")
            
            for (i in 0 until 1) { // Single page
                // Check to see if this page is in the output range
                if (containsPage(pageRanges, i)) {
                    // If so, add it to the document
                    totalPages++
                    val page = pdfDoc.startPage(i)
                    
                    // Check for cancellation
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        pdfDoc.close()
                        return
                    }
                    
                    // Draw page content for printing
                    drawPage(page)
                    
                    // Finish the page
                    pdfDoc.finishPage(page)
                }
            }
            
            // Write PDF document to output stream
            try {
                pdfDoc.writeTo(FileOutputStream(destination.fileDescriptor))
            } catch (e: IOException) {
                callback.onWriteFailed(e.toString())
                return
            } finally {
                pdfDoc.close()
            }
            
            // Signal the print framework the document is complete
            callback.onWriteFinished(pageRanges)
            
        } catch (e: Exception) {
            callback.onWriteFailed(e.toString())
        }
    }

    /**
     * Draw the ship design content on the page
     */
    private fun drawPage(page: PdfDocument.Page) {
        val canvas = page.canvas
        val pageInfo = page.info
        
        // Define margins and layout
        val margin = 50f
        val contentWidth = pageInfo.pageWidth - (2 * margin)
        var yPos = margin + 50f
        
        // Create paint objects for different text styles
        val titlePaint = Paint().apply {
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        
        val headerPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        
        val bodyPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }
        
        val linePaint = Paint().apply {
            strokeWidth = 1f
            isAntiAlias = true
        }
        
        // Draw ship header
        canvas.drawText("SHIP DESIGN", margin, yPos, titlePaint)
        yPos += 30f
        
        canvas.drawText(tableData.shipHeader, margin, yPos, headerPaint)
        yPos += 25f
        
        canvas.drawText(tableData.shipDescription, margin, yPos, bodyPaint)
        yPos += 35f
        
        // Draw table header
        canvas.drawLine(margin, yPos, margin + contentWidth, yPos, linePaint)
        yPos += 15f
        
        val headerText = String.format("%-20s %-25s %10s %12s %8s", 
            "Category", "Item", "Tons", "Cost (MCr)", "Crew")
        canvas.drawText(headerText, margin, yPos, headerPaint)
        yPos += 20f
        
        canvas.drawLine(margin, yPos, margin + contentWidth, yPos, linePaint)
        yPos += 15f
        
        // Draw table rows
        for (row in tableData.rows) {
            // Check if we need a new page (simple check)
            if (yPos > pageInfo.pageHeight - 100f) {
                // Would need pagination logic here for multi-page documents
                break
            }
            
            val category = if (row.isFirstInCategory) row.category else ""
            val rowText = String.format("%-20s %-25s %10.1f %12.3f %8d", 
                category, row.item, row.tons, row.costMCr, row.crew)
            canvas.drawText(rowText, margin, yPos, bodyPaint)
            yPos += 18f
        }
        
        // Draw totals line
        yPos += 10f
        canvas.drawLine(margin, yPos, margin + contentWidth, yPos, linePaint)
        yPos += 20f
        
        val totalsText = String.format("%-20s %-25s %10.1f %12.3f %8d", 
            "", "TOTALS", tableData.totalTons, tableData.totalCostMCr, tableData.totalCrew)
        canvas.drawText(totalsText, margin, yPos, headerPaint)
        yPos += 25f
        
        // Draw remaining tons
        val remainingText = String.format("Remaining Tons: %.1f", tableData.remainingTons)
        canvas.drawText(remainingText, margin, yPos, bodyPaint)
    }

    /**
     * Check if a page is in the specified page ranges
     */
    private fun containsPage(pageRanges: Array<out PageRange>, page: Int): Boolean {
        for (pageRange in pageRanges) {
            if (page >= pageRange.start && page <= pageRange.end) {
                return true
            }
        }
        return false
    }
}