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

package starship.virtualsoundnw.com.ui.cargo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.CargoType
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.ui.components.ComprehensiveShipSummaryPanel
import starship.virtualsoundnw.com.ui.components.ShipSummaryData
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CargoScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    onNavigateToDefenses: (Int) -> Unit = {},
    onNavigateToVehicles: (Int) -> Unit = {},
    viewModel: CargoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(shipId) {
        viewModel.loadCargoForShip(shipId)
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Show loading or error states first
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        if (uiState.errorMessage != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Error: ${uiState.errorMessage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        // Cargo Configuration comes first (when ship data is available)
        if (uiState.ship != null && !uiState.isLoading) {
            item {
                // Cargo configuration
                CargoConfigurationCard(
                    uiState = uiState,
                    onCargoUpdate = { cargoType, newTons -> 
                        viewModel.updateCargoTonnage(cargoType, newTons) 
                    }
                )
            }
        }
        
        // Ship Summary comes second (below cargo configuration)
        if (uiState.shipSummary != null && !uiState.isLoading) {
            item {
                ComprehensiveShipSummaryPanel(
                    summaryData = ShipSummaryData(
                        ship = uiState.shipSummary!!.ship,
                        enginesTonnage = uiState.shipSummary!!.enginesTonnage,
                        enginesCost = uiState.shipSummary!!.enginesCost,
                        fuelTonnage = uiState.shipSummary!!.fuelTonnage,
                        weaponsTonnage = uiState.shipSummary!!.weaponsTonnage,
                        weaponsCost = uiState.shipSummary!!.weaponsCost,
                        defensesTonnage = uiState.shipSummary!!.defensesTonnage,
                        defensesCost = uiState.shipSummary!!.defensesCost,
                        fittingsTonnage = uiState.shipSummary!!.fittingsTonnage,
                        fittingsCost = uiState.shipSummary!!.fittingsCost,
                        cargoTonnage = uiState.shipSummary!!.cargoTonnage.toDouble(),
                        cargoCost = uiState.shipSummary!!.cargoCost,
                        vehiclesTonnage = uiState.shipSummary!!.vehiclesTonnage,
                        vehiclesCost = uiState.shipSummary!!.vehiclesCost,
                        dronesTonnage = uiState.shipSummary!!.dronesTonnage,
                        dronesCost = uiState.shipSummary!!.dronesCost
                    )
                )
            }
        }
        
        item {
            // Navigation buttons
            CargoNavigationButtons(
                shipId = shipId,
                onNavigateToDefenses = onNavigateToDefenses,
                onNavigateToVehicles = onNavigateToVehicles
            )
        }
    }
}

@Composable
fun CargoConfigurationCard(
    uiState: CargoUiState,
    onCargoUpdate: (CargoType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cargo Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (uiState.isCargoEditingDisabled) {
                Text(
                    text = "Cargo editing disabled: No remaining ship tonnage available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Regular Cargo
            CargoTypeSlider(
                cargoType = CargoType.CARGO,
                currentTons = uiState.cargoTons,
                maxTons = uiState.getAvailableTonnageFor(CargoType.CARGO),
                enabled = !uiState.isCargoEditingDisabled,
                onValueChange = onCargoUpdate
            )
            
            // Spares with special 1% ship tonnage units
            CargoTypeSlider(
                cargoType = CargoType.SPARES,
                currentTons = uiState.sparesTons,
                maxTons = uiState.getAvailableTonnageFor(CargoType.SPARES),
                stepSize = uiState.sparesStepSize,
                enabled = !uiState.isCargoEditingDisabled,
                onValueChange = onCargoUpdate,
                extraInfo = "Service every ${uiState.serviceIntervalMonths} months"
            )
            
            // Cold Storage
            CargoTypeSlider(
                cargoType = CargoType.COLD_STORAGE,
                currentTons = uiState.coldStorageTons,
                maxTons = uiState.getAvailableTonnageFor(CargoType.COLD_STORAGE),
                enabled = !uiState.isCargoEditingDisabled,
                onValueChange = onCargoUpdate
            )
            
            // Secured Cargo
            CargoTypeSlider(
                cargoType = CargoType.SECURED_CARGO,
                currentTons = uiState.securedCargoTons,
                maxTons = uiState.getAvailableTonnageFor(CargoType.SECURED_CARGO),
                enabled = !uiState.isCargoEditingDisabled,
                onValueChange = onCargoUpdate
            )
            
            // Xeno Cargo
            CargoTypeSlider(
                cargoType = CargoType.XENO_CARGO,
                currentTons = uiState.xenoCargoTons,
                maxTons = uiState.getAvailableTonnageFor(CargoType.XENO_CARGO),
                enabled = !uiState.isCargoEditingDisabled,
                onValueChange = onCargoUpdate
            )
        }
    }
}

@Composable
fun CargoTypeSlider(
    cargoType: CargoType,
    currentTons: Int,
    maxTons: Int,
    stepSize: Int = 1,
    enabled: Boolean = true,
    extraInfo: String? = null,
    onValueChange: (CargoType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header row with type name and current/max tons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${cargoType.displayName}:",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$currentTons / $maxTons tons",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Cost information
        if (currentTons > 0) {
            val cost = when (cargoType) {
                CargoType.CARGO -> 0f
                CargoType.SPARES -> currentTons * cargoType.costPerTon
                CargoType.COLD_STORAGE -> cargoType.baseCost + currentTons * cargoType.costPerTon
                CargoType.SECURED_CARGO -> cargoType.baseCost + currentTons * cargoType.costPerTon
                CargoType.XENO_CARGO -> cargoType.baseCost + currentTons * cargoType.costPerTon
            }
            Text(
                text = "Cost: ${String.format(Locale("EN"), "%.3f", cost)} MCr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Extra info (e.g., service interval for spares)
        extraInfo?.let { info ->
            Text(
                text = info,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        
        // Slider
        if (maxTons > 0) {
            val steps = if (stepSize > 1) {
                maxOf(0, (maxTons / stepSize) - 1)
            } else {
                if (maxTons > 1) maxTons - 1 else 0
            }
            
            Slider(
                value = currentTons.toFloat(),
                onValueChange = { value ->
                    if (enabled) {
                        val newTons = if (stepSize > 1) {
                            // Round to nearest step size
                            ((value.roundToInt() + stepSize / 2) / stepSize) * stepSize
                        } else {
                            value.roundToInt()
                        }
                        onValueChange(cargoType, newTons.coerceIn(0, maxTons))
                    }
                },
                valueRange = 0f..maxTons.toFloat(),
                steps = steps,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "No tonnage available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CargoNavigationButtons(
    shipId: Int,
    onNavigateToDefenses: (Int) -> Unit,
    onNavigateToVehicles: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onNavigateToDefenses(shipId) }
        ) {
            Text("Back: Defenses")
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Button(
            onClick = { onNavigateToVehicles(shipId) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Next: Vehicles")
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun CargoScreenPreview() {
    MyApplicationTheme {
        CargoScreen(shipId = 1)
    }
}