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
import starship.virtualsoundnw.com.data.local.database.StarShip
import javax.inject.Inject

/**
 * UI state for the Review screen
 */
data class ReviewUiState(
    val ship: StarShip? = null,
    val shipSummary: ShipSummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val shipSummaryService: ShipSummaryService,
    private val starShipRepository: StarShipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState(isLoading = true))
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun loadDataForShip(shipId: Int) {
        viewModelScope.launch {
            try {
                combine(
                    starShipRepository.starShips,
                    shipSummaryService.getComprehensiveShipSummary(shipId)
                ) { ships, shipSummary ->
                    val ship = ships.find { it.uid == shipId }
                    Pair(ship, shipSummary)
                }.collect { (ship, shipSummary) ->
                    _uiState.value = ReviewUiState(
                        ship = ship,
                        shipSummary = shipSummary,
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
}