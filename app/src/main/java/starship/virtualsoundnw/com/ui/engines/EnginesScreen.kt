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

package starship.virtualsoundnw.com.ui.engines

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import starship.virtualsoundnw.com.data.local.database.Engine
import starship.virtualsoundnw.com.data.local.database.EngineType
import starship.virtualsoundnw.com.ui.components.ComprehensiveShipSummaryPanel
import starship.virtualsoundnw.com.ui.components.ShipSummaryData
import starship.virtualsoundnw.com.data.local.database.PowerPlantType
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

@Composable
fun EnginesScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    viewModel: EnginesViewModel = hiltViewModel(),
    onNavigateToFittings: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(shipId) {
        viewModel.loadEnginesForShip(shipId)
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
                        EnginesConfigurationHeader(ship = ship)
                    }
                    
                    item {
                        EngineSection(
                            title = "Power Plant",
                            engineType = EngineType.POWER_PLANT,
                            engines = uiState.powerPlants,
                            ship = ship,
                            isCapitalShip = uiState.isCapitalShip(),
                            onAddEngine = viewModel::addEngine,
                            onRemoveEngine = viewModel::removeEngine,
                            onUpdateEnginePerformance = viewModel::updateEnginePerformance,
                            isPerformanceValid = { true },
                            performanceRange = 1..12
                        )
                    }
                    
                    item {
                        EngineSection(
                            title = "Jump Drive",
                            engineType = EngineType.JUMP_DRIVE,
                            engines = uiState.jumpDrives,
                            ship = ship,
                            isCapitalShip = uiState.isCapitalShip(),
                            onAddEngine = viewModel::addEngine,
                            onRemoveEngine = viewModel::removeEngine,
                            onUpdateEnginePerformance = viewModel::updateEnginePerformance,
                            isPerformanceValid = viewModel::isJumpPerformanceValid,
                            performanceRange = 1..12
                        )
                    }
                    
                    item {
                        EngineSection(
                            title = "Maneuver Drive",
                            engineType = EngineType.MANEUVER_DRIVE,
                            engines = uiState.maneuverDrives,
                            ship = ship,
                            isCapitalShip = uiState.isCapitalShip(),
                            onAddEngine = viewModel::addEngine,
                            onRemoveEngine = viewModel::removeEngine,
                            onUpdateEnginePerformance = viewModel::updateEnginePerformance,
                            isPerformanceValid = viewModel::isManeuverPerformanceValid,
                            performanceRange = 0..12
                        )
                    }
                    
                    item {
                        FuelPanel(
                            ship = ship,
                            uiState = uiState
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
                                    vehiclesCost = shipSummary.vehiclesCost
                                )
                            )
                        }
                    }
                    
                    // Next: Fittings button when requirements are met
                    if (uiState.hasRequiredEngines()) {
                        item {
                            Button(
                                onClick = { onNavigateToFittings(shipId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Next: Fittings")
                            }
                        }
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
fun EnginesConfigurationHeader(ship: StarShip) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Engine Configuration",
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
fun EngineSection(
    title: String,
    engineType: EngineType,
    engines: List<Engine>,
    ship: StarShip,
    isCapitalShip: Boolean,
    onAddEngine: (EngineType, Int) -> Unit,
    onRemoveEngine: (Engine) -> Unit,
    onUpdateEnginePerformance: (Engine, Int) -> Unit,
    isPerformanceValid: (Int) -> Boolean,
    performanceRange: IntRange
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedButton(
                    onClick = {
                        val defaultPerformance = if (performanceRange.first == 0) 1 else performanceRange.first
                        onAddEngine(engineType, defaultPerformance)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Engine",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
            
            if (engines.isEmpty()) {
                Text(
                    text = "No ${title.lowercase()} configured",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    engines.forEach { engine ->
                        EngineControlCard(
                            engine = engine,
                            ship = ship,
                            canRemove = engines.size > 1,
                            onRemove = { onRemoveEngine(engine) },
                            onUpdatePerformance = { newPerformance ->
                                onUpdateEnginePerformance(engine, newPerformance)
                            },
                            isPerformanceValid = isPerformanceValid,
                            performanceRange = performanceRange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EngineControlCard(
    engine: Engine,
    ship: StarShip,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onUpdatePerformance: (Int) -> Unit,
    isPerformanceValid: (Int) -> Boolean,
    performanceRange: IntRange
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = getEngineTypeDisplayName(engine.type, ship.techLevel),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Designation: ${engine.getDesignation()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (canRemove) {
                    IconButton(
                        onClick = onRemove
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Engine",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Performance Slider
            Column {
                Text(
                    text = "Performance: ${engine.performance}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = engine.performance.toFloat(),
                    onValueChange = { newValue ->
                        val newPerformance = newValue.roundToInt()
                        if (isPerformanceValid(newPerformance)) {
                            onUpdatePerformance(newPerformance)
                        }
                    },
                    valueRange = performanceRange.first.toFloat()..performanceRange.last.toFloat(),
                    steps = performanceRange.last - performanceRange.first - 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Calculated Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tons: ${String.format("%.1f", engine.getTonnage(ship.tons))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "(${String.format("%.1f", engine.getTonnagePercentage())}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column {
                    Text(
                        text = "Cost: ${String.format("%.1f", engine.getTotalCost(ship.tons, ship.techLevel))} MCr",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "(${String.format("%.1f", engine.getCostPerTon(ship.techLevel))} MCr/ton)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FuelPanel(
    ship: StarShip,
    uiState: EnginesUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Fuel Requirements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            val fuelTons = uiState.getFuelRequirement()
            val maxJumpPerformance = uiState.jumpDrives.maxOfOrNull { it.performance } ?: 0
            val hasAntimatter = uiState.powerPlants.any { 
                PowerPlantType.getBestAvailableForTechLevel(ship.techLevel) == PowerPlantType.ANTIMATTER
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jump Performance:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (maxJumpPerformance > 0) "J-$maxJumpPerformance" else "None",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Power Plant:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (hasAntimatter) "Antimatter (10x efficiency)" else "Standard",
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
                    text = "Fuel Required:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%.1f", fuelTons)} tons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SummaryPanel(
    ship: StarShip,
    uiState: EnginesUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ship Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            val totalEngineTons = uiState.getTotalEngineTonnage()
            val totalEngineCost = uiState.getTotalEngineCost()
            val fuelTons = uiState.getFuelRequirement()
            val fittingsTons = uiState.getFittingsTonnage()
            val fittingsCost = uiState.getFittingsCost()
            val weaponsTons = uiState.getTotalWeaponsTonnage()
            val weaponsCost = uiState.getTotalWeaponsCost()
            val remainingTons = uiState.getRemainingTonnage()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Ship Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${ship.tons} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Engine Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", totalEngineTons)} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fuel Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", fuelTons)} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fittings Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", uiState.getFittingsTonnage())} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Weapons Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", weaponsTons)} tons",
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
                    text = "Remaining Tonnage:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%.1f", remainingTons)} tons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (remainingTons >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hull Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", ship.hullCost)} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Engine Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", totalEngineCost)} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fittings Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.2f", fittingsCost)} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Weapons Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.1f", weaponsCost)} MCr",
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
                    text = "Total Cost:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%.1f", ship.hullCost + totalEngineCost + fittingsCost + weaponsCost)} MCr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun getEngineTypeDisplayName(engineType: EngineType, techLevel: TechLevel): String {
    return when (engineType) {
        EngineType.POWER_PLANT -> {
            val powerPlantType = PowerPlantType.getBestAvailableForTechLevel(techLevel)
            when (powerPlantType) {
                PowerPlantType.FISSION -> "Fission Power Plant"
                PowerPlantType.FUSION -> "Fusion Power Plant"
                PowerPlantType.ADVANCED_FUSION -> "Advanced Fusion Power Plant"
                PowerPlantType.ANTIMATTER -> "Antimatter Power Plant"
            }
        }
        EngineType.JUMP_DRIVE -> "Jump Drive"
        EngineType.MANEUVER_DRIVE -> "Maneuver Drive"
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun EnginesScreenPreview() {
    MyApplicationTheme {
        val sampleShip = StarShip(
            "Enterprise",
            "Constitution class",
            200,
            TechLevel.G,
            Configuration.STANDARD
        )
        val sampleEngines = listOf(
            Engine(1, EngineType.POWER_PLANT, 5),
            Engine(1, EngineType.JUMP_DRIVE, 2),
            Engine(1, EngineType.MANEUVER_DRIVE, 3)
        )
        val uiState = EnginesUiState(
            ship = sampleShip,
            engines = sampleEngines,
            powerPlants = listOf(sampleEngines[0]),
            jumpDrives = listOf(sampleEngines[1]),
            maneuverDrives = listOf(sampleEngines[2])
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EnginesConfigurationHeader(ship = sampleShip)
            }
            
            item {
                EngineSection(
                    title = "Power Plant",
                    engineType = EngineType.POWER_PLANT,
                    engines = uiState.powerPlants,
                    ship = sampleShip,
                    isCapitalShip = uiState.isCapitalShip(),
                    onAddEngine = { _, _ -> },
                    onRemoveEngine = { },
                    onUpdateEnginePerformance = { _, _ -> },
                    isPerformanceValid = { true },
                    performanceRange = 1..12
                )
            }
            
            item {
                FuelPanel(
                    ship = sampleShip,
                    uiState = uiState
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
                            vehiclesCost = shipSummary.vehiclesCost
                        )
                    )
                }
            }
            
            // Next: Fittings button when requirements are met
            if (uiState.hasRequiredEngines()) {
                item {
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next: Fittings")
                    }
                }
            }
        }
    }
}