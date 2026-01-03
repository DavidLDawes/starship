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

package starship.virtualsoundnw.com.ui.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.CustomRepository
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.ShipSummaryService
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.local.database.CustomItem
import starship.virtualsoundnw.com.data.local.database.StarShip
import javax.inject.Inject

/**
 * UI state for the Custom screen
 */
data class CustomUiState(
    val ship: StarShip? = null,
    val customItems: List<CustomItem> = emptyList(),
    val shipSummary: ShipSummary? = null,
    val showAddCustomItemDialog: Boolean = false,
    val totalCustomTonnage: Float = 0f,
    val totalCustomCostMCr: Float = 0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CustomViewModel @Inject constructor(
    private val shipSummaryService: ShipSummaryService,
    private val customRepository: CustomRepository,
    private val starShipRepository: StarShipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomUiState(isLoading = true))
    val uiState: StateFlow<CustomUiState> = _uiState.asStateFlow()

    private var currentShipId: Int = -1

    fun loadDataForShip(shipId: Int) {
        currentShipId = shipId

        viewModelScope.launch {
            try {
                combine(
                    starShipRepository.starShips,
                    shipSummaryService.getComprehensiveShipSummary(shipId),
                    customRepository.getCustomItemsForShip(shipId)
                ) { ships, shipSummary, customItems ->
                    val ship = ships.find { it.uid == shipId }
                    Triple(ship, shipSummary, customItems)
                }.collect { (ship, shipSummary, customItems) ->
                    if (ship != null) {
                        val totalTonnage = customItems.sumOf { it.tons.toDouble() }.toFloat()
                        val totalCost = customItems.sumOf { it.costMCr.toDouble() }.toFloat()

                        _uiState.value = CustomUiState(
                            ship = ship,
                            customItems = customItems,
                            shipSummary = shipSummary,
                            totalCustomTonnage = totalTonnage,
                            totalCustomCostMCr = totalCost,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load custom item data: ${e.message}"
                )
            }
        }
    }

    fun showAddCustomItemDialog() {
        _uiState.value = _uiState.value.copy(showAddCustomItemDialog = true)
    }

    fun hideAddCustomItemDialog() {
        _uiState.value = _uiState.value.copy(showAddCustomItemDialog = false)
    }

    fun addCustomItem(name: String, tons: Float, costMCr: Float) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Item name cannot be empty"
            )
            return
        }

        if (tons <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Tonnage must be greater than 0"
            )
            return
        }

        if (costMCr < 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Cost cannot be negative"
            )
            return
        }

        viewModelScope.launch {
            try {
                customRepository.addCustomItem(currentShipId, name, tons, costMCr)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add custom item: ${e.message}"
                )
            }
        }
    }

    fun deleteCustomItem(customItem: CustomItem) {
        viewModelScope.launch {
            try {
                customRepository.deleteCustomItem(customItem)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete custom item: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
