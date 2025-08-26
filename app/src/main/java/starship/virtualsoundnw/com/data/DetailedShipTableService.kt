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
import starship.virtualsoundnw.com.data.local.database.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data models for detailed ship table display
 */
data class ShipTableRow(
    val category: String,
    val item: String,
    val tons: Double,
    val costMCr: Double,
    val crew: Int,
    val isFirstInCategory: Boolean = false
)

data class DetailedShipTableData(
    val ship: StarShip,
    val rows: List<ShipTableRow> = emptyList(),
    val totalTons: Double = 0.0,
    val totalCostMCr: Double = 0.0,
    val totalCrew: Int = 0,
    val remainingTons: Double = 0.0
) {
    val shipHeader: String get() = "${ship.name}, ${ship.tons} ton, ${ship.configuration}, ${ship.techLevel}"
    val shipDescription: String get() = ship.description ?: "Starship"
}

/**
 * Service for generating detailed ship table data with all individual components
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class DetailedShipTableService @Inject constructor(
    private val starShipRepository: StarShipRepository,
    private val enginesRepository: EnginesRepository,
    private val weaponsRepository: WeaponsRepository,
    private val defensesRepository: DefensesRepository,
    private val fittingsRepository: FittingsRepository,
    private val cargoRepository: CargoRepository,
    private val berthsRepository: BerthsRepository
) {
    
    fun getDetailedShipTable(shipId: Int): Flow<DetailedShipTableData?> {
        return starShipRepository.starShips.flatMapLatest { ships ->
            val ship = ships.find { it.uid == shipId }
            if (ship != null) {
                combine(
                    enginesRepository.getEnginesForShip(shipId),
                    weaponsRepository.getWeaponsForShip(shipId),
                    defensesRepository.getDefenseForShip(shipId),
                    fittingsRepository.getFittingForShip(shipId),
                    cargoRepository.getCargoForShip(shipId),
                    berthsRepository.getBerthsForShip(shipId)
                ) { dataArray ->
                    val engines = dataArray[0] as List<Engine>
                    val weapons = dataArray[1] as List<Weapon>
                    val defense = dataArray[2] as Defense?
                    val fitting = dataArray[3] as Fitting?
                    val cargo = dataArray[4] as Cargo?
                    val berths = dataArray[5] as Berths?
                    buildDetailedTable(ship, engines, weapons, defense, fitting, cargo, berths)
                }
            } else {
                flowOf(null)
            }
        }
    }
    
    private fun buildDetailedTable(
        ship: StarShip,
        engines: List<Engine>,
        weapons: List<Weapon>,
        defense: Defense?,
        fitting: Fitting?,
        cargo: Cargo?,
        berths: Berths?
    ): DetailedShipTableData {
        val rows = mutableListOf<ShipTableRow>()
        
        // Engines category
        val engineRows = buildEngineRows(ship, engines)
        rows.addAll(engineRows)
        
        // Weapons category  
        val weaponRows = buildWeaponRows(weapons)
        rows.addAll(weaponRows)
        
        // Defenses category
        val defenseRows = buildDefenseRows(ship, defense)
        rows.addAll(defenseRows)
        
        // Fittings category
        val fittingRows = buildFittingRows(ship, fitting)
        rows.addAll(fittingRows)
        
        // Cargo category
        val cargoRows = buildCargoRows(cargo)
        rows.addAll(cargoRows)
        
        // Berths category
        val berthRows = buildBerthRows(berths)
        rows.addAll(berthRows)
        
        // Calculate totals
        val totalTons = rows.sumOf { it.tons }
        val totalCostMCr = rows.sumOf { it.costMCr } + ship.hullCost
        val totalCrew = 0 // TODO: Calculate crew from components
        val remainingTons = ship.tons - totalTons
        
        return DetailedShipTableData(
            ship = ship,
            rows = rows,
            totalTons = totalTons,
            totalCostMCr = totalCostMCr,
            totalCrew = totalCrew,
            remainingTons = remainingTons
        )
    }
    
    private fun buildEngineRows(ship: StarShip, engines: List<Engine>): List<ShipTableRow> {
        val rows = mutableListOf<ShipTableRow>()
        val enginesByType = engines.groupBy { it.type }
        var isFirstInCategory = true
        
        // Power Plants
        enginesByType[EngineType.POWER_PLANT]?.forEach { engine ->
            rows.add(ShipTableRow(
                category = if (isFirstInCategory) "Engines" else "",
                item = "Power Plant P-${engine.performance}",
                tons = engine.getTonnage(ship.tons).toDouble(),
                costMCr = engine.getTotalCost(ship.tons, ship.techLevel).toDouble(),
                crew = 0, // Crew assigned by category, not individual engines
                isFirstInCategory = isFirstInCategory
            ))
            isFirstInCategory = false
        }
        
        // Jump Drives
        enginesByType[EngineType.JUMP_DRIVE]?.forEach { engine ->
            rows.add(ShipTableRow(
                category = "",
                item = "Jump Drive J-${engine.performance}",
                tons = engine.getTonnage(ship.tons).toDouble(),
                costMCr = engine.getTotalCost(ship.tons, ship.techLevel).toDouble(),
                crew = 0,
                isFirstInCategory = false
            ))
        }
        
        // Maneuver Drives
        enginesByType[EngineType.MANEUVER_DRIVE]?.forEach { engine ->
            rows.add(ShipTableRow(
                category = "",
                item = "Maneuver Drive M-${engine.performance}",
                tons = engine.getTonnage(ship.tons).toDouble(),
                costMCr = engine.getTotalCost(ship.tons, ship.techLevel).toDouble(),
                crew = 0,
                isFirstInCategory = false
            ))
        }
        
        // Fuel
        if (engines.isNotEmpty()) {
            val jumpPerformance = engines.filter { it.type == EngineType.JUMP_DRIVE }.maxByOrNull { it.performance }?.performance ?: 0
            val hasAntimatterPowerPlant = engines.any { it.type == EngineType.POWER_PLANT && 
                PowerPlantType.getBestAvailableForTechLevel(ship.techLevel) == PowerPlantType.ANTIMATTER }
            val fuelTonnage = calculateFuelRequirement(jumpPerformance, ship.tons, hasAntimatterPowerPlant).toDouble()
            if (fuelTonnage > 0) {
                rows.add(ShipTableRow(
                    category = "",
                    item = "Fuel",
                    tons = fuelTonnage,
                    costMCr = 0.0,
                    crew = 0,
                    isFirstInCategory = false
                ))
            }
        }
        
        return rows
    }
    
    private fun buildWeaponRows(weapons: List<Weapon>): List<ShipTableRow> {
        val rows = mutableListOf<ShipTableRow>()
        var isFirstInCategory = true
        
        // Filter out weapons with no weapon type
        val activeWeapons = weapons.filter { it.weaponType != WeaponType.NONE }
        
        // Group hardpoints separately
        val hardpoints = activeWeapons.filter { it.turretType == TurretType.HARDPOINT }
        val regularWeapons = activeWeapons.filter { it.turretType != TurretType.HARDPOINT }
        
        // Add hardpoints as a single group if any exist
        if (hardpoints.isNotEmpty()) {
            val sampleHardpoint = hardpoints.first()
            val count = hardpoints.size
            val totalTons = count * sampleHardpoint.getTotalTonnage()
            val totalCost = count * sampleHardpoint.getTotalCost()
            
            rows.add(ShipTableRow(
                category = if (isFirstInCategory) "Weapons" else "",
                item = "${count}x Hard Points",
                tons = totalTons.toDouble(),
                costMCr = totalCost.toDouble(),
                crew = 0,
                isFirstInCategory = isFirstInCategory
            ))
            isFirstInCategory = false
        }
        
        // Group identical weapons for banking
        val weaponGroups = regularWeapons.groupBy { 
            // Group by turret type and weapon type combination
            "${it.turretType.name}_${it.weaponType.name}"
        }
        
        weaponGroups.forEach { (_, weaponList) ->
            if (weaponList.isNotEmpty()) {
                val sampleWeapon = weaponList.first()
                val totalCount = weaponList.size
                
                // Create banks of 10
                val fullBanks = totalCount / 10
                val remainder = totalCount % 10
                
                // Add full banks of 10
                repeat(fullBanks) {
                    val bankSize = 10
                    val bankTons = bankSize * sampleWeapon.getTotalTonnage()
                    val bankCost = bankSize * sampleWeapon.getTotalCost()
                    
                    rows.add(ShipTableRow(
                        category = if (isFirstInCategory) "Weapons" else "",
                        item = "${bankSize}x ${sampleWeapon.getDesignation()} (Bank)",
                        tons = bankTons.toDouble(),
                        costMCr = bankCost.toDouble(),
                        crew = 1, // 1 gunner per bank of 10
                        isFirstInCategory = isFirstInCategory
                    ))
                    isFirstInCategory = false
                }
                
                // Add remainder if any
                if (remainder > 0) {
                    val remainderTons = remainder * sampleWeapon.getTotalTonnage()
                    val remainderCost = remainder * sampleWeapon.getTotalCost()
                    
                    rows.add(ShipTableRow(
                        category = if (isFirstInCategory) "Weapons" else "",
                        item = "${remainder}x ${sampleWeapon.getDesignation()}",
                        tons = remainderTons.toDouble(),
                        costMCr = remainderCost.toDouble(),
                        crew = 1, // 1 gunner for remainder group
                        isFirstInCategory = isFirstInCategory
                    ))
                    isFirstInCategory = false
                }
            }
        }
        
        return rows
    }
    
    private fun buildDefenseRows(ship: StarShip, defense: Defense?): List<ShipTableRow> {
        val rows = mutableListOf<ShipTableRow>()
        var isFirstInCategory = true
        
        defense?.let { def ->
            // Armor
            if (def.armorProtection > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Defenses" else "",
                    item = "Armor (${def.armorProtection} protection)",
                    tons = def.getArmorTonnage(ship.tons, ship.techLevel).toDouble(),
                    costMCr = def.getArmorCost(ship.tons, ship.configuration, ship.techLevel).toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            // Screens
            val category = def.getHullCodeCategory(ship.hullCode)
            
            if (def.nuclearDampers > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Defenses" else "",
                    item = "Nuclear Dampers (${def.nuclearDampers})",
                    tons = (def.nuclearDampers * category.nuclearDamperTons).toDouble(),
                    costMCr = (def.nuclearDampers * category.nuclearDamperCost).toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (def.mesonScreens > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Defenses" else "",
                    item = "Meson Screens (${def.mesonScreens})",
                    tons = (def.mesonScreens * category.mesonScreenTons).toDouble(),
                    costMCr = (def.mesonScreens * category.mesonScreenCost).toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (def.blackGlobes > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Defenses" else "",
                    item = "Black Globes (${def.blackGlobes})",
                    tons = (def.blackGlobes * category.blackGlobeTons).toDouble(),
                    costMCr = (def.blackGlobes * category.blackGlobeCost).toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
        }
        
        return rows
    }
    
    private fun buildFittingRows(ship: StarShip, fitting: Fitting?): List<ShipTableRow> {
        val rows = mutableListOf<ShipTableRow>()
        var isFirstInCategory = true
        
        fitting?.let { f ->
            if (f.getSensorTonnage() > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Fittings" else "",
                    item = "Sensors",
                    tons = f.getSensorTonnage().toDouble(),
                    costMCr = f.getSensorCost().toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            // Computer
            rows.add(ShipTableRow(
                category = if (isFirstInCategory) "Fittings" else "",
                item = "Computer ${f.computerModel.model}",
                tons = 0.0, // Computer tonnage is 0 in Traveller
                costMCr = f.getComputerCost().toDouble(),
                crew = 0,
                isFirstInCategory = isFirstInCategory
            ))
            isFirstInCategory = false
            
            // Bridge
            rows.add(ShipTableRow(
                category = "",
                item = "Bridge",
                tons = f.getBridgeTonnage(ship.tons, ship.sections).toDouble(),
                costMCr = f.getBridgeCost(ship.tons, ship.sections).toDouble(),
                crew = 0,
                isFirstInCategory = false
            ))
        }
        
        return rows
    }
    
    private fun buildCargoRows(cargo: Cargo?): List<ShipTableRow> {
        val rows = mutableListOf<ShipTableRow>()
        var isFirstInCategory = true
        
        cargo?.let { c ->
            if (c.cargoTons > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Cargo" else "",
                    item = "Cargo",
                    tons = c.cargoTons.toDouble(),
                    costMCr = 0.0,
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (c.sparesTons > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Cargo" else "",
                    item = "Spares",
                    tons = c.sparesTons.toDouble(),
                    costMCr = c.getCostForCargoType(CargoType.SPARES, c.sparesTons).toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (c.coldStorageTons > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Cargo" else "",
                    item = "Cold Storage",
                    tons = c.coldStorageTons.toDouble(),
                    costMCr = c.getCostForCargoType(CargoType.COLD_STORAGE, c.coldStorageTons).toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (c.securedCargoTons > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Cargo" else "",
                    item = "Secured Cargo",
                    tons = c.securedCargoTons.toDouble(),
                    costMCr = c.getCostForCargoType(CargoType.SECURED_CARGO, c.securedCargoTons).toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (c.xenoCargoTons > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Cargo" else "",
                    item = "Xeno Cargo",
                    tons = c.xenoCargoTons.toDouble(),
                    costMCr = c.getCostForCargoType(CargoType.XENO_CARGO, c.xenoCargoTons).toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
        }
        
        return rows
    }
    
    private fun buildBerthRows(berths: Berths?): List<ShipTableRow> {
        val rows = mutableListOf<ShipTableRow>()
        var isFirstInCategory = true
        
        berths?.let { b ->
            if (b.staterooms > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Berths" else "",
                    item = "Staterooms (${b.staterooms})",
                    tons = b.staterooms * BerthType.STATEROOMS.tonnage.toDouble(),
                    costMCr = b.staterooms * BerthType.STATEROOMS.costMCr.toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (b.luxuryStaterooms > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Berths" else "",
                    item = "Luxury Staterooms (${b.luxuryStaterooms})",
                    tons = b.luxuryStaterooms * BerthType.LUXURY_STATEROOMS.tonnage.toDouble(),
                    costMCr = b.luxuryStaterooms * BerthType.LUXURY_STATEROOMS.costMCr.toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (b.lowPassage > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Berths" else "",
                    item = "Low Passage (${b.lowPassage})",
                    tons = b.lowPassage * BerthType.LOW_PASSAGE.tonnage.toDouble(),
                    costMCr = b.lowPassage * BerthType.LOW_PASSAGE.costMCr.toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
            
            if (b.emergencyLow > 0) {
                rows.add(ShipTableRow(
                    category = if (isFirstInCategory) "Berths" else "",
                    item = "Emergency Low (${b.emergencyLow})",
                    tons = b.emergencyLow * BerthType.EMERGENCY_LOW.tonnage.toDouble(),
                    costMCr = b.emergencyLow * BerthType.EMERGENCY_LOW.costMCr.toDouble(),
                    crew = 0,
                    isFirstInCategory = isFirstInCategory
                ))
                isFirstInCategory = false
            }
        }
        
        return rows
    }
}