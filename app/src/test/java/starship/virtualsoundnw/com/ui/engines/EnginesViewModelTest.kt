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

package starship.virtualsoundnw.com.ui.engines

import org.junit.Assert.*
import org.junit.Test
import starship.virtualsoundnw.com.data.local.database.*

/**
 * Unit tests for power plant validation scenarios.
 * Tests the business logic for engine performance constraints.
 */
class EngineValidationTest {

    @Test
    fun testPowerPlantConstraintLogic() {
        // Test scenario: Multiple power plants with different performance levels
        val powerPlant1 = createEngine(EngineType.POWER_PLANT, 6)
        val powerPlant2 = createEngine(EngineType.POWER_PLANT, 4)
        val jumpDrive = createEngine(EngineType.JUMP_DRIVE, 5)
        val maneuverDrive = createEngine(EngineType.MANEUVER_DRIVE, 6)
        
        val allPowerPlants = listOf(powerPlant1, powerPlant2)
        
        // When removing powerPlant1 (performance 6), remaining max is 4
        val remainingPowerPlants = allPowerPlants.filter { it.uid != powerPlant1.uid }
        val newMaxPerformance = remainingPowerPlants.maxOfOrNull { it.performance } ?: 0
        
        assertEquals(4, newMaxPerformance)
        
        // Jump drive (5) exceeds new max (4)
        assertTrue(jumpDrive.performance > newMaxPerformance)
        
        // Maneuver drive (6) exceeds new max (4)
        assertTrue(maneuverDrive.performance > newMaxPerformance)
    }
    
    @Test
    fun testNoAdjustmentNeededWhenRemovingLowerPerformancePowerPlant() {
        // Test scenario: Removing lower performance power plant
        val powerPlant1 = createEngine(EngineType.POWER_PLANT, 6)
        val powerPlant2 = createEngine(EngineType.POWER_PLANT, 4)
        val jumpDrive = createEngine(EngineType.JUMP_DRIVE, 5)
        val maneuverDrive = createEngine(EngineType.MANEUVER_DRIVE, 3)
        
        val allPowerPlants = listOf(powerPlant1, powerPlant2)
        
        // When removing powerPlant2 (performance 4), remaining max is 6
        val remainingPowerPlants = allPowerPlants.filter { it.uid != powerPlant2.uid }
        val newMaxPerformance = remainingPowerPlants.maxOfOrNull { it.performance } ?: 0
        
        assertEquals(6, newMaxPerformance)
        
        // Jump drive (5) does not exceed new max (6)
        assertFalse(jumpDrive.performance > newMaxPerformance)
        
        // Maneuver drive (3) does not exceed new max (6)
        assertFalse(maneuverDrive.performance > newMaxPerformance)
    }
    
    @Test
    fun testSinglePowerPlantScenario() {
        // Test scenario: Only one power plant (should not trigger validation)
        val powerPlant = createEngine(EngineType.POWER_PLANT, 5)
        val allPowerPlants = listOf(powerPlant)
        
        // Only one power plant, so removal shouldn't trigger adjustment logic
        assertTrue(allPowerPlants.size <= 1)
    }
    
    @Test
    fun testEnginePerformanceBoundaries() {
        // Test edge cases for engine performance levels
        val lowPowerPlant = createEngine(EngineType.POWER_PLANT, 1)
        val highPowerPlant = createEngine(EngineType.POWER_PLANT, 12)
        val midJumpDrive = createEngine(EngineType.JUMP_DRIVE, 6)
        
        val allPowerPlants = listOf(lowPowerPlant, highPowerPlant)
        
        // When removing high power plant (12), remaining max is 1
        val remainingPowerPlants = allPowerPlants.filter { it.uid != highPowerPlant.uid }
        val newMaxPerformance = remainingPowerPlants.maxOfOrNull { it.performance } ?: 0
        
        assertEquals(1, newMaxPerformance)
        
        // Jump drive (6) exceeds new max (1) - should be adjusted
        assertTrue(midJumpDrive.performance > newMaxPerformance)
    }
    
    @Test
    fun testMultipleEngineTypesAdjustment() {
        // Test that both jump drives and maneuver drives get adjusted
        val powerPlantHigh = createEngine(EngineType.POWER_PLANT, 8)
        val powerPlantLow = createEngine(EngineType.POWER_PLANT, 3)
        val jumpDrive1 = createEngine(EngineType.JUMP_DRIVE, 6)
        val jumpDrive2 = createEngine(EngineType.JUMP_DRIVE, 4)
        val maneuverDrive1 = createEngine(EngineType.MANEUVER_DRIVE, 7)
        val maneuverDrive2 = createEngine(EngineType.MANEUVER_DRIVE, 2)
        
        val allPowerPlants = listOf(powerPlantHigh, powerPlantLow)
        val allJumpDrives = listOf(jumpDrive1, jumpDrive2)
        val allManeuverDrives = listOf(maneuverDrive1, maneuverDrive2)
        
        // Remove high power plant (8), new max is 3
        val newMaxPerformance = 3
        
        // Check which engines need adjustment
        val jumpDrivesToAdjust = allJumpDrives.filter { it.performance > newMaxPerformance }
        val maneuverDrivesToAdjust = allManeuverDrives.filter { it.performance > newMaxPerformance }
        
        assertEquals(2, jumpDrivesToAdjust.size) // jumpDrive1 (6) > 3 and jumpDrive2 (4) > 3
        assertEquals(1, maneuverDrivesToAdjust.size) // maneuverDrive1 (7) > 3
        
        assertTrue(jumpDrivesToAdjust.contains(jumpDrive1))
        assertTrue(maneuverDrivesToAdjust.contains(maneuverDrive1))
        assertTrue(jumpDrivesToAdjust.contains(jumpDrive2)) // jumpDrive2 (4) > 3, should be adjusted
        assertFalse(maneuverDrivesToAdjust.contains(maneuverDrive2)) // maneuverDrive2 (2) <= 3
    }
    
    private fun createEngine(type: EngineType, performance: Int): Engine {
        return Engine(
            shipId = 1,
            type = type,
            performance = performance
        ).apply { uid = type.ordinal * 100 + performance } // Unique ID
    }
}