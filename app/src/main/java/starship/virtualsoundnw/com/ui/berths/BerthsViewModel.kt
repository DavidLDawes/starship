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

package starship.virtualsoundnw.com.ui.berths

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
import starship.virtualsoundnw.com.data.BerthsRepository
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.CrewCalculationService
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.Berths
import starship.virtualsoundnw.com.data.local.database.BerthType
import starship.virtualsoundnw.com.data.local.database.CrewManifest
import javax.inject.Inject

/**
 * UI state for the Berths screen
 */
data class BerthsUiState(
    val ship: StarShip? = null,
    val berths: Berths? = null,
    val shipSummary: ShipSummary? = null,
    val crewManifest: CrewManifest? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    // Helper properties for berth counts
    val staterooms: Int get() = berths?.staterooms ?: 0
    val luxuryStaterooms: Int get() = berths?.luxuryStaterooms ?: 0
    val lowPassage: Int get() = berths?.lowPassage ?: 0
    val emergencyLow: Int get() = berths?.emergencyLow ?: 0
    
    // Helper properties for tonnage calculations
    private val remainingTonnage: Int get() = shipSummary?.remainingTonnage?.toInt() ?: 0
    private val currentBerthsTonnage: Float get() = berths?.getTotalTonnage() ?: 0f
    
    /**
     * Get available tonnage for a specific berth type including current allocation
     */
    fun getAvailableTonnageFor(berthType: BerthType): Int {
        val currentCount = berths?.getCountForBerthType(berthType) ?: 0
        val currentTonnage = currentCount * berthType.tonnage
        val availableTonnage = remainingTonnage + currentTonnage
        return (availableTonnage / berthType.tonnage).toInt()
    }
    
    /**
     * Check if berth editing should be disabled
     */
    val isBerthEditingDisabled: Boolean get() = remainingTonnage <= 0 && (berths?.getTotalTonnage() ?: 0f) == 0f
    
    /**
     * Get total crew count from crew manifest
     */
    val totalCrewCount: Int get() = crewManifest?.totalCrewCount ?: 0
    
    /**
     * Calculate minimum crew berths required (Crew/2 + 1)
     */
    val minimumCrewBerths: Int get() = if (totalCrewCount <= 0) 0 else (totalCrewCount / 2) + 1
    
    /**
     * Get current total crew berths (staterooms + luxury staterooms)
     */
    val currentCrewBerths: Int get() = staterooms + luxuryStaterooms
    
    /**
     * Check if current berths meet minimum crew requirements
     */
    val meetsMinimumCrewBerths: Boolean get() = currentCrewBerths >= minimumCrewBerths
    
    /**
     * Check if a berth count reduction would violate minimum crew berths
     */
    fun wouldViolateMinimumBerths(berthType: BerthType, newCount: Int): Boolean {
        val currentBerths = berths ?: return false
        val testBerths = currentBerths.withUpdatedCount(berthType, newCount)
        return testBerths.getTotalCrewBerths() < minimumCrewBerths
    }
    
    /**
     * Get minimum count for a berth type considering crew requirements
     */
    fun getMinimumCountFor(berthType: BerthType): Int {
        if (minimumCrewBerths <= 0) return 0
        
        return when (berthType) {
            BerthType.STATEROOMS -> {
                // Minimum staterooms = minimum crew berths - luxury staterooms
                val minStaterooms = minimumCrewBerths - luxuryStaterooms
                maxOf(0, minStaterooms)
            }
            BerthType.LUXURY_STATEROOMS -> {
                // Luxury staterooms can be reduced to 0 if there are enough staterooms
                0
            }
            else -> 0 // Low passage and emergency low don't count toward crew berths
        }
    }
}

@HiltViewModel
class BerthsViewModel @Inject constructor(
    private val shipSummaryService: ShipSummaryService,
    private val berthsRepository: BerthsRepository,
    private val starShipRepository: StarShipRepository,
    private val crewCalculationService: CrewCalculationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BerthsUiState(isLoading = true))
    val uiState: StateFlow<BerthsUiState> = _uiState.asStateFlow()

    private var currentShipId: Int = -1

    fun loadDataForShip(shipId: Int) {
        currentShipId = shipId
        
        viewModelScope.launch {
            try {
                combine(
                    starShipRepository.starShips,
                    shipSummaryService.getComprehensiveShipSummary(shipId),
                    berthsRepository.getBerthsForShip(shipId),
                    crewCalculationService.getCrewManifest(shipId)
                ) { ships, shipSummary, berths, crewManifest ->
                    val ship = ships.find { it.uid == shipId }
                    
                    BerthsUiState(
                        ship = ship,
                        berths = berths,
                        shipSummary = shipSummary,
                        crewManifest = crewManifest,
                        isLoading = false
                    )
                }.collect { uiState ->
                    _uiState.value = uiState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load berths data: ${e.message}"
                )
            }
        }
    }
    
    fun updateBerthCount(berthType: BerthType, newCount: Int) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val currentBerths = currentState.berths ?: Berths(shipId = currentShipId)
                val totalCrewCount = currentState.totalCrewCount
                
                // Handle special logic for crew berths (staterooms + luxury staterooms)
                var finalBerths = when (berthType) {
                    BerthType.STATEROOMS -> {
                        // Don't allow reducing staterooms below minimum if at minimum crew berths
                        val minStaterooms = currentState.getMinimumCountFor(BerthType.STATEROOMS)
                        val adjustedCount = maxOf(newCount, minStaterooms)
                        currentBerths.withUpdatedCount(berthType, adjustedCount)
                    }
                    
                    BerthType.LUXURY_STATEROOMS -> {
                        // When reducing luxury staterooms, add staterooms if needed to maintain minimum
                        val tempBerths = currentBerths.withUpdatedCount(berthType, newCount)
                        val minimumCrewBerths = if (totalCrewCount <= 0) 0 else (totalCrewCount / 2) + 1
                        
                        if (tempBerths.getTotalCrewBerths() < minimumCrewBerths) {
                            // Add staterooms to compensate
                            val stateroomsNeeded = minimumCrewBerths - tempBerths.getTotalCrewBerths()
                            tempBerths.copy(staterooms = tempBerths.staterooms + stateroomsNeeded)
                        } else {
                            tempBerths
                        }
                    }
                    
                    else -> {
                        // Low passage and emergency low have no minimum constraints
                        currentBerths.withUpdatedCount(berthType, newCount)
                    }
                }
                
                berthsRepository.insertBerths(finalBerths)
                
                // UI state will be updated automatically through the flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update berths: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}