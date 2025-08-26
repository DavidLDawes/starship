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

package starship.virtualsoundnw.com.ui.utils

import kotlin.math.ceil

/**
 * Utility functions for rounding ship design values
 */
object RoundingUtils {
    
    /**
     * Round up tons to nearest .1 units
     * Examples: 12.05 -> 12.1, 12.0 -> 12.0, 12.01 -> 12.1
     */
    fun roundUpTons(tons: Double): Double {
        return ceil(tons * 10) / 10.0
    }
    
    /**
     * Round up MCr (MegaCredits) to nearest .1 units
     * Examples: 5.05 -> 5.1, 5.0 -> 5.0, 5.01 -> 5.1
     */
    fun roundUpMCr(costMCr: Double): Double {
        return ceil(costMCr * 10) / 10.0
    }
    
    /**
     * Format tons with rounding up to .1 units for display
     */
    fun formatTons(tons: Double): String {
        return String.format("%.1f", roundUpTons(tons))
    }
    
    /**
     * Format MCr with rounding up to .1 units for display
     */
    fun formatMCr(costMCr: Double): String {
        return String.format("%.1f", roundUpMCr(costMCr))
    }
}