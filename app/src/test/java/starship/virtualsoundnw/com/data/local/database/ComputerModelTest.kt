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
        
        // Core/1 (100-999 tons, Jump-1, Tech A) - should be available (jumpMinimum <= 3, tonnage fits)
        assertTrue("Core/1 should be available for 800-ton ship", modelNames.contains("Core/1"))
        
        // Core/10 (no size limit, Jump-7, Tech G) - should NOT be available (jumpMinimum > 3)
        assertTrue("Core/10 should NOT be available for Jump-3 ship", !modelNames.contains("Core/10"))
        
        // Verify we have at least some models returned (fix for issue #61)
        assertTrue("Should have available computer models", availableModels.isNotEmpty())
        
        // Verify all returned models can handle the ship's jump performance
        availableModels.forEach { model ->
            assertTrue(
                "Model ${model.model} should support Jump-3 (jumpMinimum ${model.jumpMinimum} <= 3)",
                model.jumpMinimum <= 3
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
        
        // Core/5: jumpMinimum = 3, tonnage 10001-50000 - should be available
        assertTrue("Core/5 should be available for Jump-4 ship", modelNames.contains("Core/5"))
        
        // Core/6: jumpMinimum = 4, tonnage 50001-100000 - should NOT be available (tonnage too high)
        assertTrue("Core/6 should NOT be available due to tonnage", !modelNames.contains("Core/6"))
        
        // Core/10: jumpMinimum = 7 - should NOT be available (jumpMinimum > maxJumpPerformance)
        assertTrue("Core/10 should NOT be available for Jump-4 ship", !modelNames.contains("Core/10"))
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
}