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
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import starship.virtualsoundnw.com.data.local.database.TurretType
import starship.virtualsoundnw.com.data.local.database.WeaponType
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.ui.components.ComprehensiveShipSummaryPanel
import starship.virtualsoundnw.com.ui.components.ShipSummaryData
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

@Composable
fun WeaponsScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    viewModel: WeaponsViewModel = hiltViewModel(),
    onNavigateToFittings: (Int) -> Unit = {},
    onNavigateToDefenses: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(shipId) {
        viewModel.loadWeaponsForShip(shipId)
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Weapons Configuration",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    WeaponsSummaryCard(uiState)
                }
                
                item {
                    AddWeaponCard(
                        onAddWeapon = { turretType, weaponType ->
                            viewModel.addWeapon(turretType, weaponType)
                        },
                        onAddHardpoint = {
                            viewModel.addHardpoint()
                        }
                    )
                }
                
                items(getGroupedWeapons(uiState.weapons)) { weaponGroup ->
                    WeaponGroupCard(
                        weaponGroup = weaponGroup,
                        onIncrease = { viewModel.addWeapon(weaponGroup.turretType, weaponGroup.weaponType) },
                        onDecrease = { 
                            weaponGroup.weapons.firstOrNull()?.let { weapon ->
                                viewModel.removeWeapon(weapon)
                            }
                        }
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
                                dronesCost = shipSummary.dronesCost
                            )
                        )
                    }
                    
                    item {
                        NavigationButtons(
                            shipId = shipId,
                            onNavigateToFittings = onNavigateToFittings,
                            onNavigateToDefenses = onNavigateToDefenses
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

data class WeaponGroup(
    val turretType: TurretType,
    val weaponType: WeaponType,
    val weapons: List<starship.virtualsoundnw.com.data.local.database.Weapon>,
    val count: Int,
    val totalCost: Float,
    val totalTonnage: Float,
    val designation: String
)

fun getGroupedWeapons(weapons: List<starship.virtualsoundnw.com.data.local.database.Weapon>): List<WeaponGroup> {
    return weapons
        .groupBy { it.turretType to it.weaponType }
        .map { (key, weaponList) ->
            val (turretType, weaponType) = key
            WeaponGroup(
                turretType = turretType,
                weaponType = weaponType,
                weapons = weaponList,
                count = weaponList.size,
                totalCost = weaponList.sumOf { it.getTotalCost().toDouble() }.toFloat(),
                totalTonnage = weaponList.sumOf { it.getTotalTonnage().toDouble() }.toFloat(),
                designation = weaponList.first().getDesignation()
            )
        }
        .sortedBy { it.designation }
}

@Composable
fun WeaponsSummaryCard(uiState: WeaponsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Weapons Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hardpoints:")
                Text("${uiState.getUsedHardpoints()}/${uiState.getMaxHardpoints()}")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Cost:")
                Text("${String.format("%.2f", uiState.getTotalWeaponsCost())} MCr")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Tonnage:")
                Text("${String.format("%.1f", uiState.getTotalWeaponsTonnage())} tons")
            }
            
            if (uiState.getRemainingHardpoints() == 0) {
                Text(
                    text = "Maximum hardpoints reached",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeaponCard(
    onAddWeapon: (TurretType, WeaponType) -> Unit,
    onAddHardpoint: () -> Unit
) {
    var selectedTurretType by remember { mutableStateOf<TurretType?>(null) }
    var selectedWeaponType by remember { mutableStateOf<WeaponType?>(null) }
    var turretExpanded by remember { mutableStateOf(false) }
    var weaponExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Weapon/Hardpoint",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Turret Type Dropdown
            ExposedDropdownMenuBox(
                expanded = turretExpanded,
                onExpandedChange = { turretExpanded = !turretExpanded }
            ) {
                OutlinedTextField(
                    value = selectedTurretType?.let { getTurretDisplayName(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Turret Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = turretExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = turretExpanded,
                    onDismissRequest = { turretExpanded = false }
                ) {
                    TurretType.entries.forEach { turretType ->
                        DropdownMenuItem(
                            text = { Text(getTurretDisplayName(turretType)) },
                            onClick = {
                                selectedTurretType = turretType
                                turretExpanded = false
                                // Reset weapon selection when turret changes
                                selectedWeaponType = null
                            }
                        )
                    }
                }
            }
            
            // Weapon Type Dropdown - only show when non-hardpoint turret is selected
            if (selectedTurretType != null && selectedTurretType != TurretType.HARDPOINT) {
                ExposedDropdownMenuBox(
                    expanded = weaponExpanded,
                    onExpandedChange = { weaponExpanded = !weaponExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedWeaponType?.let { getWeaponDisplayName(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Weapon Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = weaponExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = weaponExpanded,
                        onDismissRequest = { weaponExpanded = false }
                    ) {
                        WeaponType.entries.filter { it != WeaponType.NONE }.forEach { weaponType ->
                            DropdownMenuItem(
                                text = { Text(getWeaponDisplayName(weaponType)) },
                                onClick = {
                                    selectedWeaponType = weaponType
                                    weaponExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Add button - logic depends on selected turret type
            when {
                selectedTurretType == TurretType.HARDPOINT -> {
                    Button(
                        onClick = {
                            onAddHardpoint()
                            selectedTurretType = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add hardpoint"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Hardpoint")
                    }
                }
                selectedTurretType != null && selectedWeaponType != null -> {
                    Button(
                        onClick = {
                            selectedTurretType?.let { turret ->
                                selectedWeaponType?.let { weapon ->
                                    onAddWeapon(turret, weapon)
                                    // Reset selections for next add
                                    selectedTurretType = null
                                    selectedWeaponType = null
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add weapon"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Weapon")
                    }
                }
                else -> {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add weapon"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (selectedTurretType == null) "Select Turret Type" 
                            else "Select Weapon Type"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeaponGroupCard(
    weaponGroup: WeaponGroup,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = weaponGroup.designation,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format("%.2f", weaponGroup.totalCost)} MCr, ${String.format("%.1f", weaponGroup.totalTonnage)} tons",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        enabled = weaponGroup.count > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Decrease quantity"
                        )
                    }
                    
                    Text(
                        text = weaponGroup.count.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = onIncrease
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity"
                        )
                    }
                }
            }
        }
    }
}

fun getTurretDisplayName(turretType: TurretType): String {
    return when (turretType) {
        TurretType.HARDPOINT -> "Hardpoint"
        TurretType.SINGLE -> "Single Turret"
        TurretType.DOUBLE -> "Double Turret"
        TurretType.TRIPLE -> "Triple Turret"
        TurretType.POPUP_SINGLE -> "Single Pop-up Turret"
        TurretType.POPUP_DOUBLE -> "Double Pop-up Turret"
        TurretType.POPUP_TRIPLE -> "Triple Pop-up Turret"
    }
}

@Composable
fun NavigationButtons(
    shipId: Int,
    onNavigateToFittings: (Int) -> Unit,
    onNavigateToDefenses: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onNavigateToFittings(shipId) }
        ) {
            Text("Back: Fittings")
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Button(
            onClick = { onNavigateToDefenses(shipId) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Next: Defenses")
        }
    }
}


fun getWeaponDisplayName(weaponType: WeaponType): String {
    return when (weaponType) {
        WeaponType.NONE -> "None"
        WeaponType.PULSE_LASER -> "Pulse Laser"
        WeaponType.BEAM_LASER -> "Beam Laser"
        WeaponType.PARTICLE_BEAM -> "Particle Beam"
        WeaponType.MISSILE_RACK -> "Missile Rack"
        WeaponType.SANDCASTER -> "Sandcaster"
    }
}

@Preview(showBackground = true)
@Composable
private fun WeaponsScreenPreview() {
    MyApplicationTheme {
        WeaponsScreen(
            shipId = 1,
            onNavigateToFittings = { },
            onNavigateToDefenses = { }
        )
    }
}