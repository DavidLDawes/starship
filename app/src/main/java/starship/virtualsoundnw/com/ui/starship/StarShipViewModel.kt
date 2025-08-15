/*
 * Copyright (C) 2022 The Android Open Source Project
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

package starship.virtualsoundnw.com.ui.starship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import starship.virtualsoundnw.com.data.StarShipRepository
import starship.virtualsoundnw.com.ui.starship.StarShipUiState.Error
import starship.virtualsoundnw.com.ui.starship.StarShipUiState.Loading
import starship.virtualsoundnw.com.ui.starship.StarShipUiState.Success
import javax.inject.Inject

@HiltViewModel
class StarShipViewModel @Inject constructor(
    private val starShipRepository: StarShipRepository
) : ViewModel() {

    val uiState: StateFlow<StarShipUiState> = starShipRepository
        .starShips.map<List<String>, StarShipUiState>(::Success)
        .catch { emit(Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    fun addStarShip(name: String) {
        viewModelScope.launch {
            starShipRepository.add(name)
        }
    }
}

sealed interface StarShipUiState {
    object Loading : StarShipUiState
    data class Error(val throwable: Throwable) : StarShipUiState
    data class Success(val data: List<String>) : StarShipUiState
}
