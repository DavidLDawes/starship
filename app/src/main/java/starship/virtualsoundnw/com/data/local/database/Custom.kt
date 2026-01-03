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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Custom item entity - tracks custom user-defined items for ships
 */
@Entity(
    tableName = "custom_items",
    foreignKeys = [
        ForeignKey(
            entity = StarShip::class,
            parentColumns = ["uid"],
            childColumns = ["shipId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["shipId"])
    ]
)
data class CustomItem(
    val shipId: Int,
    val name: String,
    val tons: Float,
    val costMCr: Float
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}

@Dao
interface CustomItemDao {
    @Query("SELECT * FROM custom_items WHERE shipId = :shipId ORDER BY name")
    fun getCustomItemsForShip(shipId: Int): Flow<List<CustomItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomItem(customItem: CustomItem): Long

    @Update
    suspend fun updateCustomItem(customItem: CustomItem)

    @Delete
    suspend fun deleteCustomItem(customItem: CustomItem)

    @Query("DELETE FROM custom_items WHERE shipId = :shipId")
    suspend fun deleteAllCustomItemsForShip(shipId: Int)
}
