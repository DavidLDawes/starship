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

package starship.virtualsoundnw.com.ui.engines

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import starship.virtualsoundnw.com.data.local.database.Engine
import starship.virtualsoundnw.com.data.local.database.EngineType
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration

/**
 * UI tests for [EnginesScreen].
 */
@RunWith(AndroidJUnit4::class)
class EnginesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun enginesScreen_displaysHeaderInformation() {
        val ship = STANDARD_SHIP
        val uiState = EnginesUiState(ship = ship)
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = ship,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        composeTestRule.onNodeWithText("Engine Configuration").assertExists()
        composeTestRule.onNodeWithText("Ship: ${ship.name}").assertExists()
        composeTestRule.onNodeWithText("${ship.tons} tons • TL ${ship.techLevel} • Standard Ship").assertExists()
    }

    @Test
    fun enginesScreen_displaysAllEngineSections() {
        val ship = STANDARD_SHIP
        val uiState = EnginesUiState(ship = ship)
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = ship,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        composeTestRule.onNodeWithText("Power Plant").assertExists()
        composeTestRule.onNodeWithText("Jump Drive").assertExists()
        composeTestRule.onNodeWithText("Maneuver Drive").assertExists()
    }

    @Test
    fun enginesScreen_showsNoEnginesConfiguredWhenEmpty() {
        val ship = STANDARD_SHIP
        val uiState = EnginesUiState(ship = ship)
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = ship,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        composeTestRule.onNodeWithText("No power plant configured").assertExists()
        composeTestRule.onNodeWithText("No jump drive configured").assertExists()
        composeTestRule.onNodeWithText("No maneuver drive configured").assertExists()
    }

    @Test
    fun enginesScreen_displaysEngineInformation() {
        val ship = STANDARD_SHIP
        val engines = listOf(
            Engine(1, EngineType.POWER_PLANT, 5),
            Engine(1, EngineType.JUMP_DRIVE, 2),
            Engine(1, EngineType.MANEUVER_DRIVE, 3)
        )
        val uiState = EnginesUiState(
            ship = ship,
            engines = engines,
            powerPlants = listOf(engines[0]),
            jumpDrives = listOf(engines[1]),
            maneuverDrives = listOf(engines[2])
        )
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = ship,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        // Check engine type displays
        composeTestRule.onNodeWithText("Fusion Power Plant").assertExists()
        composeTestRule.onNodeWithText("Jump Drive").assertExists()
        composeTestRule.onNodeWithText("Maneuver Drive").assertExists()

        // Check engine designations
        composeTestRule.onNodeWithText("Designation: P-5").assertExists()
        composeTestRule.onNodeWithText("Designation: J-2").assertExists()
        composeTestRule.onNodeWithText("Designation: M-3").assertExists()

        // Check performance displays
        composeTestRule.onNodeWithText("Performance: 5").assertExists()
        composeTestRule.onNodeWithText("Performance: 2").assertExists()
        composeTestRule.onNodeWithText("Performance: 3").assertExists()
    }

    @Test
    fun enginesScreen_displaysFuelPanel() {
        val ship = STANDARD_SHIP
        val engines = listOf(Engine(1, EngineType.JUMP_DRIVE, 2))
        val uiState = EnginesUiState(
            ship = ship,
            engines = engines,
            jumpDrives = engines
        )
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = ship,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        composeTestRule.onNodeWithText("Fuel Requirements").assertExists()
        composeTestRule.onNodeWithText("Jump Performance:").assertExists()
        composeTestRule.onNodeWithText("J-2").assertExists()
        composeTestRule.onNodeWithText("Power Plant:").assertExists()
        composeTestRule.onNodeWithText("Standard").assertExists()
        composeTestRule.onNodeWithText("Fuel Required:").assertExists()
    }

    @Test
    fun enginesScreen_displaysSummaryPanel() {
        val ship = STANDARD_SHIP
        val engines = listOf(
            Engine(1, EngineType.POWER_PLANT, 5),
            Engine(1, EngineType.JUMP_DRIVE, 2)
        )
        val uiState = EnginesUiState(
            ship = ship,
            engines = engines,
            powerPlants = listOf(engines[0]),
            jumpDrives = listOf(engines[1])
        )
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = ship,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        composeTestRule.onNodeWithText("Ship Summary").assertExists()
        composeTestRule.onNodeWithText("Total Ship Tonnage:").assertExists()
        composeTestRule.onNodeWithText("${ship.tons} tons").assertExists()
        composeTestRule.onNodeWithText("Engine Tonnage:").assertExists()
        composeTestRule.onNodeWithText("Fuel Tonnage:").assertExists()
        composeTestRule.onNodeWithText("Remaining Tonnage:").assertExists()
        composeTestRule.onNodeWithText("Hull Cost:").assertExists()
        composeTestRule.onNodeWithText("Engine Cost:").assertExists()
        composeTestRule.onNodeWithText("Total Cost:").assertExists()
    }

    @Test
    fun enginesScreen_showsAddButtonForEmptyEngines() {
        val ship = STANDARD_SHIP
        val uiState = EnginesUiState(ship = ship)
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = ship,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        // Should have Add buttons for each engine type
        composeTestRule.onNodeWithText("Add").assertExists()
    }

    @Test
    fun enginesScreen_showsRemoveButtonForCapitalShipWithMultipleEngines() {
        val capitalShip = CAPITAL_SHIP
        val engines = listOf(
            Engine(1, EngineType.POWER_PLANT, 5),
            Engine(1, EngineType.POWER_PLANT, 6) // Multiple power plants
        )
        val uiState = EnginesUiState(
            ship = capitalShip,
            engines = engines,
            powerPlants = engines
        )
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = capitalShip,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        // Capital ship with multiple engines should show remove buttons
        composeTestRule.onNodeWithContentDescription("Remove Engine").assertExists()
    }

    @Test
    fun enginesScreen_displaysCorrectPowerPlantTypeForTechLevel() {
        // Test TL A (Fission)
        val fissionShip = StarShip("Fission Ship", "Test", 200, TechLevel.A, Configuration.STANDARD)
        val fissionEngine = Engine(1, EngineType.POWER_PLANT, 3)
        val fissionUiState = EnginesUiState(
            ship = fissionShip,
            engines = listOf(fissionEngine),
            powerPlants = listOf(fissionEngine)
        )
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = fissionShip,
                uiState = fissionUiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        composeTestRule.onNodeWithText("Fission Power Plant").assertExists()
    }

    @Test
    fun enginesScreen_calculatesValuesCorrectly() {
        val ship = STANDARD_SHIP // 200 ton ship
        val engines = listOf(
            Engine(1, EngineType.POWER_PLANT, 5) // 4% = 8 tons
        )
        val uiState = EnginesUiState(
            ship = ship,
            engines = engines,
            powerPlants = engines
        )
        
        composeTestRule.setContent {
            EnginesConfigurationScreen(
                ship = ship,
                uiState = uiState,
                onAddEngine = { _, _ -> },
                onRemoveEngine = { },
                onUpdateEnginePerformance = { _, _ -> },
                isJumpPerformanceValid = { true }
            )
        }

        // Check calculated tonnage display
        composeTestRule.onNodeWithText("Tons: 8.0").assertExists()
        composeTestRule.onNodeWithText("(4.0%)").assertExists()
    }
}

private val STANDARD_SHIP = StarShip(
    "Test Ship",
    "Test class",
    200,
    TechLevel.C,
    Configuration.STANDARD
)

private val CAPITAL_SHIP = StarShip(
    "Capital Ship",
    "Battleship class",
    10000, // Large tonnage makes it capital ship
    TechLevel.G,
    Configuration.STANDARD
)