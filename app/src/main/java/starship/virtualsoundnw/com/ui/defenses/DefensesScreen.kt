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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import starship.virtualsoundnw.com.data.local.database.ArmorType
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

@Composable
fun DefensesScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    viewModel: DefensesViewModel = hiltViewModel(),
    onNavigateToWeapons: (Int) -> Unit = {},
    onNavigateToCargo: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(shipId) {
        viewModel.loadDefensesForShip(shipId)
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
                        text = "Defenses Configuration",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    ArmorConfigurationCard(
                        uiState = uiState,
                        onProtectionChange = viewModel::updateArmorProtection
                    )
                }
                
                // Only show screens for capital ships
                if (uiState.isCapitalShip()) {
                    item {
                        ScreensConfigurationCard(
                            uiState = uiState,
                            onNuclearDampersChange = viewModel::updateNuclearDampers,
                            onMesonScreensChange = viewModel::updateMesonScreens,
                            onBlackGlobesChange = viewModel::updateBlackGlobes
                        )
                    }
                }
                
                item {
                    DefensesSummaryCard(uiState)
                }
                
                uiState.ship?.let { ship ->
                    item {
                        SummaryPanel(
                            ship = ship,
                            uiState = uiState
                        )
                    }
                }
                
                item {
                    DefensesNavigationButtons(
                        shipId = shipId,
                        onNavigateToWeapons = onNavigateToWeapons,
                        onNavigateToCargo = onNavigateToCargo
                    )
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
fun ArmorConfigurationCard(
    uiState: DefensesUiState,
    onProtectionChange: (Int) -> Unit
) {
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Armor Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Show automatically selected armor type
            val currentArmorType = uiState.getCurrentArmorType()
            Text(
                text = "Armor Type: ${currentArmorType.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "TL ${currentArmorType.requiredTechLevel.name}+, ${currentArmorType.protectionPer5Percent} protection per 5%, ${(currentArmorType.costMultiplier * 100).toInt()}% hull cost",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Armor Protection Slider
            Text(
                text = "Protection Level: ${uiState.getCurrentArmorProtection()} / ${uiState.maxArmorProtection}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Slider(
                value = uiState.getCurrentArmorProtection().toFloat(),
                onValueChange = { onProtectionChange(it.toInt()) },
                valueRange = 0f..uiState.maxArmorProtection.toFloat(),
                steps = if (uiState.maxArmorProtection > 1) uiState.maxArmorProtection - 1 else 0,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (uiState.getCurrentArmorProtection() > 0) {
                Text(
                    text = "Tonnage: ${String.format("%.2f", uiState.getArmorTonnage())} tons",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Cost: ${String.format("%.2f", uiState.getArmorCost())} MCr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ScreensConfigurationCard(
    uiState: DefensesUiState,
    onNuclearDampersChange: (Int) -> Unit,
    onMesonScreensChange: (Int) -> Unit,
    onBlackGlobesChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Screens Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Available only for Capital Ships (>2000 tons)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val (maxNuclear, maxMeson, maxBlack) = uiState.maxScreenQuantities
            
            // Nuclear Dampers
            if (maxNuclear > 0) {
                Text(
                    text = "Nuclear Dampers: ${uiState.getCurrentNuclearDampers()} / $maxNuclear",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = uiState.getCurrentNuclearDampers().toFloat(),
                    onValueChange = { onNuclearDampersChange(it.toInt()) },
                    valueRange = 0f..maxNuclear.toFloat(),
                    steps = if (maxNuclear > 1) maxNuclear - 1 else 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Meson Screens
            if (maxMeson > 0) {
                Text(
                    text = "Meson Screens: ${uiState.getCurrentMesonScreens()} / $maxMeson",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = uiState.getCurrentMesonScreens().toFloat(),
                    onValueChange = { onMesonScreensChange(it.toInt()) },
                    valueRange = 0f..maxMeson.toFloat(),
                    steps = if (maxMeson > 1) maxMeson - 1 else 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Black Globes
            if (maxBlack > 0) {
                Text(
                    text = "Black Globes: ${uiState.getCurrentBlackGlobes()} / $maxBlack",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = uiState.getCurrentBlackGlobes().toFloat(),
                    onValueChange = { onBlackGlobesChange(it.toInt()) },
                    valueRange = 0f..maxBlack.toFloat(),
                    steps = if (maxBlack > 1) maxBlack - 1 else 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Show screen costs if any screens are present
            val screenCost = uiState.getScreenCost()
            val screenTonnage = uiState.getScreenTonnage()
            if (screenCost > 0 || screenTonnage > 0) {
                Text(
                    text = "Total Screen Tonnage: ${String.format("%.2f", screenTonnage)} tons",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total Screen Cost: ${String.format("%.2f", screenCost)} MCr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DefensesSummaryCard(uiState: DefensesUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Defenses Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Armor Protection:")
                Text("${uiState.getCurrentArmorProtection()}")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Armor:")
                Text("${String.format("%.2f", uiState.getArmorTonnage())} tons")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Armor Cost:")
                Text("${String.format("%.2f", uiState.getArmorCost())} MCr")
            }
            
            // Show screen information for capital ships
            if (uiState.isCapitalShip()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Screens:")
                    Text("${String.format("%.2f", uiState.getScreenTonnage())} tons")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Screen Cost:")
                    Text("${String.format("%.2f", uiState.getScreenCost())} MCr")
                }
            }
        }
    }
}

@Composable
fun SummaryPanel(
    ship: StarShip,
    uiState: DefensesUiState
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
            
            val armorTons = uiState.getArmorTonnage()
            val armorCost = uiState.getArmorCost()
            val screenTons = uiState.getScreenTonnage()
            val screenCost = uiState.getScreenCost()
            
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
                    text = "Armor Type:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = uiState.getCurrentArmorType().displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Armor Protection:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${uiState.getCurrentArmorProtection()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Armor Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.2f", armorTons)} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Armor Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.2f", armorCost)} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Show screen information for capital ships
            if (uiState.isCapitalShip()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nuclear Dampers:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${uiState.getCurrentNuclearDampers()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Meson Screens:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${uiState.getCurrentMesonScreens()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Black Globes:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${uiState.getCurrentBlackGlobes()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Screen Tonnage:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${String.format("%.2f", screenTons)} tons",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Screen Cost:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${String.format("%.2f", screenCost)} MCr",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hull Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.2f", ship.hullCost)} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Defenses Cost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${String.format("%.2f", ship.hullCost + armorCost + screenCost)} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DefensesNavigationButtons(
    shipId: Int,
    onNavigateToWeapons: (Int) -> Unit,
    onNavigateToCargo: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onNavigateToWeapons(shipId) }
        ) {
            Text("Back: Weapons")
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Button(
            onClick = { onNavigateToCargo(shipId) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Next: Cargo")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefensesScreenPreview() {
    MyApplicationTheme {
        DefensesScreen(shipId = 1)
    }
}