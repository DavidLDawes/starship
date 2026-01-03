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

package starship.virtualsoundnw.com.data.local.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import starship.virtualsoundnw.com.data.local.database.AppDatabase
import starship.virtualsoundnw.com.data.local.database.StarShipDao
import starship.virtualsoundnw.com.data.local.database.EngineDao
import starship.virtualsoundnw.com.data.local.database.FittingDao
import starship.virtualsoundnw.com.data.local.database.WeaponDao
import starship.virtualsoundnw.com.data.local.database.DefenseDao
import starship.virtualsoundnw.com.data.local.database.CargoDao
import starship.virtualsoundnw.com.data.local.database.VehicleDao
import starship.virtualsoundnw.com.data.local.database.VehicleAllocationDao
import starship.virtualsoundnw.com.data.local.database.DroneDao
import starship.virtualsoundnw.com.data.local.database.BerthsDao
import starship.virtualsoundnw.com.data.local.database.CustomItemDao
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    fun provideStarShipDao(appDatabase: AppDatabase): StarShipDao {
        return appDatabase.starShipDao()
    }
    
    @Provides
    fun provideEngineDao(appDatabase: AppDatabase): EngineDao {
        return appDatabase.engineDao()
    }
    
    @Provides
    fun provideFittingDao(appDatabase: AppDatabase): FittingDao {
        return appDatabase.fittingDao()
    }
    
    @Provides
    fun provideWeaponDao(appDatabase: AppDatabase): WeaponDao {
        return appDatabase.weaponDao()
    }
    
    @Provides
    fun provideDefenseDao(appDatabase: AppDatabase): DefenseDao {
        return appDatabase.defenseDao()
    }
    
    @Provides
    fun provideCargoDao(appDatabase: AppDatabase): CargoDao {
        return appDatabase.cargoDao()
    }
    
    @Provides
    fun provideVehicleDao(appDatabase: AppDatabase): VehicleDao {
        return appDatabase.vehicleDao()
    }
    
    @Provides
    fun provideVehicleAllocationDao(appDatabase: AppDatabase): VehicleAllocationDao {
        return appDatabase.vehicleAllocationDao()
    }
    
    @Provides
    fun provideDroneDao(appDatabase: AppDatabase): DroneDao {
        return appDatabase.droneDao()
    }
    
    @Provides
    fun provideBerthsDao(appDatabase: AppDatabase): BerthsDao {
        return appDatabase.berthsDao()
    }

    @Provides
    fun provideCustomItemDao(appDatabase: AppDatabase): CustomItemDao {
        return appDatabase.customItemDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "StarShip"
        ).addMigrations(
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_11_12,
            AppDatabase.MIGRATION_12_13,
            AppDatabase.MIGRATION_13_14,
            AppDatabase.MIGRATION_14_15
        ).fallbackToDestructiveMigration()
         .build()
    }
}
