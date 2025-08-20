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

package starship.virtualsoundnw.com.ui.fittings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.FittingsRepository
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.EnginesRepository
import starship.virtualsoundnw.com.data.local.database.Fitting
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.Engine
import starship.virtualsoundnw.com.data.local.database.EngineType
import starship.virtualsoundnw.com.data.local.database.SensorType
import starship.virtualsoundnw.com.data.local.database.ComputerModel
import starship.virtualsoundnw.com.data.local.database.FittingsCalculation
import javax.inject.Inject

/**
 * UI state for the Fittings screen
 */
data class FittingsUiState(
    val ship: StarShip? = null,
    val fitting: Fitting? = null,
    val engines: List<Engine> = emptyList(),
    val availableComputers: List<ComputerModel> = emptyList(),
    val maxJumpPerformance: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Get current sensor tonnage
     */
    fun getSensorTonnage(): Float = fitting?.getSensorTonnage() ?: 0f
    
    /**
     * Get current sensor cost
     */
    fun getSensorCost(): Float = fitting?.getSensorCost() ?: 0f
    
    /**
     * Get current computer cost
     */
    fun getComputerCost(): Float = fitting?.getComputerCost() ?: 0f
    
    /**
     * Get bridge tonnage
     */
    fun getBridgeTonnage(): Float = ship?.let { s -> 
        fitting?.getBridgeTonnage(s.tons) ?: (s.tons * 0.005f) 
    } ?: 0f
    
    /**
     * Get bridge cost
     */
    fun getBridgeCost(): Float = ship?.let { s -> 
        fitting?.getBridgeCost(s.tons) ?: (s.tons * 0.005f * 0.1f) 
    } ?: 0f
    
    /**
     * Calculate total fittings tonnage
     */
    fun getTotalFittingsTonnage(): Float = ship?.let { s ->
        fitting?.getTotalTonnage(s.tons) ?: (s.tons * 0.005f) // Just bridge if no fitting
    } ?: 0f
    
    /**
     * Calculate total fittings cost
     */
    fun getTotalFittingsCost(): Float = ship?.let { s ->
        fitting?.getTotalCost(s.tons) ?: (s.tons * 0.005f * 0.1f) // Just bridge if no fitting
    } ?: 0f
    
    /**
     * Get current sensor type
     */
    fun getCurrentSensorType(): SensorType = fitting?.sensorType ?: SensorType.STANDARD
    
    /**
     * Get current computer model
     */
    fun getCurrentComputerModel(): ComputerModel = fitting?.computerModel ?: ComputerModel.CORE_1
}

@HiltViewModel
class FittingsViewModel @Inject constructor(
    private val fittingsRepository: FittingsRepository,
    private val starShipRepository: StarShipRepository,
    private val enginesRepository: EnginesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FittingsUiState(isLoading = true))
    val uiState: StateFlow<FittingsUiState> = _uiState.asStateFlow()

    private var currentShipId: Int = -1

    fun loadFittingsForShip(shipId: Int) {
        currentShipId = shipId
        
        viewModelScope.launch {
            try {
                // Combine ship data, engines, and fittings
                combine(
                    starShipRepository.starShips,
                    enginesRepository.getEnginesForShip(shipId),
                    fittingsRepository.getFittingForShip(shipId)
                ) { ships, engines, fitting ->
                    val ship = ships.find { it.uid == shipId }
                    val maxJumpPerformance = engines
                        .filter { it.type == EngineType.JUMP_DRIVE }
                        .maxOfOrNull { it.performance } ?: 0
                    
                    val availableComputers = ship?.let { s ->
                        ComputerModel.getAvailableModelsForShip(
                            s.tons, 
                            maxJumpPerformance, 
                            s.techLevel
                        )
                    } ?: emptyList()
                    
                    FittingsUiState(
                        ship = ship,
                        fitting = fitting,
                        engines = engines,
                        availableComputers = availableComputers,
                        maxJumpPerformance = maxJumpPerformance,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load fittings data: ${e.message}"
                )
            }
        }
    }

    fun updateSensorType(sensorType: SensorType) {
        viewModelScope.launch {
            try {
                fittingsRepository.updateSensorType(currentShipId, sensorType)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update sensor: ${e.message}"
                )
            }
        }
    }

    fun updateComputerModel(computerModel: ComputerModel) {
        viewModelScope.launch {
            try {
                fittingsRepository.updateComputerModel(currentShipId, computerModel)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update computer: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}