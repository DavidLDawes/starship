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

import starship.virtualsoundnw.com.data.local.database.Vehicle
import starship.virtualsoundnw.com.data.local.database.VehicleDao
import starship.virtualsoundnw.com.data.local.database.TechLevel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to ensure vehicle catalog is populated with standard vehicles
 */
@Singleton
class VehiclesDataService @Inject constructor(
    private val vehicleDao: VehicleDao
) {
    
    /**
     * Ensure the vehicle catalog is populated with default vehicles
     */
    suspend fun ensureVehicleCatalogPopulated() {
        // Check if vehicles already exist
        if (areVehiclesPopulated()) {
            return // Already populated
        }
        
        // Populate with default vehicle catalog
        val defaultVehicles = getDefaultVehicles()
        vehicleDao.insertVehicles(defaultVehicles)
        
        // Verify population was successful
        val verifyVehicle = vehicleDao.getVehicleByName("Buggy")
        if (verifyVehicle == null) {
            throw RuntimeException("Failed to populate vehicle catalog")
        }
    }
    
    /**
     * Check if vehicles are already populated
     */
    private suspend fun areVehiclesPopulated(): Boolean {
        return try {
            // Check if a known vehicle exists
            val testVehicle = vehicleDao.getVehicleByName("Buggy")
            testVehicle != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Debug method to get all vehicles with their tech levels for troubleshooting
     */
    suspend fun getAllVehiclesForDebugging(): List<Vehicle> {
        return try {
            vehicleDao.getAllVehiclesSync()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Debug method to get available vehicles for specific tech level
     */
    suspend fun getAvailableVehiclesForTechLevelSync(techLevel: TechLevel): List<Vehicle> {
        return try {
            vehicleDao.getAvailableVehiclesForTechLevelSync(techLevel)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Vehicle catalog from Issue #94
     */
    private fun getDefaultVehicles(): List<Vehicle> {
        return listOf(
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
    }
}