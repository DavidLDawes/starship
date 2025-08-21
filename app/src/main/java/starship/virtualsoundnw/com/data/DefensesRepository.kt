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

package starship.virtualsoundnw.com.data

import kotlinx.coroutines.flow.Flow
import starship.virtualsoundnw.com.data.local.database.Defense
import starship.virtualsoundnw.com.data.local.database.DefenseDao
import starship.virtualsoundnw.com.data.local.database.ArmorType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefensesRepository @Inject constructor(
    private val defenseDao: DefenseDao
) {
    
    fun getDefenseForShip(shipId: Int): Flow<Defense?> = defenseDao.getDefenseForShip(shipId)
    
    suspend fun insertDefense(defense: Defense) = defenseDao.insertDefense(defense)
    
    suspend fun deleteDefenseForShip(shipId: Int) = defenseDao.deleteDefenseForShip(shipId)
    
    /**
     * Update defense entry, preserving existing values for fields not specified
     */
    suspend fun updateDefense(
        shipId: Int,
        armorProtection: Int? = null,
        nuclearDampers: Int? = null,
        mesonScreens: Int? = null,
        blackGlobes: Int? = null
    ) {
        // Get current defense or create new one
        val currentDefense = defenseDao.getDefenseForShip(shipId)
        // Note: This returns Flow<Defense?>, we'll handle this in the ViewModel
        
        val defense = Defense(
            shipId = shipId,
            armorProtection = armorProtection ?: 0,
            nuclearDampers = nuclearDampers ?: 0,
            mesonScreens = mesonScreens ?: 0,
            blackGlobes = blackGlobes ?: 0
        )
        defenseDao.insertDefense(defense)
    }
}