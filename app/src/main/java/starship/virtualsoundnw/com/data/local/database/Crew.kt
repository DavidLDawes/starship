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

package starship.virtualsoundnw.com.data.local.database

/**
 * Crew member types for different ship operations
 */
enum class CrewType {
    // Engine crew
    ENGINEER,
    
    // Bridge crew
    PILOT,
    NAVIGATOR,
    COMMANDER,
    SECURITY,
    SENSOR_OPS,
    COMMS,
    COMPUTER_OPS,
    
    // Combat crew
    GUNNER,
    
    // Support crew
    STEWARD,
    SERVICE,
    XENO_HANDLER,
    FREIGHT
}

/**
 * Crew member assignment
 */
data class CrewMember(
    val type: CrewType,
    val quantity: Int,
    val assignment: String // Description of what they're assigned to
) {
    val displayName: String get() = when (type) {
        CrewType.ENGINEER -> "Engineer"
        CrewType.PILOT -> "Pilot"
        CrewType.NAVIGATOR -> "Navigator"
        CrewType.COMMANDER -> "Commander"
        CrewType.SECURITY -> "Security"
        CrewType.SENSOR_OPS -> "Sensor Ops"
        CrewType.COMMS -> "Communications"
        CrewType.COMPUTER_OPS -> "Computer Ops"
        CrewType.GUNNER -> "Gunner"
        CrewType.STEWARD -> "Steward"
        CrewType.SERVICE -> "Service"
        CrewType.XENO_HANDLER -> "Xeno Handler"
        CrewType.FREIGHT -> "Freight"
    }
    
    val description: String get() = when {
        // Handle combined roles like "Pilot/Navigator for X ton vessel"
        assignment.startsWith("Pilot/Navigator") -> "$quantity Pilot/Navigator"
        else -> "$quantity $displayName${if (quantity > 1) "s" else ""} - $assignment"
    }
}

/**
 * Complete crew manifest for a ship
 */
data class CrewManifest(
    val engineCrew: List<CrewMember> = emptyList(),
    val bridgeCrew: List<CrewMember> = emptyList(),
    val weaponsCrew: List<CrewMember> = emptyList(),
    val defenseCrew: List<CrewMember> = emptyList(),
    val cargoCrew: List<CrewMember> = emptyList(),
    val vehicleCrew: List<CrewMember> = emptyList(),
    val droneCrew: List<CrewMember> = emptyList(),
    val berthsCrew: List<CrewMember> = emptyList()
) {
    val totalCrewCount: Int get() = allCrew.sumOf { it.quantity }
    
    val allCrew: List<CrewMember> get() = engineCrew + bridgeCrew + weaponsCrew + 
        defenseCrew + cargoCrew + vehicleCrew + droneCrew + berthsCrew
        
    val crewByType: Map<CrewType, Int> get() = allCrew
        .groupBy { it.type }
        .mapValues { (_, members) -> members.sumOf { it.quantity } }
}