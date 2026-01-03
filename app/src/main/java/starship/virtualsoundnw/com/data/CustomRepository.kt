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
import starship.virtualsoundnw.com.data.local.database.CustomItem
import starship.virtualsoundnw.com.data.local.database.CustomItemDao
import javax.inject.Inject
import javax.inject.Singleton

interface CustomRepository {
    fun getCustomItemsForShip(shipId: Int): Flow<List<CustomItem>>
    suspend fun addCustomItem(shipId: Int, name: String, tons: Float, costMCr: Float)
    suspend fun updateCustomItem(customItem: CustomItem)
    suspend fun deleteCustomItem(customItem: CustomItem)
}

@Singleton
class CustomRepositoryImpl @Inject constructor(
    private val customItemDao: CustomItemDao
) : CustomRepository {

    override fun getCustomItemsForShip(shipId: Int): Flow<List<CustomItem>> {
        return customItemDao.getCustomItemsForShip(shipId)
    }

    override suspend fun addCustomItem(shipId: Int, name: String, tons: Float, costMCr: Float) {
        val customItem = CustomItem(
            shipId = shipId,
            name = name,
            tons = tons,
            costMCr = costMCr
        )
        customItemDao.insertCustomItem(customItem)
    }

    override suspend fun updateCustomItem(customItem: CustomItem) {
        customItemDao.updateCustomItem(customItem)
    }

    override suspend fun deleteCustomItem(customItem: CustomItem) {
        customItemDao.deleteCustomItem(customItem)
    }
}
