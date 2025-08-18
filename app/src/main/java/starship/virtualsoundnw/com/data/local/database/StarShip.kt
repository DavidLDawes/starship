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
import kotlinx.coroutines.flow.Flow

enum class TechLevel {
    A, B, C, D, E, F, G, H, J
}

enum class Configuration {
    NEEDLE_WEDGE, CONE, STANDARD, CLOSE_STRUCTURE,
    SPHERE, DISPERSED_STRUCTURE, PLANETOID, BUFFERED_PLANETOID
}

fun Configuration.displayName(): String {
    return when (this) {
        Configuration.NEEDLE_WEDGE -> "Needle/Wedge"
        Configuration.CONE -> "Cone"
        Configuration.STANDARD -> "Standard"
        Configuration.CLOSE_STRUCTURE -> "Close Structure"
        Configuration.SPHERE -> "Sphere"
        Configuration.DISPERSED_STRUCTURE -> "Dispersed Structure"
        Configuration.PLANETOID -> "Planetoid"
        Configuration.BUFFERED_PLANETOID -> "Buffered Planetoid"
}

@Entity
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
    
    val shipDesignation: String get() = if (isCapitalShip) "Capital Ship" else "Ship"
}

@Dao
interface StarShipDao {
    @Query("SELECT * FROM starship ORDER BY uid DESC LIMIT 10")
    fun getStarShips(): Flow<List<StarShip>>

    @Insert
    suspend fun insertStarShip(item: StarShip)
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
