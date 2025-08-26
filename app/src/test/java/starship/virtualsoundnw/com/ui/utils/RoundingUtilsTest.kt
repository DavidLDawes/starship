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

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [RoundingUtils]
 */
class RoundingUtilsTest {

    @Test
    fun roundUpTons_exactDecimal_remainsUnchanged() {
        assertEquals(12.0, RoundingUtils.roundUpTons(12.0), 0.001)
        assertEquals(5.1, RoundingUtils.roundUpTons(5.1), 0.001)
        assertEquals(0.2, RoundingUtils.roundUpTons(0.2), 0.001)
    }

    @Test
    fun roundUpTons_needsRoundingUp_roundsToNearestTenth() {
        assertEquals(12.1, RoundingUtils.roundUpTons(12.05), 0.001)
        assertEquals(12.1, RoundingUtils.roundUpTons(12.01), 0.001)
        assertEquals(5.2, RoundingUtils.roundUpTons(5.15), 0.001)
        assertEquals(0.1, RoundingUtils.roundUpTons(0.05), 0.001)
        assertEquals(0.1, RoundingUtils.roundUpTons(0.001), 0.001)
    }

    @Test
    fun roundUpTons_zeroValue_remainsZero() {
        assertEquals(0.0, RoundingUtils.roundUpTons(0.0), 0.001)
    }

    @Test
    fun roundUpTons_negativeValues_roundsUpTowardsZero() {
        assertEquals(-12.0, RoundingUtils.roundUpTons(-12.0), 0.001)
        assertEquals(-12.0, RoundingUtils.roundUpTons(-12.05), 0.001)
        assertEquals(-12.0, RoundingUtils.roundUpTons(-12.01), 0.001)
    }

    @Test
    fun roundUpMCr_exactDecimal_remainsUnchanged() {
        assertEquals(25.0, RoundingUtils.roundUpMCr(25.0), 0.001)
        assertEquals(10.5, RoundingUtils.roundUpMCr(10.5), 0.001)
        assertEquals(0.3, RoundingUtils.roundUpMCr(0.3), 0.001)
    }

    @Test
    fun roundUpMCr_needsRoundingUp_roundsToNearestTenth() {
        assertEquals(25.1, RoundingUtils.roundUpMCr(25.05), 0.001)
        assertEquals(25.1, RoundingUtils.roundUpMCr(25.01), 0.001)
        assertEquals(10.6, RoundingUtils.roundUpMCr(10.55), 0.001)
        assertEquals(0.1, RoundingUtils.roundUpMCr(0.05), 0.001)
        assertEquals(0.1, RoundingUtils.roundUpMCr(0.001), 0.001)
    }

    @Test
    fun roundUpMCr_zeroValue_remainsZero() {
        assertEquals(0.0, RoundingUtils.roundUpMCr(0.0), 0.001)
    }

    @Test
    fun formatTons_returnsProperStringFormat() {
        assertEquals("12.1", RoundingUtils.formatTons(12.05))
        assertEquals("12.0", RoundingUtils.formatTons(12.0))
        assertEquals("0.1", RoundingUtils.formatTons(0.05))
        assertEquals("5.2", RoundingUtils.formatTons(5.15))
    }

    @Test
    fun formatMCr_returnsProperStringFormat() {
        assertEquals("25.1", RoundingUtils.formatMCr(25.05))
        assertEquals("25.0", RoundingUtils.formatMCr(25.0))
        assertEquals("0.1", RoundingUtils.formatMCr(0.05))
        assertEquals("10.6", RoundingUtils.formatMCr(10.55))
    }

    @Test
    fun formatTons_handlesLargeValues() {
        assertEquals("1000.1", RoundingUtils.formatTons(1000.05))
        assertEquals("9999.9", RoundingUtils.formatTons(9999.85))
    }

    @Test
    fun formatMCr_handlesLargeValues() {
        assertEquals("500.1", RoundingUtils.formatMCr(500.05))
        assertEquals("1234.6", RoundingUtils.formatMCr(1234.55))
    }
}