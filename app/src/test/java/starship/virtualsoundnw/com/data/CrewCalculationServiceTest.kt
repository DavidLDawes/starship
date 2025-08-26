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

import org.junit.Assert.assertEquals
import org.junit.Test
import starship.virtualsoundnw.com.data.local.database.*

/**
 * Unit tests for crew functionality updates for Issue #76
 */
class CrewCalculationServiceTest {

    @Test
    fun crewType_freight_hasCorrectDisplayName() {
        // Test the new FREIGHT crew type (Issue #76)
        val freightCrew = CrewMember(CrewType.FREIGHT, 1, "Cargo operations")
        assertEquals("Freight", freightCrew.displayName)
        assertEquals("1 Freight - Cargo operations", freightCrew.description)
    }

    @Test
    fun crewType_freight_hasCorrectPluralForm() {
        // Test plural form of FREIGHT crew type
        val freightCrew = CrewMember(CrewType.FREIGHT, 2, "Cargo operations")
        assertEquals("2 Freights - Cargo operations", freightCrew.description)
    }
    
    @Test
    fun crewType_allTypes_haveCorrectDisplayNames() {
        // Test that all crew types have proper display names
        assertEquals("Engineer", CrewMember(CrewType.ENGINEER, 1, "test").displayName)
        assertEquals("Pilot", CrewMember(CrewType.PILOT, 1, "test").displayName)
        assertEquals("Navigator", CrewMember(CrewType.NAVIGATOR, 1, "test").displayName)
        assertEquals("Commander", CrewMember(CrewType.COMMANDER, 1, "test").displayName)
        assertEquals("Security", CrewMember(CrewType.SECURITY, 1, "test").displayName)
        assertEquals("Sensor Ops", CrewMember(CrewType.SENSOR_OPS, 1, "test").displayName)
        assertEquals("Communications", CrewMember(CrewType.COMMS, 1, "test").displayName)
        assertEquals("Computer Ops", CrewMember(CrewType.COMPUTER_OPS, 1, "test").displayName)
        assertEquals("Gunner", CrewMember(CrewType.GUNNER, 1, "test").displayName)
        assertEquals("Steward", CrewMember(CrewType.STEWARD, 1, "test").displayName)
        assertEquals("Service", CrewMember(CrewType.SERVICE, 1, "test").displayName)
        assertEquals("Xeno Handler", CrewMember(CrewType.XENO_HANDLER, 1, "test").displayName)
        assertEquals("Freight", CrewMember(CrewType.FREIGHT, 1, "test").displayName)
    }

    @Test
    fun crewMember_descriptionFormat_isCorrect() {
        // Test that crew member descriptions format correctly
        val singleCrew = CrewMember(CrewType.ENGINEER, 1, "Power plant operations")
        assertEquals("1 Engineer - Power plant operations", singleCrew.description)
        
        val multipleCrew = CrewMember(CrewType.GUNNER, 3, "Turret operations")
        assertEquals("3 Gunners - Turret operations", multipleCrew.description)
    }
}