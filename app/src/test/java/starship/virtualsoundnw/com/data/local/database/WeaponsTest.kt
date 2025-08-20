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

import org.junit.Assert.assertEquals
import org.junit.Test

class WeaponsTest {

    @Test
    fun calculateMaxHardpoints_correctCalculation() {
        // Test various ship sizes
        assertEquals(0, calculateMaxHardpoints(99))   // Below 100 tons
        assertEquals(1, calculateMaxHardpoints(100))  // Exactly 100 tons
        assertEquals(1, calculateMaxHardpoints(199))  // Just under 200
        assertEquals(2, calculateMaxHardpoints(200))  // Exactly 200 tons
        assertEquals(5, calculateMaxHardpoints(500))  // 500 tons
        assertEquals(10, calculateMaxHardpoints(1000)) // 1000 tons
    }

    @Test
    fun weaponCostCalculations_correctValues() {
        // Test Single Pulse Laser
        val singlePulseLaser = Weapon(
            shipId = 1,
            turretType = TurretType.SINGLE,
            weaponType = WeaponType.PULSE_LASER
        )
        assertEquals(0.7f, singlePulseLaser.getTotalCost(), 0.01f) // 0.2 + (1 * 0.5)

        // Test Double Pulse Laser
        val doublePulseLaser = Weapon(
            shipId = 1,
            turretType = TurretType.DOUBLE,
            weaponType = WeaponType.PULSE_LASER
        )
        assertEquals(1.5f, doublePulseLaser.getTotalCost(), 0.01f) // 0.5 + (2 * 0.5)

        // Test Pop-up Triple Particle Beam
        val popupTripleParticleBeam = Weapon(
            shipId = 1,
            turretType = TurretType.POPUP_TRIPLE,
            weaponType = WeaponType.PARTICLE_BEAM
        )
        assertEquals(14.0f, popupTripleParticleBeam.getTotalCost(), 0.01f) // 2.0 + (3 * 4.0)
    }

    @Test
    fun weaponDesignations_correctNames() {
        val singlePulseLaser = Weapon(1, TurretType.SINGLE, WeaponType.PULSE_LASER)
        assertEquals("Single Pulse Laser", singlePulseLaser.getDesignation())

        val doubleMissileRack = Weapon(1, TurretType.DOUBLE, WeaponType.MISSILE_RACK)
        assertEquals("Double Missile Rack", doubleMissileRack.getDesignation())

        val popupTripleBeamLaser = Weapon(1, TurretType.POPUP_TRIPLE, WeaponType.BEAM_LASER)
        assertEquals("Pop-up Triple Beam Laser", popupTripleBeamLaser.getDesignation())
    }

    @Test
    fun weaponsCalculation_hardpointConstraints() {
        val ship = StarShip("Test Ship", "Test Class", 200, TechLevel.G, Configuration.STANDARD)
        
        // 200 ton ship should have 2 hardpoints
        val weapons = listOf(
            Weapon(1, TurretType.SINGLE, WeaponType.PULSE_LASER),
            Weapon(1, TurretType.DOUBLE, WeaponType.BEAM_LASER)
        )
        
        val calculation = WeaponsCalculation(ship, weapons)
        
        assertEquals(2, calculation.maxHardpoints)
        assertEquals(2, calculation.usedHardpoints)
        assertEquals(0, calculation.remainingHardpoints)
        assertEquals(false, calculation.canAddWeapon())
        
        // Test total cost calculation
        val expectedCost = 0.7f + 2.5f // Single Pulse + Double Beam
        assertEquals(expectedCost, calculation.totalWeaponsCost, 0.01f)
    }
}