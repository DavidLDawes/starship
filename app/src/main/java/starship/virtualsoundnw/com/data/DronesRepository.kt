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
import kotlinx.coroutines.flow.map
import starship.virtualsoundnw.com.data.local.database.Drone
import starship.virtualsoundnw.com.data.local.database.DroneAllocation
import starship.virtualsoundnw.com.data.local.database.DroneDao
import starship.virtualsoundnw.com.data.local.database.DroneWithAllocation
import starship.virtualsoundnw.com.data.local.database.TechLevel
import javax.inject.Inject
import javax.inject.Singleton

interface DronesRepository {
    fun getAllDrones(): Flow<List<Drone>>
    fun getDronesWithAllocationsForShip(shipId: Int, shipTechLevel: TechLevel): Flow<List<DroneWithAllocation>>
    suspend fun addDroneToShip(shipId: Int, droneId: Int)
    suspend fun updateDroneQuantity(shipId: Int, droneId: Int, quantity: Int)
    suspend fun removeDroneFromShip(shipId: Int, droneId: Int)
}

@Singleton
class DronesRepositoryImpl @Inject constructor(
    private val droneDao: DroneDao
) : DronesRepository {
    
    override fun getAllDrones(): Flow<List<Drone>> {
        return droneDao.getAllDrones()
    }
    
    override fun getDronesWithAllocationsForShip(
        shipId: Int, 
        shipTechLevel: TechLevel
    ): Flow<List<DroneWithAllocation>> {
        return droneDao.getDronesWithAllocationsForShip(shipId).map { rawList ->
            rawList.map { raw ->
                DroneWithAllocation.fromRaw(raw)
            }.filter { droneWithAllocation ->
                // Filter by tech level availability
                droneWithAllocation.drone.isAvailableForTechLevel(shipTechLevel)
            }
        }
    }
    
    override suspend fun addDroneToShip(shipId: Int, droneId: Int) {
        val allocation = DroneAllocation(
            shipId = shipId,
            droneId = droneId,
            quantity = 1
        )
        droneDao.insertDroneAllocation(allocation)
    }
    
    override suspend fun updateDroneQuantity(shipId: Int, droneId: Int, quantity: Int) {
        if (quantity <= 0) {
            droneDao.deleteDroneAllocationByIds(shipId, droneId)
        } else {
            val allocation = DroneAllocation(
                shipId = shipId,
                droneId = droneId,
                quantity = quantity
            )
            droneDao.insertDroneAllocation(allocation)
        }
    }
    
    override suspend fun removeDroneFromShip(shipId: Int, droneId: Int) {
        droneDao.deleteDroneAllocationByIds(shipId, droneId)
    }
}