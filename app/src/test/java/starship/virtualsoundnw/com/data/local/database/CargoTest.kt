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

/**
 * Unit tests for [Cargo] calculations and [CargoType] specifications.
 */
class CargoTest {

    @Test
    fun cargoType_cargo_hasCorrectSpecifications() {
        val cargoType = CargoType.CARGO
        assertEquals("Cargo", cargoType.displayName)
        assertEquals(0f, cargoType.baseCost, 0.001f)
        assertEquals(0f, cargoType.costPerTon, 0.001f)
    }

    @Test
    fun cargoType_frozenCargo_hasCorrectSpecifications() {
        val cargoType = CargoType.FROZEN_CARGO
        assertEquals("Frozen Cargo", cargoType.displayName)
        assertEquals(0.1f, cargoType.baseCost, 0.001f)
        assertEquals(0.01f, cargoType.costPerTon, 0.001f)
    }

    @Test
    fun cargoType_spares_hasCorrectSpecifications() {
        val cargoType = CargoType.SPARES
        assertEquals("Spares", cargoType.displayName)
        assertEquals(0f, cargoType.baseCost, 0.001f)
        assertEquals(0.1f, cargoType.costPerTon, 0.001f)
    }

    @Test
    fun cargoType_secureCargo_hasCorrectSpecifications() {
        val cargoType = CargoType.SECURE_CARGO
        assertEquals("Secure Cargo", cargoType.displayName)
        assertEquals(0f, cargoType.baseCost, 0.001f)
        assertEquals(0.2f, cargoType.costPerTon, 0.001f)
    }

    @Test
    fun getTotalTonnage_noCargo_returnsZero() {
        val cargo = createCargo()
        assertEquals(0, cargo.getTotalTonnage())
    }

    @Test
    fun getTotalTonnage_singleCargoType_returnsCorrectTonnage() {
        val cargoWithRegular = createCargo(cargoTons = 10)
        val cargoWithFrozen = createCargo(frozenCargoTons = 5)
        val cargoWithSpares = createCargo(sparesTons = 3)
        val cargoWithSecure = createCargo(secureCargoTons = 7)
        
        assertEquals(10, cargoWithRegular.getTotalTonnage())
        assertEquals(5, cargoWithFrozen.getTotalTonnage())
        assertEquals(3, cargoWithSpares.getTotalTonnage())
        assertEquals(7, cargoWithSecure.getTotalTonnage())
    }

    @Test
    fun getTotalTonnage_multipleCargoTypes_returnsSumOfAll() {
        val cargo = createCargo(
            cargoTons = 10,
            frozenCargoTons = 5,
            sparesTons = 3,
            secureCargoTons = 7
        )
        
        assertEquals(25, cargo.getTotalTonnage()) // 10 + 5 + 3 + 7
    }

    @Test
    fun getCostForCargoType_cargo_returnsZero() {
        val cargo = createCargo()
        assertEquals(0f, cargo.getCostForCargoType(CargoType.CARGO, 100), 0.001f)
    }

    @Test
    fun getCostForCargoType_frozenCargo_calculatesCorrectly() {
        val cargo = createCargo()
        // Base cost 0.1 + (tons * 0.01)
        assertEquals(0.1f + (10 * 0.01f), cargo.getCostForCargoType(CargoType.FROZEN_CARGO, 10), 0.001f)
        assertEquals(0.1f + (50 * 0.01f), cargo.getCostForCargoType(CargoType.FROZEN_CARGO, 50), 0.001f)
        assertEquals(0.1f + (0 * 0.01f), cargo.getCostForCargoType(CargoType.FROZEN_CARGO, 0), 0.001f)
    }

    @Test
    fun getCostForCargoType_spares_calculatesCorrectly() {
        val cargo = createCargo()
        // 0.1 MCr per ton
        assertEquals(10 * 0.1f, cargo.getCostForCargoType(CargoType.SPARES, 10), 0.001f)
        assertEquals(25 * 0.1f, cargo.getCostForCargoType(CargoType.SPARES, 25), 0.001f)
        assertEquals(0 * 0.1f, cargo.getCostForCargoType(CargoType.SPARES, 0), 0.001f)
    }

    @Test
    fun getCostForCargoType_secureCargo_calculatesCorrectly() {
        val cargo = createCargo()
        // 0.2 MCr per ton
        assertEquals(10 * 0.2f, cargo.getCostForCargoType(CargoType.SECURE_CARGO, 10), 0.001f)
        assertEquals(30 * 0.2f, cargo.getCostForCargoType(CargoType.SECURE_CARGO, 30), 0.001f)
        assertEquals(0 * 0.2f, cargo.getCostForCargoType(CargoType.SECURE_CARGO, 0), 0.001f)
    }

    @Test
    fun getTotalCargoCost_noCargo_returnsZero() {
        val cargo = createCargo()
        assertEquals(0f, cargo.getTotalCargoCost(), 0.001f)
    }

    @Test
    fun getTotalCargoCost_singleCargoType_calculatesCorrectly() {
        val cargoWithRegular = createCargo(cargoTons = 100)
        val cargoWithFrozen = createCargo(frozenCargoTons = 20)
        val cargoWithSpares = createCargo(sparesTons = 10)
        val cargoWithSecure = createCargo(secureCargoTons = 15)
        
        assertEquals(0f, cargoWithRegular.getTotalCargoCost(), 0.001f) // Free
        assertEquals(0.1f + (20 * 0.01f), cargoWithFrozen.getTotalCargoCost(), 0.001f) // 0.3
        assertEquals(10 * 0.1f, cargoWithSpares.getTotalCargoCost(), 0.001f) // 1.0
        assertEquals(15 * 0.2f, cargoWithSecure.getTotalCargoCost(), 0.001f) // 3.0
    }

    @Test
    fun getTotalCargoCost_multipleCargoTypes_sumsCostsCorrectly() {
        val cargo = createCargo(
            cargoTons = 50,        // 0 MCr (free)
            frozenCargoTons = 20,  // 0.1 + (20 * 0.01) = 0.3 MCr
            sparesTons = 10,       // 10 * 0.1 = 1.0 MCr
            secureCargoTons = 5    // 5 * 0.2 = 1.0 MCr
        )
        
        val expectedTotal = 0f + 0.3f + 1.0f + 1.0f // 2.3 MCr
        assertEquals(expectedTotal, cargo.getTotalCargoCost(), 0.001f)
    }

    @Test
    fun getTonnageForCargoType_returnsCorrectTonnageForEachType() {
        val cargo = createCargo(
            cargoTons = 100,
            frozenCargoTons = 20,
            sparesTons = 30,
            secureCargoTons = 10
        )
        
        assertEquals(100, cargo.getTonnageForCargoType(CargoType.CARGO))
        assertEquals(20, cargo.getTonnageForCargoType(CargoType.FROZEN_CARGO))
        assertEquals(30, cargo.getTonnageForCargoType(CargoType.SPARES))
        assertEquals(10, cargo.getTonnageForCargoType(CargoType.SECURE_CARGO))
    }

    @Test
    fun withUpdatedTonnage_updatesCorrectCargoType() {
        val originalCargo = createCargo(
            cargoTons = 10,
            frozenCargoTons = 20,
            sparesTons = 30,
            secureCargoTons = 40
        )
        
        val updatedRegular = originalCargo.withUpdatedTonnage(CargoType.CARGO, 100)
        val updatedFrozen = originalCargo.withUpdatedTonnage(CargoType.FROZEN_CARGO, 200)
        val updatedSpares = originalCargo.withUpdatedTonnage(CargoType.SPARES, 300)
        val updatedSecure = originalCargo.withUpdatedTonnage(CargoType.SECURE_CARGO, 400)
        
        // Check regular cargo update
        assertEquals(100, updatedRegular.cargoTons)
        assertEquals(20, updatedRegular.frozenCargoTons) // preserved
        assertEquals(30, updatedRegular.sparesTons) // preserved
        assertEquals(40, updatedRegular.secureCargoTons) // preserved
        
        // Check frozen cargo update
        assertEquals(10, updatedFrozen.cargoTons) // preserved
        assertEquals(200, updatedFrozen.frozenCargoTons)
        assertEquals(30, updatedFrozen.sparesTons) // preserved
        assertEquals(40, updatedFrozen.secureCargoTons) // preserved
        
        // Check spares update
        assertEquals(10, updatedSpares.cargoTons) // preserved
        assertEquals(20, updatedSpares.frozenCargoTons) // preserved
        assertEquals(300, updatedSpares.sparesTons)
        assertEquals(40, updatedSpares.secureCargoTons) // preserved
        
        // Check secure cargo update
        assertEquals(10, updatedSecure.cargoTons) // preserved
        assertEquals(20, updatedSecure.frozenCargoTons) // preserved
        assertEquals(30, updatedSecure.sparesTons) // preserved
        assertEquals(400, updatedSecure.secureCargoTons)
    }

    @Test
    fun cargoCalculation_totalTonnage_calculatesCorrectly() {
        val ship = StarShip("Test Ship", "Test Description", 1000, TechLevel.C, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 100,
            frozenCargoTons = 50,
            sparesTons = 25,
            secureCargoTons = 10
        )
        
        val calculation = CargoCalculation(ship, cargo)
        assertEquals(185, calculation.totalTonnage) // 100 + 50 + 25 + 10
    }

    @Test
    fun cargoCalculation_totalCost_calculatesCorrectly() {
        val ship = StarShip("Test Ship", "Test Description", 1000, TechLevel.C, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 100,       // 0 MCr
            frozenCargoTons = 20,  // 0.1 + (20 * 0.01) = 0.3 MCr
            sparesTons = 10,       // 10 * 0.1 = 1.0 MCr
            secureCargoTons = 5    // 5 * 0.2 = 1.0 MCr
        )
        
        val calculation = CargoCalculation(ship, cargo)
        assertEquals(2.3f, calculation.totalCost, 0.001f)
    }

    @Test
    fun cargoCalculation_availableTonnage_calculatesCorrectly() {
        val ship = StarShip("Test Ship", "Test Description", 1000, TechLevel.C, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 100,
            frozenCargoTons = 50
        )
        
        val calculation = CargoCalculation(ship, cargo)
        assertEquals(850, calculation.availableTonnage) // 1000 - (100 + 50)
    }

    @Test
    fun cargoCalculation_nullCargo_handledCorrectly() {
        val ship = StarShip("Test Ship", "Test Description", 1000, TechLevel.C, Configuration.STANDARD)
        val calculation = CargoCalculation(ship, null)
        
        assertEquals(0, calculation.totalTonnage)
        assertEquals(0f, calculation.totalCost, 0.001f)
        assertEquals(0, calculation.cargoTons)
        assertEquals(0, calculation.frozenCargoTons)
        assertEquals(0, calculation.sparesTons)
        assertEquals(0, calculation.secureCargoTons)
        assertEquals(1000, calculation.availableTonnage)
    }

    @Test
    fun realWorldExample_mixedCargo_1000TonMerchant() {
        // Realistic scenario: 1000-ton merchant ship with mixed cargo
        val ship = StarShip("Free Trader", "A small merchant vessel", 1000, TechLevel.C, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 200,       // Standard cargo (free)
            frozenCargoTons = 50,  // Frozen goods: 0.1 + (50 * 0.01) = 0.6 MCr
            sparesTons = 20,       // Ship spares: 20 * 0.1 = 2.0 MCr
            secureCargoTons = 10   // Valuable goods: 10 * 0.2 = 2.0 MCr
        )
        
        val calculation = CargoCalculation(ship, cargo)
        
        assertEquals(280, calculation.totalTonnage)
        assertEquals(4.6f, calculation.totalCost, 0.001f) // 0 + 0.6 + 2.0 + 2.0
        assertEquals(720, calculation.availableTonnage) // 1000 - 280
        
        // Test individual cargo type calculations
        assertEquals(0f, cargo.getCostForCargoType(CargoType.CARGO, 200), 0.001f)
        assertEquals(0.6f, cargo.getCostForCargoType(CargoType.FROZEN_CARGO, 50), 0.001f)
        assertEquals(2.0f, cargo.getCostForCargoType(CargoType.SPARES, 20), 0.001f)
        assertEquals(2.0f, cargo.getCostForCargoType(CargoType.SECURE_CARGO, 10), 0.001f)
    }

    @Test
    fun realWorldExample_highValueCargo_largeShip() {
        // High-value scenario: Large ship carrying mostly secure cargo and spares
        val ship = StarShip("Heavy Freighter", "Large cargo vessel", 5000, TechLevel.D, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 100,        // Regular cargo (free)
            frozenCargoTons = 200,  // Medical supplies: 0.1 + (200 * 0.01) = 2.1 MCr
            sparesTons = 150,       // Large spare parts inventory: 150 * 0.1 = 15.0 MCr
            secureCargoTons = 50    // High-value electronics: 50 * 0.2 = 10.0 MCr
        )
        
        val calculation = CargoCalculation(ship, cargo)
        
        assertEquals(500, calculation.totalTonnage)
        assertEquals(27.1f, calculation.totalCost, 0.001f) // 0 + 2.1 + 15.0 + 10.0
        assertEquals(4500, calculation.availableTonnage)
        
        // Verify that the cost breakdown matches expectations
        assertEquals(0f, cargo.getCostForCargoType(CargoType.CARGO, 100), 0.001f)
        assertEquals(2.1f, cargo.getCostForCargoType(CargoType.FROZEN_CARGO, 200), 0.001f)
        assertEquals(15.0f, cargo.getCostForCargoType(CargoType.SPARES, 150), 0.001f)
        assertEquals(10.0f, cargo.getCostForCargoType(CargoType.SECURE_CARGO, 50), 0.001f)
    }

    @Test
    fun edgeCases_zeroTonnage_handledCorrectly() {
        val cargo = createCargo(cargoTons = 0, frozenCargoTons = 0)
        
        assertEquals(0, cargo.getTotalTonnage())
        assertEquals(0f, cargo.getTotalCargoCost(), 0.001f)
        
        // Even with zero tonnage, frozen cargo still has base cost
        assertEquals(0f, cargo.getCostForCargoType(CargoType.CARGO, 0), 0.001f)
        assertEquals(0.1f, cargo.getCostForCargoType(CargoType.FROZEN_CARGO, 0), 0.001f)
        assertEquals(0f, cargo.getCostForCargoType(CargoType.SPARES, 0), 0.001f)
        assertEquals(0f, cargo.getCostForCargoType(CargoType.SECURE_CARGO, 0), 0.001f)
    }

    private fun createCargo(
        shipId: Int = 1,
        cargoTons: Int = 0,
        frozenCargoTons: Int = 0,
        sparesTons: Int = 0,
        secureCargoTons: Int = 0
    ): Cargo {
        return Cargo(
            shipId = shipId,
            cargoTons = cargoTons,
            frozenCargoTons = frozenCargoTons,
            sparesTons = sparesTons,
            secureCargoTons = secureCargoTons
        )
    }
}