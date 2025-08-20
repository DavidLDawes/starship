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

package starship.virtualsoundnw.com.data.local.database

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [StarShip] calculated properties.
 */
class StarShipTest {

    @Test
    fun hullClass_smallShips_returnsCorrectCodes() {
        // Test non-capital ships
        assertEquals("1", createShip(100).hullClass)
        assertEquals("2", createShip(200).hullClass)
        assertEquals("3", createShip(300).hullClass)
        assertEquals("4", createShip(400).hullClass)
        assertEquals("5", createShip(500).hullClass)
        assertEquals("A", createShip(1000).hullClass)
        assertEquals("L", createShip(2000).hullClass)
    }

    @Test
    fun hullClass_capitalShips_returnsCorrectCodes() {
        // Test capital ships
        assertEquals("CA", createShip(2500).hullClass)
        assertEquals("CB", createShip(3500).hullClass)
        assertEquals("CC", createShip(4500).hullClass)
        assertEquals("CF", createShip(8000).hullClass)
        assertEquals("CZ", createShip(1500000).hullClass)
    }

    @Test
    fun hullCost_standardConfiguration_returnsBaseCost() {
        // Standard configuration has no multiplier (1.0x)
        // Base cost is 0.1 MCr per ton
        assertEquals(10.0f, createShip(100, Configuration.STANDARD).hullCost, 0.01f)
        assertEquals(20.0f, createShip(200, Configuration.STANDARD).hullCost, 0.01f)
        assertEquals(50.0f, createShip(500, Configuration.STANDARD).hullCost, 0.01f)
        assertEquals(100.0f, createShip(1000, Configuration.STANDARD).hullCost, 0.01f)
        assertEquals(200.0f, createShip(2000, Configuration.STANDARD).hullCost, 0.01f)
    }

    @Test
    fun hullCost_configurationMultipliers_applyCorrectly() {
        val baseTons = 1000
        val baseCost = 100.0f // 1000 tons * 0.1 MCr per ton
        
        // Test all configuration multipliers
        assertEquals(baseCost * 1.2f, createShip(baseTons, Configuration.NEEDLE_WEDGE).hullCost, 0.01f) // +20%
        assertEquals(baseCost * 1.1f, createShip(baseTons, Configuration.CONE).hullCost, 0.01f) // +10%
        assertEquals(baseCost * 1.0f, createShip(baseTons, Configuration.STANDARD).hullCost, 0.01f) // No change
        assertEquals(baseCost * 0.9f, createShip(baseTons, Configuration.CLOSE_STRUCTURE).hullCost, 0.01f) // -10%
        assertEquals(baseCost * 0.8f, createShip(baseTons, Configuration.SPHERE).hullCost, 0.01f) // -20%
        assertEquals(baseCost * 0.5f, createShip(baseTons, Configuration.DISPERSED_STRUCTURE).hullCost, 0.01f) // -50%
        assertEquals(baseCost * 0.004f, createShip(baseTons, Configuration.PLANETOID).hullCost, 0.01f) // -99.6%
        assertEquals(baseCost * 0.004f, createShip(baseTons, Configuration.BUFFERED_PLANETOID).hullCost, 0.01f) // -99.6%
    }

    @Test
    fun hullCost_planetoidConfigurations_extremelyLowCost() {
        // Planetoid configurations should be very cheap (99.6% discount)
        val ship = createShip(1000, Configuration.PLANETOID)
        assertEquals(0.4f, ship.hullCost, 0.01f) // 100 * 0.004 = 0.4 MCr
        
        val bufferedShip = createShip(1000, Configuration.BUFFERED_PLANETOID)  
        assertEquals(0.4f, bufferedShip.hullCost, 0.01f) // Same discount
    }

    @Test
    fun isCapitalShip_correctClassification() {
        assertEquals(false, createShip(2000).isCapitalShip)
        assertEquals(true, createShip(2001).isCapitalShip)
        assertEquals(true, createShip(10000).isCapitalShip)
    }

    @Test
    fun shipDesignation_correctDesignation() {
        assertEquals("Ship", createShip(2000).shipDesignation)
        assertEquals("Capital Ship", createShip(2001).shipDesignation)
    }

    @Test
    fun configurationDisplayName_returnsCorrectDisplayNames() {
        assertEquals("Standard", Configuration.STANDARD.displayName())
        assertEquals("Needle/Wedge", Configuration.NEEDLE_WEDGE.displayName())
        assertEquals("Dispersed Structure", Configuration.DISPERSED_STRUCTURE.displayName())
        assertEquals("Buffered Planetoid", Configuration.BUFFERED_PLANETOID.displayName())
    }

    @Test
    fun hullCode_singleCharacterCodes_correctForSmallShips() {
        // Test exact tonnages from Issue #68 specification
        assertEquals("A", createShip(100).hullCode)
        assertEquals("A", createShip(191).hullCode)
        assertEquals("A", createShip(200).hullCode)
        assertEquals("B", createShip(201).hullCode)
        assertEquals("B", createShip(250).hullCode)
        assertEquals("B", createShip(300).hullCode)
        assertEquals("C", createShip(301).hullCode)
        assertEquals("C", createShip(400).hullCode)
        assertEquals("D", createShip(401).hullCode)
        assertEquals("D", createShip(500).hullCode)
        assertEquals("E", createShip(501).hullCode)
        assertEquals("E", createShip(600).hullCode)
        assertEquals("F", createShip(601).hullCode)
        assertEquals("F", createShip(700).hullCode)
        assertEquals("G", createShip(701).hullCode)
        assertEquals("G", createShip(800).hullCode)
        assertEquals("H", createShip(801).hullCode)
        assertEquals("H", createShip(900).hullCode)
        assertEquals("J", createShip(901).hullCode)
        assertEquals("J", createShip(1000).hullCode)
    }

    @Test
    fun hullCode_singleCharacterCodes_correctForLargerShips() {
        // Test the higher ranges for single character codes
        assertEquals("K", createShip(1001).hullCode)
        assertEquals("K", createShip(1100).hullCode)
        assertEquals("L", createShip(1101).hullCode)
        assertEquals("L", createShip(1200).hullCode)
        assertEquals("M", createShip(1201).hullCode)
        assertEquals("M", createShip(1300).hullCode)
        assertEquals("N", createShip(1301).hullCode)
        assertEquals("N", createShip(1400).hullCode)
        assertEquals("P", createShip(1401).hullCode)
        assertEquals("P", createShip(1500).hullCode)
        assertEquals("Q", createShip(1501).hullCode)
        assertEquals("Q", createShip(1600).hullCode)
        assertEquals("R", createShip(1601).hullCode)
        assertEquals("R", createShip(1700).hullCode)
        assertEquals("S", createShip(1701).hullCode)
        assertEquals("S", createShip(1800).hullCode)
        assertEquals("T", createShip(1801).hullCode)
        assertEquals("T", createShip(1900).hullCode)
        assertEquals("U", createShip(1901).hullCode)
        assertEquals("U", createShip(2000).hullCode)
    }

    @Test
    fun hullCode_twoCharacterCodes_correctForCapitalShips() {
        // Test exact tonnages from Issue #68 specification
        assertEquals("CA", createShip(3000).hullCode)
        assertEquals("CB", createShip(4000).hullCode)
        assertEquals("CC", createShip(5000).hullCode)
        assertEquals("CD", createShip(6000).hullCode)
        assertEquals("CE", createShip(7500).hullCode)
        assertEquals("CF", createShip(10000).hullCode)
        assertEquals("CG", createShip(15000).hullCode)
        assertEquals("CH", createShip(20000).hullCode)
        assertEquals("CJ", createShip(25000).hullCode)
        assertEquals("CK", createShip(30000).hullCode)
        assertEquals("CL", createShip(40000).hullCode)
        assertEquals("CM", createShip(50000).hullCode)
        assertEquals("CN", createShip(60000).hullCode)
        assertEquals("CP", createShip(75000).hullCode)
        assertEquals("CQ", createShip(100000).hullCode)
        assertEquals("CR", createShip(200000).hullCode)
        assertEquals("CS", createShip(300000).hullCode)
        assertEquals("CT", createShip(400000).hullCode)
        assertEquals("CU", createShip(500000).hullCode)
        assertEquals("CV", createShip(600000).hullCode)
        assertEquals("CW", createShip(700000).hullCode)
        assertEquals("CX", createShip(800000).hullCode)
        assertEquals("CY", createShip(900000).hullCode)
        assertEquals("CZ", createShip(1000000).hullCode)
    }

    @Test
    fun hullCode_unknownTonnages_returnsUnknown() {
        // Test tonnages that don't match the exact specifications
        assertEquals("Unknown", createShip(150).hullCode) // Between 100 and 191-200 range
        assertEquals("Unknown", createShip(2500).hullCode) // Between 2000 and 3000
        assertEquals("Unknown", createShip(3500).hullCode) // Between 3000 and 4000
        assertEquals("Unknown", createShip(1500000).hullCode) // Above 1,000,000
    }

    private fun createShip(tons: Int, configuration: Configuration = Configuration.STANDARD): StarShip {
        return StarShip(
            name = "Test Ship",
            description = "Test description",
            tons = tons,
            techLevel = TechLevel.C,
            configuration = configuration
        )
    }
}