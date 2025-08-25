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
import starship.virtualsoundnw.com.data.local.database.Berths
import starship.virtualsoundnw.com.data.local.database.BerthsDao
import javax.inject.Inject
import javax.inject.Singleton

interface BerthsRepository {
    fun getBerthsForShip(shipId: Int): Flow<Berths?>
    suspend fun insertBerths(berths: Berths)
    suspend fun updateBerths(berths: Berths)
    suspend fun deleteBerths(berths: Berths)
    suspend fun deleteBerthsForShip(shipId: Int)
    suspend fun getBerthsForShipSync(shipId: Int): Berths?
}

@Singleton
class DefaultBerthsRepository @Inject constructor(
    private val berthsDao: BerthsDao
) : BerthsRepository {

    override fun getBerthsForShip(shipId: Int): Flow<Berths?> {
        return berthsDao.getBerthsForShip(shipId)
    }

    override suspend fun insertBerths(berths: Berths) {
        berthsDao.insertBerths(berths)
    }

    override suspend fun updateBerths(berths: Berths) {
        berthsDao.updateBerths(berths)
    }

    override suspend fun deleteBerths(berths: Berths) {
        berthsDao.deleteBerths(berths)
    }

    override suspend fun deleteBerthsForShip(shipId: Int) {
        berthsDao.deleteBerthsForShip(shipId)
    }

    override suspend fun getBerthsForShipSync(shipId: Int): Berths? {
        return berthsDao.getBerthsForShipSync(shipId)
    }
}