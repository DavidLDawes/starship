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
import org.junit.Assert.assertFalse
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

    // === Screen Tests ===
    
    @Test
    fun getMaxScreenQuantities_techLevelAB_noScreensAvailable() {
        val defense = createDefense()
        assertEquals(Triple(0, 0, 0), defense.getMaxScreenQuantities(TechLevel.A))
        assertEquals(Triple(0, 0, 0), defense.getMaxScreenQuantities(TechLevel.B))
    }
    
    @Test
    fun getMaxScreenQuantities_techLevelC_nuclearAndMesonOnly() {
        val defense = createDefense()
        val expected = Triple(1, 1, 0) // Nuclear: 1, Meson: 1, Black Globe: 0
        assertEquals(expected, defense.getMaxScreenQuantities(TechLevel.C))
    }
    
    @Test
    fun getMaxScreenQuantities_techLevelF_allScreensAvailable() {
        val defense = createDefense()
        val expected = Triple(6, 6, 3) // Nuclear: 6, Meson: 6, Black Globe: 3
        assertEquals(expected, defense.getMaxScreenQuantities(TechLevel.F))
    }
    
    @Test
    fun getMaxScreenQuantities_techLevelJ_maximumQuantities() {
        val defense = createDefense()
        val expected = Triple(14, 12, 6) // Nuclear: 14, Meson: 12, Black Globe: 6
        assertEquals(expected, defense.getMaxScreenQuantities(TechLevel.J))
    }
    
    @Test
    fun getHullCodeCategory_correctCategoryForEachRange() {
        val defense = createDefense()
        
        // Test CA-CE range
        assertEquals(HullCodeCategory.CA_TO_CE, defense.getHullCodeCategory("CA"))
        assertEquals(HullCodeCategory.CA_TO_CE, defense.getHullCodeCategory("CC"))
        assertEquals(HullCodeCategory.CA_TO_CE, defense.getHullCodeCategory("CE"))
        
        // Test CF-CK range  
        assertEquals(HullCodeCategory.CF_TO_CK, defense.getHullCodeCategory("CF"))
        assertEquals(HullCodeCategory.CF_TO_CK, defense.getHullCodeCategory("CH"))
        assertEquals(HullCodeCategory.CF_TO_CK, defense.getHullCodeCategory("CK"))
        
        // Test CL-CQ range
        assertEquals(HullCodeCategory.CL_TO_CQ, defense.getHullCodeCategory("CL"))
        assertEquals(HullCodeCategory.CL_TO_CQ, defense.getHullCodeCategory("CN"))
        assertEquals(HullCodeCategory.CL_TO_CQ, defense.getHullCodeCategory("CQ"))
        
        // Test CR-CV range
        assertEquals(HullCodeCategory.CR_TO_CV, defense.getHullCodeCategory("CR"))
        assertEquals(HullCodeCategory.CR_TO_CV, defense.getHullCodeCategory("CT"))
        assertEquals(HullCodeCategory.CR_TO_CV, defense.getHullCodeCategory("CV"))
        
        // Test CW-CZ range
        assertEquals(HullCodeCategory.CW_TO_CZ, defense.getHullCodeCategory("CW"))
        assertEquals(HullCodeCategory.CW_TO_CZ, defense.getHullCodeCategory("CY"))
        assertEquals(HullCodeCategory.CW_TO_CZ, defense.getHullCodeCategory("CZ"))
        
        // Test unknown code defaults to CA_TO_CE
        assertEquals(HullCodeCategory.CA_TO_CE, defense.getHullCodeCategory("XX"))
    }
    
    @Test
    fun getScreenTonnage_noScreens_returnsZero() {
        val defense = createDefense()
        assertEquals(0f, defense.getScreenTonnage("CA"), 0.001f)
    }
    
    @Test
    fun getScreenTonnage_singleScreens_correctTonnage() {
        // Test with CA hull code category
        val nuclearDefense = createDefense(nuclearDampers = 1)
        val mesonDefense = createDefense(mesonScreens = 1)
        val blackDefense = createDefense(blackGlobes = 1)
        
        assertEquals(20f, nuclearDefense.getScreenTonnage("CA"), 0.001f)
        assertEquals(50f, mesonDefense.getScreenTonnage("CA"), 0.001f)
        assertEquals(10f, blackDefense.getScreenTonnage("CA"), 0.001f)
    }
    
    @Test
    fun getScreenTonnage_multipleScreens_correctTonnage() {
        val defense = createDefense(nuclearDampers = 2, mesonScreens = 1, blackGlobes = 1)
        val expectedTonnage = (2 * 20) + (1 * 50) + (1 * 10) // 40 + 50 + 10 = 100
        assertEquals(expectedTonnage.toFloat(), defense.getScreenTonnage("CA"), 0.001f)
    }
    
    @Test
    fun getScreenTonnage_differentHullCodes_correctTonnage() {
        val defense = createDefense(nuclearDampers = 1)
        
        assertEquals(20f, defense.getScreenTonnage("CA"), 0.001f)  // CA-CE
        assertEquals(30f, defense.getScreenTonnage("CF"), 0.001f)  // CF-CK  
        assertEquals(40f, defense.getScreenTonnage("CL"), 0.001f)  // CL-CQ
        assertEquals(50f, defense.getScreenTonnage("CR"), 0.001f)  // CR-CV
        assertEquals(60f, defense.getScreenTonnage("CW"), 0.001f)  // CW-CZ
    }
    
    @Test
    fun getScreenCost_noScreens_returnsZero() {
        val defense = createDefense()
        assertEquals(0f, defense.getScreenCost("CA"), 0.001f)
    }
    
    @Test
    fun getScreenCost_singleScreens_correctCost() {
        // Test with CA hull code category
        val nuclearDefense = createDefense(nuclearDampers = 1)
        val mesonDefense = createDefense(mesonScreens = 1)
        val blackDefense = createDefense(blackGlobes = 1)
        
        assertEquals(30f, nuclearDefense.getScreenCost("CA"), 0.001f)
        assertEquals(70f, mesonDefense.getScreenCost("CA"), 0.001f)
        assertEquals(100f, blackDefense.getScreenCost("CA"), 0.001f)
    }
    
    @Test
    fun getScreenCost_multipleScreens_correctCost() {
        val defense = createDefense(nuclearDampers = 2, mesonScreens = 1, blackGlobes = 1)
        val expectedCost = (2 * 30) + (1 * 70) + (1 * 100) // 60 + 70 + 100 = 230
        assertEquals(expectedCost.toFloat(), defense.getScreenCost("CA"), 0.001f)
    }
    
    @Test
    fun getScreenCost_differentHullCodes_correctCost() {
        val defense = createDefense(nuclearDampers = 1)
        
        assertEquals(30f, defense.getScreenCost("CA"), 0.001f)   // CA-CE
        assertEquals(40f, defense.getScreenCost("CF"), 0.001f)   // CF-CK  
        assertEquals(50f, defense.getScreenCost("CL"), 0.001f)   // CL-CQ
        assertEquals(60f, defense.getScreenCost("CR"), 0.001f)   // CR-CV
        assertEquals(70f, defense.getScreenCost("CW"), 0.001f)   // CW-CZ
    }
    
    @Test
    fun isCapitalShip_correctClassification() {
        val defense = createDefense()
        assertTrue(defense.isCapitalShip(2001))
        assertTrue(defense.isCapitalShip(10000))
        assertFalse(defense.isCapitalShip(2000))
        assertFalse(defense.isCapitalShip(1000))
    }
    
    @Test
    fun screenCalculations_realWorldExample_TLF_3000TonCapitalShip() {
        // Test realistic scenario: 3000-ton TL F capital ship with screens
        val defense = createDefense(nuclearDampers = 2, mesonScreens = 1, blackGlobes = 1)
        val hullCode = "CA" // 3000 tons = CA hull code
        
        // Expected values based on CA-CE category
        val expectedTonnage = (2 * 20) + (1 * 50) + (1 * 10) // 40 + 50 + 10 = 100 tons
        val expectedCost = (2 * 30) + (1 * 70) + (1 * 100) // 60 + 70 + 100 = 230 MCr
        val expectedMaxQuantities = Triple(6, 6, 3) // TL F maximums
        
        assertEquals(expectedTonnage.toFloat(), defense.getScreenTonnage(hullCode), 0.001f)
        assertEquals(expectedCost.toFloat(), defense.getScreenCost(hullCode), 0.001f)
        assertEquals(expectedMaxQuantities, defense.getMaxScreenQuantities(TechLevel.F))
        assertTrue(defense.isCapitalShip(3000))
    }
    
    @Test
    fun screenCalculations_highTechExample_TLJ_100000TonCapitalShip() {
        // Test TL J ship with maximum screens
        val defense = createDefense(nuclearDampers = 14, mesonScreens = 12, blackGlobes = 6)
        val hullCode = "CQ" // 100000 tons = CQ hull code
        
        // Expected values based on CL-CQ category  
        val expectedTonnage = (14 * 40) + (12 * 70) + (6 * 20) // 560 + 840 + 120 = 1520 tons
        val expectedCost = (14 * 50) + (12 * 90) + (6 * 200) // 700 + 1080 + 1200 = 2980 MCr
        val expectedMaxQuantities = Triple(14, 12, 6) // TL J maximums
        
        assertEquals(expectedTonnage.toFloat(), defense.getScreenTonnage(hullCode), 0.001f)
        assertEquals(expectedCost.toFloat(), defense.getScreenCost(hullCode), 0.001f)
        assertEquals(expectedMaxQuantities, defense.getMaxScreenQuantities(TechLevel.J))
        assertTrue(defense.isCapitalShip(100000))
    }

    @Test
    fun defenseUpdates_multipleChanges_preserveExistingValues() {
        // Test that demonstrates the fix for Issue #77
        // Multiple updates should preserve existing values correctly
        val initialDefense = createDefense(protection = 5, nuclearDampers = 2)
        
        // Verify initial state
        assertEquals(5, initialDefense.armorProtection)
        assertEquals(2, initialDefense.nuclearDampers)
        assertEquals(0, initialDefense.mesonScreens)
        assertEquals(0, initialDefense.blackGlobes)
        
        // Simulate updating meson screens while preserving other values
        val updatedDefense = Defense(
            shipId = initialDefense.shipId,
            armorProtection = initialDefense.armorProtection, // preserved
            nuclearDampers = initialDefense.nuclearDampers,   // preserved  
            mesonScreens = 3,  // updated
            blackGlobes = initialDefense.blackGlobes  // preserved
        ).apply { uid = initialDefense.uid }
        
        // Verify update preserved existing values
        assertEquals(5, updatedDefense.armorProtection)  // preserved
        assertEquals(2, updatedDefense.nuclearDampers)   // preserved
        assertEquals(3, updatedDefense.mesonScreens)     // updated
        assertEquals(0, updatedDefense.blackGlobes)      // preserved
        
        // Simulate updating black globes while preserving other values
        val finalDefense = Defense(
            shipId = updatedDefense.shipId,
            armorProtection = updatedDefense.armorProtection, // preserved
            nuclearDampers = updatedDefense.nuclearDampers,   // preserved  
            mesonScreens = updatedDefense.mesonScreens,       // preserved
            blackGlobes = 1  // updated
        ).apply { uid = updatedDefense.uid }
        
        // Verify final state has all updates preserved
        assertEquals(5, finalDefense.armorProtection)  // still preserved
        assertEquals(2, finalDefense.nuclearDampers)   // still preserved  
        assertEquals(3, finalDefense.mesonScreens)     // still preserved
        assertEquals(1, finalDefense.blackGlobes)      // updated
    }

    private fun createDefense(
        protection: Int = 0,
        nuclearDampers: Int = 0,
        mesonScreens: Int = 0,
        blackGlobes: Int = 0
    ): Defense {
        return Defense(
            shipId = 1,
            armorProtection = protection,
            nuclearDampers = nuclearDampers,
            mesonScreens = mesonScreens,
            blackGlobes = blackGlobes
        )
    }
}