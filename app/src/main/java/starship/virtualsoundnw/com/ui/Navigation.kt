/*
 * Copyright (C) 2022 The Android Open Source Project, 2025 David L. Dawes
 * Notice: As this license requires, be aware this file has been changed by David L. Dawes since cloning it from github.
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

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import starship.virtualsoundnw.com.ui.starship.StarShipScreen
import starship.virtualsoundnw.com.ui.engines.EnginesScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "ship") {
        composable("ship") { 
            StarShipScreen(
                modifier = Modifier.padding(16.dp),
                onNavigateToEngines = { shipId ->
                    navController.navigate("engines/$shipId")
                }
            ) 
        }
        composable("engines/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            EnginesScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
