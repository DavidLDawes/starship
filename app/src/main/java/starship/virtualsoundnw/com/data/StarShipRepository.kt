/*
 * Copyright (C) 2022 The Android Open Source Project, 2025 David L. Dawes
 * Notice: As this license requires, be aware this file has been changed by David L. Dawes since cloning it from github.
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.CrewManifest
import starship.virtualsoundnw.com.data.local.database.StarShipDao
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration
import javax.inject.Inject

/**
 * Ship summary data including tonnage and cost calculations from all systems
 */
data class ShipSummary(
    val ship: StarShip,
    val enginesTonnage: Double = 0.0,
    val enginesCost: Double = 0.0,
    val fuelTonnage: Double = 0.0,
    val weaponsTonnage: Double = 0.0,
    val weaponsCost: Double = 0.0,
    val defensesTonnage: Double = 0.0,
    val defensesCost: Double = 0.0,
    val fittingsTonnage: Double = 0.0,
    val fittingsCost: Double = 0.0,
    val cargoTonnage: Int = 0,
    val cargoCost: Double = 0.0,
    val vehiclesTonnage: Double = 0.0,
    val vehiclesCost: Double = 0.0,
    val dronesTonnage: Double = 0.0,
    val dronesCost: Double = 0.0,
    val berthsTonnage: Double = 0.0,
    val berthsCost: Double = 0.0,
    val crewManifest: CrewManifest? = null
) {
    val totalUsedTonnage: Double get() = enginesTonnage + fuelTonnage + weaponsTonnage + defensesTonnage + fittingsTonnage + cargoTonnage + vehiclesTonnage + dronesTonnage + berthsTonnage
    val remainingTonnage: Double get() = ship.tons - totalUsedTonnage
    val totalSystemsCost: Double get() = enginesCost + weaponsCost + defensesCost + fittingsCost + cargoCost + vehiclesCost + dronesCost + berthsCost
    val totalShipCost: Double get() = ship.hullCost + totalSystemsCost
}

interface StarShipRepository {
    val starShips: Flow<List<StarShip>>

    suspend fun add(starShip: StarShip)
    
    suspend fun delete(starShip: StarShip)
    
    fun getShipSummary(shipId: Int): Flow<ShipSummary?>
}

class DefaultStarShipRepository @Inject constructor(
    private val starShipDao: StarShipDao
) : StarShipRepository {

    override val starShips: Flow<List<StarShip>> =
        starShipDao.getStarShips()

    override suspend fun add(starShip: StarShip) {
        starShipDao.insertStarShip(starShip)
    }
    
    override suspend fun delete(starShip: StarShip) {
        starShipDao.deleteStarShip(starShip)
    }
    
    override fun getShipSummary(shipId: Int): Flow<ShipSummary?> {
        return starShips.map { ships ->
            ships.find { it.uid == shipId }?.let { ship ->
                // For now, return a basic ship summary without system calculations
                // This will be enhanced later when all repositories are properly configured
                ShipSummary(ship = ship)
            }
        }
    }
}
