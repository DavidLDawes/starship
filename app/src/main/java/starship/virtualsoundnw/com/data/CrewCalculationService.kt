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
import starship.virtualsoundnw.com.data.BerthsRepository
import starship.virtualsoundnw.com.data.local.database.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

/**
 * Service for calculating crew requirements based on ship systems and configuration
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CrewCalculationService @Inject constructor(
    private val starShipRepository: StarShipRepository,
    private val enginesRepository: EnginesRepository,
    private val weaponsRepository: WeaponsRepository,
    private val defensesRepository: DefensesRepository,
    private val cargoRepository: CargoRepository,
    private val vehiclesRepository: VehiclesRepository,
    private val dronesRepository: DronesRepository,
    private val berthsRepository: BerthsRepository
) {
    
    /**
     * Calculate complete crew manifest for a ship
     */
    fun getCrewManifest(shipId: Int): Flow<CrewManifest?> {
        return starShipRepository.starShips.flatMapLatest { ships ->
            val ship = ships.find { it.uid == shipId }
            if (ship != null) {
                combine(
                    combine(
                        enginesRepository.getEnginesForShip(shipId),
                        weaponsRepository.getWeaponsForShip(shipId),
                        defensesRepository.getDefenseForShip(shipId)
                    ) { engines, weapons, defense -> Triple(engines, weapons, defense) },
                    combine(
                        cargoRepository.getCargoForShip(shipId),
                        vehiclesRepository.getVehiclesWithAllocationsForShip(shipId, ship.techLevel),
                        dronesRepository.getDronesWithAllocationsForShip(shipId, ship.techLevel),
                        berthsRepository.getBerthsForShip(shipId)
                    ) { cargo, vehicles, drones, berths -> 
                        listOf(cargo, vehicles, drones, berths) 
                    }
                ) { systemsA, systemsB ->
                    val (engines, weapons, defense) = systemsA
                    val cargo = systemsB[0] as Cargo?
                    val vehicles = systemsB[1] as List<VehicleWithAllocation>
                    val drones = systemsB[2] as List<DroneWithAllocation>
                    val berths = systemsB[3] as Berths?
                    calculateCrewManifest(ship, engines, weapons, defense, cargo, vehicles, drones, berths)
                }
            } else {
                flowOf(null)
            }
        }
    }
    
    private fun calculateCrewManifest(
        ship: StarShip,
        engines: List<Engine>,
        weapons: List<Weapon>,
        defense: Defense?,
        cargo: Cargo?,
        vehicles: List<VehicleWithAllocation>,
        drones: List<DroneWithAllocation>,
        berths: Berths?
    ): CrewManifest {
        // Calculate all crew except stewards first
        val engineCrew = calculateEngineCrew(ship, engines)
        val bridgeCrew = calculateBridgeCrew(ship)
        val weaponsCrew = calculateWeaponsCrew(weapons)
        val defenseCrew = calculateDefenseCrew(defense)
        val cargoCrew = calculateCargoCrew(cargo)
        val vehicleCrew = calculateVehicleCrew(vehicles)
        val droneCrew = calculateDroneCrew(drones)
        
        // Calculate stewards based on total crew + passengers (Issue #76)
        val berthsCrew = calculateBerthsCrew(
            berths, 
            engineCrew + bridgeCrew + weaponsCrew + defenseCrew + cargoCrew + vehicleCrew + droneCrew
        )
        
        return CrewManifest(
            engineCrew = engineCrew,
            bridgeCrew = bridgeCrew,
            weaponsCrew = weaponsCrew,
            defenseCrew = defenseCrew,
            cargoCrew = cargoCrew,
            vehicleCrew = vehicleCrew,
            droneCrew = droneCrew,
            berthsCrew = berthsCrew
        )
    }
    
    /**
     * Calculate engine crew requirements
     * - Basic: 1 Engineer per 100 tons of engine (rounded up)
     * - 100-200 ton ships: 1 engineer handles all 3 engine types
     * - 300+ ton ships: 1 engineer per 100 tons per engine type
     */
    private fun calculateEngineCrew(ship: StarShip, engines: List<Engine>): List<CrewMember> {
        val totalEngineTonnage = engines.sumOf { it.getTonnage(ship.tons).toDouble() }.toFloat()
        val crew = mutableListOf<CrewMember>()
        
        if (totalEngineTonnage <= 0) return crew
        
        when {
            ship.tons <= 200 -> {
                // Single engineer for all engines
                val engineersNeeded = ceil(totalEngineTonnage / 100.0).toInt()
                crew.add(CrewMember(
                    type = CrewType.ENGINEER,
                    quantity = engineersNeeded,
                    assignment = "All engines (${totalEngineTonnage} tons total)"
                ))
            }
            else -> {
                // Separate engineers by engine type for 300+ ton ships
                val enginesByType = engines.groupBy { it.type }
                
                enginesByType.forEach { (type, typeEngines) ->
                    val typeTonnage = typeEngines.sumOf { it.getTonnage(ship.tons).toDouble() }.toFloat()
                    if (typeTonnage > 0) {
                        val engineersNeeded = ceil(typeTonnage / 100.0).toInt()
                        val typeName = when (type) {
                            EngineType.POWER_PLANT -> "Power Plant"
                            EngineType.JUMP_DRIVE -> "Jump Drive"
                            EngineType.MANEUVER_DRIVE -> "Maneuver Drive"
                        }
                        crew.add(CrewMember(
                            type = CrewType.ENGINEER,
                            quantity = engineersNeeded,
                            assignment = "$typeName (${typeTonnage} tons)"
                        ))
                    }
                }
            }
        }
        
        return crew
    }
    
    /**
     * Calculate bridge crew requirements (Issue #76)
     * - 100-200 tons: 1 Pilot/Navigator combined
     * - 300+ tons: 1 Pilot + 1 Navigator  
     * - Capital ships: 1 Captain + 2 Comms + 2 Sensors + 1 Computer for 8 Bridge Crew total
     */
    private fun calculateBridgeCrew(ship: StarShip): List<CrewMember> {
        val crew = mutableListOf<CrewMember>()
        
        when {
            ship.tons <= 200 -> {
                crew.add(CrewMember(
                    type = CrewType.PILOT,
                    quantity = 1,
                    assignment = "Pilot/Navigator for ${ship.tons} ton vessel"
                ))
            }
            ship.tons <= 2000 -> {
                crew.add(CrewMember(
                    type = CrewType.PILOT,
                    quantity = 1,
                    assignment = "Ship operations"
                ))
                crew.add(CrewMember(
                    type = CrewType.NAVIGATOR,
                    quantity = 1,
                    assignment = "Navigation and astrogation"
                ))
            }
            else -> {
                // Capital ship bridge crew (Issue #76): Captain + 2 Comms + 2 Sensors + Computer = 8 total
                crew.add(CrewMember(
                    type = CrewType.COMMANDER,
                    quantity = 1,
                    assignment = "Captain - capital ship command"
                ))
                crew.add(CrewMember(
                    type = CrewType.PILOT,
                    quantity = 1,
                    assignment = "Ship operations"
                ))
                crew.add(CrewMember(
                    type = CrewType.NAVIGATOR,
                    quantity = 1,
                    assignment = "Navigation and astrogation"
                ))
                crew.add(CrewMember(
                    type = CrewType.COMMS,
                    quantity = 2,
                    assignment = "Communications operations"
                ))
                crew.add(CrewMember(
                    type = CrewType.SENSOR_OPS,
                    quantity = 2,
                    assignment = "Sensor operations"
                ))
                crew.add(CrewMember(
                    type = CrewType.COMPUTER_OPS,
                    quantity = 1,
                    assignment = "Computer operations"
                ))
            }
        }
        
        return crew
    }
    
    /**
     * Calculate weapons crew requirements
     * - 1 Gunner per up to 10 turrets of single type
     * - Hardpoints (empty mounting points) do not require gunners
     */
    private fun calculateWeaponsCrew(weapons: List<Weapon>): List<CrewMember> {
        val crew = mutableListOf<CrewMember>()
        
        // Filter out hardpoints (empty mounting points) - only actual turrets with weapons need gunners
        val actualTurrets = weapons.filter { 
            it.turretType != TurretType.HARDPOINT && it.weaponType != WeaponType.NONE 
        }
        
        // Group actual turrets by weapon and turret type
        val turretsByType = actualTurrets.groupBy { "${it.weaponType.name} ${it.turretType.name}" }
        
        turretsByType.forEach { (weaponTypeKey, weaponGroup) ->
            val turretCount = weaponGroup.size // Each weapon represents one armed turret
            if (turretCount > 0) {
                val gunnersNeeded = ceil(turretCount / 10.0).toInt()
                crew.add(CrewMember(
                    type = CrewType.GUNNER,
                    quantity = gunnersNeeded,
                    assignment = "$weaponTypeKey ($turretCount armed turrets)"
                ))
            }
        }
        
        return crew
    }
    
    /**
     * Calculate defense crew requirements  
     * - 1 Gunner per 100 tons of Shield (rounded up)
     */
    private fun calculateDefenseCrew(defense: Defense?): List<CrewMember> {
        val crew = mutableListOf<CrewMember>()
        
        defense?.let { def ->
            val shieldTonnage = def.getScreenTonnage("")
            if (shieldTonnage > 0) {
                val gunnersNeeded = ceil(shieldTonnage / 100.0).toInt()
                crew.add(CrewMember(
                    type = CrewType.GUNNER,
                    quantity = gunnersNeeded,
                    assignment = "Shield operations (${shieldTonnage} tons)"
                ))
            }
        }
        
        return crew
    }
    
    /**
     * Calculate cargo crew requirements
     * - 1 Freight if total cargo > 100 tons
     * - 1 Security if Secured Cargo > 0
     * - 1 Xeno per 25 tons of Xeno Cargo
     */
    private fun calculateCargoCrew(cargo: Cargo?): List<CrewMember> {
        val crew = mutableListOf<CrewMember>()
        
        cargo?.let { c ->
            // Freight for general cargo operations (Issue #76)
            val totalCargo = c.cargoTons + c.coldStorageTons + c.sparesTons + c.securedCargoTons + c.xenoCargoTons
            if (totalCargo > 100) {
                crew.add(CrewMember(
                    type = CrewType.FREIGHT,
                    quantity = 1,
                    assignment = "Cargo operations (${totalCargo} tons total)"
                ))
            }
            
            // Security for secured cargo
            if (c.securedCargoTons > 0) {
                crew.add(CrewMember(
                    type = CrewType.SECURITY,
                    quantity = 1,
                    assignment = "Secured cargo (${c.securedCargoTons} tons)"
                ))
            }
            
            // Xeno handlers for xeno cargo
            if (c.xenoCargoTons > 0) {
                val xenoHandlersNeeded = ceil(c.xenoCargoTons / 25.0).toInt()
                crew.add(CrewMember(
                    type = CrewType.XENO_HANDLER,
                    quantity = xenoHandlersNeeded,
                    assignment = "Xeno cargo (${c.xenoCargoTons} tons)"
                ))
            }
        }
        
        return crew
    }
    
    /**
     * Calculate vehicle crew requirements (Issue #76)
     * - 1 Pilot per vehicle
     * - 1 Service per vehicle
     */
    private fun calculateVehicleCrew(vehicles: List<VehicleWithAllocation>): List<CrewMember> {
        val crew = mutableListOf<CrewMember>()
        
        val totalVehicles = vehicles.sumOf { it.quantity }
        if (totalVehicles > 0) {
            // Pilots for vehicles
            crew.add(CrewMember(
                type = CrewType.PILOT,
                quantity = totalVehicles,
                assignment = "Vehicle pilots ($totalVehicles vehicles)"
            ))
            
            // Service for vehicles
            crew.add(CrewMember(
                type = CrewType.SERVICE,
                quantity = totalVehicles,
                assignment = "Vehicle service ($totalVehicles vehicles)"
            ))
        }
        
        return crew
    }
    
    /**
     * Calculate drone crew requirements (Issue #76)
     * - 1 Service per 100 tons of drones (rounded up)
     */
    private fun calculateDroneCrew(drones: List<DroneWithAllocation>): List<CrewMember> {
        val crew = mutableListOf<CrewMember>()
        
        val totalDroneTonnage = drones.sumOf { 
            it.drone.tons.toDouble() * it.quantity 
        }
        if (totalDroneTonnage > 0) {
            val serviceNeeded = ceil(totalDroneTonnage / 100.0).toInt()
            crew.add(CrewMember(
                type = CrewType.SERVICE,
                quantity = serviceNeeded,
                assignment = "Drone maintenance (${totalDroneTonnage} tons)"
            ))
        }
        
        return crew
    }
    
    /**
     * Calculate berths crew requirements (Issue #76)
     * - 1 Steward per 8 total (crew + passengers), rounded up
     * - Passengers = (Staterooms + Luxury Staterooms) - Total Crew
     */
    private fun calculateBerthsCrew(berths: Berths?, otherCrew: List<CrewMember>): List<CrewMember> {
        val crew = mutableListOf<CrewMember>()
        
        berths?.let { b ->
            val totalStaterooms = b.staterooms + b.luxuryStaterooms
            val totalOtherCrew = otherCrew.sumOf { it.quantity }
            
            if (totalStaterooms > 0) {
                // Calculate stewards iteratively to handle circular dependency
                var stewardsNeeded = 0
                var totalCrewIncludingStewards: Int
                
                do {
                    val previousStewards = stewardsNeeded
                    totalCrewIncludingStewards = totalOtherCrew + stewardsNeeded
                    val passengers = maxOf(0, totalStaterooms - totalCrewIncludingStewards)
                    val totalCrewAndPassengers = totalCrewIncludingStewards + passengers
                    stewardsNeeded = ceil(totalCrewAndPassengers / 8.0).toInt()
                } while (stewardsNeeded != previousStewards && stewardsNeeded < 50) // Safety limit
                
                if (stewardsNeeded > 0) {
                    val totalCrewFinal = totalOtherCrew + stewardsNeeded
                    val passengersFinal = maxOf(0, totalStaterooms - totalCrewFinal)
                    crew.add(CrewMember(
                        type = CrewType.STEWARD,
                        quantity = stewardsNeeded,
                        assignment = "Service for ${totalCrewFinal} crew + ${passengersFinal} passengers"
                    ))
                }
            }
        }
        
        return crew
    }
}