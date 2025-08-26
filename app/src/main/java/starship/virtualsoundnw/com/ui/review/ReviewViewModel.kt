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

package starship.virtualsoundnw.com.ui.review

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
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.DetailedShipTableService
import starship.virtualsoundnw.com.data.DetailedShipTableData
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.ui.print.ShipPrintService
import android.content.Context
import javax.inject.Inject

/**
 * UI state for the Review screen
 */
data class ReviewUiState(
    val ship: StarShip? = null,
    val shipSummary: ShipSummary? = null,
    val detailedTableData: DetailedShipTableData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSaveAsDialog: Boolean = false,
    val saveAsLoading: Boolean = false,
    val saveAsError: String? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val shipSummaryService: ShipSummaryService,
    private val starShipRepository: StarShipRepository,
    private val detailedShipTableService: DetailedShipTableService,
    private val shipPrintService: ShipPrintService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState(isLoading = true))
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun loadDataForShip(shipId: Int) {
        viewModelScope.launch {
            try {
                combine(
                    starShipRepository.starShips,
                    shipSummaryService.getComprehensiveShipSummary(shipId),
                    detailedShipTableService.getDetailedShipTable(shipId)
                ) { ships, shipSummary, detailedTableData ->
                    val ship = ships.find { it.uid == shipId }
                    Triple(ship, shipSummary, detailedTableData)
                }.collect { (ship, shipSummary, detailedTableData) ->
                    _uiState.value = ReviewUiState(
                        ship = ship,
                        shipSummary = shipSummary,
                        detailedTableData = detailedTableData,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load review data: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun showSaveAsDialog() {
        _uiState.value = _uiState.value.copy(
            showSaveAsDialog = true,
            saveAsError = null
        )
    }
    
    fun hideSaveAsDialog() {
        _uiState.value = _uiState.value.copy(
            showSaveAsDialog = false,
            saveAsLoading = false,
            saveAsError = null
        )
    }
    
    fun saveAs(newName: String) {
        val currentShip = _uiState.value.ship
        if (currentShip == null) {
            _uiState.value = _uiState.value.copy(
                saveAsError = "No ship selected"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                saveAsLoading = true,
                saveAsError = null
            )
            
            try {
                val result = starShipRepository.saveAs(currentShip.uid, newName)
                if (result.isSuccess) {
                    // Success - close dialog
                    _uiState.value = _uiState.value.copy(
                        showSaveAsDialog = false,
                        saveAsLoading = false,
                        saveAsError = null
                    )
                    // Optionally, you might want to navigate to the new ship or show a success message
                } else {
                    // Handle error
                    _uiState.value = _uiState.value.copy(
                        saveAsLoading = false,
                        saveAsError = result.exceptionOrNull()?.message ?: "Failed to save ship"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveAsLoading = false,
                    saveAsError = e.message ?: "Unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Print the current ship design
     */
    fun printShipDesign(context: Context) {
        val currentShip = _uiState.value.ship
        val currentTableData = _uiState.value.detailedTableData
        
        if (currentShip != null && currentTableData != null) {
            shipPrintService.printShipDesign(context, currentShip.name, currentTableData)
        }
    }
}