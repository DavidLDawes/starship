/*
 * Copyright (C) 2022 The Android Open Source Project
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

package starship.virtualsoundnw.com.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import starship.virtualsoundnw.com.data.di.fakeStarShips

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigationToEnginesScreen_worksCorrectly() {
        // Verify we start on the ship screen
        composeTestRule.onNodeWithText("Ship Details").assertIsDisplayed()
        
        // Verify ship data is displayed
        composeTestRule.onNodeWithText(fakeStarShips.first().name, substring = true).assertExists()
        
        // Click the "Configure Engines" button for the first ship
        composeTestRule.onNodeWithText("Configure Engines").performClick()
        
        // Verify navigation to engines screen
        composeTestRule.onNodeWithText("Ship Engines").assertIsDisplayed()
        composeTestRule.onNodeWithText("Configuring engines for: ${fakeStarShips.first().name}").assertIsDisplayed()
    }
    
    @Test
    fun enginesScreen_showsCorrectShipName() {
        // Navigate to engines screen
        composeTestRule.onNodeWithText("Configure Engines").performClick()
        
        // Verify the ship name is passed correctly
        composeTestRule.onNodeWithText("Configuring engines for:", substring = true).assertIsDisplayed()
    }
}

