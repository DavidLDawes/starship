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
 * Cargo types with their cost specifications according to Issue #90
 */
enum class CargoType(
    val displayName: String,
    val baseCost: Float,        // Base cost in MCr (for types with fixed base costs)
    val costPerTon: Float       // Cost per ton in MCr
) {
    CARGO("Cargo", 0f, 0f),                         // Free
    SPARES("Spares", 0f, 0.1f),                    // 0.1 MCr/ton
    COLD_STORAGE("Cold Storage", 0.1f, 0.01f),       // 0.01 MCr/ton
    SECURED_CARGO("Secured Cargo", 1f, 0.2f),       // 1 MCr base + 0.2 MCr/ton
    XENO_CARGO("Xeno Cargo", 5f, 0.25f)            // 5 MCr base + 0.25 MCr/ton
}

/**
 * Ship cargo entity - stores cargo allocations for each ship
 */
@Entity(
    tableName = "cargo",
    foreignKeys = [
        ForeignKey(
            entity = StarShip::class,
            parentColumns = ["uid"],
            childColumns = ["shipId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shipId"], unique = true)]
)
data class Cargo(
    val shipId: Int,
    val cargoTons: Int = 0,         // Regular cargo tonnage (free)
    val sparesTons: Int = 0,        // Spares tonnage (0.05 MCr/ton) - allocated in 1% ship tonnage units
    val coldStorageTons: Int = 0,   // Cold storage tonnage (0.005 MCr/ton)
    val securedCargoTons: Int = 0,  // Secured cargo tonnage (1 MCr base + 0.1 MCr/ton)
    val xenoCargoTons: Int = 0      // Xeno cargo tonnage (5 MCr base + 0.25 MCr/ton)
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Calculate total tonnage used by all cargo types
     */
    fun getTotalTonnage(): Int {
        return cargoTons + sparesTons + coldStorageTons + securedCargoTons + xenoCargoTons
    }
    
    /**
     * Calculate cost for a specific cargo type
     */
    fun getCostForCargoType(cargoType: CargoType, tons: Int): Float {
        return when (cargoType) {
            CargoType.CARGO -> 0f  // Free
            CargoType.SPARES -> tons * cargoType.costPerTon
            CargoType.COLD_STORAGE ->  if (tons > 0) cargoType.baseCost + (tons * cargoType.costPerTon) else 0f
            CargoType.SECURED_CARGO -> if (tons > 0) cargoType.baseCost + (tons * cargoType.costPerTon) else 0f
            CargoType.XENO_CARGO -> if (tons > 0) cargoType.baseCost + (tons * cargoType.costPerTon) else 0f
        }
    }
    
    /**
     * Calculate total cost for all cargo types
     */
    fun getTotalCargoCost(): Float {
        val cargoCost = getCostForCargoType(CargoType.CARGO, cargoTons)
        val sparesCost = getCostForCargoType(CargoType.SPARES, sparesTons)
        val coldStorageCost = getCostForCargoType(CargoType.COLD_STORAGE, coldStorageTons)
        val securedCargoCost = getCostForCargoType(CargoType.SECURED_CARGO, securedCargoTons)
        val xenoCargoCost = getCostForCargoType(CargoType.XENO_CARGO, xenoCargoTons)
        
        return cargoCost + sparesCost + coldStorageCost + securedCargoCost + xenoCargoCost
    }
    
    /**
     * Get tonnage for a specific cargo type
     */
    fun getTonnageForCargoType(cargoType: CargoType): Int {
        return when (cargoType) {
            CargoType.CARGO -> cargoTons
            CargoType.SPARES -> sparesTons
            CargoType.COLD_STORAGE -> coldStorageTons
            CargoType.SECURED_CARGO -> securedCargoTons
            CargoType.XENO_CARGO -> xenoCargoTons
        }
    }
    
    /**
     * Create a new Cargo instance with updated tonnage for a specific cargo type
     */
    fun withUpdatedTonnage(cargoType: CargoType, newTons: Int): Cargo {
        return when (cargoType) {
            CargoType.CARGO -> copy(cargoTons = newTons)
            CargoType.SPARES -> copy(sparesTons = newTons)
            CargoType.COLD_STORAGE -> copy(coldStorageTons = newTons)
            CargoType.SECURED_CARGO -> copy(securedCargoTons = newTons)
            CargoType.XENO_CARGO -> copy(xenoCargoTons = newTons)
        }
    }
    
    /**
     * Calculate service interval in months based on spares tonnage
     * Service every 1 month + 1 month per 1% of ship tonnage allocated to Spares
     */
    fun getServiceIntervalMonths(shipTonnage: Int): Int {
        if (shipTonnage <= 0) return 1
        val sparesPercentage = (sparesTons.toFloat() / shipTonnage) * 100
        return 1 + sparesPercentage.toInt()
    }
}

@Dao
interface CargoDao {
    @Query("SELECT * FROM cargo WHERE shipId = :shipId")
    fun getCargoForShip(shipId: Int): Flow<Cargo?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCargo(cargo: Cargo)
    
    @Update
    suspend fun updateCargo(cargo: Cargo)
    
    @Delete
    suspend fun deleteCargo(cargo: Cargo)
    
    @Query("DELETE FROM cargo WHERE shipId = :shipId")
    suspend fun deleteCargoForShip(shipId: Int)
    
    @Query("SELECT * FROM cargo WHERE shipId = :shipId")
    suspend fun getCargoForShipSync(shipId: Int): Cargo?
}

/**
 * Data class for cargo calculations and state
 */
data class CargoCalculation(
    val ship: StarShip,
    val cargo: Cargo?
) {
    val totalTonnage: Int get() = cargo?.getTotalTonnage() ?: 0
    val totalCost: Float get() = cargo?.getTotalCargoCost() ?: 0f
    val cargoTons: Int get() = cargo?.cargoTons ?: 0
    val sparesTons: Int get() = cargo?.sparesTons ?: 0
    val coldStorageTons: Int get() = cargo?.coldStorageTons ?: 0
    val securedCargoTons: Int get() = cargo?.securedCargoTons ?: 0
    val xenoCargoTons: Int get() = cargo?.xenoCargoTons ?: 0
    val availableTonnage: Int get() = ship.tons - totalTonnage // Simplified - would need to account for other systems
    val serviceIntervalMonths: Int get() = cargo?.getServiceIntervalMonths(ship.tons) ?: 1
}