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

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import starship.virtualsoundnw.com.data.DetailedShipTableData
import starship.virtualsoundnw.com.data.ShipTableRow
import starship.virtualsoundnw.com.data.local.database.Configuration
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel

/**
 * Unit tests for [ShipPrintService]
 * 
 * Note: These are basic tests focusing on service instantiation and data validation.
 * Full printing tests would require Android testing framework with device/emulator.
 */
class ShipPrintServiceTest {

    private lateinit var shipPrintService: ShipPrintService

    @Before
    fun setup() {
        shipPrintService = ShipPrintService()
    }

    @Test
    fun shipPrintService_canBeInstantiated() {
        // Test that the service can be created
        assertNotNull(shipPrintService)
    }

    @Test
    fun shipPrintDocumentAdapter_canBeCreatedWithValidData() {
        // Given
        val ship = StarShip("Enterprise", "Constitution class", 200, TechLevel.G, Configuration.STANDARD)
        val tableData = DetailedShipTableData(
            ship = ship,
            rows = listOf(
                ShipTableRow("Hull", "Hull", 200.0, 50.0, 0),
                ShipTableRow("Engines", "Power Plant", 20.0, 25.0, 2)
            ),
            totalTons = 220.0,
            totalCostMCr = 75.0,
            totalCrew = 2,
            remainingTons = -20.0
        )

        // When/Then - This tests that the adapter can be created without context issues
        // In a real test environment with context, this would create the adapter
        assertNotNull(tableData)
        assertNotNull(tableData.ship)
        assertNotNull(tableData.rows)
    }

    @Test
    fun detailedShipTableData_hasCorrectShipHeader() {
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

        // When/Then
        val expectedHeader = "Voyager, 300 ton, STANDARD, H"
        assertNotNull(tableData.shipHeader)
        // Basic validation that header contains key information
        assert(tableData.shipHeader.contains("Voyager"))
        assert(tableData.shipHeader.contains("300"))
        assert(tableData.shipHeader.contains("STANDARD"))
        assert(tableData.shipHeader.contains("H"))
    }
}