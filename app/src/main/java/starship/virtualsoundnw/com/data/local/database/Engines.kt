/*
 * Copyright (C) 2022 The Android Open Source Project
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
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.coroutines.flow.Flow

/**
 * Engine types for starships
 */
enum class EngineType {
    POWER_PLANT, JUMP_DRIVE, MANEUVER_DRIVE
}

/**
 * Power plant types by tech level
 */
enum class PowerPlantType(val requiredTechLevel: TechLevel, val costPerTon: Float) {
    FISSION(TechLevel.A, 1.0f),
    FUSION(TechLevel.B, 2.0f),
    ADVANCED_FUSION(TechLevel.F, 5.0f),
    ANTIMATTER(TechLevel.H, 2.5f);
    
    companion object {
        fun getBestAvailableForTechLevel(techLevel: TechLevel): PowerPlantType {
            return when (techLevel) {
                TechLevel.A -> FISSION
                TechLevel.B, TechLevel.C, TechLevel.D, TechLevel.E -> FUSION
                TechLevel.F, TechLevel.G -> ADVANCED_FUSION
                TechLevel.H, TechLevel.J -> ANTIMATTER
            }
        }
    }
}

/**
 * Individual engine entity
 */
@Entity(
    tableName = "engines",
    foreignKeys = [
        ForeignKey(
            entity = StarShip::class,
            parentColumns = ["uid"],
            childColumns = ["shipId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shipId"])]
)
data class Engine(
    val shipId: Int,
    val type: EngineType,
    val performance: Int // 1-12 for Power/Jump, 0-12 for Maneuver (0 = no engine)
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Calculated tonnage as percentage of ship tonnage
     */
    fun getTonnagePercentage(): Float {
        return when (type) {
            EngineType.POWER_PLANT -> calculatePowerPlantTonnagePercentage(performance)
            EngineType.MANEUVER_DRIVE -> calculateManeuverDriveTonnagePercentage(performance)
            EngineType.JUMP_DRIVE -> calculateJumpDriveTonnagePercentage(performance)
        }
    }
    
    /**
     * Calculate actual tonnage for this engine given ship tonnage
     */
    fun getTonnage(shipTonnage: Int): Float {
        return shipTonnage * (getTonnagePercentage() / 100f)
    }
    
    /**
     * Calculate cost per ton for this engine
     */
    fun getCostPerTon(shipTechLevel: TechLevel): Float {
        return when (type) {
            EngineType.POWER_PLANT -> PowerPlantType.getBestAvailableForTechLevel(shipTechLevel).costPerTon
            EngineType.MANEUVER_DRIVE -> calculateManeuverDriveCostPerTon(performance)
            EngineType.JUMP_DRIVE -> calculateJumpDriveCostPerTon(performance)
        }
    }
    
    /**
     * Calculate total engine cost
     */
    fun getTotalCost(shipTonnage: Int, shipTechLevel: TechLevel): Float {
        return getTonnage(shipTonnage) * getCostPerTon(shipTechLevel)
    }
    
    /**
     * Get engine designation for capital ships (e.g., "P-5", "J-2", "M-3")
     */
    fun getDesignation(): String {
        val prefix = when (type) {
            EngineType.POWER_PLANT -> "P"
            EngineType.JUMP_DRIVE -> "J"
            EngineType.MANEUVER_DRIVE -> "M"
        }
        return "$prefix-$performance"
    }
}

@Dao
interface EngineDao {
    @Query("SELECT * FROM engines WHERE shipId = :shipId")
    fun getEnginesForShip(shipId: Int): Flow<List<Engine>>
    
    @Query("SELECT * FROM engines WHERE shipId = :shipId AND type = :engineType")
    fun getEnginesForShipByType(shipId: Int, engineType: EngineType): Flow<List<Engine>>
    
    @Insert
    suspend fun insertEngine(engine: Engine)
    
    @Query("DELETE FROM engines WHERE shipId = :shipId")
    suspend fun deleteEnginesForShip(shipId: Int)
    
    @Query("DELETE FROM engines WHERE shipId = :shipId AND type = :engineType")
    suspend fun deleteEnginesForShipByType(shipId: Int, engineType: EngineType)
}

// Helper calculation functions
private fun calculatePowerPlantTonnagePercentage(performance: Int): Float {
    return when (performance) {
        1 -> 1.5f
        2 -> 2.0f
        3 -> 2.5f
        4 -> 3.0f
        5 -> 4.0f
        6 -> 5.0f
        7 -> 6.0f
        8 -> 7.0f
        9 -> 8.0f
        10 -> 9.0f
        11 -> 10.0f
        12 -> 12.0f
        else -> throw IllegalArgumentException("Power plant performance must be 1-12")
    }
}

private fun calculateManeuverDriveTonnagePercentage(performance: Int): Float {
    return when (performance) {
        0 -> 0.0f
        1 -> 1.0f
        2 -> 1.25f
        3 -> 1.5f
        4 -> 1.75f
        5 -> 2.5f
        6 -> 3.25f
        7 -> 4.0f
        8 -> 4.75f
        9 -> 5.5f
        10 -> 6.25f
        11 -> 7.0f
        12 -> 8.0f
        else -> throw IllegalArgumentException("Maneuver drive performance must be 0-12")
    }
}

private fun calculateJumpDriveTonnagePercentage(performance: Int): Float {
    return when (performance) {
        1 -> 2.0f
        2 -> 3.0f
        3 -> 4.0f
        4 -> 5.0f
        5 -> 6.0f
        6 -> 7.0f
        7 -> 8.0f
        8 -> 9.0f
        9 -> 10.0f
        10 -> 11.0f
        11 -> 12.0f
        12 -> 13.0f
        else -> throw IllegalArgumentException("Jump drive performance must be 1-12")
    }
}

private fun calculateManeuverDriveCostPerTon(performance: Int): Float {
    return when (performance) {
        0 -> 0.0f
        1, 2, 3, 4, 5, 6 -> 0.5f
        7, 8 -> 0.6f
        9, 10 -> 0.7f
        11, 12 -> 0.8f
        else -> throw IllegalArgumentException("Maneuver drive performance must be 0-12")
    }
}

private fun calculateJumpDriveCostPerTon(performance: Int): Float {
    return when (performance) {
        1, 2, 3, 4, 5, 6 -> 2.0f
        7 -> 2.2f
        8 -> 2.3f
        9 -> 2.5f
        10 -> 2.6f
        11 -> 2.9f
        12 -> 3.0f
        else -> throw IllegalArgumentException("Jump drive performance must be 1-12")
    }
}

/**
 * Check if jump drive performance is valid for given tech level
 */
fun isJumpDrivePerformanceValidForTechLevel(performance: Int, techLevel: TechLevel): Boolean {
    val requiredTechLevel = when (performance) {
        1 -> TechLevel.A
        2 -> TechLevel.B
        3 -> TechLevel.C
        4 -> TechLevel.D
        5 -> TechLevel.E
        6 -> TechLevel.F
        7, 8 -> TechLevel.G
        9, 10 -> TechLevel.H
        11, 12 -> TechLevel.J
        else -> return false
    }
    
    return techLevel >= requiredTechLevel
}

/**
 * Calculate fuel requirements for a ship with engines
 */
fun calculateFuelRequirement(
    jumpPerformance: Int,
    shipTonnage: Int,
    hasAntimatterPowerPlant: Boolean
): Float {
    val baseFuelPercentage = (jumpPerformance * 10 + 2) / 100f // J * 10% + 2%
    val baseFuel = shipTonnage * baseFuelPercentage
    
    return if (hasAntimatterPowerPlant) {
        baseFuel / 10f // Antimatter uses 1/10 fuel
    } else {
        baseFuel
    }
}