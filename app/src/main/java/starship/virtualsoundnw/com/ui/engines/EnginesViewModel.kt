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
import starship.virtualsoundnw.com.data.FittingsRepository
import starship.virtualsoundnw.com.data.local.database.Engine
import starship.virtualsoundnw.com.data.local.database.EngineType
import starship.virtualsoundnw.com.data.local.database.PowerPlantType
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.Fitting
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
    val fitting: Fitting? = null,
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
     * Calculate fittings tonnage
     */
    fun getFittingsTonnage(): Float {
        return ship?.let { ship ->
            fitting?.getTotalTonnage(ship.tons) ?: (ship.tons * 0.005f) // Just bridge if no fitting
        } ?: 0f
    }
    
    /**
     * Calculate fittings cost
     */
    fun getFittingsCost(): Float {
        return ship?.let { ship ->
            fitting?.getTotalCost(ship.tons) ?: (ship.tons * 0.005f * 0.1f) // Just bridge if no fitting
        } ?: 0f
    }
    
    /**
     * Calculate remaining tonnage including fittings
     */
    fun getRemainingTonnage(): Float {
        return ship?.let { ship ->
            ship.tons - getTotalEngineTonnage() - getFuelRequirement() - getFittingsTonnage()
        } ?: 0f
    }
    
    /**
     * Check if ship is capital ship (allows multiple engines)
     */
    fun isCapitalShip(): Boolean {
        return ship?.isCapitalShip ?: false
    }
    
    /**
     * Check if ship has required engines to proceed to fittings
     * Requires at least 1 power plant and 1 jump drive
     */
    fun hasRequiredEngines(): Boolean {
        return powerPlants.isNotEmpty() && jumpDrives.isNotEmpty()
    }
}

@HiltViewModel
class EnginesViewModel @Inject constructor(
    private val enginesRepository: EnginesRepository,
    private val starShipRepository: StarShipRepository,
    private val fittingsRepository: FittingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnginesUiState(isLoading = true))
    val uiState: StateFlow<EnginesUiState> = _uiState.asStateFlow()

    private var currentShipId: Int = -1

    fun loadEnginesForShip(shipId: Int) {
        currentShipId = shipId
        
        viewModelScope.launch {
            try {
                // Combine ship data with engine data and fittings
                combine(
                    starShipRepository.starShips,
                    enginesRepository.getEnginesForShip(shipId),
                    fittingsRepository.getFittingForShip(shipId)
                ) { ships, engines, fitting ->
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
                        fitting = fitting,
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
                // If removing a power plant, check if we need to adjust other engines
                if (engine.type == EngineType.POWER_PLANT) {
                    adjustEnginesAfterPowerPlantRemoval(engine)
                } else {
                    enginesRepository.removeEngine(engine)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to remove engine: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun adjustEnginesAfterPowerPlantRemoval(powerPlantToRemove: Engine) {
        val currentState = _uiState.value
        val currentPowerPlants = currentState.powerPlants
        
        // Only proceed if there are multiple power plants
        if (currentPowerPlants.size <= 1) {
            enginesRepository.removeEngine(powerPlantToRemove)
            return
        }
        
        // Calculate the new maximum power plant performance after removal
        val remainingPowerPlants = currentPowerPlants.filter { it.uid != powerPlantToRemove.uid }
        val newMaxPowerPlantPerformance = remainingPowerPlants.maxOfOrNull { it.performance } ?: 0
        
        // Remove the power plant first
        enginesRepository.removeEngine(powerPlantToRemove)
        
        // Check and adjust jump drives that exceed the new maximum
        val jumpDriveAdjustments = currentState.jumpDrives.filter { 
            it.performance > newMaxPowerPlantPerformance 
        }
        
        // Check and adjust maneuver drives that exceed the new maximum
        val maneuverDriveAdjustments = currentState.maneuverDrives.filter { 
            it.performance > newMaxPowerPlantPerformance 
        }
        
        // Apply adjustments to jump drives
        jumpDriveAdjustments.forEach { jumpDrive ->
            enginesRepository.removeEngine(jumpDrive)
            val adjustedJumpDrive = jumpDrive.copy(performance = newMaxPowerPlantPerformance)
            enginesRepository.addEngine(adjustedJumpDrive)
        }
        
        // Apply adjustments to maneuver drives
        maneuverDriveAdjustments.forEach { maneuverDrive ->
            enginesRepository.removeEngine(maneuverDrive)
            val adjustedManeuverDrive = maneuverDrive.copy(performance = newMaxPowerPlantPerformance)
            enginesRepository.addEngine(adjustedManeuverDrive)
        }
        
        // Show informative message if adjustments were made
        val totalAdjustments = jumpDriveAdjustments.size + maneuverDriveAdjustments.size
        if (totalAdjustments > 0) {
            val message = buildString {
                append("Power plant removed. ")
                append("$totalAdjustments engine(s) adjusted to match new maximum power plant performance ($newMaxPowerPlantPerformance).")
            }
            _uiState.value = _uiState.value.copy(errorMessage = message)
        }
    }

    fun updateEnginePerformance(engine: Engine, newPerformance: Int) {
        viewModelScope.launch {
            try {
                // Remove old engine and add new one with updated performance
                enginesRepository.removeEngine(engine)
                val updatedEngine = engine.copy(performance = newPerformance)
                enginesRepository.addEngine(updatedEngine)
                
                // If we lowered a power plant's performance, check and adjust other engines
                if (engine.type == EngineType.POWER_PLANT && newPerformance < engine.performance) {
                    adjustEnginesAfterPowerPlantPerformanceReduction(newPerformance)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update engine: ${e.message}"
                )
            }
        }
    }

    fun isJumpPerformanceValid(performance: Int): Boolean {
        val ship = _uiState.value.ship ?: return false
        val maxPowerPlantPerformance = getMaxPowerPlantPerformance()
        
        // Jump drives must not exceed max power plant performance AND must be valid for tech level
        return performance <= maxPowerPlantPerformance && 
               isJumpDrivePerformanceValidForTechLevel(performance, ship.techLevel)
    }
    
    fun isManeuverPerformanceValid(performance: Int): Boolean {
        val maxPowerPlantPerformance = getMaxPowerPlantPerformance()
        
        // Maneuver drives must not exceed max power plant performance
        return performance <= maxPowerPlantPerformance
    }
    
    private fun getMaxPowerPlantPerformance(): Int {
        return _uiState.value.powerPlants.maxOfOrNull { it.performance } ?: 0
    }
    
    private suspend fun adjustEnginesAfterPowerPlantPerformanceReduction(newMaxPowerPlantPerformance: Int) {
        val currentState = _uiState.value
        
        // Check and adjust jump drives that exceed the new maximum
        val jumpDriveAdjustments = currentState.jumpDrives.filter { 
            it.performance > newMaxPowerPlantPerformance 
        }
        
        // Check and adjust maneuver drives that exceed the new maximum
        val maneuverDriveAdjustments = currentState.maneuverDrives.filter { 
            it.performance > newMaxPowerPlantPerformance 
        }
        
        // Apply adjustments to jump drives
        jumpDriveAdjustments.forEach { jumpDrive ->
            enginesRepository.removeEngine(jumpDrive)
            val adjustedJumpDrive = jumpDrive.copy(performance = newMaxPowerPlantPerformance)
            enginesRepository.addEngine(adjustedJumpDrive)
        }
        
        // Apply adjustments to maneuver drives
        maneuverDriveAdjustments.forEach { maneuverDrive ->
            enginesRepository.removeEngine(maneuverDrive)
            val adjustedManeuverDrive = maneuverDrive.copy(performance = newMaxPowerPlantPerformance)
            enginesRepository.addEngine(adjustedManeuverDrive)
        }
        
        // Show informative message if adjustments were made
        val totalAdjustments = jumpDriveAdjustments.size + maneuverDriveAdjustments.size
        if (totalAdjustments > 0) {
            val message = buildString {
                append("Power plant performance reduced. ")
                append("$totalAdjustments engine(s) adjusted to match new maximum power plant performance ($newMaxPowerPlantPerformance).")
            }
            _uiState.value = _uiState.value.copy(errorMessage = message)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}