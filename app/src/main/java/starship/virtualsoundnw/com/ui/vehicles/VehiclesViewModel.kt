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

package starship.virtualsoundnw.com.ui.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.ShipSummaryService
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.VehiclesRepository
import starship.virtualsoundnw.com.data.VehiclesDataService
import starship.virtualsoundnw.com.data.CrewCalculationService
import starship.virtualsoundnw.com.data.local.database.CrewMember
import starship.virtualsoundnw.com.data.local.database.CrewManifest
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.Vehicle
import starship.virtualsoundnw.com.data.local.database.VehicleWithAllocation
import javax.inject.Inject

/**
 * UI state for the Vehicles screen
 */
data class VehiclesUiState(
    val ship: StarShip? = null,
    val availableVehicles: List<Vehicle> = emptyList(),
    val vehiclesWithAllocations: List<VehicleWithAllocation> = emptyList(),
    val shipSummary: ShipSummary? = null,
    val vehiclesCrew: List<CrewMember> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddVehicleDialog: Boolean = false
) {
    val allocatedVehicles: List<VehicleWithAllocation> get() = vehiclesWithAllocations.filter { it.isAllocated }
    val totalVehicleTonnage: Float get() = allocatedVehicles.sumOf { it.extendedTonnage.toDouble() }.toFloat()
    val totalVehicleCostMCr: Float get() = allocatedVehicles.sumOf { it.extendedCostMCr.toDouble() }.toFloat()
    val totalVehicleCount: Int get() = allocatedVehicles.sumOf { it.quantity }
}

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val starShipRepository: StarShipRepository,
    private val vehiclesRepository: VehiclesRepository,
    private val shipSummaryService: ShipSummaryService,
    private val vehiclesDataService: VehiclesDataService,
    private val crewCalculationService: CrewCalculationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiclesUiState(isLoading = true))
    val uiState: StateFlow<VehiclesUiState> = _uiState.asStateFlow()
    
    private var currentShipId: Int = -1

    fun loadVehiclesForShip(shipId: Int) {
        currentShipId = shipId
        
        viewModelScope.launch {
            try {
                // Ensure vehicle catalog is populated first
                vehiclesDataService.ensureVehicleCatalogPopulated()
                
                // Use flatMapLatest to get ship first, then combine with vehicles data
                starShipRepository.starShips.flatMapLatest { ships ->
                    val ship = ships.find { it.uid == shipId }
                    if (ship != null) {
                        // Now get vehicles, crew, and summary with correct tech level
                        combine(
                            vehiclesRepository.getVehiclesWithAllocationsForShip(shipId, ship.techLevel),
                            shipSummaryService.getComprehensiveShipSummary(shipId),
                            crewCalculationService.getCrewManifest(shipId)
                        ) { vehiclesWithAllocations, shipSummary, crewManifest ->
                            VehiclesUiState(
                                ship = ship,
                                vehiclesWithAllocations = vehiclesWithAllocations,
                                shipSummary = shipSummary,
                                vehiclesCrew = crewManifest?.vehicleCrew ?: emptyList(),
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    } else {
                        flowOf(
                            VehiclesUiState(
                                isLoading = false,
                                errorMessage = "Ship with ID $shipId not found"
                            )
                        )
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load vehicles: ${e.message}"
                )
            }
        }
    }
    
    private fun getCurrentShipTechLevel() = _uiState.value.ship?.techLevel ?: starship.virtualsoundnw.com.data.local.database.TechLevel.A
    
    fun showAddVehicleDialog() {
        _uiState.value = _uiState.value.copy(showAddVehicleDialog = true)
        
        // Load available vehicles for current ship's tech level
        viewModelScope.launch {
            try {
                // Ensure vehicle catalog is populated
                vehiclesDataService.ensureVehicleCatalogPopulated()
                val ship = _uiState.value.ship
                if (ship != null) {
                    // Use sync version to avoid Flow collection issues
                    val availableVehicles = vehiclesDataService.getAvailableVehiclesForTechLevelSync(ship.techLevel)
                    _uiState.value = _uiState.value.copy(
                        availableVehicles = availableVehicles,
                        errorMessage = null // Clear any previous errors
                    )
                    
                    // If no vehicles are available, provide helpful error message
                    if (availableVehicles.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "No vehicles available for Tech Level ${ship.techLevel}. Available vehicles require higher tech levels."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Cannot load vehicles: Ship data not available"
                    )
                }
            } catch (e: Exception) {
                val ship = _uiState.value.ship
                val debugInfo = if (ship != null) {
                    "Ship TL: ${ship.techLevel}"
                } else {
                    "No ship loaded"
                }
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load available vehicles: ${e.message}. Debug: $debugInfo"
                )
            }
        }
    }
    
    fun hideAddVehicleDialog() {
        _uiState.value = _uiState.value.copy(showAddVehicleDialog = false)
    }
    
    fun addVehicle(vehicleId: Int) {
        viewModelScope.launch {
            try {
                vehiclesRepository.addVehicleToShip(currentShipId, vehicleId, 1)
                // Clear any error messages on success
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add vehicle: ${e.message}"
                )
            }
        }
    }
    
    fun incrementVehicle(vehicleId: Int) {
        viewModelScope.launch {
            try {
                vehiclesRepository.addVehicleToShip(currentShipId, vehicleId, 1)
                // Immediately clear any error messages on successful operation
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to increment vehicle: ${e.message}"
                )
            }
        }
    }
    
    fun decrementVehicle(vehicleId: Int) {
        viewModelScope.launch {
            try {
                vehiclesRepository.removeVehicleFromShip(currentShipId, vehicleId, 1)
                // Immediately clear any error messages on successful operation
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to decrement vehicle: ${e.message}"
                )
            }
        }
    }
    
    fun setVehicleQuantity(vehicleId: Int, quantity: Int) {
        viewModelScope.launch {
            try {
                vehiclesRepository.setVehicleQuantity(currentShipId, vehicleId, quantity)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to set vehicle quantity: ${e.message}"
                )
            }
        }
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Debug method to force repopulate vehicles and show debug info
     */
    fun debugVehiclesPopulation() {
        viewModelScope.launch {
            try {
                // Force populate vehicles
                vehiclesDataService.ensureVehicleCatalogPopulated()
                
                // Get debug info
                val allVehicles = vehiclesDataService.getAllVehiclesForDebugging()
                val ship = _uiState.value.ship
                val availableForShip = if (ship != null) {
                    vehiclesDataService.getAvailableVehiclesForTechLevelSync(ship.techLevel)
                } else {
                    emptyList()
                }
                
                val debugMessage = "Total vehicles: ${allVehicles.size}, Available for ship: ${availableForShip.size}"
                _uiState.value = _uiState.value.copy(
                    errorMessage = debugMessage,
                    availableVehicles = availableForShip
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Debug failed: ${e.message}"
                )
            }
        }
    }
}