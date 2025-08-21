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

package starship.virtualsoundnw.com.ui.defenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.DefensesRepository
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.local.database.Defense
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.ArmorType
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.DefensesCalculation
import javax.inject.Inject

/**
 * UI state for the Defenses screen
 */
data class DefensesUiState(
    val ship: StarShip? = null,
    val defense: Defense? = null,
    val maxArmorProtection: Int = 0,
    val maxScreenQuantities: Triple<Int, Int, Int> = Triple(0, 0, 0), // Nuclear Damper, Meson Screen, Black Globe
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Get current armor tonnage
     */
    fun getArmorTonnage(): Float = defense?.let { d ->
        ship?.let { s -> d.getArmorTonnage(s.tons, s.techLevel) }
    } ?: 0f
    
    /**
     * Get current armor cost
     */
    fun getArmorCost(): Float = defense?.let { d ->
        ship?.let { s -> d.getArmorCost(s.tons, s.configuration, s.techLevel) }
    } ?: 0f
    
    /**
     * Get current armor protection level
     */
    fun getCurrentArmorProtection(): Int = defense?.armorProtection ?: 0
    
    /**
     * Get automatically selected armor type based on ship's tech level
     */
    fun getCurrentArmorType(): ArmorType = ship?.let { s ->
        defense?.getArmorType(s.techLevel) ?: 
        (if (s.techLevel >= TechLevel.E) ArmorType.BONDED_SUPERDENSE else ArmorType.CRYSTALIRON)
    } ?: ArmorType.CRYSTALIRON
    
    /**
     * Get current screen tonnage
     */
    fun getScreenTonnage(): Float = defense?.let { d ->
        ship?.let { s -> d.getScreenTonnage(s.hullCode) }
    } ?: 0f
    
    /**
     * Get current screen cost
     */
    fun getScreenCost(): Float = defense?.let { d ->
        ship?.let { s -> d.getScreenCost(s.hullCode) }
    } ?: 0f
    
    /**
     * Get current nuclear damper count
     */
    fun getCurrentNuclearDampers(): Int = defense?.nuclearDampers ?: 0
    
    /**
     * Get current meson screen count
     */
    fun getCurrentMesonScreens(): Int = defense?.mesonScreens ?: 0
    
    /**
     * Get current black globe count
     */
    fun getCurrentBlackGlobes(): Int = defense?.blackGlobes ?: 0
    
    /**
     * Check if ship is a capital ship (required for screens)
     */
    fun isCapitalShip(): Boolean = ship?.tons?.let { it > 2000 } ?: false
}

@HiltViewModel
class DefensesViewModel @Inject constructor(
    private val defensesRepository: DefensesRepository,
    private val starShipRepository: StarShipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DefensesUiState(isLoading = true))
    val uiState: StateFlow<DefensesUiState> = _uiState.asStateFlow()

    private var currentShipId: Int = -1

    fun loadDefensesForShip(shipId: Int) {
        currentShipId = shipId
        
        viewModelScope.launch {
            try {
                // Combine ship data and defenses
                combine(
                    starShipRepository.starShips,
                    defensesRepository.getDefenseForShip(shipId)
                ) { ships, defense ->
                    val ship = ships.find { it.uid == shipId }
                    
                    val maxArmorProtection = ship?.let { s ->
                        defense?.getMaxArmorProtection(s.techLevel) ?: 
                        Defense(shipId, 0).getMaxArmorProtection(s.techLevel)
                    } ?: 0
                    
                    val maxScreenQuantities = ship?.let { s ->
                        defense?.getMaxScreenQuantities(s.techLevel) ?: 
                        Defense(shipId, 0).getMaxScreenQuantities(s.techLevel)
                    } ?: Triple(0, 0, 0)
                    
                    DefensesUiState(
                        ship = ship,
                        defense = defense,
                        maxArmorProtection = maxArmorProtection,
                        maxScreenQuantities = maxScreenQuantities,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load defenses data: ${e.message}"
                )
            }
        }
    }

    fun updateArmorProtection(protection: Int) {
        viewModelScope.launch {
            try {
                // Preserve existing screen values when updating armor
                val currentDefense = _uiState.value.defense
                defensesRepository.updateDefense(
                    shipId = currentShipId,
                    armorProtection = protection,
                    nuclearDampers = currentDefense?.nuclearDampers,
                    mesonScreens = currentDefense?.mesonScreens,
                    blackGlobes = currentDefense?.blackGlobes
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update armor protection: ${e.message}"
                )
            }
        }
    }
    
    fun updateNuclearDampers(count: Int) {
        viewModelScope.launch {
            try {
                val currentDefense = _uiState.value.defense
                defensesRepository.updateDefense(
                    shipId = currentShipId,
                    armorProtection = currentDefense?.armorProtection,
                    nuclearDampers = count,
                    mesonScreens = currentDefense?.mesonScreens,
                    blackGlobes = currentDefense?.blackGlobes
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update nuclear dampers: ${e.message}"
                )
            }
        }
    }
    
    fun updateMesonScreens(count: Int) {
        viewModelScope.launch {
            try {
                val currentDefense = _uiState.value.defense
                defensesRepository.updateDefense(
                    shipId = currentShipId,
                    armorProtection = currentDefense?.armorProtection,
                    nuclearDampers = currentDefense?.nuclearDampers,
                    mesonScreens = count,
                    blackGlobes = currentDefense?.blackGlobes
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update meson screens: ${e.message}"
                )
            }
        }
    }
    
    fun updateBlackGlobes(count: Int) {
        viewModelScope.launch {
            try {
                val currentDefense = _uiState.value.defense
                defensesRepository.updateDefense(
                    shipId = currentShipId,
                    armorProtection = currentDefense?.armorProtection,
                    nuclearDampers = currentDefense?.nuclearDampers,
                    mesonScreens = currentDefense?.mesonScreens,
                    blackGlobes = count
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update black globes: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}