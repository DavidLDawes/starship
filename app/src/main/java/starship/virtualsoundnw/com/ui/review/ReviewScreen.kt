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

package starship.virtualsoundnw.com.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.ui.components.ComprehensiveShipSummaryPanel
import starship.virtualsoundnw.com.ui.components.ShipSummaryData
import starship.virtualsoundnw.com.ui.components.DetailedShipTable
import starship.virtualsoundnw.com.ui.components.SaveAsDialog
import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme

@Composable
fun ReviewScreen(
    shipId: Int,
    modifier: Modifier = Modifier,
    onNavigateToBerths: (Int) -> Unit = {},
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
                        ReviewHeader(ship = shipSummary.ship)
                    }
                    
                    uiState.detailedTableData?.let { tableData ->
                        item {
                            DetailedShipTable(tableData = tableData)
                        }
                    }
                    
                    item {
                        ReviewActionButtons(
                            onSaveAs = viewModel::showSaveAsDialog,
                            onPrint = { viewModel.printShipDesign(context) }
                        )
                    }
                    
                    item {
                        ReviewNavigationButtons(
                            shipId = shipId,
                            onNavigateToBerths = onNavigateToBerths
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
        
        // Save As Dialog
        if (uiState.showSaveAsDialog) {
            uiState.ship?.let { ship ->
                SaveAsDialog(
                    currentShipName = ship.name,
                    isLoading = uiState.saveAsLoading,
                    errorMessage = uiState.saveAsError,
                    onSave = viewModel::saveAs,
                    onCancel = viewModel::hideSaveAsDialog
                )
            }
        }
    }
}

@Composable
private fun ReviewHeader(ship: StarShip) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ship Review",
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
private fun ReviewActionButtons(
    onSaveAs: () -> Unit,
    onPrint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = onSaveAs,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Save As...")
                }
                
                Button(
                    onClick = onPrint,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Print")
                }
            }
        }
    }
}

@Composable
fun ReviewNavigationButtons(
    shipId: Int,
    onNavigateToBerths: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        OutlinedButton(
            onClick = { onNavigateToBerths(shipId) }
        ) {
            Text("Back: Berths")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewScreenPreview() {
    MyApplicationTheme {
        ReviewScreen(shipId = 1)
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewActionButtonsPreview() {
    MyApplicationTheme {
        ReviewActionButtons(
            onSaveAs = {},
            onPrint = {}
        )
    }
}