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

package starship.virtualsoundnw.com.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.Engine
import starship.virtualsoundnw.com.data.local.database.Weapon
import starship.virtualsoundnw.com.data.local.database.Defense
import starship.virtualsoundnw.com.data.local.database.Fitting
import starship.virtualsoundnw.com.data.local.database.Cargo
import starship.virtualsoundnw.com.data.local.database.VehicleAllocation
import starship.virtualsoundnw.com.data.local.database.VehicleWithAllocation
import starship.virtualsoundnw.com.data.local.database.DroneWithAllocation
import starship.virtualsoundnw.com.data.local.database.EngineType
import starship.virtualsoundnw.com.data.local.database.PowerPlantType
import starship.virtualsoundnw.com.data.local.database.calculateFuelRequirement
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized service for calculating comprehensive ship summaries
 * across all systems without creating circular dependencies
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ShipSummaryService @Inject constructor(
    private val starShipRepository: StarShipRepository,
    private val enginesRepository: EnginesRepository,
    private val weaponsRepository: WeaponsRepository,
    private val defensesRepository: DefensesRepository,
    private val fittingsRepository: FittingsRepository,
    private val cargoRepository: CargoRepository,
    private val vehiclesRepository: VehiclesRepository,
    private val dronesRepository: DronesRepository,
    private val berthsRepository: BerthsRepository,
    private val customRepository: CustomRepository,
    private val crewCalculationService: CrewCalculationService
) {
    
    /**
     * Get comprehensive ship summary with all system data
     */
    fun getComprehensiveShipSummary(shipId: Int): Flow<ShipSummary?> {
        return starShipRepository.starShips.flatMapLatest { ships ->
            val ship = ships.find { it.uid == shipId }
            if (ship != null) {
                combine(
                    combine(
                        enginesRepository.getEnginesForShip(shipId),
                        weaponsRepository.getWeaponsForShip(shipId),
                        defensesRepository.getDefenseForShip(shipId)
                    ) { engines, weapons, defenses -> Triple(engines, weapons, defenses) },
                    combine(
                        fittingsRepository.getFittingForShip(shipId),
                        cargoRepository.getCargoForShip(shipId),
                        combine(
                            vehiclesRepository.getVehiclesWithAllocationsForShip(shipId, ship.techLevel),
                            dronesRepository.getDronesWithAllocationsForShip(shipId, ship.techLevel),
                            combine(
                                berthsRepository.getBerthsForShip(shipId),
                                customRepository.getCustomItemsForShip(shipId)
                            ) { berths, customItems -> Pair(berths, customItems) }
                        ) { vehicles, drones, berthsAndCustom -> Triple(vehicles, drones, berthsAndCustom) }
                    ) { fitting, cargo, vehiclesDronesBerthsCustom ->
                        Triple(fitting, cargo, vehiclesDronesBerthsCustom)
                    },
                    crewCalculationService.getCrewManifest(shipId)
                ) { systemsA, systemsB, crewManifest ->
                val (engines, weapons, defenses) = systemsA
                val (fitting, cargo, vehiclesDronesBerthsCustom) = systemsB
                val (vehiclesWithAllocations, dronesWithAllocations, berthsAndCustom) = vehiclesDronesBerthsCustom
                val (berths, customItems) = berthsAndCustom
                
                // Calculate engine tonnage (without fuel)
                val enginesTonnage = engines.sumOf { it.getTonnage(ship.tons).toDouble() }
                val enginesCost = engines.sumOf { it.getTotalCost(ship.tons, ship.techLevel).toDouble() }
                
                // Calculate fuel requirement separately
                val jumpPerformance = engines.filter { it.type == EngineType.JUMP_DRIVE }
                    .maxOfOrNull { it.performance.toInt() } ?: 0
                val hasAntimatterPowerPlant = engines.filter { it.type == EngineType.POWER_PLANT }
                    .any { engine -> 
                        PowerPlantType.getBestAvailableForTechLevel(ship.techLevel) == 
                        PowerPlantType.ANTIMATTER 
                    }
                val fuelTonnage = calculateFuelRequirement(
                    jumpPerformance, ship.tons, hasAntimatterPowerPlant
                ).toDouble()
                
                val weaponsTonnage = weapons.sumOf { it.getTotalTonnage().toDouble() }
                val weaponsCost = weapons.sumOf { it.getTotalCost().toDouble() }
                
                val defensesTonnage = defenses?.let { d ->
                    (d.getArmorTonnage(ship.tons, ship.techLevel) + d.getScreenTonnage(ship.hullCode)).toDouble()
                } ?: 0.0
                val defensesCost = defenses?.let { d ->
                    (d.getArmorCost(ship.tons, ship.configuration, ship.techLevel) + d.getScreenCost(ship.hullCode)).toDouble()
                } ?: 0.0
                
                val fittingsTonnage = fitting?.getTotalTonnage(ship.tons, ship.sections)?.toDouble() ?: 0.0
                val fittingsCost = fitting?.getTotalCost(ship.tons, ship.sections)?.toDouble() ?: 0.0
                
                val cargoTonnage = cargo?.getTotalTonnage()?.toDouble() ?: 0.0
                val cargoCost = cargo?.getTotalCargoCost()?.toDouble() ?: 0.0
                
                // Calculate vehicles tonnage and cost
                val vehiclesTonnage = vehiclesWithAllocations.sumOf { it.extendedTonnage.toDouble() }
                val vehiclesCost = vehiclesWithAllocations.sumOf { it.extendedCostMCr.toDouble() }
                
                // Calculate drones tonnage and cost
                val dronesTonnage = dronesWithAllocations.sumOf { it.extendedTonnage.toDouble() }
                val dronesCost = dronesWithAllocations.sumOf { it.extendedCostMCr.toDouble() }
                
                // Calculate berths tonnage and cost
                val berthsTonnage = berths?.getTotalTonnage()?.toDouble() ?: 0.0
                val berthsCost = berths?.getTotalBerthsCost()?.toDouble() ?: 0.0

                // Calculate custom items tonnage and cost
                val customTonnage = customItems.sumOf { it.tons.toDouble() }
                val customCost = customItems.sumOf { it.costMCr.toDouble() }

                ShipSummary(
                    ship = ship,
                    enginesTonnage = enginesTonnage,
                    enginesCost = enginesCost,
                    fuelTonnage = fuelTonnage,
                    weaponsTonnage = weaponsTonnage,
                    weaponsCost = weaponsCost,
                    defensesTonnage = defensesTonnage,
                    defensesCost = defensesCost,
                    fittingsTonnage = fittingsTonnage,
                    fittingsCost = fittingsCost,
                    cargoTonnage = cargoTonnage.toInt(),
                    cargoCost = cargoCost,
                    vehiclesTonnage = vehiclesTonnage,
                    vehiclesCost = vehiclesCost,
                    dronesTonnage = dronesTonnage,
                    dronesCost = dronesCost,
                    berthsTonnage = berthsTonnage,
                    berthsCost = berthsCost,
                    customTonnage = customTonnage,
                    customCost = customCost,
                    crewManifest = crewManifest
                )
                }
            } else {
                flowOf(null)
            }
        }
    }
}