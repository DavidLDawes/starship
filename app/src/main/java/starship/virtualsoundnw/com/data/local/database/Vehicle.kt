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
 * Vehicle catalog entity - defines available vehicles with specifications
 */
@Entity(tableName = "vehicles")
data class Vehicle(
    val name: String,                    // e.g. "Honey Badger 4 ton Off-Roader"
    val tons: Float,                     // Tonnage (e.g. 4.0)
    val costCredits: Long,              // Cost in Credits (e.g. 52436)
    val minimumTechLevel: TechLevel?    // Minimum TL required (null if no requirement)
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Get cost in MCr for display
     */
    fun getCostMCr(): Float = costCredits / 1_000_000f
    
    /**
     * Check if vehicle is available for given tech level
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
 * Vehicle allocation entity - tracks quantities of vehicles on specific ships
 */
@Entity(
    tableName = "vehicle_allocations",
    foreignKeys = [
        ForeignKey(
            entity = StarShip::class,
            parentColumns = ["uid"],
            childColumns = ["shipId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["uid"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["shipId"]),
        Index(value = ["vehicleId"]),
        Index(value = ["shipId", "vehicleId"], unique = true)
    ]
)
data class VehicleAllocation(
    val shipId: Int,
    val vehicleId: Int,
    val quantity: Int = 1
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Calculate extended tonnage (quantity * vehicle tons)
     */
    fun getExtendedTonnage(vehicle: Vehicle): Float = quantity * vehicle.tons
    
    /**
     * Calculate extended cost (quantity * vehicle cost)
     */
    fun getExtendedCostCredits(vehicle: Vehicle): Long = quantity * vehicle.costCredits
    
    /**
     * Calculate extended cost in MCr
     */
    fun getExtendedCostMCr(vehicle: Vehicle): Float = getExtendedCostCredits(vehicle) / 1_000_000f
}

/**
 * Raw data class for Room query results
 */
data class VehicleWithAllocationRaw(
    val vehicleUid: Int,
    val vehicleName: String,
    val vehicleTons: Float,
    val vehicleCostCredits: Long,
    val vehicleMinimumTechLevel: TechLevel?,
    val allocationUid: Int?,
    val allocationShipId: Int?,
    val allocationVehicleId: Int?,
    val allocationQuantity: Int?
)

/**
 * Combined data for UI display
 */
data class VehicleWithAllocation(
    val vehicle: Vehicle,
    val allocation: VehicleAllocation?
) {
    val quantity: Int get() = allocation?.quantity ?: 0
    val extendedTonnage: Float get() = allocation?.getExtendedTonnage(vehicle) ?: 0f
    val extendedCostMCr: Float get() = allocation?.getExtendedCostMCr(vehicle) ?: 0f
    val isAllocated: Boolean get() = allocation != null && allocation.quantity > 0
    
    companion object {
        fun fromRaw(raw: VehicleWithAllocationRaw): VehicleWithAllocation {
            val vehicle = Vehicle(
                name = raw.vehicleName,
                tons = raw.vehicleTons,
                costCredits = raw.vehicleCostCredits,
                minimumTechLevel = raw.vehicleMinimumTechLevel
            ).apply { uid = raw.vehicleUid }
            
            val allocation = if (raw.allocationUid != null && raw.allocationShipId != null && 
                                raw.allocationVehicleId != null && raw.allocationQuantity != null) {
                VehicleAllocation(
                    shipId = raw.allocationShipId,
                    vehicleId = raw.allocationVehicleId,
                    quantity = raw.allocationQuantity
                ).apply { uid = raw.allocationUid }
            } else {
                null
            }
            
            return VehicleWithAllocation(vehicle, allocation)
        }
    }
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY name")
    fun getAllVehicles(): Flow<List<Vehicle>>
    
    @Query("SELECT * FROM vehicles WHERE minimumTechLevel IS NULL OR minimumTechLevel <= :techLevel ORDER BY name")
    fun getAvailableVehiclesForTechLevel(techLevel: TechLevel): Flow<List<Vehicle>>
    
    @Query("SELECT * FROM vehicles WHERE name = :name LIMIT 1")
    suspend fun getVehicleByName(name: String): Vehicle?
    
    @Query("SELECT * FROM vehicles ORDER BY name")
    suspend fun getAllVehiclesSync(): List<Vehicle>
    
    @Query("SELECT * FROM vehicles WHERE minimumTechLevel IS NULL OR minimumTechLevel <= :techLevel ORDER BY name")
    suspend fun getAvailableVehiclesForTechLevelSync(techLevel: TechLevel): List<Vehicle>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)
    
    @Update
    suspend fun updateVehicle(vehicle: Vehicle)
    
    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)
}

@Dao
interface VehicleAllocationDao {
    @Query("SELECT * FROM vehicle_allocations WHERE shipId = :shipId")
    fun getAllocationsForShip(shipId: Int): Flow<List<VehicleAllocation>>
    
    @Query("SELECT * FROM vehicle_allocations WHERE shipId = :shipId AND vehicleId = :vehicleId")
    suspend fun getAllocation(shipId: Int, vehicleId: Int): VehicleAllocation?
    
    @Query("""
        SELECT v.uid as vehicleUid, v.name as vehicleName, v.tons as vehicleTons, 
               v.costCredits as vehicleCostCredits, v.minimumTechLevel as vehicleMinimumTechLevel,
               va.uid as allocationUid, va.shipId as allocationShipId, va.vehicleId as allocationVehicleId, 
               va.quantity as allocationQuantity
        FROM vehicles v 
        LEFT JOIN vehicle_allocations va ON v.uid = va.vehicleId AND va.shipId = :shipId 
        WHERE v.minimumTechLevel IS NULL OR v.minimumTechLevel <= :techLevel
        ORDER BY v.name
    """)
    suspend fun getVehiclesWithAllocationsRaw(shipId: Int, techLevel: TechLevel): List<VehicleWithAllocationRaw>
    
    @Query("SELECT * FROM vehicles WHERE minimumTechLevel IS NULL OR minimumTechLevel <= :techLevel ORDER BY name")
    suspend fun getAvailableVehiclesSync(techLevel: TechLevel): List<Vehicle>
    
    @Query("SELECT * FROM vehicle_allocations WHERE shipId = :shipId")
    suspend fun getAllocationsForShipSync(shipId: Int): List<VehicleAllocation>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: VehicleAllocation)
    
    @Update
    suspend fun updateAllocation(allocation: VehicleAllocation)
    
    @Delete
    suspend fun deleteAllocation(allocation: VehicleAllocation)
    
    @Query("DELETE FROM vehicle_allocations WHERE shipId = :shipId")
    suspend fun deleteAllocationsForShip(shipId: Int)
    
    @Query("DELETE FROM vehicle_allocations WHERE shipId = :shipId AND vehicleId = :vehicleId")
    suspend fun deleteAllocation(shipId: Int, vehicleId: Int)
}

/**
 * Data class for vehicle calculations and state
 */
data class VehicleCalculation(
    val ship: StarShip,
    val vehiclesWithAllocations: List<VehicleWithAllocation>
) {
    val totalVehicleTonnage: Float get() = vehiclesWithAllocations.sumOf { it.extendedTonnage.toDouble() }.toFloat()
    val totalVehicleCostMCr: Float get() = vehiclesWithAllocations.sumOf { it.extendedCostMCr.toDouble() }.toFloat()
    val allocatedVehicles: List<VehicleWithAllocation> get() = vehiclesWithAllocations.filter { it.isAllocated }
    val totalVehicleCount: Int get() = allocatedVehicles.sumOf { it.quantity }
}