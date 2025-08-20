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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import starship.virtualsoundnw.com.data.local.database.ArmorType
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

@Composable
fun DefensesScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    viewModel: DefensesViewModel = hiltViewModel(),
    onNavigateToWeapons: (Int) -> Unit = {}
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
                        onArmorTypeChange = viewModel::updateArmorType,
                        onProtectionChange = viewModel::updateArmorProtection
                    )
                }
                
                item {
                    DefensesSummaryCard(uiState)
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmorConfigurationCard(
    uiState: DefensesUiState,
    onArmorTypeChange: (ArmorType) -> Unit,
    onProtectionChange: (Int) -> Unit
) {
    var armorTypeExpanded by remember { mutableStateOf(false) }
    
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
            
            if (uiState.availableArmorTypes.isNotEmpty()) {
                // Armor Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = armorTypeExpanded,
                    onExpandedChange = { armorTypeExpanded = !armorTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.getCurrentArmorType().displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Armor Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = armorTypeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = armorTypeExpanded,
                        onDismissRequest = { armorTypeExpanded = false }
                    ) {
                        uiState.availableArmorTypes.forEach { armorType ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(armorType.displayName)
                                        Text(
                                            "TL ${armorType.requiredTechLevel.name}+, ${armorType.protectionPer5Percent} protection per 5%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onArmorTypeChange(armorType)
                                    armorTypeExpanded = false
                                }
                            )
                        }
                    }
                }
                
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
            } else {
                Text(
                    text = "No armor types available for this tech level",
                    style = MaterialTheme.typography.bodyMedium,
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
                Text("Armor Tonnage:")
                Text("${String.format("%.2f", uiState.getArmorTonnage())} tons")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Armor Cost:")
                Text("${String.format("%.2f", uiState.getArmorCost())} MCr")
            }
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