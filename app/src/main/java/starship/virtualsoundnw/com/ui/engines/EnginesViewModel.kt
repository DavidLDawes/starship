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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.EnginesRepository
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.local.database.Engine
import starship.virtualsoundnw.com.data.local.database.EngineType
import starship.virtualsoundnw.com.data.local.database.PowerPlantType
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.calculateFuelRequirement
import starship.virtualsoundnw.com.data.local.database.isJumpDrivePerformanceValidForTechLevel
import javax.inject.Inject

/**
 * UI state for the Engines screen
 */
data class EnginesUiState(
    val ship: StarShip? = null,
    val engines: List<Engine> = emptyList(),
    val powerPlants: List<Engine> = emptyList(),
    val jumpDrives: List<Engine> = emptyList(),
    val maneuverDrives: List<Engine> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Calculate total engine tonnage
     */
    fun getTotalEngineTonnage(): Float {
        return ship?.let { ship ->
            engines.sumOf { engine -> engine.getTonnage(ship.tons).toDouble() }
        }?.toFloat() ?: 0f
    }
    
    /**
     * Calculate total engine cost
     */
    fun getTotalEngineCost(): Float {
        return ship?.let { ship ->
            engines.sumOf { engine -> engine.getTotalCost(ship.tons, ship.techLevel).toDouble() }
        }?.toFloat() ?: 0f
    }
    
    /**
     * Calculate fuel requirement
     */
    fun getFuelRequirement(): Float {
        return ship?.let { ship ->
            val maxJumpPerformance = jumpDrives.maxOfOrNull { it.performance } ?: 0
            val hasAntimatterPowerPlant = powerPlants.any { 
                PowerPlantType.getBestAvailableForTechLevel(ship.techLevel) == PowerPlantType.ANTIMATTER
            }
            calculateFuelRequirement(maxJumpPerformance, ship.tons, hasAntimatterPowerPlant)
        } ?: 0f
    }
    
    /**
     * Calculate remaining tonnage
     */
    fun getRemainingTonnage(): Float {
        return ship?.let { ship ->
            ship.tons - getTotalEngineTonnage() - getFuelRequirement()
        } ?: 0f
    }
    
    /**
     * Check if ship is capital ship (allows multiple engines)
     */
    fun isCapitalShip(): Boolean {
        return ship?.isCapitalShip ?: false
    }
}

@HiltViewModel
class EnginesViewModel @Inject constructor(
    private val enginesRepository: EnginesRepository,
    private val starShipRepository: StarShipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnginesUiState(isLoading = true))
    val uiState: StateFlow<EnginesUiState> = _uiState.asStateFlow()

    private var currentShipId: Int = -1

    fun loadEnginesForShip(shipId: Int) {
        currentShipId = shipId
        
        viewModelScope.launch {
            try {
                // Combine ship data with engine data
                combine(
                    starShipRepository.starShips,
                    enginesRepository.getEnginesForShip(shipId)
                ) { ships, engines ->
                    val ship = ships.find { it.uid == shipId }
                    val powerPlants = engines.filter { it.type == EngineType.POWER_PLANT }
                    val jumpDrives = engines.filter { it.type == EngineType.JUMP_DRIVE }
                    val maneuverDrives = engines.filter { it.type == EngineType.MANEUVER_DRIVE }
                    
                    EnginesUiState(
                        ship = ship,
                        engines = engines,
                        powerPlants = powerPlants,
                        jumpDrives = jumpDrives,
                        maneuverDrives = maneuverDrives,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load engine data: ${e.message}"
                )
            }
        }
    }

    fun addEngine(engineType: EngineType, performance: Int) {
        viewModelScope.launch {
            try {
                val engine = Engine(
                    shipId = currentShipId,
                    type = engineType,
                    performance = performance
                )
                enginesRepository.addEngine(engine)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add engine: ${e.message}"
                )
            }
        }
    }

    fun removeEngine(engine: Engine) {
        viewModelScope.launch {
            try {
                enginesRepository.removeEngine(engine)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to remove engine: ${e.message}"
                )
            }
        }
    }

    fun updateEnginePerformance(engine: Engine, newPerformance: Int) {
        viewModelScope.launch {
            try {
                // Remove old engine and add new one with updated performance
                enginesRepository.removeEngine(engine)
                val updatedEngine = engine.copy(performance = newPerformance)
                enginesRepository.addEngine(updatedEngine)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update engine: ${e.message}"
                )
            }
        }
    }

    fun isJumpPerformanceValid(performance: Int): Boolean {
        val ship = _uiState.value.ship ?: return false
        return isJumpDrivePerformanceValidForTechLevel(performance, ship.techLevel)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}