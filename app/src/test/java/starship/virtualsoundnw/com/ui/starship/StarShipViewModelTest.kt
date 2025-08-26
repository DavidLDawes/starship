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

package starship.virtualsoundnw.com.ui.starship


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class StarShipViewModelTest {
    
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun uiState_initiallyLoading() = runTest {
        val viewModel = StarShipViewModel(FakeStarShipRepository())
        assertEquals(viewModel.uiState.first(), StarShipUiState.Loading)
    }

    @Test
    fun uiState_onItemSaved_isDisplayed() = runTest {
        val viewModel = StarShipViewModel(FakeStarShipRepository())
        assertEquals(viewModel.uiState.first(), StarShipUiState.Loading)
    }

    @Test
    fun addStarShip_withUniqueName_succeeds() = runTest {
        val repository = FakeStarShipRepository()
        val viewModel = StarShipViewModel(repository)
        
        val newShip = StarShip("Unique Name", "Test ship", 200, TechLevel.C, Configuration.STANDARD)
        viewModel.addStarShip(newShip)
        
        // Need to wait for the coroutine to complete
        advanceUntilIdle()
        
        // Should not have any form errors
        assertEquals(null, viewModel.formErrorState.value)
    }

    @Test
    fun addStarShip_withDuplicateName_showsError() = runTest {
        val repository = FakeStarShipRepository()
        // Add a ship first
        val existingShip = StarShip("Existing Ship", "First ship", 200, TechLevel.C, Configuration.STANDARD)
        repository.addWithNameValidation(existingShip)
        
        val viewModel = StarShipViewModel(repository)
        
        // Try to add another ship with the same name
        val duplicateShip = StarShip("Existing Ship", "Duplicate ship", 300, TechLevel.D, Configuration.CONE)
        viewModel.addStarShip(duplicateShip)
        
        // Need to wait for the coroutine to complete
        advanceUntilIdle()
        
        // Should show name error
        val errorState = viewModel.formErrorState.value
        assertEquals("There's already a ship with that name", errorState?.nameError)
    }

    @Test
    fun addStarShip_withDuplicateNameCaseInsensitive_showsError() = runTest {
        val repository = FakeStarShipRepository()
        // Add a ship first
        val existingShip = StarShip("Enterprise", "First ship", 200, TechLevel.C, Configuration.STANDARD)
        repository.addWithNameValidation(existingShip)
        
        val viewModel = StarShipViewModel(repository)
        
        // Try to add another ship with same name but different case
        val duplicateShip = StarShip("ENTERPRISE", "Duplicate ship", 300, TechLevel.D, Configuration.CONE)
        viewModel.addStarShip(duplicateShip)
        
        // Need to wait for the coroutine to complete
        advanceUntilIdle()
        
        // Should show name error (case-insensitive check)
        val errorState = viewModel.formErrorState.value
        assertEquals("There's already a ship with that name", errorState?.nameError)
    }
    
    @Test
    fun clearFormError_clearsErrorState() = runTest {
        val repository = FakeStarShipRepository()
        // Add a ship first
        val existingShip = StarShip("Existing Ship", "First ship", 200, TechLevel.C, Configuration.STANDARD)
        repository.addWithNameValidation(existingShip)
        
        val viewModel = StarShipViewModel(repository)
        
        // Create error by adding duplicate
        val duplicateShip = StarShip("Existing Ship", "Duplicate ship", 300, TechLevel.D, Configuration.CONE)
        viewModel.addStarShip(duplicateShip)
        
        // Need to wait for the coroutine to complete
        advanceUntilIdle()
        
        // Verify error exists
        assertEquals("There's already a ship with that name", viewModel.formErrorState.value?.nameError)
        
        // Clear the error
        viewModel.clearFormError()
        
        // Verify error is cleared
        assertEquals(null, viewModel.formErrorState.value)
    }
}

private class FakeStarShipRepository : StarShipRepository {

    private val data = mutableListOf<StarShip>()

    override val starShips: Flow<List<StarShip>>
        get() = flow { emit(data.toList()) }

    override suspend fun add(starShip: StarShip) {
        data.add(0, starShip)
    }
    
    override suspend fun delete(starShip: StarShip) {
        data.remove(starShip)
    }
    
    override fun getShipSummary(shipId: Int): Flow<ShipSummary?> {
        return flowOf(
            data.find { it.uid == shipId }?.let { ship ->
                ShipSummary(ship = ship)
            }
        )
    }
    
    override suspend fun saveAs(originalShipId: Int, newName: String): Result<StarShip> {
        return Result.failure(NotImplementedError("SaveAs not implemented in fake repository"))
    }
    
    override suspend fun doesShipNameExist(name: String): Boolean {
        return data.any { it.name.equals(name, ignoreCase = true) }
    }
    
    override suspend fun addWithNameValidation(starShip: StarShip): Result<StarShip> {
        return if (doesShipNameExist(starShip.name)) {
            Result.failure(IllegalArgumentException("There's already a ship with that name"))
        } else {
            data.add(0, starShip)
            Result.success(starShip)
        }
    }
}
