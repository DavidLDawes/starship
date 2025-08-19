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

package starship.virtualsoundnw.com.data

import kotlinx.coroutines.flow.Flow
import starship.virtualsoundnw.com.data.local.database.Engine
import starship.virtualsoundnw.com.data.local.database.EngineDao
import starship.virtualsoundnw.com.data.local.database.EngineType
import javax.inject.Inject

class DefaultEnginesRepository @Inject constructor(
    private val engineDao: EngineDao
) : EnginesRepository {

    override fun getEnginesForShip(shipId: Int): Flow<List<Engine>> {
        return engineDao.getEnginesForShip(shipId)
    }

    override fun getEnginesForShipByType(shipId: Int, engineType: EngineType): Flow<List<Engine>> {
        return engineDao.getEnginesForShipByType(shipId, engineType)
    }

    override suspend fun addEngine(engine: Engine) {
        engineDao.insertEngine(engine)
    }

    override suspend fun removeEngine(engine: Engine) {
        engineDao.deleteEngine(engine)
    }

    override suspend fun clearEnginesForShip(shipId: Int) {
        engineDao.deleteEnginesForShip(shipId)
    }

    override suspend fun clearEnginesForShipByType(shipId: Int, engineType: EngineType) {
        engineDao.deleteEnginesForShipByType(shipId, engineType)
    }
}