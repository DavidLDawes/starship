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
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [Defense] armor calculations and [ArmorType] specifications.
 */
class DefensesTest {

    @Test
    fun armorType_crystaliron_hasCorrectSpecifications() {
        val armor = ArmorType.CRYSTALIRON
        assertEquals("Crystaliron", armor.displayName)
        assertEquals(TechLevel.A, armor.requiredTechLevel)
        assertEquals(4, armor.protectionPer5Percent)
        assertEquals(0.20f, armor.costMultiplier, 0.001f)
        assertEquals(false, armor.usesMaxTechLevel)
    }

    @Test
    fun armorType_bondedSuperdense_hasCorrectSpecifications() {
        val armor = ArmorType.BONDED_SUPERDENSE
        assertEquals("Bonded Superdense", armor.displayName)
        assertEquals(TechLevel.E, armor.requiredTechLevel)
        assertEquals(6, armor.protectionPer5Percent)
        assertEquals(0.50f, armor.costMultiplier, 0.001f)
        assertEquals(true, armor.usesMaxTechLevel)
    }

    @Test
    fun getArmorType_automaticSelection_correctForTechLevel() {
        val defense = createDefense()
        
        // TL D or less should get Crystaliron
        assertEquals(ArmorType.CRYSTALIRON, defense.getArmorType(TechLevel.A))
        assertEquals(ArmorType.CRYSTALIRON, defense.getArmorType(TechLevel.B))
        assertEquals(ArmorType.CRYSTALIRON, defense.getArmorType(TechLevel.C))
        assertEquals(ArmorType.CRYSTALIRON, defense.getArmorType(TechLevel.D))
        
        // TL E or better should get Bonded Superdense
        assertEquals(ArmorType.BONDED_SUPERDENSE, defense.getArmorType(TechLevel.E))
        assertEquals(ArmorType.BONDED_SUPERDENSE, defense.getArmorType(TechLevel.F))
        assertEquals(ArmorType.BONDED_SUPERDENSE, defense.getArmorType(TechLevel.G))
    }

    @Test
    fun getMaxArmorProtection_autoSelected_crystaliron_limitedByTechLevelOr13() {
        // For TL D or less, automatically selects Crystaliron with max min(TL, 13)
        val defense = createDefense()
        assertEquals(10, defense.getMaxArmorProtection(TechLevel.A)) // TL A = 10, Crystaliron
        assertEquals(11, defense.getMaxArmorProtection(TechLevel.B)) // TL B = 11, Crystaliron
        assertEquals(12, defense.getMaxArmorProtection(TechLevel.C)) // TL C = 12, Crystaliron
        assertEquals(13, defense.getMaxArmorProtection(TechLevel.D)) // TL D = 13, Crystaliron
    }

    @Test
    fun getMaxArmorProtection_autoSelected_bondedSuperdense_limitedByTechLevel() {
        // For TL E or better, automatically selects Bonded Superdense with max = TL
        val defense = createDefense()
        assertEquals(14, defense.getMaxArmorProtection(TechLevel.E)) // TL E = 14, Bonded Superdense
        assertEquals(15, defense.getMaxArmorProtection(TechLevel.F)) // TL F = 15, Bonded Superdense
        assertEquals(16, defense.getMaxArmorProtection(TechLevel.G)) // TL G = 16, Bonded Superdense
    }

    @Test
    fun getArmorTonnage_noProtection_returnsZero() {
        val defense = createDefense(0)
        assertEquals(0f, defense.getArmorTonnage(1000, TechLevel.C), 0.001f)
    }

    @Test
    fun getArmorTonnage_techLevelAB_uses125Percent() {
        // TL A/B use 1.25% per protection point
        val defense = createDefense(1)
        val expectedA = 1000 * 0.0125f // 12.5 tons for 1 protection on 1000-ton ship
        assertEquals(expectedA, defense.getArmorTonnage(1000, TechLevel.A), 0.001f)
        assertEquals(expectedA, defense.getArmorTonnage(1000, TechLevel.B), 0.001f)
    }

    @Test
    fun getArmorTonnage_techLevelCPlus_uses833Percent() {
        // TL C+ use 5/6% (0.833%) per protection point
        val defense = createDefense(1)
        val expectedC = 1000 * (5f/6f/100f) // ~8.333 tons for 1 protection on 1000-ton ship
        assertEquals(expectedC, defense.getArmorTonnage(1000, TechLevel.C), 0.001f)
        assertEquals(expectedC, defense.getArmorTonnage(1000, TechLevel.D), 0.001f)
        assertEquals(expectedC, defense.getArmorTonnage(1000, TechLevel.E), 0.001f)
    }

    @Test
    fun getArmorTonnage_multipleProtection_scalesLinearly() {
        val defense3 = createDefense(3)
        val defense1 = createDefense(1)
        
        val tonnage1 = defense1.getArmorTonnage(1000, TechLevel.C)
        val tonnage3 = defense3.getArmorTonnage(1000, TechLevel.C)
        
        assertEquals(tonnage1 * 3, tonnage3, 0.001f)
    }

    @Test
    fun getArmorCost_noProtection_returnsZero() {
        val defense = createDefense(0)
        assertEquals(0f, defense.getArmorCost(1000, Configuration.STANDARD, TechLevel.C), 0.001f)
    }

    @Test
    fun getArmorCost_crystaliron_autoSelected_uses20PercentOfHullCost() {
        // TL C automatically selects Crystaliron, test with max protection (12)
        val defense = createDefense(12) 
        val shipTons = 1000
        val hullCost = shipTons * 0.1f * 1.0f // Standard config = 100 MCr
        val expectedFullArmorCost = hullCost * 0.20f // 20 MCr for full Crystaliron armor
        
        assertEquals(expectedFullArmorCost, defense.getArmorCost(shipTons, Configuration.STANDARD, TechLevel.C), 0.001f)
    }

    @Test
    fun getArmorCost_bondedSuperdense_autoSelected_uses50PercentOfHullCost() {
        // TL E automatically selects Bonded Superdense, test with max protection (14)
        val defense = createDefense(14)
        val shipTons = 1000
        val hullCost = shipTons * 0.1f * 1.0f // Standard config = 100 MCr
        val expectedFullArmorCost = hullCost * 0.50f // 50 MCr for full Bonded Superdense armor
        
        assertEquals(expectedFullArmorCost, defense.getArmorCost(shipTons, Configuration.STANDARD, TechLevel.E), 0.001f)
    }

    @Test
    fun getArmorCost_partialProtection_scalesProportionally() {
        val maxProtectionDefense = createDefense(12) // Max at TL C (Crystaliron)
        val halfProtectionDefense = createDefense(6) // Half max
        
        val maxCost = maxProtectionDefense.getArmorCost(1000, Configuration.STANDARD, TechLevel.C)
        val halfCost = halfProtectionDefense.getArmorCost(1000, Configuration.STANDARD, TechLevel.C)
        
        assertEquals(maxCost / 2, halfCost, 0.001f)
    }

    @Test
    fun getArmorCost_differentConfigurations_affectsHullCostCalculation() {
        val defense = createDefense(12) // Max protection at TL C (Crystaliron)
        val shipTons = 1000
        
        val standardCost = defense.getArmorCost(shipTons, Configuration.STANDARD, TechLevel.C)
        val needleWedgeCost = defense.getArmorCost(shipTons, Configuration.NEEDLE_WEDGE, TechLevel.C)
        val sphereCost = defense.getArmorCost(shipTons, Configuration.SPHERE, TechLevel.C)
        
        // Needle/Wedge is 1.2x hull cost, so armor cost should be 1.2x
        assertEquals(standardCost * 1.2f, needleWedgeCost, 0.001f)
        
        // Sphere is 0.8x hull cost, so armor cost should be 0.8x
        assertEquals(standardCost * 0.8f, sphereCost, 0.001f)
    }


    @Test
    fun armorCalculations_realWorldExample_1000TonShip() {
        // Test realistic scenario: 1000-ton TL C ship with automatically selected Crystaliron armor
        val defense = createDefense(6) // Half max protection
        val shipTons = 1000
        val techLevel = TechLevel.C
        val config = Configuration.STANDARD
        
        // Expected values (TL C automatically selects Crystaliron)
        val expectedMaxProtection = 12 // TL C = 12, Crystaliron
        val expectedTonnage = 6 * 1000 * (5f/6f/100f) // ~50 tons
        val hullCost = 1000 * 0.1f // 100 MCr
        val expectedCost = hullCost * 0.20f * (6f/12f) // 10 MCr (half of full 20 MCr)
        
        assertEquals(expectedMaxProtection, defense.getMaxArmorProtection(techLevel))
        assertEquals(expectedTonnage, defense.getArmorTonnage(shipTons, techLevel), 0.001f)
        assertEquals(expectedCost, defense.getArmorCost(shipTons, config, techLevel), 0.001f)
        assertEquals(ArmorType.CRYSTALIRON, defense.getArmorType(techLevel))
    }

    @Test
    fun armorCalculations_highTechExample_bondedSuperdense() {
        // Test TL F ship with automatically selected Bonded Superdense armor
        val defense = createDefense(10) // Partial protection
        val shipTons = 2000
        val techLevel = TechLevel.F
        val config = Configuration.STANDARD
        
        // Expected values (TL F automatically selects Bonded Superdense)
        val expectedMaxProtection = 15 // TL F = 15, Bonded Superdense
        val expectedTonnage = 10 * 2000 * (5f/6f/100f) // ~166.67 tons
        val hullCost = 2000 * 0.1f // 200 MCr
        val expectedCost = hullCost * 0.50f * (10f/15f) // ~66.67 MCr
        
        assertEquals(expectedMaxProtection, defense.getMaxArmorProtection(techLevel))
        assertEquals(expectedTonnage, defense.getArmorTonnage(shipTons, techLevel), 0.001f)
        assertEquals(expectedCost, defense.getArmorCost(shipTons, config, techLevel), 0.001f)
        assertEquals(ArmorType.BONDED_SUPERDENSE, defense.getArmorType(techLevel))
    }

    private fun createDefense(protection: Int = 0): Defense {
        return Defense(
            shipId = 1,
            armorProtection = protection
        )
    }
}