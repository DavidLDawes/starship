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
 * Berth types with their specifications according to Issue #112
 */
enum class BerthType(
    val displayName: String,
    val tonnage: Float,      // Tonnage required per berth
    val costMCr: Float       // Cost per berth in MCr
) {
    STATEROOMS("Staterooms", 4f, 0.5f),
    LUXURY_STATEROOMS("Luxury Staterooms", 5f, 0.6f),
    LOW_PASSAGE("Low Passage", 0.5f, 0.05f),
    EMERGENCY_LOW("Emergency Low", 1f, 0.1f)
}

/**
 * Ship berths entity - stores berth allocations for each ship
 */
@Entity(
    tableName = "berths",
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
data class Berths(
    val shipId: Int,
    val staterooms: Int = 0,        // Number of staterooms (4 tons, 0.5 MCr each)
    val luxuryStaterooms: Int = 0,  // Number of luxury staterooms (5 tons, 0.6 MCr each)
    val lowPassage: Int = 0,        // Number of low passage berths (0.5 tons, 0.05 MCr each)
    val emergencyLow: Int = 0       // Number of emergency low berths (1 ton, 0.1 MCr each)
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Calculate total tonnage used by all berth types
     */
    fun getTotalTonnage(): Float {
        return (staterooms * BerthType.STATEROOMS.tonnage) +
               (luxuryStaterooms * BerthType.LUXURY_STATEROOMS.tonnage) +
               (lowPassage * BerthType.LOW_PASSAGE.tonnage) +
               (emergencyLow * BerthType.EMERGENCY_LOW.tonnage)
    }
    
    /**
     * Calculate cost for a specific berth type
     */
    fun getCostForBerthType(berthType: BerthType, count: Int): Float {
        return count * berthType.costMCr
    }
    
    /**
     * Calculate total cost for all berth types
     */
    fun getTotalBerthsCost(): Float {
        return getCostForBerthType(BerthType.STATEROOMS, staterooms) +
               getCostForBerthType(BerthType.LUXURY_STATEROOMS, luxuryStaterooms) +
               getCostForBerthType(BerthType.LOW_PASSAGE, lowPassage) +
               getCostForBerthType(BerthType.EMERGENCY_LOW, emergencyLow)
    }
    
    /**
     * Get count for a specific berth type
     */
    fun getCountForBerthType(berthType: BerthType): Int {
        return when (berthType) {
            BerthType.STATEROOMS -> staterooms
            BerthType.LUXURY_STATEROOMS -> luxuryStaterooms
            BerthType.LOW_PASSAGE -> lowPassage
            BerthType.EMERGENCY_LOW -> emergencyLow
        }
    }
    
    /**
     * Create a new Berths instance with updated count for a specific berth type
     */
    fun withUpdatedCount(berthType: BerthType, newCount: Int): Berths {
        return when (berthType) {
            BerthType.STATEROOMS -> copy(staterooms = newCount)
            BerthType.LUXURY_STATEROOMS -> copy(luxuryStaterooms = newCount)
            BerthType.LOW_PASSAGE -> copy(lowPassage = newCount)
            BerthType.EMERGENCY_LOW -> copy(emergencyLow = newCount)
        }
    }
    
    /**
     * Calculate total passenger capacity
     */
    fun getTotalPassengerCapacity(): Int {
        return staterooms + luxuryStaterooms + lowPassage + emergencyLow
    }
    
    /**
     * Calculate total staterooms + luxury staterooms for steward calculation
     */
    fun getTotalStateroomsForStewards(): Int {
        return staterooms + luxuryStaterooms
    }
}

@Dao
interface BerthsDao {
    @Query("SELECT * FROM berths WHERE shipId = :shipId")
    fun getBerthsForShip(shipId: Int): Flow<Berths?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBerths(berths: Berths)
    
    @Update
    suspend fun updateBerths(berths: Berths)
    
    @Delete
    suspend fun deleteBerths(berths: Berths)
    
    @Query("DELETE FROM berths WHERE shipId = :shipId")
    suspend fun deleteBerthsForShip(shipId: Int)
    
    @Query("SELECT * FROM berths WHERE shipId = :shipId")
    suspend fun getBerthsForShipSync(shipId: Int): Berths?
}