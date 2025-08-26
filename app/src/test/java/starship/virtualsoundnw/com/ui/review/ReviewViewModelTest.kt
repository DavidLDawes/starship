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

package starship.virtualsoundnw.com.ui.review

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import starship.virtualsoundnw.com.data.ShipSummaryService
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.DetailedShipTableService
import starship.virtualsoundnw.com.data.DetailedShipTableData
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration

/**
 * Unit tests for [ReviewViewModel] Save As functionality.
 * 
 * Note: These are simplified tests focusing on dialog state management.
 * Full integration tests would require proper mocking of service dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    @Test
    fun reviewUiState_initialState_isCorrect() = runTest {
        // Create a basic test to verify the initial state includes new Save As fields
        val initialState = ReviewUiState()
        
        assertFalse(initialState.showSaveAsDialog)
        assertFalse(initialState.saveAsLoading)
        assertEquals(null, initialState.saveAsError)
    }

    // Note: Additional tests would require proper dependency injection setup
    // or mocking framework to test the full ViewModel behavior
}

// For now, we'll focus on testing just the dialog state management
// Full integration tests would require proper mocking framework or test doubles

private class FakeStarShipRepository(
    private val shouldFailSaveAs: Boolean = false
) : StarShipRepository {
    
    override val starShips: Flow<List<StarShip>> = flowOf(
        listOf(
            StarShip(
                name = "Test Ship",
                description = "Test description",
                tons = 200,
                techLevel = TechLevel.C,
                configuration = Configuration.STANDARD
            ).apply { uid = 1 }
        )
    )

    override suspend fun add(starShip: StarShip) {
        // No-op for testing
    }

    override suspend fun delete(starShip: StarShip) {
        // No-op for testing
    }

    override fun getShipSummary(shipId: Int): Flow<ShipSummary?> {
        return flowOf(null)
    }

    override suspend fun saveAs(originalShipId: Int, newName: String): Result<StarShip> {
        return if (shouldFailSaveAs) {
            Result.failure(IllegalArgumentException("A ship with this name already exists"))
        } else {
            val newShip = StarShip(
                name = newName,
                description = "Copy of original ship",
                tons = 200,
                techLevel = TechLevel.C,
                configuration = Configuration.STANDARD
            ).apply { uid = 100 }
            Result.success(newShip)
        }
    }
}