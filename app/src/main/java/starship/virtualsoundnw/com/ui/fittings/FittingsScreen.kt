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

package starship.virtualsoundnw.com.ui.fittings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import starship.virtualsoundnw.com.data.local.database.Configuration
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.SensorType
import starship.virtualsoundnw.com.data.local.database.ComputerModel
import starship.virtualsoundnw.com.data.local.database.Fitting
import starship.virtualsoundnw.com.ui.components.ComprehensiveShipSummaryPanel
import starship.virtualsoundnw.com.ui.components.ShipSummaryData
import starship.virtualsoundnw.com.ui.components.CrewSummaryPanel
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

@Composable
fun FittingsScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    viewModel: FittingsViewModel = hiltViewModel(),
    onNavigateToEngines: (Int) -> Unit = {},
    onNavigateToWeapons: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(shipId) {
        viewModel.loadFittingsForShip(shipId)
    }
    
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Box(modifier = modifier) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            uiState.ship?.let { ship ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FittingsHeader(ship = ship)
                    }
                    
                    item {
                        SensorSection(
                            currentSensorType = uiState.getCurrentSensorType(),
                            onSensorTypeSelected = viewModel::updateSensorType
                        )
                    }
                    
                    item {
                        ComputerSection(
                            currentComputerModel = uiState.getCurrentComputerModel(),
                            availableComputers = uiState.availableComputers,
                            onComputerModelSelected = viewModel::updateComputerModel
                        )
                    }
                    
                    item {
                        BridgeSection(
                            ship = ship,
                            uiState = uiState
                        )
                    }
                    
                    item {
                        FittingsSummaryPanel(
                            ship = ship,
                            uiState = uiState
                        )
                    }
                    
                    // Fittings Crew Summary (Issue #76 comment)
                    item {
                        CrewSummaryPanel(
                            title = "Fittings Crew",
                            crewMembers = uiState.bridgeCrew
                        )
                    }
                    
                    item {
                        uiState.shipSummary?.let { shipSummary ->
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
                                    dronesCost = shipSummary.dronesCost
                                )
                            )
                        }
                    }
                    
                    item {
                        NavigationButtons(
                            shipId = shipId,
                            onNavigateToEngines = onNavigateToEngines,
                            onNavigateToWeapons = onNavigateToWeapons
                        )
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun FittingsHeader(ship: StarShip) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ship Fittings",
                style = MaterialTheme.typography.headlineSmall
            )
            val shipDesignation = if (ship.isCapitalShip) "Capital Ship" else "Ship"
            Text(
                text = "$shipDesignation: ${ship.name}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${ship.tons} tons • TL ${ship.techLevel} • ${if (ship.isCapitalShip) "Capital Ship" else "Standard Ship"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorSection(
    currentSensorType: SensorType,
    onSensorTypeSelected: (SensorType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sensors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = currentSensorType.name.replace("_", " ").lowercase()
                        .split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sensor Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    SensorType.values().forEach { sensorType ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(
                                        sensorType.name.replace("_", " ").lowercase()
                                            .split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }
                                    )
                                    Text(
                                        text = "${sensorType.tons} tons • ${sensorType.cost} MCr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onSensorTypeSelected(sensorType)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Current sensor info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${currentSensorType.tons} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${currentSensorType.cost} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComputerSection(
    currentComputerModel: ComputerModel,
    availableComputers: List<ComputerModel>,
    onComputerModelSelected: (ComputerModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Computer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = currentComputerModel.model,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Computer Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableComputers.forEach { computer ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(computer.model)
                                    Text(
                                        text = "Rating ${computer.rating} • TL ${computer.requiredTechLevel} • ${computer.cost} MCr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onComputerModelSelected(computer)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Current computer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rating:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${currentComputerModel.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${currentComputerModel.cost} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BridgeSection(
    ship: StarShip,
    uiState: FittingsUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Bridge",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Automatically calculated as 0.5% of ship tonnage",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", uiState.getBridgeTonnage())} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.2f", uiState.getBridgeCost())} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FittingsSummaryPanel(
    ship: StarShip,
    uiState: FittingsUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Fittings Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sensors:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", uiState.getSensorTonnage())} tons • ${String.format("%.2f", uiState.getSensorCost())} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Computer:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "0 tons • ${String.format("%.1f", uiState.getComputerCost())} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bridge:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", uiState.getBridgeTonnage())} tons • ${String.format("%.2f", uiState.getBridgeCost())} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Fittings:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%.1f", uiState.getTotalFittingsTonnage())} tons • ${String.format("%.1f", uiState.getTotalFittingsCost())} MCr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun NavigationButtons(
    shipId: Int,
    onNavigateToEngines: (Int) -> Unit,
    onNavigateToWeapons: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onNavigateToEngines(shipId) }
        ) {
            Text("Back: Engines")
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Button(
            onClick = { onNavigateToWeapons(shipId) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Next: Weapons")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FittingsScreenPreview() {
    MyApplicationTheme {
        val sampleShip = StarShip(
            "Constitution",
            "Constitution class",
            400,
            TechLevel.G,
            Configuration.STANDARD
        )
        val sampleFitting = Fitting(
            shipId = 1,
            sensorType = SensorType.ADVANCED,
            computerModel = ComputerModel.CORE_3
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FittingsHeader(ship = sampleShip)
            }
            
            item {
                SensorSection(
                    currentSensorType = sampleFitting.sensorType,
                    onSensorTypeSelected = { }
                )
            }
            
            item {
                ComputerSection(
                    currentComputerModel = sampleFitting.computerModel,
                    availableComputers = listOf(ComputerModel.CORE_1, ComputerModel.CORE_2, ComputerModel.CORE_3),
                    onComputerModelSelected = { }
                )
            }
            
            item {
                BridgeSection(
                    ship = sampleShip,
                    uiState = FittingsUiState(
                        ship = sampleShip,
                        fitting = sampleFitting
                    )
                )
            }
            
            item {
                NavigationButtons(
                    shipId = 1,
                    onNavigateToEngines = { },
                    onNavigateToWeapons = { }
                )
            }
        }
    }
}