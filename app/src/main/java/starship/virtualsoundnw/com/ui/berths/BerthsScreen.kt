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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.BerthType
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration
import starship.virtualsoundnw.com.ui.components.ComprehensiveShipSummaryPanel
import starship.virtualsoundnw.com.ui.components.ShipSummaryData
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

@Composable
fun BerthsScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    viewModel: BerthsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(shipId) {
        viewModel.loadDataForShip(shipId)
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            uiState.shipSummary?.let { shipSummary ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        BerthsConfigurationHeader(ship = shipSummary.ship)
                    }
                    
                    item {
                        BerthsConfigurationCard(
                            uiState = uiState,
                            onBerthUpdate = { berthType, newCount -> 
                                viewModel.updateBerthCount(berthType, newCount) 
                            }
                        )
                    }
                    
                    item {
                        ComprehensiveShipSummaryPanel(
                            summaryData = ShipSummaryData(
                                ship = shipSummary.ship,
                                enginesTonnage = shipSummary.enginesTonnage,
                                enginesCost = shipSummary.enginesCost,
                                fuelTonnage = shipSummary.fuelTonnage,
                                weaponsTonnage = shipSummary.weaponsTonnage,
                                weaponsCost = shipSummary.weaponsCost,
                                defensesTonnage = shipSummary.defensesTonnage,
                                defensesCost = shipSummary.defensesCost,
                                fittingsTonnage = shipSummary.fittingsTonnage,
                                fittingsCost = shipSummary.fittingsCost,
                                cargoTonnage = shipSummary.cargoTonnage.toDouble(),
                                cargoCost = shipSummary.cargoCost,
                                vehiclesTonnage = shipSummary.vehiclesTonnage,
                                vehiclesCost = shipSummary.vehiclesCost,
                                dronesTonnage = shipSummary.dronesTonnage,
                                dronesCost = shipSummary.dronesCost,
                                berthsTonnage = shipSummary.berthsTonnage,
                                berthsCost = shipSummary.berthsCost
                            )
                        )
                    }
                }
            } ?: run {
                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BerthsConfigurationHeader(ship: StarShip) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Berths Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${ship.name} (${ship.tons} tons, TL ${ship.techLevel})",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BerthsConfigurationCard(
    uiState: BerthsUiState,
    onBerthUpdate: (BerthType, Int) -> Unit,
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
                text = "Berths Allocation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (uiState.isBerthEditingDisabled) {
                Text(
                    text = "Berths editing disabled: No remaining ship tonnage available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Staterooms
            BerthTypeSlider(
                berthType = BerthType.STATEROOMS,
                currentCount = uiState.staterooms,
                maxCount = uiState.getAvailableTonnageFor(BerthType.STATEROOMS),
                enabled = !uiState.isBerthEditingDisabled,
                onValueChange = onBerthUpdate
            )
            
            // Luxury Staterooms
            BerthTypeSlider(
                berthType = BerthType.LUXURY_STATEROOMS,
                currentCount = uiState.luxuryStaterooms,
                maxCount = uiState.getAvailableTonnageFor(BerthType.LUXURY_STATEROOMS),
                enabled = !uiState.isBerthEditingDisabled,
                onValueChange = onBerthUpdate
            )
            
            // Low Passage
            BerthTypeSlider(
                berthType = BerthType.LOW_PASSAGE,
                currentCount = uiState.lowPassage,
                maxCount = uiState.getAvailableTonnageFor(BerthType.LOW_PASSAGE),
                enabled = !uiState.isBerthEditingDisabled,
                onValueChange = onBerthUpdate
            )
            
            // Emergency Low
            BerthTypeSlider(
                berthType = BerthType.EMERGENCY_LOW,
                currentCount = uiState.emergencyLow,
                maxCount = uiState.getAvailableTonnageFor(BerthType.EMERGENCY_LOW),
                enabled = !uiState.isBerthEditingDisabled,
                onValueChange = onBerthUpdate
            )
        }
    }
}

@Composable
fun BerthTypeSlider(
    berthType: BerthType,
    currentCount: Int,
    maxCount: Int,
    enabled: Boolean = true,
    onValueChange: (BerthType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header row with type name and current/max count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${berthType.displayName}:",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$currentCount / $maxCount berths",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Tonnage and cost information
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${berthType.tonnage} tons each",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (currentCount > 0) {
                val totalCost = currentCount * berthType.costMCr
                Text(
                    text = "Cost: ${String.format(Locale("EN"), "%.3f", totalCost)} MCr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Total tonnage used by this berth type
        if (currentCount > 0) {
            val totalTonnage = currentCount * berthType.tonnage
            Text(
                text = "Total: ${totalTonnage} tons",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        
        // Slider
        if (maxCount > 0) {
            val steps = if (maxCount > 1) maxCount - 1 else 0
            
            Slider(
                value = currentCount.toFloat(),
                onValueChange = { value ->
                    if (enabled) {
                        val newCount = value.roundToInt().coerceIn(0, maxCount)
                        onValueChange(berthType, newCount)
                    }
                },
                valueRange = 0f..maxCount.toFloat(),
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

@Preview(showBackground = true)
@Composable
private fun BerthsScreenPreview() {
    MyApplicationTheme {
        BerthsScreen(shipId = 1)
    }
}