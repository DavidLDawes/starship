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

package starship.virtualsoundnw.com.ui.drones

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
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
import starship.virtualsoundnw.com.ui.components.ComprehensiveShipSummaryPanel
import starship.virtualsoundnw.com.ui.components.ShipSummaryData
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

@Composable
fun DronesScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    onNavigateToVehicles: (Int) -> Unit = {},
    onNavigateToBerths: (Int) -> Unit = {},
    viewModel: DronesViewModel = hiltViewModel()
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DronesManagementPanel(
                        dronesWithAllocations = uiState.dronesWithAllocations,
                        availableDrones = uiState.availableDrones,
                        totalCount = uiState.totalDroneCount,
                        totalTonnage = uiState.totalDroneTonnage,
                        totalCost = uiState.totalDroneCostMCr,
                        onShowAddDroneDialog = viewModel::showAddDroneDialog,
                        onIncrementDrone = viewModel::incrementDrone,
                        onDecrementDrone = viewModel::decrementDrone
                    )
                }
                
                uiState.shipSummary?.let { shipSummary ->
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
                
                item {
                    DronesNavigationButtons(
                        shipId = shipId,
                        onNavigateToVehicles = onNavigateToVehicles,
                        onNavigateToBerths = onNavigateToBerths
                    )
                }
            }
        }
        
        // Add Drone Dialog
        if (uiState.showAddDroneDialog) {
            AddDroneDialog(
                availableDrones = uiState.availableDrones,
                onDroneSelected = { droneId ->
                    viewModel.addDrone(droneId)
                    viewModel.hideAddDroneDialog()
                },
                onDismiss = viewModel::hideAddDroneDialog
            )
        }
        
        // Error Dialog
        uiState.errorMessage?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = viewModel::clearError,
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = viewModel::clearError) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun DronesManagementPanel(
    dronesWithAllocations: List<starship.virtualsoundnw.com.data.local.database.DroneWithAllocation>,
    availableDrones: List<starship.virtualsoundnw.com.data.local.database.Drone>,
    totalCount: Int,
    totalTonnage: Float,
    totalCost: Float,
    onShowAddDroneDialog: () -> Unit,
    onIncrementDrone: (Int) -> Unit,
    onDecrementDrone: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Add Drone button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Drones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                
                FilledTonalButton(
                    onClick = onShowAddDroneDialog,
                    enabled = availableDrones.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Drones")
                }
            }
            
            // Summary
            if (totalCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total: $totalCount drones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", totalTonnage)} tons • ${String.format("%.3f", totalCost)} MCr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Drone List - Only show drones with quantity > 0
            val activeDrones = dronesWithAllocations.filter { it.quantity > 0 }
            if (activeDrones.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "No drones configured. Use 'Add Drone' to get started.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeDrones.forEach { droneWithAllocation ->
                        DroneAllocationItem(
                            droneWithAllocation = droneWithAllocation,
                            onIncrement = { onIncrementDrone(droneWithAllocation.drone.uid) },
                            onDecrement = { onDecrementDrone(droneWithAllocation.drone.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DroneAllocationItem(
    droneWithAllocation: starship.virtualsoundnw.com.data.local.database.DroneWithAllocation,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Drone info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = droneWithAllocation.drone.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${droneWithAllocation.drone.tons} tons • ${String.format("%.3f", droneWithAllocation.drone.costMCr)} MCr each",
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
                text = "${droneWithAllocation.quantity}",
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
                text = "${String.format("%.1f", droneWithAllocation.extendedTonnage)} tons",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.3f", droneWithAllocation.extendedCostMCr)} MCr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddDroneDialog(
    availableDrones: List<starship.virtualsoundnw.com.data.local.database.Drone>,
    onDroneSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedDrone by remember { mutableStateOf<starship.virtualsoundnw.com.data.local.database.Drone?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Drone") },
        text = {
            Column {
                Text(
                    text = "Select a drone type to add to your ship:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Dropdown for drone selection
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedDrone?.name ?: "Choose Drone Type",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableDrones.forEach { drone ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = drone.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${drone.tons} tons, ${String.format("%.3f", drone.costMCr)} MCr",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedDrone = drone
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedDrone?.let { drone ->
                        onDroneSelected(drone.uid)
                    }
                },
                enabled = selectedDrone != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DronesNavigationButtons(
    shipId: Int,
    onNavigateToVehicles: (Int) -> Unit,
    onNavigateToBerths: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onNavigateToVehicles(shipId) }
        ) {
            Text("Back: Vehicles")
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Button(
            onClick = { onNavigateToBerths(shipId) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Next: Berths")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DronesScreenPreview() {
    MyApplicationTheme {
        DronesScreen(shipId = 1)
    }
}