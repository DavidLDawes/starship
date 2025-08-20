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
 * Armor types with their specifications according to Issue #70
 */
enum class ArmorType(
    val displayName: String,
    val requiredTechLevel: TechLevel,
    val protectionPer5Percent: Int,  // Protection value per 5% of ship tons
    val costMultiplier: Float,       // Percentage of base hull cost for full armor
    val usesMaxTechLevel: Boolean    // Whether max armor is limited by tech level
) {
    CRYSTALIRON("Crystaliron", TechLevel.A, 4, 0.20f, false), // Max is TL or 13, whichever less
    BONDED_SUPERDENSE("Bonded Superdense", TechLevel.E, 6, 0.50f, true) // Max is TL
}

/**
 * Ship defenses entity - stores defensive systems for each ship
 */
@Entity(
    tableName = "defenses",
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
data class Defense(
    val shipId: Int,
    val armorProtection: Int = 0  // Selected protection level (0 to max)
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Get the armor type automatically based on tech level
     * TL D or less: Crystaliron, TL E or better: Bonded Superdense
     */
    fun getArmorType(techLevel: TechLevel): ArmorType {
        return if (techLevel >= TechLevel.E) {
            ArmorType.BONDED_SUPERDENSE
        } else {
            ArmorType.CRYSTALIRON
        }
    }
    
    /**
     * Calculate maximum armor protection based on tech level and automatically selected armor type
     */
    fun getMaxArmorProtection(techLevel: TechLevel): Int {
        val techLevelValue = techLevel.ordinal + 10  // TL A=10, B=11, C=12, etc.
        val armorType = getArmorType(techLevel)
        
        return when (armorType) {
            ArmorType.CRYSTALIRON -> minOf(techLevelValue, 13)
            ArmorType.BONDED_SUPERDENSE -> techLevelValue
        }
    }
    
    /**
     * Calculate armor tonnage based on ship tonnage and tech level
     */
    fun getArmorTonnage(shipTons: Int, techLevel: TechLevel): Float {
        if (armorProtection == 0) return 0f
        
        // Tonnage per protection point varies by tech level
        val tonnagePerProtection = when {
            techLevel == TechLevel.A || techLevel == TechLevel.B -> shipTons * 0.0125f  // 1.25%
            else -> shipTons * (5f/6f/100f)  // 0.833% (5/6 of 1%)
        }
        
        return armorProtection * tonnagePerProtection
    }
    
    /**
     * Calculate armor cost based on hull cost and selected protection level
     */
    fun getArmorCost(shipTons: Int, configuration: Configuration, techLevel: TechLevel): Float {
        if (armorProtection == 0) return 0f
        
        // Calculate base hull cost (same logic as in StarShip.kt)
        val baseCost = shipTons * 0.1f
        val multiplier = when (configuration) {
            Configuration.NEEDLE_WEDGE -> 1.2f
            Configuration.CONE -> 1.1f
            Configuration.STANDARD -> 1.0f
            Configuration.CLOSE_STRUCTURE -> 0.9f
            Configuration.SPHERE -> 0.8f
            Configuration.DISPERSED_STRUCTURE -> 0.5f
            Configuration.PLANETOID -> 0.004f
            Configuration.BUFFERED_PLANETOID -> 0.004f
        }
        val hullCost = baseCost * multiplier
        
        // Full armor cost is a percentage of hull cost based on automatically selected armor type
        val armorType = getArmorType(techLevel)
        val fullArmorCost = hullCost * armorType.costMultiplier
        
        // Scale by selected protection level vs maximum possible
        val maxProtection = getMaxArmorProtection(techLevel)
        val protectionRatio = if (maxProtection > 0) armorProtection.toFloat() / maxProtection else 0f
        
        return fullArmorCost * protectionRatio
    }
}

@Dao
interface DefenseDao {
    @Query("SELECT * FROM defenses WHERE shipId = :shipId")
    fun getDefenseForShip(shipId: Int): Flow<Defense?>
    
    @Insert
    suspend fun insertDefense(defense: Defense)
    
    @Delete
    suspend fun deleteDefense(defense: Defense)
    
    @Query("DELETE FROM defenses WHERE shipId = :shipId")
    suspend fun deleteDefenseForShip(shipId: Int)
}

/**
 * Data class for defenses calculations and state
 */
data class DefensesCalculation(
    val ship: StarShip,
    val defense: Defense?,
    val maxArmorProtection: Int
) {
    val armorTonnage: Float get() = defense?.getArmorTonnage(ship.tons, ship.techLevel) ?: 0f
    val armorCost: Float get() = defense?.getArmorCost(ship.tons, ship.configuration, ship.techLevel) ?: 0f
    val currentArmorProtection: Int get() = defense?.armorProtection ?: 0
    val selectedArmorType: ArmorType get() = defense?.getArmorType(ship.techLevel) ?: 
        if (ship.techLevel >= TechLevel.E) ArmorType.BONDED_SUPERDENSE else ArmorType.CRYSTALIRON
}