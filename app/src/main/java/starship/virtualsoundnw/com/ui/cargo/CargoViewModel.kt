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
import starship.virtualsoundnw.com.data.local.database.CargoType
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.CargoRepository
import starship.virtualsoundnw.com.data.ShipSummaryService
import javax.inject.Inject

/**
 * UI state for cargo with ship summary integration and all cargo types
 */
data class CargoUiState(
    val shipSummary: ShipSummary? = null,
    val cargoTons: Int = 0,
    val sparesTons: Int = 0,
    val coldStorageTons: Int = 0,
    val securedCargoTons: Int = 0,
    val xenoCargoTons: Int = 0,
    val maxCargoTons: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val ship: StarShip? get() = shipSummary?.ship
    val isCargoEditingDisabled: Boolean get() = shipSummary?.let { it.remainingTonnage <= 0 } ?: false
    
    /**
     * Check if a specific cargo type can be edited when ship is over-tonnage
     * Allow reducing non-zero cargo types to free up tonnage
     */
    fun canEditCargoType(cargoType: CargoType): Boolean {
        val remainingTonnage = shipSummary?.remainingTonnage ?: 0.0
        if (remainingTonnage > 0) return true // Normal editing when tonnage available
        
        // When over-tonnage, only allow editing cargo types that have current allocation > 0
        return when (cargoType) {
            CargoType.CARGO -> cargoTons > 0
            CargoType.SPARES -> sparesTons > 0
            CargoType.COLD_STORAGE -> coldStorageTons > 0
            CargoType.SECURED_CARGO -> securedCargoTons > 0
            CargoType.XENO_CARGO -> xenoCargoTons > 0
        }
    }
    val serviceIntervalMonths: Int get() = shipSummary?.let { summary -> 
        if (summary.ship.tons > 0) {
            val sparesPercentage = (sparesTons.toFloat() / summary.ship.tons) * 100
            1 + sparesPercentage.toInt()
        } else {
            1
        }
    } ?: 1
    
    /**
     * Get maximum spares tonnage in 1% ship tonnage units
     */
    val maxSparesTons: Int get() = ship?.let { it.tons / 100 * 100 } ?: 0
    
    /**
     * Get available tonnage for a specific cargo type
     * When ship is over-tonnage, only allow reducing current allocations
     */
    fun getAvailableTonnageFor(cargoType: CargoType): Int {
        val currentTotalTonnage = cargoTons + sparesTons + coldStorageTons + securedCargoTons + xenoCargoTons
        val currentTypeTonnage = when (cargoType) {
            CargoType.CARGO -> cargoTons
            CargoType.SPARES -> sparesTons
            CargoType.COLD_STORAGE -> coldStorageTons
            CargoType.SECURED_CARGO -> securedCargoTons
            CargoType.XENO_CARGO -> xenoCargoTons
        }
        
        val remainingTonnage = shipSummary?.remainingTonnage ?: 0.0
        
        // When ship is over-tonnage, only allow reducing current allocation
        if (remainingTonnage <= 0) {
            return currentTypeTonnage
        }
        
        // Normal case: ship has available tonnage
        // Special constraint for Spares: maximum 11% of ship tonnage (for service every 12 months)
        if (cargoType == CargoType.SPARES) {
            val maxSparesAllowed = ship?.let { (it.tons * 0.11).toInt() } ?: 0
            val availableFromGeneralTonnage = maxCargoTons - (currentTotalTonnage - currentTypeTonnage)
            return minOf(maxSparesAllowed, availableFromGeneralTonnage)
        }
        
        return maxCargoTons - (currentTotalTonnage - currentTypeTonnage)
    }
    
    /**
     * Get step size for spares (1% of ship tonnage)
     */
    val sparesStepSize: Int get() = ship?.let { maxOf(1, it.tons / 100) } ?: 1
}

@HiltViewModel
class CargoViewModel @Inject constructor(
    private val starShipRepository: StarShipRepository,
    private val cargoRepository: CargoRepository,
    private val shipSummaryService: ShipSummaryService
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
                shipSummaryService.getComprehensiveShipSummary(shipId).collect { shipSummary ->
                    if (shipSummary == null) {
                        _uiState.value = CargoUiState(
                            isLoading = false,
                            errorMessage = "Ship with ID $shipId not found"
                        )
                        return@collect
                    }
                    
                    // Get current cargo data from the ship summary
                    val currentCargo = cargoRepository.getCargoForShip(shipSummary.ship.uid).first()
                    
                    // Calculate maximum cargo tonnage based on remaining tonnage
                    val actualRemainingTonnage = shipSummary.remainingTonnage + shipSummary.cargoTonnage
                    
                    val maxCargoTons = if (actualRemainingTonnage <= 0) {
                        0
                    } else {
                        // Normal case: use remaining tonnage, fallback to 80% if needed
                        val remainingTonnage = if (shipSummary.remainingTonnage > 0) {
                            actualRemainingTonnage.toInt()
                        } else {
                            (shipSummary.ship.tons * 0.8).toInt()
                        }
                        maxOf(0, remainingTonnage)
                    }
                    
                    _uiState.value = CargoUiState(
                        shipSummary = shipSummary,
                        cargoTons = currentCargo?.cargoTons ?: 0,
                        sparesTons = currentCargo?.sparesTons ?: 0,
                        coldStorageTons = currentCargo?.coldStorageTons ?: 0,
                        securedCargoTons = currentCargo?.securedCargoTons ?: 0,
                        xenoCargoTons = currentCargo?.xenoCargoTons ?: 0,
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
     * Update cargo tonnage for a specific cargo type
     */
    fun updateCargoTonnage(cargoType: CargoType, newTons: Int) {
        val currentState = _uiState.value
        val ship = currentState.ship ?: return
        
        // Don't allow updates if this specific cargo type can't be edited
        if (!currentState.canEditCargoType(cargoType)) {
            return
        }
        
        // Special handling for spares - must be in units of 1% ship tonnage
        val clampedTons = if (cargoType == CargoType.SPARES) {
            val stepSize = currentState.sparesStepSize
            val maxSpares = currentState.getAvailableTonnageFor(cargoType)
            // Round to nearest step size and clamp to available tonnage
            val roundedTons = (newTons / stepSize) * stepSize
            roundedTons.coerceIn(0, maxSpares)
        } else {
            newTons.coerceIn(0, currentState.getAvailableTonnageFor(cargoType))
        }
        
        viewModelScope.launch {
            try {
                // Update specific cargo type in the database
                cargoRepository.updateCargoTonnage(
                    shipId = ship.uid,
                    cargoType = cargoType,
                    newTons = clampedTons
                )
                
                // Update the UI state immediately for better responsiveness
                _uiState.value = when (cargoType) {
                    CargoType.CARGO -> currentState.copy(cargoTons = clampedTons)
                    CargoType.SPARES -> currentState.copy(sparesTons = clampedTons)
                    CargoType.COLD_STORAGE -> currentState.copy(coldStorageTons = clampedTons)
                    CargoType.SECURED_CARGO -> currentState.copy(securedCargoTons = clampedTons)
                    CargoType.XENO_CARGO -> currentState.copy(xenoCargoTons = clampedTons)
                }
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    errorMessage = "Failed to update ${cargoType.displayName.lowercase()}: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Legacy method for backward compatibility - updates regular cargo
     */
    fun updateCargoTonnage(newTons: Int) {
        updateCargoTonnage(CargoType.CARGO, newTons)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}