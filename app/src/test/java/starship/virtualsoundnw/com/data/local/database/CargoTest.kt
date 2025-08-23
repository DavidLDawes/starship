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
    fun cargoType_spares_hasCorrectSpecifications() {
        val cargoType = CargoType.SPARES
        assertEquals("Spares", cargoType.displayName)
        assertEquals(0f, cargoType.baseCost, 0.001f)
        assertEquals(0.1f, cargoType.costPerTon, 0.001f)
    }

    @Test
    fun cargoType_coldStorage_hasCorrectSpecifications() {
        val cargoType = CargoType.COLD_STORAGE
        assertEquals("Cold Storage", cargoType.displayName)
        assertEquals(0.1f, cargoType.baseCost, 0.001f)
        assertEquals(0.01f, cargoType.costPerTon, 0.001f)
    }

    @Test
    fun cargoType_securedCargo_hasCorrectSpecifications() {
        val cargoType = CargoType.SECURED_CARGO
        assertEquals("Secured Cargo", cargoType.displayName)
        assertEquals(1f, cargoType.baseCost, 0.001f)
        assertEquals(0.2f, cargoType.costPerTon, 0.001f)
    }

    @Test
    fun cargoType_xenoCargo_hasCorrectSpecifications() {
        val cargoType = CargoType.XENO_CARGO
        assertEquals("Xeno Cargo", cargoType.displayName)
        assertEquals(5f, cargoType.baseCost, 0.001f)
        assertEquals(0.25f, cargoType.costPerTon, 0.001f)
    }

    @Test
    fun getTotalTonnage_noCargo_returnsZero() {
        val cargo = createCargo()
        assertEquals(0, cargo.getTotalTonnage())
    }

    @Test
    fun getTotalTonnage_singleCargoType_returnsCorrectTonnage() {
        val cargoWithRegular = createCargo(cargoTons = 10)
        val cargoWithSpares = createCargo(sparesTons = 3)
        val cargoWithColdStorage = createCargo(coldStorageTons = 5)
        val cargoWithSecured = createCargo(securedCargoTons = 7)
        val cargoWithXeno = createCargo(xenoCargoTons = 4)
        
        assertEquals(10, cargoWithRegular.getTotalTonnage())
        assertEquals(3, cargoWithSpares.getTotalTonnage())
        assertEquals(5, cargoWithColdStorage.getTotalTonnage())
        assertEquals(7, cargoWithSecured.getTotalTonnage())
        assertEquals(4, cargoWithXeno.getTotalTonnage())
    }

    @Test
    fun getTotalTonnage_multipleCargoTypes_returnsSumOfAll() {
        val cargo = createCargo(
            cargoTons = 10,
            sparesTons = 3,
            coldStorageTons = 5,
            securedCargoTons = 7,
            xenoCargoTons = 4
        )
        
        assertEquals(29, cargo.getTotalTonnage()) // 10 + 3 + 5 + 7 + 4
    }

    @Test
    fun getCostForCargoType_cargo_returnsZero() {
        val cargo = createCargo()
        assertEquals(0f, cargo.getCostForCargoType(CargoType.CARGO, 100), 0.001f)
    }

    @Test
    fun getCostForCargoType_spares_calculatesCorrectly() {
        val cargo = createCargo()
        // 0.05 MCr per ton
        assertEquals(10 * 0.1f, cargo.getCostForCargoType(CargoType.SPARES, 10), 0.001f)
        assertEquals(25 * 0.1f, cargo.getCostForCargoType(CargoType.SPARES, 25), 0.001f)
        assertEquals(0 * 0.1f, cargo.getCostForCargoType(CargoType.SPARES, 0), 0.001f)
    }

    @Test
    fun getCostForCargoType_coldStorage_calculatesCorrectly() {
        val cargo = createCargo()
        // 0.005 MCr per ton
        assertEquals(10 * 0.01f + 0.1F, cargo.getCostForCargoType(CargoType.COLD_STORAGE, 10), 0.001f)
        assertEquals(50 * 0.01f + 0.1F, cargo.getCostForCargoType(CargoType.COLD_STORAGE, 50), 0.001f)
        assertEquals(0 * 0.01f, cargo.getCostForCargoType(CargoType.COLD_STORAGE, 0), 0.001f)
    }

    @Test
    fun getCostForCargoType_securedCargo_calculatesCorrectly() {
        val cargo = createCargo()
        // Base cost 1.0 + (tons * 0.1) when tons > 0
        assertEquals(1.0f + (10 * 0.2f), cargo.getCostForCargoType(CargoType.SECURED_CARGO, 10), 0.001f)
        assertEquals(1.0f + (30 * 0.2f), cargo.getCostForCargoType(CargoType.SECURED_CARGO, 30), 0.001f)
        assertEquals(0f, cargo.getCostForCargoType(CargoType.SECURED_CARGO, 0), 0.001f)
    }

    @Test
    fun getCostForCargoType_xenoCargo_calculatesCorrectly() {
        val cargo = createCargo()
        // Base cost 5.0 + (tons * 0.25) when tons > 0
        assertEquals(5.0f + (10 * 0.25f), cargo.getCostForCargoType(CargoType.XENO_CARGO, 10), 0.001f)
        assertEquals(5.0f + (50 * 0.25f), cargo.getCostForCargoType(CargoType.XENO_CARGO, 50), 0.001f)
        assertEquals(0f, cargo.getCostForCargoType(CargoType.XENO_CARGO, 0), 0.001f)
    }

    @Test
    fun getTotalCargoCost_noCargo_returnsZero() {
        val cargo = createCargo()
        assertEquals(0f, cargo.getTotalCargoCost(), 0.001f)
    }

    @Test
    fun getTotalCargoCost_singleCargoType_calculatesCorrectly() {
        val cargoWithRegular = createCargo(cargoTons = 100)
        val cargoWithSpares = createCargo(sparesTons = 10)
        val cargoWithColdStorage = createCargo(coldStorageTons = 20)
        val cargoWithSecured = createCargo(securedCargoTons = 15)
        val cargoWithXeno = createCargo(xenoCargoTons = 5)
        
        assertEquals(0f, cargoWithRegular.getTotalCargoCost(), 0.001f) // Free
        assertEquals(10 * 0.1f, cargoWithSpares.getTotalCargoCost(), 0.001f) // 0.5
        assertEquals(20 * 0.01f + 0.1f, cargoWithColdStorage.getTotalCargoCost(), 0.001f) // 0.1
        assertEquals(1.0f + (15 * 0.2f), cargoWithSecured.getTotalCargoCost(), 0.001f) // 2.5
        assertEquals(5.0f + (5 * 0.25f), cargoWithXeno.getTotalCargoCost(), 0.001f) // 6.25
    }

    @Test
    fun getTotalCargoCost_multipleCargoTypes_sumsCostsCorrectly() {
        val cargo = createCargo(
            cargoTons = 50,        // 0 MCr (free)
            sparesTons = 20,       // 20 * 0.1 = 2.0 MCr
            coldStorageTons = 100, // 100 * 0.01 + .1 = 1.1 MCr
            securedCargoTons = 5,  // 1.0 + (5 * 0.2) = 2 MCr
            xenoCargoTons = 2      // 5.0 + (2 * 0.25) = 5.5 MCr
        )
        
        val expectedTotal = 0f + 2.0f + 1.1f + 2f + 5.5f // 10.6 MCr
        assertEquals(expectedTotal, cargo.getTotalCargoCost(), 0.001f)
    }

    @Test
    fun getTonnageForCargoType_returnsCorrectTonnageForEachType() {
        val cargo = createCargo(
            cargoTons = 100,
            coldStorageTons = 20,
            sparesTons = 30,
            securedCargoTons = 10
        )
        
        assertEquals(100, cargo.getTonnageForCargoType(CargoType.CARGO))
        assertEquals(20, cargo.getTonnageForCargoType(CargoType.COLD_STORAGE))
        assertEquals(30, cargo.getTonnageForCargoType(CargoType.SPARES))
        assertEquals(10, cargo.getTonnageForCargoType(CargoType.SECURED_CARGO))
    }

    @Test
    fun withUpdatedTonnage_updatesCorrectCargoType() {
        val originalCargo = createCargo(
            cargoTons = 10,
            coldStorageTons = 20,
            sparesTons = 30,
            securedCargoTons = 40
        )
        
        val updatedRegular = originalCargo.withUpdatedTonnage(CargoType.CARGO, 100)
        val updatedFrozen = originalCargo.withUpdatedTonnage(CargoType.COLD_STORAGE, 200)
        val updatedSpares = originalCargo.withUpdatedTonnage(CargoType.SPARES, 300)
        val updatedSecure = originalCargo.withUpdatedTonnage(CargoType.SECURED_CARGO, 400)
        
        // Check regular cargo update
        assertEquals(100, updatedRegular.cargoTons)
        assertEquals(20, updatedRegular.coldStorageTons) // preserved
        assertEquals(30, updatedRegular.sparesTons) // preserved
        assertEquals(40, updatedRegular.securedCargoTons) // preserved
        
        // Check frozen cargo update
        assertEquals(10, updatedFrozen.cargoTons) // preserved
        assertEquals(200, updatedFrozen.coldStorageTons)
        assertEquals(30, updatedFrozen.sparesTons) // preserved
        assertEquals(40, updatedFrozen.securedCargoTons) // preserved
        
        // Check spares update
        assertEquals(10, updatedSpares.cargoTons) // preserved
        assertEquals(20, updatedSpares.coldStorageTons) // preserved
        assertEquals(300, updatedSpares.sparesTons)
        assertEquals(40, updatedSpares.securedCargoTons) // preserved
        
        // Check secure cargo update
        assertEquals(10, updatedSecure.cargoTons) // preserved
        assertEquals(20, updatedSecure.coldStorageTons) // preserved
        assertEquals(30, updatedSecure.sparesTons) // preserved
        assertEquals(400, updatedSecure.securedCargoTons)
    }

    @Test
    fun cargoCalculation_totalTonnage_calculatesCorrectly() {
        val ship = StarShip("Test Ship", "Test Description", 1000, TechLevel.C, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 100,
            coldStorageTons = 50,
            sparesTons = 25,
            securedCargoTons = 10
        )
        
        val calculation = CargoCalculation(ship, cargo)
        assertEquals(185, calculation.totalTonnage) // 100 + 50 + 25 + 10
    }

    @Test
    fun cargoCalculation_totalCost_calculatesCorrectly() {
        val ship = StarShip("Test Ship", "Test Description", 1000, TechLevel.C, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 100,       // 0 MCr
            coldStorageTons = 20,  // 0.1 + (20 * 0.01) = 0.3 MCr
            sparesTons = 10,       // 10 * 0.1 = 1.0 MCr
            securedCargoTons = 5    // 1 + 5 * 0.2 = 2 MCr
        )
        
        val calculation = CargoCalculation(ship, cargo)
        assertEquals(3.3f, calculation.totalCost, 0.001f)
    }

    @Test
    fun cargoCalculation_availableTonnage_calculatesCorrectly() {
        val ship = StarShip("Test Ship", "Test Description", 1000, TechLevel.C, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 100,
            coldStorageTons = 50
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
        assertEquals(0, calculation.coldStorageTons)
        assertEquals(0, calculation.sparesTons)
        assertEquals(0, calculation.securedCargoTons)
        assertEquals(1000, calculation.availableTonnage)
    }

    @Test
    fun realWorldExample_mixedCargo_1000TonMerchant() {
        // Realistic scenario: 1000-ton merchant ship with mixed cargo
        val ship = StarShip("Free Trader", "A small merchant vessel", 1000, TechLevel.C, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 200,       // Standard cargo (free)
            coldStorageTons = 50,  // Frozen goods: 0.1 + (50 * 0.01) = 0.6 MCr
            sparesTons = 20,       // Ship spares: 20 * 0.1 = 2.0 MCr
            securedCargoTons = 10   // Valuable goods: 1 + 10 * 0.2 = 3.0 MCr
        )
        
        val calculation = CargoCalculation(ship, cargo)
        
        assertEquals(280, calculation.totalTonnage)
        assertEquals(5.6f, calculation.totalCost, 0.001f) // 0 + 0.6 + 2.0 + 3.0
        assertEquals(720, calculation.availableTonnage) // 1000 - 280
        
        // Test individual cargo type calculations
        assertEquals(0f, cargo.getCostForCargoType(CargoType.CARGO, 200), 0.001f)
        assertEquals(0.6f, cargo.getCostForCargoType(CargoType.COLD_STORAGE, 50), 0.001f)
        assertEquals(2.0f, cargo.getCostForCargoType(CargoType.SPARES, 20), 0.001f)
        assertEquals(3.0f, cargo.getCostForCargoType(CargoType.SECURED_CARGO, 10), 0.001f)
    }

    @Test
    fun realWorldExample_highValueCargo_largeShip() {
        // High-value scenario: Large ship carrying mostly secure cargo and spares
        val ship = StarShip("Heavy Freighter", "Large cargo vessel", 5000, TechLevel.D, Configuration.STANDARD)
        val cargo = createCargo(
            cargoTons = 100,        // Regular cargo (free)
            coldStorageTons = 200,  // Medical supplies: 0.1 + (200 * 0.01) = 2.1 MCr
            sparesTons = 150,       // Large spare parts inventory: 150 * 0.1 = 15.0 MCr
            securedCargoTons = 50    // High-value electronics: 1 + 50 * 0.2 = 11.0 MCr
        )
        
        val calculation = CargoCalculation(ship, cargo)
        
        assertEquals(500, calculation.totalTonnage)
        assertEquals(28.1f, calculation.totalCost, 0.001f) // 0 + 2.1 + 15.0 + 11.0
        assertEquals(4500, calculation.availableTonnage)
        
        // Verify that the cost breakdown matches expectations
        assertEquals(0f, cargo.getCostForCargoType(CargoType.CARGO, 100), 0.001f)
        assertEquals(2.1f, cargo.getCostForCargoType(CargoType.COLD_STORAGE, 200), 0.001f)
        assertEquals(15.0f, cargo.getCostForCargoType(CargoType.SPARES, 150), 0.001f)
        assertEquals(11.0f, cargo.getCostForCargoType(CargoType.SECURED_CARGO, 50), 0.001f)
    }

    @Test
    fun edgeCases_zeroTonnage_handledCorrectly() {
        val cargo = createCargo(cargoTons = 0, coldStorageTons = 0)
        
        assertEquals(0, cargo.getTotalTonnage())
        assertEquals(0f, cargo.getTotalCargoCost(), 0.001f)
        
        // Test individual cargo type costs with zero tonnage
        // Regular cargo is always free
        assertEquals(0f, cargo.getCostForCargoType(CargoType.CARGO, 0), 0.001f)
        // Frozen cargo has base cost even with zero tonnage when explicitly requested
        assertEquals(0f, cargo.getCostForCargoType(CargoType.COLD_STORAGE, 0), 0.001f)
        // Spares and secure cargo are per-ton only
        assertEquals(0f, cargo.getCostForCargoType(CargoType.SPARES, 0), 0.001f)
        assertEquals(0f, cargo.getCostForCargoType(CargoType.SECURED_CARGO, 0), 0.001f)
    }

    private fun createCargo(
        shipId: Int = 1,
        cargoTons: Int = 0,
        sparesTons: Int = 0,
        coldStorageTons: Int = 0,
        securedCargoTons: Int = 0,
        xenoCargoTons: Int = 0
    ): Cargo {
        return Cargo(
            shipId = shipId,
            cargoTons = cargoTons,
            sparesTons = sparesTons,
            coldStorageTons = coldStorageTons,
            securedCargoTons = securedCargoTons,
            xenoCargoTons = xenoCargoTons
        )
    }
}