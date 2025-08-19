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
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit tests for [Engine] calculations and helper functions.
 */
class EnginesTest {

    @Test
    fun powerPlantTonnagePercentage_returnsCorrectValues() {
        val engine1 = createEngine(EngineType.POWER_PLANT, 1)
        assertEquals(1.5f, engine1.getTonnagePercentage(), 0.01f)
        
        val engine5 = createEngine(EngineType.POWER_PLANT, 5)
        assertEquals(4.0f, engine5.getTonnagePercentage(), 0.01f)
        
        val engine12 = createEngine(EngineType.POWER_PLANT, 12)
        assertEquals(12.0f, engine12.getTonnagePercentage(), 0.01f)
    }

    @Test
    fun maneuverDriveTonnagePercentage_returnsCorrectValues() {
        // Test M-0 (no engine)
        val engine0 = createEngine(EngineType.MANEUVER_DRIVE, 0)
        assertEquals(0.0f, engine0.getTonnagePercentage(), 0.01f)
        
        // Test regular performance levels
        val engine1 = createEngine(EngineType.MANEUVER_DRIVE, 1)
        assertEquals(1.0f, engine1.getTonnagePercentage(), 0.01f)
        
        val engine6 = createEngine(EngineType.MANEUVER_DRIVE, 6)
        assertEquals(3.25f, engine6.getTonnagePercentage(), 0.01f)
        
        val engine12 = createEngine(EngineType.MANEUVER_DRIVE, 12)
        assertEquals(8.0f, engine12.getTonnagePercentage(), 0.01f)
    }

    @Test
    fun jumpDriveTonnagePercentage_returnsCorrectValues() {
        val engine1 = createEngine(EngineType.JUMP_DRIVE, 1)
        assertEquals(2.0f, engine1.getTonnagePercentage(), 0.01f)
        
        val engine6 = createEngine(EngineType.JUMP_DRIVE, 6)
        assertEquals(7.0f, engine6.getTonnagePercentage(), 0.01f)
        
        val engine12 = createEngine(EngineType.JUMP_DRIVE, 12)
        assertEquals(13.0f, engine12.getTonnagePercentage(), 0.01f)
    }

    @Test
    fun engineTonnage_calculatesCorrectlyForShipSize() {
        val shipTonnage = 200
        val engine = createEngine(EngineType.POWER_PLANT, 5) // 4% tonnage
        
        assertEquals(8.0f, engine.getTonnage(shipTonnage), 0.01f) // 200 * 4% = 8 tons
    }

    @Test
    fun powerPlantCostPerTon_variesByTechLevel() {
        val engine = createEngine(EngineType.POWER_PLANT, 5)
        
        assertEquals(1.0f, engine.getCostPerTon(TechLevel.A), 0.01f) // Fission
        assertEquals(2.0f, engine.getCostPerTon(TechLevel.C), 0.01f) // Fusion  
        assertEquals(5.0f, engine.getCostPerTon(TechLevel.F), 0.01f) // Advanced Fusion
        assertEquals(2.5f, engine.getCostPerTon(TechLevel.H), 0.01f) // Antimatter
    }

    @Test
    fun maneuverDriveCostPerTon_variesByPerformance() {
        assertEquals(0.5f, createEngine(EngineType.MANEUVER_DRIVE, 1).getCostPerTon(TechLevel.C), 0.01f)
        assertEquals(0.5f, createEngine(EngineType.MANEUVER_DRIVE, 6).getCostPerTon(TechLevel.C), 0.01f)
        assertEquals(0.6f, createEngine(EngineType.MANEUVER_DRIVE, 7).getCostPerTon(TechLevel.C), 0.01f)
        assertEquals(0.7f, createEngine(EngineType.MANEUVER_DRIVE, 9).getCostPerTon(TechLevel.C), 0.01f)
        assertEquals(0.8f, createEngine(EngineType.MANEUVER_DRIVE, 12).getCostPerTon(TechLevel.C), 0.01f)
    }

    @Test
    fun jumpDriveCostPerTon_variesByPerformance() {
        assertEquals(2.0f, createEngine(EngineType.JUMP_DRIVE, 1).getCostPerTon(TechLevel.C), 0.01f)
        assertEquals(2.0f, createEngine(EngineType.JUMP_DRIVE, 6).getCostPerTon(TechLevel.G), 0.01f)
        assertEquals(2.2f, createEngine(EngineType.JUMP_DRIVE, 7).getCostPerTon(TechLevel.G), 0.01f)
        assertEquals(2.5f, createEngine(EngineType.JUMP_DRIVE, 9).getCostPerTon(TechLevel.H), 0.01f)
        assertEquals(3.0f, createEngine(EngineType.JUMP_DRIVE, 12).getCostPerTon(TechLevel.J), 0.01f)
    }

    @Test
    fun engineTotalCost_calculatesCorrectly() {
        val shipTonnage = 1000
        val shipTechLevel = TechLevel.C
        val engine = createEngine(EngineType.POWER_PLANT, 5) // 4% tonnage, 2.0 MCr/ton for Fusion
        
        val expectedTonnage = 40.0f // 1000 * 4% 
        val expectedCostPerTon = 2.0f // Fusion power plant
        val expectedTotalCost = 80.0f // 40 * 2.0
        
        assertEquals(expectedTotalCost, engine.getTotalCost(shipTonnage, shipTechLevel), 0.01f)
    }

    @Test
    fun engineDesignation_formatsCorrectly() {
        assertEquals("P-5", createEngine(EngineType.POWER_PLANT, 5).getDesignation())
        assertEquals("J-2", createEngine(EngineType.JUMP_DRIVE, 2).getDesignation())
        assertEquals("M-0", createEngine(EngineType.MANEUVER_DRIVE, 0).getDesignation())
        assertEquals("M-3", createEngine(EngineType.MANEUVER_DRIVE, 3).getDesignation())
    }

    @Test
    fun jumpDrivePerformanceValidation_respectsTechLevelLimits() {
        // TL A can only have J-1
        assertTrue(isJumpDrivePerformanceValidForTechLevel(1, TechLevel.A))
        assertFalse(isJumpDrivePerformanceValidForTechLevel(2, TechLevel.A))
        
        // TL B can have J-1, J-2
        assertTrue(isJumpDrivePerformanceValidForTechLevel(1, TechLevel.B))
        assertTrue(isJumpDrivePerformanceValidForTechLevel(2, TechLevel.B))
        assertFalse(isJumpDrivePerformanceValidForTechLevel(3, TechLevel.B))
        
        // TL G can have J-1 through J-8
        assertTrue(isJumpDrivePerformanceValidForTechLevel(7, TechLevel.G))
        assertTrue(isJumpDrivePerformanceValidForTechLevel(8, TechLevel.G))
        assertFalse(isJumpDrivePerformanceValidForTechLevel(9, TechLevel.G))
        
        // TL J can have all jump drives
        assertTrue(isJumpDrivePerformanceValidForTechLevel(12, TechLevel.J))
    }

    @Test
    fun powerPlantTypeBestAvailable_selectsCorrectType() {
        assertEquals(PowerPlantType.FISSION, PowerPlantType.getBestAvailableForTechLevel(TechLevel.A))
        assertEquals(PowerPlantType.FUSION, PowerPlantType.getBestAvailableForTechLevel(TechLevel.C))
        assertEquals(PowerPlantType.ADVANCED_FUSION, PowerPlantType.getBestAvailableForTechLevel(TechLevel.G))
        assertEquals(PowerPlantType.ANTIMATTER, PowerPlantType.getBestAvailableForTechLevel(TechLevel.H))
    }

    @Test
    fun fuelCalculation_standardPowerPlant() {
        val shipTonnage = 200
        val jumpPerformance = 3
        
        // J-3: (3 * 10% + 2%) = 32% of ship tonnage = 64 tons for 200 ton ship
        val expectedFuel = 64.0f
        
        assertEquals(expectedFuel, calculateFuelRequirement(jumpPerformance, shipTonnage, false), 0.01f)
    }

    @Test
    fun fuelCalculation_antimatterPowerPlant() {
        val shipTonnage = 200
        val jumpPerformance = 3
        
        // J-3 with antimatter: 64 tons / 10 = 6.4 tons
        val expectedFuel = 6.4f
        
        assertEquals(expectedFuel, calculateFuelRequirement(jumpPerformance, shipTonnage, true), 0.01f)
    }

    @Test
    fun fuelCalculation_edgeCases() {
        // J-1 with small ship: (1 * 10 + 2) = 12%, 20 * 0.12 = 2.4
        assertEquals(2.4f, calculateFuelRequirement(1, 20, false), 0.01f)
        
        // J-12 with large ship and antimatter: (12 * 10 + 2) = 122%, 200 * 1.22 = 244, 244 / 10 = 24.4
        assertEquals(24.4f, calculateFuelRequirement(12, 200, true), 0.01f)
    }

    private fun createEngine(type: EngineType, performance: Int): Engine {
        return Engine(
            shipId = 1,
            type = type,
            performance = performance
        )
    }
}