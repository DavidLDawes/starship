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

package starship.virtualsoundnw.com.data

import org.junit.Test
import org.junit.Assert.*
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Vehicle

/**
 * Unit tests to verify vehicle tech level filtering logic
 */
class VehiclesDataServiceTest {
    
    @Test
    fun testTechLevelComparison() {
        // Test the basic enum comparison logic
        assertTrue("TL F should be >= TL A", TechLevel.F >= TechLevel.A)
        assertTrue("TL F should be >= TL B", TechLevel.F >= TechLevel.B)
        assertTrue("TL F should be >= TL C", TechLevel.F >= TechLevel.C)
        assertTrue("TL F should be >= TL D", TechLevel.F >= TechLevel.D)
        assertTrue("TL F should be >= TL E", TechLevel.F >= TechLevel.E)
        assertTrue("TL F should be >= TL F", TechLevel.F >= TechLevel.F)
        assertFalse("TL F should not be >= TL G", TechLevel.F >= TechLevel.G)
        assertFalse("TL F should not be >= TL H", TechLevel.F >= TechLevel.H)
        assertFalse("TL F should not be >= TL J", TechLevel.F >= TechLevel.J)
    }
    
    @Test
    fun testVehicleAvailabilityForTechLevel() {
        // Test vehicles with different tech level requirements
        val vehicleNoTL = Vehicle("Buggy", 0.5f, 6000, null)
        val vehicleTLA = Vehicle("Honey Badger", 4.0f, 52436, TechLevel.A)
        val vehicleTLC = Vehicle("HMULV", 8.0f, 225000, TechLevel.C)
        val vehicleTLG = Vehicle("Future Vehicle", 1.0f, 1000000, TechLevel.G)
        
        // Test TL F ship
        assertTrue("Vehicle with no TL should be available to TL F", vehicleNoTL.isAvailableForTechLevel(TechLevel.F))
        assertTrue("Vehicle with TL A should be available to TL F", vehicleTLA.isAvailableForTechLevel(TechLevel.F))
        assertTrue("Vehicle with TL C should be available to TL F", vehicleTLC.isAvailableForTechLevel(TechLevel.F))
        assertFalse("Vehicle with TL G should not be available to TL F", vehicleTLG.isAvailableForTechLevel(TechLevel.F))
        
        // Test TL C ship
        assertTrue("Vehicle with no TL should be available to TL C", vehicleNoTL.isAvailableForTechLevel(TechLevel.C))
        assertTrue("Vehicle with TL A should be available to TL C", vehicleTLA.isAvailableForTechLevel(TechLevel.C))
        assertTrue("Vehicle with TL C should be available to TL C", vehicleTLC.isAvailableForTechLevel(TechLevel.C))
        assertFalse("Vehicle with TL G should not be available to TL C", vehicleTLG.isAvailableForTechLevel(TechLevel.C))
    }
    
    @Test 
    fun testDefaultVehicleCatalogCoverage() {
        // Test that our default vehicle catalog has the right distribution
        val defaultVehicles = listOf(
            Vehicle("Honey Badger 4 ton Off-Roader", 4.0f, 52436, TechLevel.A),
            Vehicle("Hawk ATV", 8.0f, 95000, TechLevel.A),
            Vehicle("HMULV", 8.0f, 225000, TechLevel.C),
            Vehicle("Buggy", 0.5f, 6000, null),
            Vehicle("Speeder", 1.0f, 18000, TechLevel.B),
            Vehicle("EULV", 1.5f, 32500, TechLevel.C),
            Vehicle("Hover Tank", 20.0f, 7500000, TechLevel.C),
            Vehicle("G-Carrier", 8.0f, 271000, TechLevel.B),
            Vehicle("Air/Raft", 4.0f, 275000, TechLevel.C),
            Vehicle("ATV", 12.0f, 50000, null),
            Vehicle("AFV", 10.0f, 180000, TechLevel.A),
            Vehicle("Grav APC", 8.0f, 350000, TechLevel.C),
            Vehicle("Grav Belt", 0.0f, 100000, TechLevel.C),
            Vehicle("Bike", 0.5f, 1500, null),
            Vehicle("G-Bike", 1.0f, 19500, TechLevel.C),
            Vehicle("Ship's Boat", 30.0f, 8165000, TechLevel.A),
            Vehicle("Pinnace", 40.0f, 20931000, TechLevel.A)
        )
        
        // Count available vehicles for TL F ship
        val availableForTLF = defaultVehicles.filter { it.isAvailableForTechLevel(TechLevel.F) }
        val availableForTLC = defaultVehicles.filter { it.isAvailableForTechLevel(TechLevel.C) }
        val availableForTLA = defaultVehicles.filter { it.isAvailableForTechLevel(TechLevel.A) }
        
        assertTrue("TL F ship should have many vehicles available", availableForTLF.size >= 13)
        assertTrue("TL C ship should have some vehicles available", availableForTLC.size >= 10)
        assertTrue("TL A ship should have some vehicles available", availableForTLA.size >= 3)
        
        // Vehicles with no TL should be available to all
        val noTLVehicles = defaultVehicles.filter { it.minimumTechLevel == null }
        assertTrue("Should have vehicles with no TL requirement", noTLVehicles.isNotEmpty())
        
        for (vehicle in noTLVehicles) {
            assertTrue("No TL vehicle should be available to TL A", vehicle.isAvailableForTechLevel(TechLevel.A))
            assertTrue("No TL vehicle should be available to TL F", vehicle.isAvailableForTechLevel(TechLevel.F))
            assertTrue("No TL vehicle should be available to TL J", vehicle.isAvailableForTechLevel(TechLevel.J))
        }
    }
}