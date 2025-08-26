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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import starship.virtualsoundnw.com.data.DetailedShipTableData
import starship.virtualsoundnw.com.data.ShipTableRow
import starship.virtualsoundnw.com.data.local.database.Configuration
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel

/**
 * Unit tests for [ShipCsvExportService]
 */
class ShipCsvExportServiceTest {

    private lateinit var csvExportService: ShipCsvExportService

    @Before
    fun setup() {
        csvExportService = ShipCsvExportService()
    }

    @Test
    fun csvExportService_canBeInstantiated() {
        assertNotNull(csvExportService)
    }

    @Test
    fun generateCsv_includesShipHeader() {
        // Given
        val ship = StarShip("Enterprise", "Constitution class", 200, TechLevel.G, Configuration.STANDARD)
        val tableData = DetailedShipTableData(
            ship = ship,
            rows = emptyList(),
            totalTons = 200.0,
            totalCostMCr = 50.0,
            totalCrew = 10,
            remainingTons = 0.0
        )

        // When
        val csvContent = csvExportService.generateCsv(tableData)

        // Then
        assertTrue(csvContent.contains("Ship Name,Enterprise"))
        assertTrue(csvContent.contains("Description,Constitution class"))
        assertTrue(csvContent.contains("Tonnage,200"))
        assertTrue(csvContent.contains("Tech Level,G"))
        assertTrue(csvContent.contains("Configuration,STANDARD"))
    }

    @Test
    fun generateCsv_includesTableHeaders() {
        // Given
        val ship = StarShip("Voyager", "Intrepid class", 300, TechLevel.H, Configuration.STANDARD)
        val tableData = DetailedShipTableData(
            ship = ship,
            rows = emptyList(),
            totalTons = 300.0,
            totalCostMCr = 100.0,
            totalCrew = 15,
            remainingTons = 0.0
        )

        // When
        val csvContent = csvExportService.generateCsv(tableData)

        // Then
        assertTrue(csvContent.contains("Category,Item,Tons,Cost (MCr),Crew"))
    }

    @Test
    fun generateCsv_includesDataRows() {
        // Given
        val ship = StarShip("Defiant", "Escort class", 170, TechLevel.J, Configuration.STANDARD)
        val rows = listOf(
            ShipTableRow("Hull", "Hull", 170.0, 42.5, 0, true),
            ShipTableRow("Engines", "Power Plant", 20.0, 30.0, 2, true),
            ShipTableRow("Engines", "Jump Drive", 15.0, 22.5, 0, false)
        )
        val tableData = DetailedShipTableData(
            ship = ship,
            rows = rows,
            totalTons = 205.0,
            totalCostMCr = 95.0,
            totalCrew = 2,
            remainingTons = -35.0
        )

        // When
        val csvContent = csvExportService.generateCsv(tableData)

        // Then
        assertTrue(csvContent.contains("Hull,Hull,170.0,42.5,0"))
        assertTrue(csvContent.contains("Engines,Power Plant,20.0,30.0,2"))
        assertTrue(csvContent.contains(",Jump Drive,15.0,22.5,0")) // Category should be empty for non-first
    }

    @Test
    fun generateCsv_includesTotalsAndRemaining() {
        // Given
        val ship = StarShip("Serenity", "Firefly class", 250, TechLevel.D, Configuration.STANDARD)
        val tableData = DetailedShipTableData(
            ship = ship,
            rows = emptyList(),
            totalTons = 250.0,
            totalCostMCr = 62.5,
            totalCrew = 8,
            remainingTons = 0.0
        )

        // When
        val csvContent = csvExportService.generateCsv(tableData)

        // Then
        assertTrue(csvContent.contains("TOTALS,,250.0,62.5,8"))
        assertTrue(csvContent.contains("Remaining Tons,,0.0,,"))
    }

    @Test
    fun generateFileName_sanitizesShipName() {
        // Given
        val shipName = "My Ship's \"Special\" Name!"

        // When
        val fileName = csvExportService.generateFileName(shipName)

        // Then
        // The regex replaces all non-alphanumeric characters (except - and _) with _
        assertEquals("My_Ship_s__Special__Name__design.csv", fileName)
    }

    @Test
    fun escapeForCsv_handlesCommasAndQuotes() {
        // Given
        val ship = StarShip("Test Ship", "A ship, with \"special\" characters", 100, TechLevel.C, Configuration.STANDARD)
        val tableData = DetailedShipTableData(
            ship = ship,
            rows = emptyList(),
            totalTons = 100.0,
            totalCostMCr = 25.0,
            totalCrew = 5,
            remainingTons = 0.0
        )

        // When
        val csvContent = csvExportService.generateCsv(tableData)

        // Then
        // Should properly escape the description with commas and quotes
        assertTrue(csvContent.contains("\"A ship, with \"\"special\"\" characters\""))
    }
}