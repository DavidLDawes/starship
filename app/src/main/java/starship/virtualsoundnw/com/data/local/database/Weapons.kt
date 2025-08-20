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
 * Weapon types available for turrets
 */
enum class WeaponType(val cost: Float) {
    NONE(0.0f),  // Used for hardpoints
    PULSE_LASER(0.5f),
    BEAM_LASER(1.0f),
    PARTICLE_BEAM(4.0f),
    MISSILE_RACK(0.75f),
    SANDCASTER(0.25f)
}

/**
 * Turret types with their base costs
 */
enum class TurretType(val baseCost: Float, val tonnage: Float, val isPopUp: Boolean, val weaponCapacity: Int) {
    HARDPOINT(1.0f, 0.0f, false, 0),
    SINGLE(0.2f, 1.0f, false, 1),
    DOUBLE(0.5f, 1.0f, false, 2),
    TRIPLE(1.0f, 1.0f, false, 3),
    POPUP_SINGLE(1.2f, 2.0f, true, 1),
    POPUP_DOUBLE(1.5f, 2.0f, true, 2),
    POPUP_TRIPLE(2.0f, 2.0f, true, 3)
}

/**
 * Individual weapon installation entity
 * Represents a single turret with its weapon configuration
 */
@Entity(
    tableName = "weapons",
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
data class Weapon(
    val shipId: Int,
    val turretType: TurretType,
    val weaponType: WeaponType
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    
    /**
     * Calculate total cost for this weapon installation
     * Cost = turret base cost + (weapon capacity × weapon cost)
     */
    fun getTotalCost(): Float {
        return turretType.baseCost + (turretType.weaponCapacity * weaponType.cost)
    }
    
    /**
     * Calculate total tonnage for this weapon installation
     * Tonnage = turret tonnage only (weapons don't add tonnage)
     */
    fun getTotalTonnage(): Float {
        return turretType.tonnage
    }
    
    /**
     * Get weapon designation (e.g., "Single Pulse Laser", "Pop-up Triple Particle Beam")
     */
    fun getDesignation(): String {
        // Hardpoints don't have weapons
        if (turretType == TurretType.HARDPOINT) {
            return "Hard Point"
        }
        
        val turretName = when (turretType) {
            TurretType.HARDPOINT -> "Hard Point"
            TurretType.SINGLE -> ""  // Drop "Single" per feedback
            TurretType.DOUBLE -> "Double"
            TurretType.TRIPLE -> "Triple"
            TurretType.POPUP_SINGLE -> "Pop-up"  // Drop "Single" from pop-up too
            TurretType.POPUP_DOUBLE -> "Pop-up Double"
            TurretType.POPUP_TRIPLE -> "Pop-up Triple"
        }
        
        val weaponName = when (weaponType) {
            WeaponType.NONE -> ""
            WeaponType.PULSE_LASER -> "Pulse Laser"
            WeaponType.BEAM_LASER -> "Beam Laser"
            WeaponType.PARTICLE_BEAM -> "Particle Beam"
            WeaponType.MISSILE_RACK -> "Missile Rack"
            WeaponType.SANDCASTER -> "Sandcaster"
        }
        
        return if (turretName.isEmpty()) weaponName else "$turretName $weaponName"
    }
}

@Dao
interface WeaponDao {
    @Query("SELECT * FROM weapons WHERE shipId = :shipId")
    fun getWeaponsForShip(shipId: Int): Flow<List<Weapon>>
    
    @Query("SELECT * FROM weapons WHERE shipId = :shipId AND turretType = :turretType")
    fun getWeaponsForShipByTurretType(shipId: Int, turretType: TurretType): Flow<List<Weapon>>
    
    @Query("SELECT * FROM weapons WHERE shipId = :shipId AND weaponType = :weaponType")
    fun getWeaponsForShipByWeaponType(shipId: Int, weaponType: WeaponType): Flow<List<Weapon>>
    
    @Insert
    suspend fun insertWeapon(weapon: Weapon)
    
    @Delete
    suspend fun deleteWeapon(weapon: Weapon)
    
    @Query("DELETE FROM weapons WHERE shipId = :shipId")
    suspend fun deleteWeaponsForShip(shipId: Int)
    
    @Query("DELETE FROM weapons WHERE shipId = :shipId AND turretType = :turretType")
    suspend fun deleteWeaponsForShipByTurretType(shipId: Int, turretType: TurretType)
}

/**
 * Calculate maximum hardpoints available for a ship
 * 1 hardpoint per 100 tons (rounded down)
 */
fun calculateMaxHardpoints(shipTonnage: Int): Int {
    return shipTonnage / 100
}

/**
 * Data class for weapons calculations and state
 */
data class WeaponsCalculation(
    val ship: StarShip,
    val weapons: List<Weapon>
) {
    val maxHardpoints: Int get() = calculateMaxHardpoints(ship.tons)
    val usedHardpoints: Int get() = weapons.size
    val remainingHardpoints: Int get() = maxHardpoints - usedHardpoints
    val totalWeaponsCost: Float get() = weapons.sumOf { it.getTotalCost().toDouble() }.toFloat()
    val totalWeaponsTonnage: Float get() = weapons.sumOf { it.getTotalTonnage().toDouble() }.toFloat()
    
    /**
     * Check if adding another weapon would exceed hardpoint limits
     */
    fun canAddWeapon(): Boolean = remainingHardpoints > 0
    
    /**
     * Group weapons by type for display purposes
     */
    fun getWeaponsByTurretType(): Map<TurretType, List<Weapon>> {
        return weapons.groupBy { it.turretType }
    }
    
    /**
     * Group weapons by weapon type for display purposes
     */
    fun getWeaponsByWeaponType(): Map<WeaponType, List<Weapon>> {
        return weapons.groupBy { it.weaponType }
    }
}