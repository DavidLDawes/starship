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

package starship.virtualsoundnw.com.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import starship.virtualsoundnw.com.data.ShipSummary
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.ui.utils.RoundingUtils

/**
 * Data class to hold ship summary information from all systems
 */
data class ShipSummaryData(
    val ship: StarShip,
    val enginesTonnage: Double = 0.0,
    val enginesCost: Double = 0.0,
    val fuelTonnage: Double = 0.0,
    val weaponsTonnage: Double = 0.0,
    val weaponsCost: Double = 0.0,
    val defensesTonnage: Double = 0.0,
    val defensesCost: Double = 0.0,
    val fittingsTonnage: Double = 0.0,
    val fittingsCost: Double = 0.0,
    val cargoTonnage: Double = 0.0,
    val cargoCost: Double = 0.0,
    val vehiclesTonnage: Double = 0.0,
    val vehiclesCost: Double = 0.0,
    val dronesTonnage: Double = 0.0,
    val dronesCost: Double = 0.0,
    val berthsTonnage: Double = 0.0,
    val berthsCost: Double = 0.0,
    val customTonnage: Double = 0.0,
    val customCost: Double = 0.0
) {
    val totalSystemsTonnage: Double get() = enginesTonnage + fuelTonnage + weaponsTonnage + defensesTonnage + fittingsTonnage + cargoTonnage + vehiclesTonnage + dronesTonnage + berthsTonnage + customTonnage
    val totalSystemsCost: Double get() = enginesCost + weaponsCost + defensesCost + fittingsCost + cargoCost + vehiclesCost + dronesCost + berthsCost + customCost + ship.hullCost
    val remainingTonnage: Double get() = ship.tons - totalSystemsTonnage
}

/**
 * Extension function to convert ShipSummary to ShipSummaryData
 * This eliminates repetitive mapping code across all screen files
 */
fun ShipSummary.toShipSummaryData(): ShipSummaryData {
    return ShipSummaryData(
        ship = this.ship,
        enginesTonnage = this.enginesTonnage,
        enginesCost = this.enginesCost,
        fuelTonnage = this.fuelTonnage,
        weaponsTonnage = this.weaponsTonnage,
        weaponsCost = this.weaponsCost,
        defensesTonnage = this.defensesTonnage,
        defensesCost = this.defensesCost,
        fittingsTonnage = this.fittingsTonnage,
        fittingsCost = this.fittingsCost,
        cargoTonnage = this.cargoTonnage.toDouble(),
        cargoCost = this.cargoCost,
        vehiclesTonnage = this.vehiclesTonnage,
        vehiclesCost = this.vehiclesCost,
        dronesTonnage = this.dronesTonnage,
        dronesCost = this.dronesCost,
        berthsTonnage = this.berthsTonnage,
        berthsCost = this.berthsCost,
        customTonnage = this.customTonnage,
        customCost = this.customCost
    )
}

/**
 * Comprehensive ship summary panel that shows all system details
 * This replaces the individual screen-specific summary panels for consistency
 */
@Composable
fun ComprehensiveShipSummaryPanel(
    summaryData: ShipSummaryData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
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
            
            // Total ship tonnage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Ship Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${summaryData.ship.tons} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Sections (for capital ships)
            if (summaryData.ship.sections > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sections:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${summaryData.ship.sections}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Engines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Engines:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.enginesTonnage)} tons (${RoundingUtils.formatMCr(summaryData.enginesCost)} MCr)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Fuel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fuel Tonnage:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.fuelTonnage)} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Weapons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Weapons:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.weaponsTonnage)} tons (${RoundingUtils.formatMCr(summaryData.weaponsCost)} MCr)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Defenses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Defenses:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.defensesTonnage)} tons (${RoundingUtils.formatMCr(summaryData.defensesCost)} MCr)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Fittings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fittings:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.fittingsTonnage)} tons (${RoundingUtils.formatMCr(summaryData.fittingsCost)} MCr)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Cargo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cargo:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.cargoTonnage)} tons (${RoundingUtils.formatMCr(summaryData.cargoCost)} MCr)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Vehicles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vehicles:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.vehiclesTonnage)} tons (${RoundingUtils.formatMCr(summaryData.vehiclesCost)} MCr)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Drones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Drones:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.dronesTonnage)} tons (${RoundingUtils.formatMCr(summaryData.dronesCost)} MCr)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Berths
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Berths:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.berthsTonnage)} tons (${RoundingUtils.formatMCr(summaryData.berthsCost)} MCr)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            HorizontalDivider()
            
            // Totals
            Text(
                text = "Totals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Remaining Tonnage:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${RoundingUtils.formatTons(summaryData.remainingTonnage)} tons",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (summaryData.remainingTonnage >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Ship Cost:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${RoundingUtils.formatMCr(summaryData.totalSystemsCost)} MCr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}