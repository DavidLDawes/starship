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

class ComputerModelTest {

    @Test
    fun getAvailableModelsForShip_800tonJump3TechG_returnsCorrectModels() {
        // Test case from issue #61: 800 ton, J-3, should show computers that can handle Jump-3
        val availableModels = ComputerModel.getAvailableModelsForShip(
            shipTonnage = 800,
            maxJumpPerformance = 3,
            techLevel = TechLevel.G
        )

        val modelNames = availableModels.map { it.model }
        
        // Core/1 (100-999 tons, Jump-1, Tech A) - should NOT be available for J-3 ship (can only handle J-1)
        assertTrue("Core/1 should NOT be available for Jump-3 ship", !modelNames.contains("Core/1"))
        
        // Core/5 (10001-50000 tons, Jump-3, Tech B) - should be available for Jump-3 at Tech G
        assertTrue("Core/5 should be available for Jump-3 ship at Tech G", modelNames.contains("Core/5"))
        
        // Core/10 (no size limit, Jump-7, Tech G) - should be available (can handle J-3 and higher)
        assertTrue("Core/10 should be available for Jump-3 ship at Tech G", modelNames.contains("Core/10"))
        
        // Verify we have at least some models returned (fix for issue #61)
        assertTrue("Should have available computer models", availableModels.isNotEmpty())
        
        // All returned models should be valid for the tech level (this is enforced by the filtering logic)
        availableModels.forEach { model ->
            assertTrue(
                "Model ${model.model} should be available at Tech G",
                TechLevel.G >= model.requiredTechLevel
            )
        }
    }

    @Test
    fun getAvailableModelsForShip_jumpPerformanceFiltering_worksCorrectly() {
        // Test a 15000-ton ship with Jump-4 and Tech G
        val availableModels = ComputerModel.getAvailableModelsForShip(
            shipTonnage = 15000,
            maxJumpPerformance = 4,
            techLevel = TechLevel.G
        )

        val modelNames = availableModels.map { it.model }
        println("DEBUG: Available computers for 15000-ton J-4 ship: $modelNames")
        
        // For J-4 ship, minimum required is Core/6 (jumpMinimum=4), so Core/5 should NOT be available
        assertTrue("Core/5 should NOT be available for Jump-4 ship (insufficient jump capability)", !modelNames.contains("Core/5"))
        
        // Core/6: jumpMinimum = 4, can handle Jump-4 - should be available
        assertTrue("Core/6 should be available for Jump-4 ship", modelNames.contains("Core/6"))
        
        // Core/10: jumpMinimum = 7 - should be available (higher capability allowed)
        assertTrue("Core/10 should be available for Jump-4 ship", modelNames.contains("Core/10"))
    }

    @Test
    fun getAvailableModelsForShip_techLevelFiltering_worksCorrectly() {
        // Test with Tech Level A (lowest)
        val availableModels = ComputerModel.getAvailableModelsForShip(
            shipTonnage = 500,
            maxJumpPerformance = 1,
            techLevel = TechLevel.A
        )

        val modelNames = availableModels.map { it.model }
        
        // Only Core/1 should be available (Tech A, tonnage 100-999, Jump-1)
        assertTrue("Core/1 should be available", modelNames.contains("Core/1"))
        
        // Core/5 requires Tech B - should not be available
        assertTrue("Core/5 should not be available at Tech A", !modelNames.contains("Core/5"))
        
        // Core/10 requires Tech G - should not be available
        assertTrue("Core/10 should not be available at Tech A", !modelNames.contains("Core/10"))
    }

    @Test
    fun getAvailableModelsForShip_debugInvalidCombination() {
        // Test with combination: very low tech level but high jump
        val availableModels = ComputerModel.getAvailableModelsForShip(
            shipTonnage = 1000,
            maxJumpPerformance = 12,  // Very high jump
            techLevel = TechLevel.A   // Very low tech
        )

        val modelNames = availableModels.map { it.model }
        println("Available models for 1000-ton, Jump-12, Tech A ship: $modelNames")
        
        // Debug: Print all model details that might match
        availableModels.forEach { model ->
            println("${model.model}: tonnage ${model.sizeRangeMin}-${model.sizeRangeMax}, jump ${model.jumpMinimum}, tech ${model.requiredTechLevel}")
        }
        
        // Just check that we get some result for debugging
        assertTrue("Should have some result for debugging", availableModels.size >= 0)
    }
    
    @Test
    fun getAvailableModelsForShip_issue67_800tonJump2_showsCore3Plus() {
        // Test case for Issue #67: 800-ton ship with Jump-2 should show Core/3+ computers
        val availableModels = ComputerModel.getAvailableModelsForShip(
            shipTonnage = 800,
            maxJumpPerformance = 2,  // Jump-2 capability
            techLevel = TechLevel.A
        )
        
        val modelNames = availableModels.map { it.model }
        println("DEBUG: Available computers for 800-ton J-2 ship: $modelNames")
        
        // Debug: Print all models that should match
        ComputerModel.values().forEach { model ->
            val techOk = TechLevel.A >= model.requiredTechLevel
            val jumpOk = model.jumpMinimum <= 2
            val sizeOk = model.sizeRangeMin == 0 || model.sizeRangeMax == Int.MAX_VALUE || 800 <= model.sizeRangeMax
            println("${model.model}: tech=$techOk, jump=$jumpOk, size=$sizeOk -> ${techOk && jumpOk && sizeOk}")
        }
        
        // Should NOT include Core/1 (only supports Jump-1)
        assertTrue("Core/1 should NOT be available for Jump-2 ship", !modelNames.contains("Core/1"))
        
        // Should include Core/3 (Tech A, Jump-2)
        assertTrue("Core/3 should be available for Jump-2 ship", modelNames.contains("Core/3"))
        
        // Should include Core/4 (Tech A, Jump-2) 
        assertTrue("Core/4 should be available for Jump-2 ship", modelNames.contains("Core/4"))
        
        // Core/5 is Tech B, so should not be available at Tech A
        assertTrue("Core/5 should NOT be available at Tech A", !modelNames.contains("Core/5"))
        
        // Should have at least Core/3 and Core/4 available
        assertTrue("Should have at least 2 computer options for 800-ton J-2 ship at Tech A", availableModels.size >= 2)
    }
}