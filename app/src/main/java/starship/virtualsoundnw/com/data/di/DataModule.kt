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

package starship.virtualsoundnw.com.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.DefaultStarShipRepository
import starship.virtualsoundnw.com.data.EnginesRepository
import starship.virtualsoundnw.com.data.DefaultEnginesRepository
import starship.virtualsoundnw.com.data.FittingsRepository
import starship.virtualsoundnw.com.data.DefaultFittingsRepository
import starship.virtualsoundnw.com.data.WeaponsRepository
import starship.virtualsoundnw.com.data.DefaultWeaponsRepository
import starship.virtualsoundnw.com.data.DefensesRepository
import starship.virtualsoundnw.com.data.CargoRepository
import starship.virtualsoundnw.com.data.DronesRepository
import starship.virtualsoundnw.com.data.DronesRepositoryImpl
import starship.virtualsoundnw.com.data.BerthsRepository
import starship.virtualsoundnw.com.data.DefaultBerthsRepository
import starship.virtualsoundnw.com.data.CustomRepository
import starship.virtualsoundnw.com.data.CustomRepositoryImpl
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsStarShipRepository(
        starShipRepository: DefaultStarShipRepository
    ): StarShipRepository
    
    @Singleton
    @Binds
    fun bindsEnginesRepository(
        enginesRepository: DefaultEnginesRepository
    ): EnginesRepository
    
    @Singleton
    @Binds
    fun bindsFittingsRepository(
        fittingsRepository: DefaultFittingsRepository
    ): FittingsRepository
    
    @Singleton
    @Binds
    fun bindsWeaponsRepository(
        weaponsRepository: DefaultWeaponsRepository
    ): WeaponsRepository
    
    @Singleton
    @Binds
    fun bindsDronesRepository(
        dronesRepository: DronesRepositoryImpl
    ): DronesRepository
    
    @Singleton
    @Binds
    fun bindsBerthsRepository(
        berthsRepository: DefaultBerthsRepository
    ): BerthsRepository

    @Singleton
    @Binds
    fun bindsCustomRepository(
        customRepository: CustomRepositoryImpl
    ): CustomRepository
}

class FakeStarShipRepository @Inject constructor() : StarShipRepository {
    override val starShips: Flow<List<StarShip>> = flowOf(fakeStarShips)

    override suspend fun add(starShip: StarShip) {
        throw NotImplementedError()
    }
    
    override suspend fun delete(starShip: StarShip) {
        throw NotImplementedError()
    }
    
    override fun getShipSummary(shipId: Int): Flow<ShipSummary?> {
        return flowOf(
            fakeStarShips.find { it.uid == shipId }?.let { ship ->
                ShipSummary(ship = ship)
            }
        )
    }
    
    override suspend fun saveAs(originalShipId: Int, newName: String): Result<StarShip> {
        return Result.failure(NotImplementedError("SaveAs not implemented in fake repository"))
    }
    
    override suspend fun doesShipNameExist(name: String): Boolean {
        return fakeStarShips.any { it.name.equals(name, ignoreCase = true) }
    }
    
    override suspend fun addWithNameValidation(starShip: StarShip): Result<StarShip> {
        return if (doesShipNameExist(starShip.name)) {
            Result.failure(IllegalArgumentException("There's already a ship with that name"))
        } else {
            Result.success(starShip)
        }
    }
}

// Test data for fake repository
val fakeStarShips = listOf(
    StarShip("One", "First test ship", 200, TechLevel.C, Configuration.STANDARD),

    StarShip("Two", "Second test ship", 400, TechLevel.E, Configuration.NEEDLE_WEDGE),
    StarShip("Three", "Third test ship", 600, TechLevel.G, Configuration.SPHERE)
)
