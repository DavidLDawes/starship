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

package starship.virtualsoundnw.com.ui.drones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.ShipSummaryService
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.DronesRepository
import starship.virtualsoundnw.com.data.DronesDataService
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.local.database.Drone
import starship.virtualsoundnw.com.data.local.database.DroneWithAllocation
import starship.virtualsoundnw.com.data.local.database.StarShip
import javax.inject.Inject

/**
 * UI state for the Drones screen
 */
data class DronesUiState(
    val ship: StarShip? = null,
    val dronesWithAllocations: List<DroneWithAllocation> = emptyList(),
    val availableDrones: List<Drone> = emptyList(),
    val shipSummary: ShipSummary? = null,
    val showAddDroneDialog: Boolean = false,
    val totalDroneCount: Int = 0,
    val totalDroneTonnage: Float = 0f,
    val totalDroneCostMCr: Float = 0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DronesViewModel @Inject constructor(
    private val shipSummaryService: ShipSummaryService,
    private val dronesRepository: DronesRepository,
    private val dronesDataService: DronesDataService,
    private val starShipRepository: StarShipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DronesUiState(isLoading = true))
    val uiState: StateFlow<DronesUiState> = _uiState.asStateFlow()

    private var currentShipId: Int = -1

    fun loadDataForShip(shipId: Int) {
        currentShipId = shipId
        
        viewModelScope.launch {
            try {
                // Ensure drone catalog is populated first
                dronesDataService.ensureDroneCatalogPopulated()
                
                combine(
                    starShipRepository.starShips,
                    shipSummaryService.getComprehensiveShipSummary(shipId),
                    dronesRepository.getAllDrones()
                ) { ships, shipSummary, allDrones ->
                    val ship = ships.find { it.uid == shipId }
                    val availableDrones = allDrones.filter { drone ->
                        ship?.let { drone.isAvailableForTechLevel(it.techLevel) } ?: true
                    }
                    Triple(ship, shipSummary, availableDrones)
                }.collect { (ship, shipSummary, availableDrones) ->
                    if (ship != null) {
                        dronesRepository.getDronesWithAllocationsForShip(shipId, ship.techLevel)
                            .collect { dronesWithAllocations ->
                                val totalCount = dronesWithAllocations.sumOf { it.quantity }
                                val totalTonnage = dronesWithAllocations.sumOf { it.extendedTonnage.toDouble() }.toFloat()
                                val totalCost = dronesWithAllocations.sumOf { it.extendedCostMCr.toDouble() }.toFloat()
                                
                                _uiState.value = DronesUiState(
                                    ship = ship,
                                    dronesWithAllocations = dronesWithAllocations,
                                    availableDrones = availableDrones,
                                    shipSummary = shipSummary,
                                    totalDroneCount = totalCount,
                                    totalDroneTonnage = totalTonnage,
                                    totalDroneCostMCr = totalCost,
                                    isLoading = false
                                )
                            }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load drone data: ${e.message}"
                )
            }
        }
    }
    
    fun showAddDroneDialog() {
        _uiState.value = _uiState.value.copy(showAddDroneDialog = true)
    }
    
    fun hideAddDroneDialog() {
        _uiState.value = _uiState.value.copy(showAddDroneDialog = false)
    }
    
    fun addDrone(droneId: Int) {
        viewModelScope.launch {
            try {
                dronesRepository.addDroneToShip(currentShipId, droneId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add drone: ${e.message}"
                )
            }
        }
    }
    
    fun incrementDrone(droneId: Int) {
        viewModelScope.launch {
            try {
                val currentAllocation = _uiState.value.dronesWithAllocations.find { 
                    it.drone.uid == droneId 
                }
                val newQuantity = (currentAllocation?.quantity ?: 0) + 1
                dronesRepository.updateDroneQuantity(currentShipId, droneId, newQuantity)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to increment drone: ${e.message}"
                )
            }
        }
    }
    
    fun decrementDrone(droneId: Int) {
        viewModelScope.launch {
            try {
                val currentAllocation = _uiState.value.dronesWithAllocations.find { 
                    it.drone.uid == droneId 
                }
                val newQuantity = (currentAllocation?.quantity ?: 1) - 1
                dronesRepository.updateDroneQuantity(currentShipId, droneId, newQuantity)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to decrement drone: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}