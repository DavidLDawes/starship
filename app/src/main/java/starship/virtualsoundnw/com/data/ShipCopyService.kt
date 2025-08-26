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

import kotlinx.coroutines.flow.first
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.StarShipDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for copying complete ship designs with all systems
 */
@Singleton
class ShipCopyService @Inject constructor(
    private val starShipDao: StarShipDao,
    private val enginesRepository: EnginesRepository,
    private val weaponsRepository: WeaponsRepository,
    private val defensesRepository: DefensesRepository,
    private val fittingsRepository: FittingsRepository,
    private val cargoRepository: CargoRepository,
    private val vehiclesRepository: VehiclesRepository,
    private val dronesRepository: DronesRepository,
    private val berthsRepository: BerthsRepository
) {

    /**
     * Copy a complete ship design with all systems to a new name
     * @param originalShipId The ID of the ship to copy
     * @param newName The name for the new ship
     * @return Result containing the new ship or error
     */
    suspend fun copyShipDesign(originalShipId: Int, newName: String): Result<StarShip> {
        return try {
            // Get the original ship
            val originalShips = starShipDao.getStarShips().first()
            val originalShip = originalShips.find { it.uid == originalShipId }
                ?: return Result.failure(IllegalArgumentException("Ship with ID $originalShipId not found"))

            // Check if name already exists
            val existingNames = originalShips.map { it.name.lowercase() }
            if (existingNames.contains(newName.lowercase())) {
                return Result.failure(IllegalArgumentException("A ship with the name '$newName' already exists"))
            }

            // Create the new ship
            val newShip = originalShip.copy(name = newName)
            newShip.uid = 0 // Reset ID for new insert
            starShipDao.insertStarShip(newShip)

            // Get the newly inserted ship to get its ID
            val newShips = starShipDao.getStarShips().first()
            val insertedShip = newShips.find { it.name == newName }
                ?: return Result.failure(RuntimeException("Failed to retrieve newly created ship"))

            val newShipId = insertedShip.uid

            // Copy all engines
            val originalEngines = enginesRepository.getEnginesForShip(originalShipId).first()
            originalEngines.forEach { engine ->
                val newEngine = engine.copy(shipId = newShipId)
                newEngine.uid = 0 // Reset ID for new insert
                enginesRepository.addEngine(newEngine)
            }

            // Copy all weapons
            val originalWeapons = weaponsRepository.getWeaponsForShip(originalShipId).first()
            originalWeapons.forEach { weapon ->
                val newWeapon = weapon.copy(shipId = newShipId)
                newWeapon.uid = 0 // Reset ID for new insert
                weaponsRepository.addWeapon(newWeapon)
            }

            // Copy defenses (single entity)
            val originalDefense = defensesRepository.getDefenseForShip(originalShipId).first()
            originalDefense?.let { defense ->
                val newDefense = defense.copy(shipId = newShipId)
                newDefense.uid = 0 // Reset ID for new insert
                defensesRepository.insertDefense(newDefense)
            }

            // Copy fitting (single entity)
            val originalFitting = fittingsRepository.getFittingForShip(originalShipId).first()
            originalFitting?.let { fitting ->
                val newFitting = fitting.copy(shipId = newShipId)
                newFitting.uid = 0 // Reset ID for new insert
                fittingsRepository.updateFitting(newFitting)
            }

            // Copy cargo (single entity)
            val originalCargo = cargoRepository.getCargoForShip(originalShipId).first()
            originalCargo?.let { cargo ->
                val newCargo = cargo.copy(shipId = newShipId)
                newCargo.uid = 0 // Reset ID for new insert
                cargoRepository.insertCargo(newCargo)
            }

            // Copy vehicles
            val originalVehicles = vehiclesRepository.getAllocationsForShip(originalShipId).first()
            originalVehicles.forEach { vehicle ->
                val newVehicle = vehicle.copy(shipId = newShipId)
                newVehicle.uid = 0 // Reset ID for new insert
                vehiclesRepository.addVehicleToShip(newShipId, newVehicle.vehicleId, newVehicle.quantity)
            }

            // Copy drones - need to get drone allocations differently
            val originalDroneAllocations = dronesRepository.getDronesWithAllocationsForShip(originalShipId, originalShip.techLevel).first()
            originalDroneAllocations.filter { it.allocation != null }.forEach { droneWithAllocation ->
                val allocation = droneWithAllocation.allocation!!
                dronesRepository.updateDroneQuantity(newShipId, allocation.droneId, allocation.quantity)
            }

            // Copy berths (single entity)
            val originalBerths = berthsRepository.getBerthsForShip(originalShipId).first()
            originalBerths?.let { berths ->
                val newBerths = berths.copy(shipId = newShipId)
                newBerths.uid = 0 // Reset ID for new insert
                berthsRepository.insertBerths(newBerths)
            }

            Result.success(insertedShip)
        } catch (e: Exception) {
            Result.failure(RuntimeException("Failed to copy ship design: ${e.message}", e))
        }
    }
}