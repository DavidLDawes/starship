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

import starship.virtualsoundnw.com.data.local.database.Drone
import starship.virtualsoundnw.com.data.local.database.DroneDao
import starship.virtualsoundnw.com.data.local.database.TechLevel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing drone catalog data
 * Note: Predefined drones are populated by MIGRATION_11_12 during database upgrade
 */
@Singleton
class DronesDataService @Inject constructor(
    private val droneDao: DroneDao
) {
    
    /**
     * Ensure the drone catalog is populated with default drones from Issue #106
     */
    suspend fun ensureDroneCatalogPopulated() {
        // Check if drones already exist (they should be populated by migration)
        if (areDronesPopulated()) {
            return // Already populated
        }
        
        // If for some reason migration didn't work, populate manually
        val defaultDrones = getDefaultDrones()
        droneDao.insertDrones(defaultDrones)
        
        // Verify population was successful
        val verifyDrone = droneDao.getDroneByName("Centurion Security Robot")
        if (verifyDrone == null) {
            throw RuntimeException("Failed to populate drone catalog")
        }
    }
    
    /**
     * Check if drones are already populated
     */
    private suspend fun areDronesPopulated(): Boolean {
        return try {
            // Check if a known drone exists
            val testDrone = droneDao.getDroneByName("Centurion Security Robot")
            testDrone != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the default drone catalog from Issue #106
     */
    private fun getDefaultDrones(): List<Drone> {
        return listOf(
            Drone(
                name = "Centurion Security Robot",
                tons = 0.5f,
                costMCr = 0.12f,
                minimumTechLevel = TechLevel.C // TL C2 - using C as closest match
            ),
            Drone(
                name = "Robodog Assault Bot",
                tons = 0.5f,
                costMCr = 0.012f,
                minimumTechLevel = null // No tech level specified
            ),
            Drone(
                name = "Fury Helicopter Gunship",
                tons = 8.0f,
                costMCr = 1.2f,
                minimumTechLevel = null // No tech level specified
            ),
            Drone(
                name = "ATLAS Combat Droid",
                tons = 1.0f,
                costMCr = 0.024f,
                minimumTechLevel = null // No tech level specified
            )
        )
    }
    
    /**
     * Debug method to get all drones with their tech levels for troubleshooting
     */
    suspend fun getAllDronesForDebugging(): List<Drone> {
        return try {
            droneDao.getAllDronesSync()
        } catch (e: Exception) {
            emptyList()
        }
    }
}