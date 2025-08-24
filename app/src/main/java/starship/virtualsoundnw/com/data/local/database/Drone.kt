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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.coroutines.flow.Flow

/**
 * Drone catalog entity - defines available drones with specifications
 */
@Entity(tableName = "drones")
data class Drone(
    val name: String,                    // e.g. "Centurion Security Robot"
    val tons: Float,                     // Tonnage (e.g. 0.5)
    val costMCr: Float,                 // Cost in MCr (e.g. 0.12)
    val minimumTechLevel: TechLevel?    // Minimum TL required (null if no requirement)
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Check if drone is available for given tech level
     */
    fun isAvailableForTechLevel(shipTechLevel: TechLevel): Boolean {
        return minimumTechLevel == null || shipTechLevel >= minimumTechLevel
    }
    
    /**
     * Format display name with tonnage
     */
    fun getDisplayName(): String = "$name ($tons tons)"
}

/**
 * Drone allocation entity - tracks quantities of drones on specific ships
 */
@Entity(
    tableName = "drone_allocations",
    foreignKeys = [
        ForeignKey(
            entity = StarShip::class,
            parentColumns = ["uid"],
            childColumns = ["shipId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Drone::class,
            parentColumns = ["uid"],
            childColumns = ["droneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["shipId"]),
        Index(value = ["droneId"]),
        Index(value = ["shipId", "droneId"], unique = true)
    ]
)
data class DroneAllocation(
    val shipId: Int,
    val droneId: Int,
    val quantity: Int = 1
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Calculate extended tonnage (quantity * drone tons)
     */
    fun getExtendedTonnage(drone: Drone): Float = quantity * drone.tons
    
    /**
     * Calculate extended cost (quantity * drone cost)
     */
    fun getExtendedCostMCr(drone: Drone): Float = quantity * drone.costMCr
}

/**
 * Raw data class for Room query results
 */
data class DroneWithAllocationRaw(
    val droneUid: Int,
    val droneName: String,
    val droneTons: Float,
    val droneCostMCr: Float,
    val droneMinimumTechLevel: TechLevel?,
    val allocationUid: Int?,
    val allocationShipId: Int?,
    val allocationDroneId: Int?,
    val allocationQuantity: Int?
)

/**
 * Combined data for UI display
 */
data class DroneWithAllocation(
    val drone: Drone,
    val allocation: DroneAllocation?
) {
    val quantity: Int get() = allocation?.quantity ?: 0
    val extendedTonnage: Float get() = allocation?.getExtendedTonnage(drone) ?: 0f
    val extendedCostMCr: Float get() = allocation?.getExtendedCostMCr(drone) ?: 0f
    val isAllocated: Boolean get() = allocation != null && allocation.quantity > 0
    
    companion object {
        fun fromRaw(raw: DroneWithAllocationRaw): DroneWithAllocation {
            val drone = Drone(
                name = raw.droneName,
                tons = raw.droneTons,
                costMCr = raw.droneCostMCr,
                minimumTechLevel = raw.droneMinimumTechLevel
            ).apply { uid = raw.droneUid }
            
            val allocation = if (raw.allocationUid != null && raw.allocationShipId != null && 
                                raw.allocationDroneId != null && raw.allocationQuantity != null) {
                DroneAllocation(
                    shipId = raw.allocationShipId,
                    droneId = raw.allocationDroneId,
                    quantity = raw.allocationQuantity
                ).apply { uid = raw.allocationUid }
            } else null
            
            return DroneWithAllocation(drone, allocation)
        }
    }
}

@Dao
interface DroneDao {
    @Query("""
        SELECT * FROM drones 
        ORDER BY name
    """)
    fun getAllDrones(): Flow<List<Drone>>
    
    @Query("""
        SELECT 
            d.uid as droneUid,
            d.name as droneName,
            d.tons as droneTons,
            d.costMCr as droneCostMCr,
            d.minimumTechLevel as droneMinimumTechLevel,
            da.uid as allocationUid,
            da.shipId as allocationShipId,
            da.droneId as allocationDroneId,
            da.quantity as allocationQuantity
        FROM drones d
        LEFT JOIN drone_allocations da ON d.uid = da.droneId AND da.shipId = :shipId
        ORDER BY d.name
    """)
    fun getDronesWithAllocationsForShip(shipId: Int): Flow<List<DroneWithAllocationRaw>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrone(drone: Drone): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrones(drones: List<Drone>)
    
    @Query("SELECT * FROM drones WHERE name = :name LIMIT 1")
    suspend fun getDroneByName(name: String): Drone?
    
    @Query("SELECT * FROM drones")
    suspend fun getAllDronesSync(): List<Drone>
    
    @Update
    suspend fun updateDrone(drone: Drone)
    
    @Delete
    suspend fun deleteDrone(drone: Drone)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDroneAllocation(allocation: DroneAllocation): Long
    
    @Update
    suspend fun updateDroneAllocation(allocation: DroneAllocation)
    
    @Delete
    suspend fun deleteDroneAllocation(allocation: DroneAllocation)
    
    @Query("DELETE FROM drone_allocations WHERE shipId = :shipId AND droneId = :droneId")
    suspend fun deleteDroneAllocationByIds(shipId: Int, droneId: Int)
    
    @Query("DELETE FROM drone_allocations WHERE shipId = :shipId")
    suspend fun deleteAllDroneAllocationsForShip(shipId: Int)
}