/*
 * Copyright (C) 2022 The Android Open Source Project
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