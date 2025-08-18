/*
 * Copyright (C) 2022 The Android Open Source Project
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

package starship.virtualsoundnw.com.ui.starship

import starship.virtualsoundnw.com.ui.theme.MyApplicationTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration

@Composable
fun StarShipScreen(modifier: Modifier = Modifier, viewModel: StarShipViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is StarShipUiState.Loading -> {
            Text("Loading...")
        }
        is StarShipUiState.Error -> {
            Text("Error: ${state.throwable.message}")
        }
        is StarShipUiState.Success -> {
            StarShipScreen(
                items = state.data,
                onSave = viewModel::addStarShip,
                modifier = modifier
            )
        }
    }
}

@Composable
internal fun StarShipScreen(
    items: List<StarShip>,
    onSave: (starShip: StarShip) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        // Ship Input Form
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Ship Details",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                ShipInputForm(onSave = onSave)
            }
        }
        
        // Saved Ships List
        Text(
            text = "Saved Ships",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        items.forEach { ship ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Name: ${ship.name}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Description: ${ship.description}")
                    Text(text = "Tonnage: ${ship.tons}")
                    Text(text = "Tech Level: ${ship.techLevel}")
                    Text(text = "Configuration: ${ship.configuration}")
                }
            }
        }
    }
}

@Composable
private fun ShipInputForm(
    onSave: (starShip: StarShip) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tons by remember { mutableStateOf("200") }
    var techLevel by remember { mutableStateOf(TechLevel.C) }
    var configuration by remember { mutableStateOf(Configuration.STANDARD) }
    var techLevelDropdownExpanded by remember { mutableStateOf(false) }
    var configurationDropdownExpanded by remember { mutableStateOf(false) }
    var tonsError by remember { mutableStateOf(false) }
    
    // Validate tons input
    LaunchedEffect(tons) {
        tonsError = tons.toIntOrNull()?.let { it < 100 } ?: true
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ship Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = tons,
            onValueChange = { tons = it },
            label = { Text("Tonnage") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = tonsError,
            supportingText = if (tonsError) { { Text("Minimum 100 tons required") } } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tech Level Dropdown
        Column {
            TextButton(
                onClick = { techLevelDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tech Level: $techLevel")
                    Text("▼")
                }
            }
            DropdownMenu(
                expanded = techLevelDropdownExpanded,
                onDismissRequest = { techLevelDropdownExpanded = false }
            ) {
                TechLevel.entries.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level.name) },
                        onClick = {
                            techLevel = level
                            techLevelDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        // Configuration Dropdown
        Column {
            TextButton(
                onClick = { configurationDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Configuration: ${configuration.displayName()}")
                    Text("▼")
                }
            }
            DropdownMenu(
                expanded = configurationDropdownExpanded,
                onDismissRequest = { configurationDropdownExpanded = false }
            ) {
                Configuration.entries.forEach { config ->
                    DropdownMenuItem(
                        text = { Text(config.displayName()) },
                        onClick = {
                            configuration = config
                            configurationDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Button(
            onClick = {
                if (name.isNotBlank() && description.isNotBlank() && !tonsError) {
                    onSave(
                        StarShip(
                            name = name,
                            description = description,
                            tons = tons.toInt(),
                            techLevel = techLevel,
                            configuration = configuration
                        )
                    )
                    // Reset form
                    name = ""
                    description = ""
                    tons = "200"
                    techLevel = TechLevel.C
                    configuration = Configuration.STANDARD
                }
            },
            enabled = name.isNotBlank() && description.isNotBlank() && !tonsError,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Ship")
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    MyApplicationTheme {
        StarShipScreen(
            items = listOf(
                StarShip("Enterprise", "Constitution class", 200, TechLevel.G, Configuration.STANDARD),
                StarShip("Millennium Falcon", "Modified freighter", 400, TechLevel.E, Configuration.NEEDLE_WEDGE)
            ),
            onSave = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun PortraitPreview() {
    MyApplicationTheme {
        StarShipScreen(
            items = listOf(
                StarShip("Voyager", "Intrepid class", 300, TechLevel.H, Configuration.SPHERE)
            ),
            onSave = {}
        )
    }
}

// Extension function to display user-friendly configuration names
private fun Configuration.displayName(): String = when (this) {
    Configuration.NEEDLE_WEDGE -> "Needle/Wedge"
    Configuration.CONE -> "Cone"
    Configuration.STANDARD -> "Standard (Cylinder)"
    Configuration.CLOSE_STRUCTURE -> "Close Structure"
    Configuration.SPHERE -> "Sphere"
    Configuration.DISPERSED_STRUCTURE -> "Dispersed Structure"
    Configuration.PLANETOID -> "Planetoid"
    Configuration.BUFFERED_PLANETOID -> "Buffered Planetoid"
}
