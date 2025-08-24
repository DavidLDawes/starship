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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.Vehicle
import starship.virtualsoundnw.com.data.local.database.VehicleWithAllocation
import starship.virtualsoundnw.com.ui.components.ComprehensiveShipSummaryPanel
import starship.virtualsoundnw.com.ui.components.ShipSummaryData
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

@Composable
fun VehiclesScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    onNavigateToCargo: (Int) -> Unit = {},
    onNavigateToDrones: (Int) -> Unit = {},
    viewModel: VehiclesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(shipId) {
        viewModel.loadVehiclesForShip(shipId)
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
                        VehiclesConfigurationHeader(ship = shipSummary.ship)
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
                                dronesCost = shipSummary.dronesCost
                            )
                        )
                    }
                    
                    item {
                        VehiclesManagementPanel(
                            vehiclesWithAllocations = uiState.vehiclesWithAllocations,
                            totalVehicleCount = uiState.totalVehicleCount,
                            totalVehicleTonnage = uiState.totalVehicleTonnage,
                            totalVehicleCostMCr = uiState.totalVehicleCostMCr,
                            onAddVehicle = { viewModel.showAddVehicleDialog() },
                            onIncrementVehicle = viewModel::incrementVehicle,
                            onDecrementVehicle = viewModel::decrementVehicle
                        )
                    }
                    
                    item {
                        VehiclesNavigationButtons(
                            shipId = shipId,
                            onNavigateToCargo = onNavigateToCargo,
                            onNavigateToDrones = onNavigateToDrones
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
        
        // Add Vehicle Dialog
        if (uiState.showAddVehicleDialog) {
            AddVehicleDialog(
                availableVehicles = uiState.availableVehicles,
                onDismiss = { viewModel.hideAddVehicleDialog() },
                onAddVehicle = { vehicleId ->
                    viewModel.addVehicle(vehicleId)
                    viewModel.hideAddVehicleDialog()
                }
            )
        }
    }
}

@Composable
fun VehiclesConfigurationHeader(ship: StarShip) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Vehicles Configuration",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Ship: ${ship.name}",
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

@Composable
fun VehiclesManagementPanel(
    vehiclesWithAllocations: List<VehicleWithAllocation>,
    totalVehicleCount: Int,
    totalVehicleTonnage: Float,
    totalVehicleCostMCr: Float,
    onAddVehicle: () -> Unit,
    onIncrementVehicle: (Int) -> Unit,
    onDecrementVehicle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Add Vehicle button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vehicles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                
                FilledTonalButton(
                    onClick = onAddVehicle
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Vehicles")
                }
            }
            
            // Summary
            if (totalVehicleCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total: $totalVehicleCount vehicles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", totalVehicleTonnage)} tons • ${String.format("%.1f", totalVehicleCostMCr)} MCr",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Vehicles List
            if (vehiclesWithAllocations.filter { it.isAllocated }.isNotEmpty()) {
                HorizontalDivider()
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    vehiclesWithAllocations.filter { it.isAllocated }.forEach { vehicleWithAllocation ->
                        VehicleAllocationItem(
                            vehicleWithAllocation = vehicleWithAllocation,
                            onIncrement = { onIncrementVehicle(vehicleWithAllocation.vehicle.uid) },
                            onDecrement = { onDecrementVehicle(vehicleWithAllocation.vehicle.uid) }
                        )
                    }
                }
            } else {
                Text(
                    text = "No vehicles configured. Use 'Add Vehicles' to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun VehicleAllocationItem(
    vehicleWithAllocation: VehicleWithAllocation,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vehicle info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = vehicleWithAllocation.vehicle.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${vehicleWithAllocation.vehicle.tons} tons • ${String.format("%.1f", vehicleWithAllocation.vehicle.getCostMCr())} MCr each",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Quantity controls
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDecrement) {
                Text("-", style = MaterialTheme.typography.titleMedium)
            }
            
            Text(
                text = "${vehicleWithAllocation.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            
            IconButton(onClick = onIncrement) {
                Text("+", style = MaterialTheme.typography.titleMedium)
            }
        }
        
        // Extended totals
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${String.format("%.1f", vehicleWithAllocation.extendedTonnage)} tons",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.1f", vehicleWithAllocation.extendedCostMCr)} MCr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddVehicleDialog(
    availableVehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onAddVehicle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Vehicles")
        },
        text = {
            if (availableVehicles.isEmpty()) {
                Text("No vehicles are available for this ship's tech level.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableVehicles) { vehicle ->
                        OutlinedButton(
                            onClick = { onAddVehicle(vehicle.uid) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = vehicle.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${vehicle.tons} tons • ${String.format("%.1f", vehicle.getCostMCr())} MCr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        modifier = modifier
    )
}

@Composable
fun VehiclesNavigationButtons(
    shipId: Int,
    onNavigateToCargo: (Int) -> Unit,
    onNavigateToDrones: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onNavigateToCargo(shipId) }
        ) {
            Text("Back: Cargo")
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Button(
            onClick = { onNavigateToDrones(shipId) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Next: Drones")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VehiclesScreenPreview() {
    MyApplicationTheme {
        VehiclesScreen(shipId = 1)
    }
}