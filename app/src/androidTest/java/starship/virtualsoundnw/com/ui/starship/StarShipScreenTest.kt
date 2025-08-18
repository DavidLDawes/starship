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

package starship.virtualsoundnw.com.ui.starship

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import starship.virtualsoundnw.com.data.local.database.StarShip
import starship.virtualsoundnw.com.data.local.database.TechLevel
import starship.virtualsoundnw.com.data.local.database.Configuration

/**
 * UI tests for [StarShipScreen].
 */
@RunWith(AndroidJUnit4::class)
class StarShipScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent {
            StarShipScreen(FAKE_DATA, onSave = {})
        }
    }

    @Test
    fun shipInputForm_displaysAllFields() {
        composeTestRule.onNodeWithText("Ship Name").assertExists()
        composeTestRule.onNodeWithText("Description").assertExists() 
        composeTestRule.onNodeWithText("Tonnage").assertExists()
        composeTestRule.onNodeWithText("Tech Level: C").assertExists()
        composeTestRule.onNodeWithText("Configuration: STANDARD").assertExists()
    }

    @Test
    fun saveButton_disabledWhenFieldsEmpty() {
        composeTestRule.onNodeWithText("Save Ship").assertIsNotEnabled()
    }

    @Test
    fun savedShips_displayCorrectly() {
        composeTestRule.onNodeWithText("Name: Enterprise").assertExists()
        composeTestRule.onNodeWithText("Description: Constitution class").assertExists()
        composeTestRule.onNodeWithText("Tonnage: 200").assertExists()
        composeTestRule.onNodeWithText("Tech Level: G").assertExists()
    }
}

private val FAKE_DATA = listOf(
    StarShip("Enterprise", "Constitution class", 200, TechLevel.G, Configuration.STANDARD),
    StarShip("Voyager", "Intrepid class", 300, TechLevel.H, Configuration.DISTRIBUTED)
)
