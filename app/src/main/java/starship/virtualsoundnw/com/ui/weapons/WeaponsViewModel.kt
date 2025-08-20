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

package starship.virtualsoundnw.com.ui.weapons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.WeaponsRepository
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.data.local.database.Weapon
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.WeaponType
import starship.virtualsoundnw.com.data.local.database.TurretType
import starship.virtualsoundnw.com.data.local.database.calculateMaxHardpoints
import javax.inject.Inject

/**
 * UI state for the Weapons screen
 */
data class WeaponsUiState(
    val ship: StarShip? = null,
    val weapons: List<Weapon> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Calculate maximum hardpoints for this ship
     */
    fun getMaxHardpoints(): Int = ship?.let { calculateMaxHardpoints(it.tons) } ?: 0
    
    /**
     * Calculate used hardpoints (one per weapon)
     */
    fun getUsedHardpoints(): Int = weapons.size
    
    /**
     * Calculate remaining hardpoints
     */
    fun getRemainingHardpoints(): Int = getMaxHardpoints() - getUsedHardpoints()
    
    /**
     * Check if another weapon can be added
     */
    fun canAddWeapon(): Boolean = getRemainingHardpoints() > 0
    
    /**
     * Calculate total weapons cost
     */
    fun getTotalWeaponsCost(): Float = weapons.sumOf { it.getTotalCost().toDouble() }.toFloat()
    
    /**
     * Calculate total weapons tonnage
     */
    fun getTotalWeaponsTonnage(): Float = weapons.sumOf { it.getTotalTonnage().toDouble() }.toFloat()
    
    /**
     * Group weapons by turret type
     */
    fun getWeaponsByTurretType(): Map<TurretType, List<Weapon>> {
        return weapons.groupBy { it.turretType }
    }
    
    /**
     * Group weapons by weapon type
     */
    fun getWeaponsByWeaponType(): Map<WeaponType, List<Weapon>> {
        return weapons.groupBy { it.weaponType }
    }
    
    /**
     * Get count of weapons by turret and weapon type combination
     */
    fun getWeaponCount(turretType: TurretType, weaponType: WeaponType): Int {
        return weapons.count { it.turretType == turretType && it.weaponType == weaponType }
    }
}

@HiltViewModel
class WeaponsViewModel @Inject constructor(
    private val weaponsRepository: WeaponsRepository,
    private val starShipRepository: StarShipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeaponsUiState(isLoading = true))
    val uiState: StateFlow<WeaponsUiState> = _uiState.asStateFlow()

    private var currentShipId: Int = -1

    fun loadWeaponsForShip(shipId: Int) {
        currentShipId = shipId
        
        viewModelScope.launch {
            try {
                // Combine ship data with weapons data
                combine(
                    starShipRepository.starShips,
                    weaponsRepository.getWeaponsForShip(shipId)
                ) { ships, weapons ->
                    val ship = ships.find { it.uid == shipId }
                    
                    WeaponsUiState(
                        ship = ship,
                        weapons = weapons,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load weapons data: ${e.message}"
                )
            }
        }
    }

    fun addWeapon(turretType: TurretType, weaponType: WeaponType) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                
                // Check hardpoint constraint
                if (!currentState.canAddWeapon()) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Cannot add weapon: Maximum hardpoints (${currentState.getMaxHardpoints()}) reached"
                    )
                    return@launch
                }
                
                val weapon = Weapon(
                    shipId = currentShipId,
                    turretType = turretType,
                    weaponType = weaponType
                )
                weaponsRepository.addWeapon(weapon)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add weapon: ${e.message}"
                )
            }
        }
    }

    fun removeWeapon(weapon: Weapon) {
        viewModelScope.launch {
            try {
                weaponsRepository.removeWeapon(weapon)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to remove weapon: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}