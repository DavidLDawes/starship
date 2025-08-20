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
import starship.virtualsoundnw.com.data.local.database.Weapon
import starship.virtualsoundnw.com.data.local.database.WeaponType
import starship.virtualsoundnw.com.data.local.database.TurretType

interface WeaponsRepository {
    fun getWeaponsForShip(shipId: Int): Flow<List<Weapon>>
    fun getWeaponsForShipByTurretType(shipId: Int, turretType: TurretType): Flow<List<Weapon>>
    fun getWeaponsForShipByWeaponType(shipId: Int, weaponType: WeaponType): Flow<List<Weapon>>
    suspend fun addWeapon(weapon: Weapon)
    suspend fun removeWeapon(weapon: Weapon)
    suspend fun clearWeaponsForShip(shipId: Int)
    suspend fun clearWeaponsForShipByTurretType(shipId: Int, turretType: TurretType)
}