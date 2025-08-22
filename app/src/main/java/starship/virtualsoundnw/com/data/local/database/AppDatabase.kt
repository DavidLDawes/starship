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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [StarShip::class, Engine::class, Fitting::class, Weapon::class, Defense::class, Cargo::class], version = 10)
abstract class AppDatabase : RoomDatabase() {
    abstract fun starShipDao(): StarShipDao
    abstract fun engineDao(): EngineDao
    abstract fun fittingDao(): FittingDao
    abstract fun weaponDao(): WeaponDao
    abstract fun defenseDao(): DefenseDao
    abstract fun cargoDao(): CargoDao

    companion object {
        /**
         * Migration from version 9 to 10: Update Cargo table structure
         * - Rename frozenCargoTons to coldStorageTons
         * - Rename secureCargoTons to securedCargoTons  
         * - Add xenoCargoTons column
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Step 1: Create a new temporary cargo table with the new structure
                database.execSQL("""
                    CREATE TABLE cargo_new (
                        uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        shipId INTEGER NOT NULL,
                        cargoTons INTEGER NOT NULL DEFAULT 0,
                        sparesTons INTEGER NOT NULL DEFAULT 0,
                        coldStorageTons INTEGER NOT NULL DEFAULT 0,
                        securedCargoTons INTEGER NOT NULL DEFAULT 0,
                        xenoCargoTons INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(shipId) REFERENCES starships(uid) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Step 2: Copy data from old table to new table, mapping old column names
                database.execSQL("""
                    INSERT INTO cargo_new (uid, shipId, cargoTons, sparesTons, coldStorageTons, securedCargoTons, xenoCargoTons)
                    SELECT uid, shipId, cargoTons, sparesTons, 
                           COALESCE(frozenCargoTons, 0) as coldStorageTons,
                           COALESCE(secureCargoTons, 0) as securedCargoTons,
                           0 as xenoCargoTons
                    FROM cargo
                """.trimIndent())

                // Step 3: Drop the old table
                database.execSQL("DROP TABLE cargo")

                // Step 4: Rename the new table to the original name
                database.execSQL("ALTER TABLE cargo_new RENAME TO cargo")

                // Step 5: Recreate the index
                database.execSQL("CREATE UNIQUE INDEX index_cargo_shipId ON cargo(shipId)")
            }
        }
    }
}
