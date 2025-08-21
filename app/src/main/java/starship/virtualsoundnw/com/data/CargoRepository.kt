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

import kotlinx.coroutines.flow.Flow
import starship.virtualsoundnw.com.data.local.database.Cargo
import starship.virtualsoundnw.com.data.local.database.CargoDao
import starship.virtualsoundnw.com.data.local.database.CargoType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CargoRepository @Inject constructor(
    private val cargoDao: CargoDao
) {
    
    fun getCargoForShip(shipId: Int): Flow<Cargo?> = cargoDao.getCargoForShip(shipId)
    
    suspend fun insertCargo(cargo: Cargo) = cargoDao.insertCargo(cargo)
    
    suspend fun deleteCargoForShip(shipId: Int) = cargoDao.deleteCargoForShip(shipId)
    
    /**
     * Update cargo tonnage for a specific cargo type, preserving existing values for other types
     */
    suspend fun updateCargoTonnage(
        shipId: Int,
        cargoType: CargoType,
        newTons: Int
    ) {
        // Get current cargo or create new one with defaults
        val currentCargo = cargoDao.getCargoForShipSync(shipId)
        
        val cargo = if (currentCargo != null) {
            // Update existing cargo, preserving current values for other types
            currentCargo.withUpdatedTonnage(cargoType, newTons).apply {
                // Preserve the existing uid for update
                uid = currentCargo.uid
            }
        } else {
            // Create new cargo with specified tonnage for the cargo type, defaulting others to 0
            Cargo(
                shipId = shipId,
                cargoTons = if (cargoType == CargoType.CARGO) newTons else 0,
                frozenCargoTons = if (cargoType == CargoType.FROZEN_CARGO) newTons else 0,
                sparesTons = if (cargoType == CargoType.SPARES) newTons else 0,
                secureCargoTons = if (cargoType == CargoType.SECURE_CARGO) newTons else 0
            )
        }
        
        if (currentCargo != null) {
            cargoDao.updateCargo(cargo)
        } else {
            cargoDao.insertCargo(cargo)
        }
    }
    
    /**
     * Update multiple cargo types at once, preserving existing values for unspecified types
     */
    suspend fun updateCargo(
        shipId: Int,
        cargoTons: Int? = null,
        frozenCargoTons: Int? = null,
        sparesTons: Int? = null,
        secureCargoTons: Int? = null
    ) {
        // Get current cargo or create new one with defaults
        val currentCargo = cargoDao.getCargoForShipSync(shipId)
        
        val cargo = if (currentCargo != null) {
            // Update existing cargo, preserving current values for unspecified fields
            Cargo(
                shipId = shipId,
                cargoTons = cargoTons ?: currentCargo.cargoTons,
                frozenCargoTons = frozenCargoTons ?: currentCargo.frozenCargoTons,
                sparesTons = sparesTons ?: currentCargo.sparesTons,
                secureCargoTons = secureCargoTons ?: currentCargo.secureCargoTons
            ).apply {
                // Preserve the existing uid for update
                uid = currentCargo.uid
            }
        } else {
            // Create new cargo with specified values, defaulting others to 0
            Cargo(
                shipId = shipId,
                cargoTons = cargoTons ?: 0,
                frozenCargoTons = frozenCargoTons ?: 0,
                sparesTons = sparesTons ?: 0,
                secureCargoTons = secureCargoTons ?: 0
            )
        }
        
        if (currentCargo != null) {
            cargoDao.updateCargo(cargo)
        } else {
            cargoDao.insertCargo(cargo)
        }
    }
}