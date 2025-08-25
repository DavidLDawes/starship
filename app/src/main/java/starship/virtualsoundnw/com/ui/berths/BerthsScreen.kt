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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.CrewMember
import starship.virtualsoundnw.com.data.local.database.CrewManifest
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
                    
                    uiState.crewManifest?.let { crewManifest ->
                        item {
                            CrewManifestPanel(crewManifest = crewManifest)
                        }
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
                text = "Crew & Berths Configuration",
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
private fun CrewManifestPanel(crewManifest: CrewManifest) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Crew Manifest",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Total Crew: ${crewManifest.totalCrewCount}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show crew by category
            listOf(
                "Bridge Crew" to crewManifest.bridgeCrew,
                "Engine Crew" to crewManifest.engineCrew,
                "Weapons Crew" to crewManifest.weaponsCrew,
                "Defense Crew" to crewManifest.defenseCrew,
                "Cargo Crew" to crewManifest.cargoCrew,
                "Vehicle Crew" to crewManifest.vehicleCrew,
                "Drone Crew" to crewManifest.droneCrew,
                "Berths Crew" to crewManifest.berthsCrew
            ).forEach { (category, crew) ->
                if (crew.isNotEmpty()) {
                    CrewCategorySection(category = category, crew = crew)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun CrewCategorySection(category: String, crew: List<CrewMember>) {
    Column {
        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        crew.forEach { crewMember ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = crewMember.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
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