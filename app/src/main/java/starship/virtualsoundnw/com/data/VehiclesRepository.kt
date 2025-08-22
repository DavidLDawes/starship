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
import starship.virtualsoundnw.com.data.local.database.Vehicle
import starship.virtualsoundnw.com.data.local.database.VehicleAllocation
import starship.virtualsoundnw.com.data.local.database.VehicleAllocationDao
import starship.virtualsoundnw.com.data.local.database.VehicleDao
import starship.virtualsoundnw.com.data.local.database.VehicleWithAllocation
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import starship.virtualsoundnw.com.data.local.database.TechLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehiclesRepository @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val vehicleAllocationDao: VehicleAllocationDao
) {
    
    /**
     * Get all available vehicles in catalog
     */
    fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    
    /**
     * Get vehicles available for specific tech level
     */
    fun getAvailableVehiclesForTechLevel(techLevel: TechLevel): Flow<List<Vehicle>> = 
        vehicleDao.getAvailableVehiclesForTechLevel(techLevel)
    
    /**
     * Get vehicles with allocations for a specific ship
     */
    fun getVehiclesWithAllocationsForShip(shipId: Int, techLevel: TechLevel): Flow<List<VehicleWithAllocation>> {
        return flow {
            val rawData = vehicleAllocationDao.getVehiclesWithAllocationsRaw(shipId, techLevel)
            val result = rawData.map { VehicleWithAllocation.fromRaw(it) }
            emit(result)
        }
    }
    
    /**
     * Get allocations for a specific ship
     */
    fun getAllocationsForShip(shipId: Int): Flow<List<VehicleAllocation>> =
        vehicleAllocationDao.getAllocationsForShip(shipId)
    
    /**
     * Add a vehicle to ship (increment quantity or create new allocation)
     */
    suspend fun addVehicleToShip(shipId: Int, vehicleId: Int, quantity: Int = 1) {
        val existingAllocation = vehicleAllocationDao.getAllocation(shipId, vehicleId)
        
        if (existingAllocation != null) {
            // Update existing allocation
            val updatedAllocation = existingAllocation.copy(
                quantity = existingAllocation.quantity + quantity
            ).apply { uid = existingAllocation.uid }
            vehicleAllocationDao.updateAllocation(updatedAllocation)
        } else {
            // Create new allocation
            vehicleAllocationDao.insertAllocation(
                VehicleAllocation(shipId = shipId, vehicleId = vehicleId, quantity = quantity)
            )
        }
    }
    
    /**
     * Remove a vehicle from ship (decrement quantity or remove allocation)
     */
    suspend fun removeVehicleFromShip(shipId: Int, vehicleId: Int, quantity: Int = 1) {
        val existingAllocation = vehicleAllocationDao.getAllocation(shipId, vehicleId)
        
        if (existingAllocation != null) {
            val newQuantity = existingAllocation.quantity - quantity
            if (newQuantity <= 0) {
                // Remove allocation entirely
                vehicleAllocationDao.deleteAllocation(existingAllocation)
            } else {
                // Update with reduced quantity
                val updatedAllocation = existingAllocation.copy(quantity = newQuantity)
                    .apply { uid = existingAllocation.uid }
                vehicleAllocationDao.updateAllocation(updatedAllocation)
            }
        }
    }
    
    /**
     * Set specific quantity for a vehicle on ship
     */
    suspend fun setVehicleQuantity(shipId: Int, vehicleId: Int, quantity: Int) {
        if (quantity <= 0) {
            vehicleAllocationDao.deleteAllocation(shipId, vehicleId)
        } else {
            val existingAllocation = vehicleAllocationDao.getAllocation(shipId, vehicleId)
            
            if (existingAllocation != null) {
                val updatedAllocation = existingAllocation.copy(quantity = quantity)
                    .apply { uid = existingAllocation.uid }
                vehicleAllocationDao.updateAllocation(updatedAllocation)
            } else {
                vehicleAllocationDao.insertAllocation(
                    VehicleAllocation(shipId = shipId, vehicleId = vehicleId, quantity = quantity)
                )
            }
        }
    }
    
    /**
     * Clear all vehicle allocations for a ship
     */
    suspend fun clearAllVehiclesForShip(shipId: Int) {
        vehicleAllocationDao.deleteAllocationsForShip(shipId)
    }
    
    /**
     * Insert vehicles into catalog (for initial setup)
     */
    suspend fun insertVehicles(vehicles: List<Vehicle>) {
        vehicleDao.insertVehicles(vehicles)
    }
    
    /**
     * Insert single vehicle into catalog
     */
    suspend fun insertVehicle(vehicle: Vehicle): Long {
        return vehicleDao.insertVehicle(vehicle)
    }
}