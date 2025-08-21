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
 * Screen types available for capital ships according to Issue #70
 */
enum class ScreenType(
    val displayName: String,
    val requiredTechLevel: TechLevel
) {
    NUCLEAR_DAMPER("Nuclear Damper", TechLevel.C),
    MESON_SCREEN("Meson Screen", TechLevel.C),
    BLACK_GLOBE("Black Globe", TechLevel.F)
}

/**
 * Hull code categories for screen cost calculations
 */
enum class HullCodeCategory(
    val range: String,
    val nuclearDamperTons: Int,
    val nuclearDamperCost: Int,
    val mesonScreenTons: Int,
    val mesonScreenCost: Int,
    val blackGlobeTons: Int,
    val blackGlobeCost: Int
) {
    CA_TO_CE("CA-CE", 20, 30, 50, 70, 10, 100),
    CF_TO_CK("CF-CK", 30, 40, 60, 80, 15, 150),
    CL_TO_CQ("CL-CQ", 40, 50, 70, 90, 20, 200),
    CR_TO_CV("CR-CV", 50, 60, 80, 100, 25, 250),
    CW_TO_CZ("CW-CZ", 60, 70, 90, 110, 30, 300)
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
    val armorProtection: Int = 0,  // Selected protection level (0 to max)
    val nuclearDampers: Int = 0,   // Number of Nuclear Dampers (0 to max for TL)
    val mesonScreens: Int = 0,     // Number of Meson Screens (0 to max for TL)
    val blackGlobes: Int = 0       // Number of Black Globes (0 to max for TL)
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
    
    /**
     * Get maximum screen quantities based on tech level
     */
    fun getMaxScreenQuantities(techLevel: TechLevel): Triple<Int, Int, Int> {
        return when (techLevel) {
            TechLevel.A, TechLevel.B -> Triple(0, 0, 0)  // No screens available
            TechLevel.C -> Triple(1, 1, 0)   // Nuclear Damper: 1, Meson Screen: 1, Black Globe: 0
            TechLevel.D -> Triple(2, 2, 0)   // Nuclear Damper: 2, Meson Screen: 2, Black Globe: 0
            TechLevel.E -> Triple(4, 4, 0)   // Nuclear Damper: 4, Meson Screen: 4, Black Globe: 0
            TechLevel.F -> Triple(6, 6, 3)   // Nuclear Damper: 6, Meson Screen: 6, Black Globe: 3
            TechLevel.G -> Triple(9, 8, 4)   // Nuclear Damper: 9, Meson Screen: 8, Black Globe: 4
            TechLevel.H -> Triple(11, 10, 5) // Nuclear Damper: 11, Meson Screen: 10, Black Globe: 5
            TechLevel.J -> Triple(14, 12, 6) // Nuclear Damper: 14, Meson Screen: 12, Black Globe: 6
        }
    }
    
    /**
     * Get hull code category for cost calculations
     */
    fun getHullCodeCategory(hullCode: String): HullCodeCategory {
        return when (hullCode) {
            "CA", "CB", "CC", "CD", "CE" -> HullCodeCategory.CA_TO_CE
            "CF", "CG", "CH", "CJ", "CK" -> HullCodeCategory.CF_TO_CK
            "CL", "CM", "CN", "CP", "CQ" -> HullCodeCategory.CL_TO_CQ
            "CR", "CS", "CT", "CU", "CV" -> HullCodeCategory.CR_TO_CV
            "CW", "CX", "CY", "CZ" -> HullCodeCategory.CW_TO_CZ
            else -> HullCodeCategory.CA_TO_CE // Default for non-capital ships or unknown codes
        }
    }
    
    /**
     * Calculate total screen tonnage for all screens
     */
    fun getScreenTonnage(hullCode: String): Float {
        val category = getHullCodeCategory(hullCode)
        val nuclearDamperTons = nuclearDampers * category.nuclearDamperTons
        val mesonScreenTons = mesonScreens * category.mesonScreenTons
        val blackGlobeTons = blackGlobes * category.blackGlobeTons
        return (nuclearDamperTons + mesonScreenTons + blackGlobeTons).toFloat()
    }
    
    /**
     * Calculate total screen cost for all screens
     */
    fun getScreenCost(hullCode: String): Float {
        val category = getHullCodeCategory(hullCode)
        val nuclearDamperCost = nuclearDampers * category.nuclearDamperCost
        val mesonScreenCost = mesonScreens * category.mesonScreenCost
        val blackGlobeCost = blackGlobes * category.blackGlobeCost
        return (nuclearDamperCost + mesonScreenCost + blackGlobeCost).toFloat()
    }
    
    /**
     * Check if ship is a capital ship (required for screens)
     */
    fun isCapitalShip(shipTons: Int): Boolean = shipTons > 2000
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
    val maxArmorProtection: Int,
    val maxScreenQuantities: Triple<Int, Int, Int>  // Nuclear Damper, Meson Screen, Black Globe
) {
    val armorTonnage: Float get() = defense?.getArmorTonnage(ship.tons, ship.techLevel) ?: 0f
    val armorCost: Float get() = defense?.getArmorCost(ship.tons, ship.configuration, ship.techLevel) ?: 0f
    val screenTonnage: Float get() = defense?.getScreenTonnage(ship.hullCode) ?: 0f
    val screenCost: Float get() = defense?.getScreenCost(ship.hullCode) ?: 0f
    val currentArmorProtection: Int get() = defense?.armorProtection ?: 0
    val currentNuclearDampers: Int get() = defense?.nuclearDampers ?: 0
    val currentMesonScreens: Int get() = defense?.mesonScreens ?: 0
    val currentBlackGlobes: Int get() = defense?.blackGlobes ?: 0
    val selectedArmorType: ArmorType get() = defense?.getArmorType(ship.techLevel) ?: 
        if (ship.techLevel >= TechLevel.E) ArmorType.BONDED_SUPERDENSE else ArmorType.CRYSTALIRON
    val isCapitalShip: Boolean get() = ship.tons > 2000
}