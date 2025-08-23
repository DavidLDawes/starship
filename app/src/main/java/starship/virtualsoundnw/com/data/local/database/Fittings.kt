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
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.coroutines.flow.Flow

/**
 * Sensor types with their tonnage and cost specifications
 */
enum class SensorType(val tons: Float, val cost: Float) {
    STANDARD(0f, 0f),
    BASIC_CIVILIAN(1f, 0.05f),
    BASIC_MILITARY(2f, 1f),
    ADVANCED(3f, 2f),
    VERY_ADVANCED(5f, 4f)
}

/**
 * Computer models with their specifications
 */
enum class ComputerModel(
    val model: String,
    val sizeRangeMin: Int, // Ship size minimum (0 means no size restriction)
    val sizeRangeMax: Int, // Ship size maximum (Int.MAX_VALUE means no upper limit)
    val jumpMinimum: Int,  // Minimum jump performance this computer supports
    val requiredTechLevel: TechLevel,
    val rating: Int,
    val cost: Float
) {
    CORE_1("Core/1", 100, 999, 1, TechLevel.A, 40, 12f),
    CORE_2("Core/2", 1000, 2999, 1, TechLevel.A, 40, 12f),
    CORE_3("Core/3", 3000, 5000, 2, TechLevel.A, 40, 12f),
    CORE_4("Core/4", 5001, 10000, 2, TechLevel.A, 50, 20f),
    CORE_5("Core/5", 10001, 50000, 3, TechLevel.B, 60, 30f),
    CORE_6("Core/6", 50001, 100000, 4, TechLevel.C, 70, 50f),
    CORE_7("Core/7", 100001, Int.MAX_VALUE, 5, TechLevel.D, 80, 70f),
    CORE_8("Core/8", 100001, Int.MAX_VALUE, 6, TechLevel.E, 90, 100f),
    CORE_9("Core/9", 100001, Int.MAX_VALUE, 6, TechLevel.F, 100, 130f),
    CORE_10("Core/10", 0, Int.MAX_VALUE, 7, TechLevel.G, 120, 160f),
    CORE_11("Core/11", 0, Int.MAX_VALUE, 8, TechLevel.G, 150, 200f),
    CORE_12("Core/12", 0, Int.MAX_VALUE, 9, TechLevel.G, 180, 240f),
    CORE_13("Core/13", 0, Int.MAX_VALUE, 10, TechLevel.H, 220, 300f),
    CORE_14("Core/14", 0, Int.MAX_VALUE, 11, TechLevel.H, 260, 360f),
    CORE_15("Core/15", 0, Int.MAX_VALUE, 12, TechLevel.H, 300, 420f),
    CORE_16("Core/16", 0, Int.MAX_VALUE, 10, TechLevel.H, 350, 500f),
    CORE_17("Core/17", 0, Int.MAX_VALUE, 10, TechLevel.J, 400, 580f),
    CORE_18("Core/18", 0, Int.MAX_VALUE, 10, TechLevel.J, 450, 670f),
    CORE_19("Core/19", 0, Int.MAX_VALUE, 10, TechLevel.J, 500, 760f),
    CORE_20("Core/20", 0, Int.MAX_VALUE, 10, TechLevel.J, 600, 860f);
    
    companion object {
        /**
         * Get available computer models for a ship based on size, jump performance, and tech level
         */
        fun getAvailableModelsForShip(
            shipTonnage: Int,
            maxJumpPerformance: Int,
            techLevel: TechLevel
        ): List<ComputerModel> {
            // Find minimum computer for tonnage
            val minForTonnage = values()
                .filter { techLevel >= it.requiredTechLevel }
                .filter { shipTonnage >= it.sizeRangeMin && 
                         (it.sizeRangeMax == Int.MAX_VALUE || shipTonnage <= it.sizeRangeMax) }
                .minByOrNull { it.ordinal }
            
            // Find minimum computer for jump performance
            val minForJump = values()
                .filter { techLevel >= it.requiredTechLevel }
                .filter { it.jumpMinimum >= maxJumpPerformance }
                .minByOrNull { it.ordinal } // Use ordinal to get the earliest/lowest computer that can handle the jump
            
            // Use whichever requirement is higher (by ordinal - later in enum = higher)
            val minimumRequired = listOfNotNull(minForTonnage, minForJump)
                .maxByOrNull { it.ordinal }
            
            return if (minimumRequired != null) {
                // Return minimum required computer and all higher computers
                values().filter { model ->
                    techLevel >= model.requiredTechLevel &&
                    model.ordinal >= minimumRequired.ordinal
                }
            } else {
                emptyList()
            }
        }
        
        /**
         * Get minimum required computer for ship size and jump performance
         */
        fun getMinimumRequiredModel(
            shipTonnage: Int,
            maxJumpPerformance: Int,
            techLevel: TechLevel
        ): ComputerModel? {
            val availableModels = getAvailableModelsForShip(shipTonnage, maxJumpPerformance, techLevel)
            return availableModels.minByOrNull { it.cost }
        }
    }
}

/**
 * Ship fittings entity - stores selected fittings for each ship
 */
@Entity(
    tableName = "fittings",
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
data class Fitting(
    val shipId: Int,
    val sensorType: SensorType = SensorType.STANDARD,
    val computerModel: ComputerModel = ComputerModel.CORE_1
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Calculate total fittings tonnage including bridge
     */
    fun getTotalTonnage(shipTonnage: Int): Float {
        val bridgeTonnage = calculateBridgeTonnage(shipTonnage)
        return sensorType.tons + bridgeTonnage
    }
    
    /**
     * Calculate total fittings cost including bridge
     */
    fun getTotalCost(shipTonnage: Int): Float {
        val bridgeCost = calculateBridgeCost(shipTonnage)
        return sensorType.cost + computerModel.cost + bridgeCost
    }
    
    /**
     * Get sensor tonnage
     */
    fun getSensorTonnage(): Float = sensorType.tons
    
    /**
     * Get sensor cost
     */
    fun getSensorCost(): Float = sensorType.cost
    
    /**
     * Get computer cost
     */
    fun getComputerCost(): Float = computerModel.cost
    
    /**
     * Get bridge tonnage based on ship size and capital ship status
     */
    fun getBridgeTonnage(shipTonnage: Int): Float = calculateBridgeTonnage(shipTonnage)
    
    /**
     * Get bridge cost (0.1 MCr per ton)
     */
    fun getBridgeCost(shipTonnage: Int): Float = calculateBridgeCost(shipTonnage)
}

@Dao
interface FittingDao {
    @Query("SELECT * FROM fittings WHERE shipId = :shipId")
    fun getFittingForShip(shipId: Int): Flow<Fitting?>
    
    @Insert
    suspend fun insertFitting(fitting: Fitting)
    
    @Delete
    suspend fun deleteFitting(fitting: Fitting)
    
    @Query("DELETE FROM fittings WHERE shipId = :shipId")
    suspend fun deleteFittingForShip(shipId: Int)
}

// Helper calculation functions
private fun calculateBridgeTonnage(shipTonnage: Int): Float {
    return if (shipTonnage > 2000) {
        // Capital ships use 0.5% of ship tonnage
        shipTonnage * 0.005f
    } else {
        // Non-capital ships use tiered system
        when {
            shipTonnage <= 200 -> 10f
            shipTonnage <= 1000 -> 20f  // 300-1000 tons
            shipTonnage <= 2000 -> 40f  // 1100-2000 tons
            else -> shipTonnage * 0.005f // Fallback to percentage (shouldn't happen)
        }
    }
}

private fun calculateBridgeCost(shipTonnage: Int): Float {
    val bridgeTonnage = calculateBridgeTonnage(shipTonnage)
    return bridgeTonnage * 0.1f // 0.1 MCr per ton
}

/**
 * Data class for fittings calculations and state
 */
data class FittingsCalculation(
    val ship: StarShip,
    val fitting: Fitting?,
    val availableComputers: List<ComputerModel>,
    val maxJumpPerformance: Int
) {
    val sensorTonnage: Float get() = fitting?.getSensorTonnage() ?: 0f
    val sensorCost: Float get() = fitting?.getSensorCost() ?: 0f
    val computerCost: Float get() = fitting?.getComputerCost() ?: 0f
    val bridgeTonnage: Float get() = calculateBridgeTonnage(ship.tons)
    val bridgeCost: Float get() = calculateBridgeCost(ship.tons)
    val totalFittingsTonnage: Float get() = sensorTonnage + bridgeTonnage
    val totalFittingsCost: Float get() = sensorCost + computerCost + bridgeCost
}