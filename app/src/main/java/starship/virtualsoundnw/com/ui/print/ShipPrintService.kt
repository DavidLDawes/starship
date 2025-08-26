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
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import starship.virtualsoundnw.com.data.DetailedShipTableData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling ship design printing functionality
 */
@Singleton
class ShipPrintService @Inject constructor() {
    
    /**
     * Print ship design with detailed table
     */
    fun printShipDesign(
        context: Context,
        shipName: String,
        tableData: DetailedShipTableData
    ): PrintJob? {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Ship Design - $shipName"
        
        // Create print adapter
        val printAdapter = ShipPrintDocumentAdapter(context, tableData)
        
        // Start print job
        return printManager.print(
            jobName,
            printAdapter,
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .build()
        )
    }
}