/*
 * Copyright (C) 2022 The Android Open Source Project, 2025 David L. Dawes
 * Notice: As this license requires, be aware this file has been changed by David L. Dawes since cloning it from github.
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
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

enum class TechLevel {
    A, B, C, D, E, F, G, H, J
}

enum class Configuration {
    NEEDLE_WEDGE, CONE, STANDARD, CLOSE_STRUCTURE,
    SPHERE, DISPERSED_STRUCTURE, PLANETOID, BUFFERED_PLANETOID
}

@Entity(
    indices = [Index(value = ["name"], unique = true)]
)
data class StarShip(
    val name: String,
    val description: String,
    val tons: Int,
    val techLevel: TechLevel,
    val configuration: Configuration
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    // Calculated properties
    val isCapitalShip: Boolean get() = tons > 2000
    
    val hullClass: String get() = calculateHullClass(tons)
    
    val hullCost: Float get() = calculateHullCost(tons, configuration)
    
    val hullCode: String get() = calculateHullCode(tons)
    
    val sections: Int get() = calculateSections(hullCode)
    
    val shipDesignation: String get() = if (isCapitalShip) "Capital Ship" else "Ship"
}

@Dao
interface StarShipDao {
    @Query("SELECT * FROM starship ORDER BY uid DESC LIMIT 10")
    fun getStarShips(): Flow<List<StarShip>>

    @Insert
    suspend fun insertStarShip(item: StarShip)
    
    @Delete
    suspend fun deleteStarShip(item: StarShip)
    
    @Query("SELECT COUNT(*) FROM starship WHERE name = :name COLLATE NOCASE")
    suspend fun countShipsWithName(name: String): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM starship WHERE name = :name COLLATE NOCASE)")
    suspend fun doesShipNameExist(name: String): Boolean
}

// Helper functions for ship calculations
private fun calculateHullClass(tons: Int): String {
    return when {
        // Non-capital ships (up to 2000 tons)
        tons <= 100 -> "1"
        tons <= 200 -> "2"
        tons <= 300 -> "3"
        tons <= 400 -> "4"
        tons <= 500 -> "5"
        tons <= 600 -> "6"
        tons <= 700 -> "7"
        tons <= 800 -> "8"
        tons <= 900 -> "9"
        tons <= 1000 -> "A"
        tons <= 1200 -> "C"
        tons <= 1400 -> "E"
        tons <= 1600 -> "G"
        tons <= 1800 -> "J"
        tons <= 2000 -> "L"
        
        // Capital ships (2001+ tons)
        tons <= 3000 -> "CA"
        tons <= 4000 -> "CB"
        tons <= 5000 -> "CC"
        tons <= 6000 -> "CD"
        tons <= 7500 -> "CE"
        tons <= 10000 -> "CF"
        tons <= 15000 -> "CG"
        tons <= 20000 -> "CH"
        tons <= 25000 -> "CJ"
        tons <= 30000 -> "CK"
        tons <= 40000 -> "CL"
        tons <= 50000 -> "CM"
        tons <= 60000 -> "CN"
        tons <= 75000 -> "CP"
        tons <= 100000 -> "CQ"
        tons <= 200000 -> "CR"
        tons <= 300000 -> "CS"
        tons <= 400000 -> "CT"
        tons <= 500000 -> "CU"
        tons <= 600000 -> "CV"
        tons <= 700000 -> "CW"
        tons <= 800000 -> "CX"
        tons <= 900000 -> "CY"
        else -> "CZ" // 1,000,000+ tons
    }
}

private fun calculateHullCost(tons: Int, configuration: Configuration): Float {
    // Base cost is 0.1 MCr per ton
    val baseCost = tons * 0.1f
    
    // Apply configuration multipliers
    val multiplier = when (configuration) {
        Configuration.NEEDLE_WEDGE -> 1.2f      // +20%
        Configuration.CONE -> 1.1f              // +10%
        Configuration.STANDARD -> 1.0f          // No change
        Configuration.CLOSE_STRUCTURE -> 0.9f   // -10%
        Configuration.SPHERE -> 0.8f            // -20%
        Configuration.DISPERSED_STRUCTURE -> 0.5f // -50%
        Configuration.PLANETOID -> 0.004f       // -99.6%
        Configuration.BUFFERED_PLANETOID -> 0.004f // -99.6%
    }
    
    return baseCost * multiplier
}

/**
 * Calculate Hull Code based on ship tonnage according to Issue #68 specifications
 */
private fun calculateHullCode(tons: Int): String {
    return when (tons) {
        // Single character codes for ships 100-2000 tons
        100 -> "A"
        in 191..200 -> "A"
        in 201..300 -> "B"
        in 301..400 -> "C"
        in 401..500 -> "D"
        in 501..600 -> "E"
        in 601..700 -> "F"
        in 701..800 -> "G"
        in 801..900 -> "H"
        in 901..1000 -> "J"
        in 1001..1100 -> "K"
        in 1101..1200 -> "L"
        in 1201..1300 -> "M"
        in 1301..1400 -> "N"
        in 1401..1500 -> "P"
        in 1501..1600 -> "Q"
        in 1601..1700 -> "R"
        in 1701..1800 -> "S"
        in 1801..1900 -> "T"
        in 1901..2000 -> "U"
        
        // Two character codes for capital ships 3000-1,000,000 tons
        3000 -> "CA"
        4000 -> "CB"
        5000 -> "CC"
        6000 -> "CD"
        7500 -> "CE"
        10000 -> "CF"
        15000 -> "CG"
        20000 -> "CH"
        25000 -> "CJ"
        30000 -> "CK"
        40000 -> "CL"
        50000 -> "CM"
        60000 -> "CN"
        75000 -> "CP"
        100000 -> "CQ"
        200000 -> "CR"
        300000 -> "CS"
        400000 -> "CT"
        500000 -> "CU"
        600000 -> "CV"
        700000 -> "CW"
        800000 -> "CX"
        900000 -> "CY"
        1000000 -> "CZ"
        
        // For tonnages not exactly matching the table, return "Unknown"
        // This handles edge cases like 150 tons, 2500 tons, etc.
        else -> "Unknown"
    }
}

/**
 * Calculate number of sections for capital ships based on Hull Code according to Issue #102 specifications
 */
private fun calculateSections(hullCode: String): Int {
    return when (hullCode) {
        // CA to CE: 2 sections
        "CA", "CB", "CC", "CD", "CE" -> 2
        
        // CF to CK: 3 sections
        "CF", "CG", "CH", "CJ", "CK" -> 3
        
        // CL to CQ: 4 sections
        "CL", "CM", "CN", "CP", "CQ" -> 4
        
        // CR to CV: 5 sections
        "CR", "CS", "CT", "CU", "CV" -> 5
        
        // CW to CZ: 6 sections
        "CW", "CX", "CY", "CZ" -> 6
        
        // Non-capital ships always have 1 section
        else -> 1
    }
}
