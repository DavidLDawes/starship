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
import starship.virtualsoundnw.com.ui.fittings.FittingsScreen
import starship.virtualsoundnw.com.ui.weapons.WeaponsScreen
import starship.virtualsoundnw.com.ui.defenses.DefensesScreen
import starship.virtualsoundnw.com.ui.cargo.CargoScreen
import starship.virtualsoundnw.com.ui.vehicles.VehiclesScreen
import starship.virtualsoundnw.com.ui.drones.DronesScreen
import starship.virtualsoundnw.com.ui.custom.CustomScreen
import starship.virtualsoundnw.com.ui.berths.BerthsScreen
import starship.virtualsoundnw.com.ui.review.ReviewScreen

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
                modifier = Modifier.padding(16.dp),
                onNavigateToFittings = { shipId ->
                    navController.navigate("fittings/$shipId")
                }
            )
        }
        composable("fittings/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            FittingsScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToEngines = { shipId ->
                    navController.navigate("engines/$shipId")
                },
                onNavigateToWeapons = { shipId ->
                    navController.navigate("weapons/$shipId")
                }
            )
        }
        composable("weapons/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            WeaponsScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToFittings = { shipId ->
                    navController.navigate("fittings/$shipId")
                },
                onNavigateToDefenses = { shipId ->
                    navController.navigate("defenses/$shipId")
                }
            )
        }
        composable("defenses/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            DefensesScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToWeapons = { shipId ->
                    navController.navigate("weapons/$shipId")
                },
                onNavigateToCargo = { shipId ->
                    navController.navigate("cargo/$shipId")
                }
            )
        }
        composable("cargo/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            CargoScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToDefenses = { shipId ->
                    navController.navigate("defenses/$shipId")
                },
                onNavigateToVehicles = { shipId ->
                    navController.navigate("vehicles/$shipId")
                }
            )
        }
        composable("vehicles/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            VehiclesScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToCargo = { shipId ->
                    navController.navigate("cargo/$shipId")
                },
                onNavigateToDrones = { shipId ->
                    navController.navigate("drones/$shipId")
                }
            )
        }
        composable("drones/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            DronesScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToVehicles = { shipId ->
                    navController.navigate("vehicles/$shipId")
                },
                onNavigateToBerths = { shipId ->
                    navController.navigate("custom/$shipId")
                }
            )
        }
        composable("custom/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            CustomScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToDrones = { shipId ->
                    navController.navigate("drones/$shipId")
                },
                onNavigateToBerths = { shipId ->
                    navController.navigate("berths/$shipId")
                }
            )
        }
        composable("berths/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            BerthsScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToDrones = { shipId ->
                    navController.navigate("custom/$shipId")
                },
                onNavigateToReview = { shipId ->
                    navController.navigate("review/$shipId")
                }
            )
        }
        composable("review/{shipId}") { backStackEntry ->
            val shipId = backStackEntry.arguments?.getString("shipId")?.toIntOrNull() ?: -1
            ReviewScreen(
                shipId = shipId,
                modifier = Modifier.padding(16.dp),
                onNavigateToBerths = { shipId ->
                    navController.navigate("berths/$shipId")
                }
            )
        }
    }
}
