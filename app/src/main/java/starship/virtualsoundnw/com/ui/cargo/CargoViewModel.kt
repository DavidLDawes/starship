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

package starship.virtualsoundnw.com.ui.cargo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.CargoRepository
import javax.inject.Inject

/**
 * UI state for cargo with ship summary integration
 */
data class CargoUiState(
    val shipSummary: ShipSummary? = null,
    val cargoTons: Int = 0,
    val maxCargoTons: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val ship: StarShip? get() = shipSummary?.ship
    val isCargoEditingDisabled: Boolean get() = shipSummary?.let { it.remainingTonnage <= 0 } ?: false
}

@HiltViewModel
class CargoViewModel @Inject constructor(
    private val starShipRepository: StarShipRepository,
    private val cargoRepository: CargoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CargoUiState())
    val uiState: StateFlow<CargoUiState> = _uiState.asStateFlow()

    /**
     * Load ship summary and initialize cargo UI
     */
    fun loadCargoForShip(shipId: Int) {
        _uiState.value = CargoUiState(isLoading = true)
        
        viewModelScope.launch {
            try {
                starShipRepository.getShipSummary(shipId).collect { shipSummary ->
                    if (shipSummary == null) {
                        _uiState.value = CargoUiState(
                            isLoading = false,
                            errorMessage = "Ship with ID $shipId not found"
                        )
                        return@collect
                    }
                    
                    // Calculate maximum cargo tonnage based on remaining tonnage
                    val actualRemainingTonnage = shipSummary.remainingTonnage + shipSummary.cargoTonnage
                    
                    val (cargoTons, maxCargoTons) = if (actualRemainingTonnage <= 0) {
                        // If no remaining tonnage, clear cargo in database and disable editing
                        if (shipSummary.cargoTonnage > 0) {
                            cargoRepository.updateCargo(shipId = shipSummary.ship.uid, cargoTons = 0)
                        }
                        0 to 0
                    } else {
                        // Normal case: use remaining tonnage, fallback to 80% if needed
                        val remainingTonnage = if (shipSummary.remainingTonnage > 0) {
                            actualRemainingTonnage.toInt()
                        } else {
                            (shipSummary.ship.tons * 0.8).toInt()
                        }
                        val maxTons = maxOf(0, remainingTonnage)
                        shipSummary.cargoTonnage to maxTons
                    }
                    
                    _uiState.value = CargoUiState(
                        shipSummary = shipSummary,
                        cargoTons = cargoTons,
                        maxCargoTons = maxCargoTons,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = CargoUiState(
                    isLoading = false,
                    errorMessage = "Failed to load ship: ${e.message}"
                )
            }
        }
    }

    /**
     * Update cargo tonnage
     */
    fun updateCargoTonnage(newTons: Int) {
        val currentState = _uiState.value
        val ship = currentState.ship ?: return
        
        // Don't allow updates if cargo editing is disabled (remaining tonnage <= 0)
        if (currentState.isCargoEditingDisabled) {
            return
        }
        
        val clampedTons = newTons.coerceIn(0, currentState.maxCargoTons)
        
        viewModelScope.launch {
            try {
                // Update cargo in the database - for now just update regular cargo
                cargoRepository.updateCargo(
                    shipId = ship.uid,
                    cargoTons = clampedTons
                )
                
                // The UI state will be updated automatically through the ship summary flow
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    errorMessage = "Failed to update cargo: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}