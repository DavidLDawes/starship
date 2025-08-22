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
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.ShipSummaryService
import javax.inject.Inject

/**
 * UI state for the Vehicles screen
 */
data class VehiclesUiState(
    val shipSummary: ShipSummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    private val shipSummaryService: ShipSummaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiclesUiState())
    val uiState: StateFlow<VehiclesUiState> = _uiState.asStateFlow()

    fun loadVehiclesForShip(shipId: Int) {
        _uiState.value = VehiclesUiState(isLoading = true)
        
        viewModelScope.launch {
            try {
                shipSummaryService.getComprehensiveShipSummary(shipId).collect { shipSummary ->
                    _uiState.value = VehiclesUiState(
                        shipSummary = shipSummary,
                        isLoading = false,
                        errorMessage = if (shipSummary == null) "Ship with ID $shipId not found" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = VehiclesUiState(
                    isLoading = false,
                    errorMessage = "Failed to load ship summary: ${e.message}"
                )
            }
        }
    }
}