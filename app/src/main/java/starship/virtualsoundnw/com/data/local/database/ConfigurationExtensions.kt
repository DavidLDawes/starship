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

/**
 * Extension function to provide user-friendly display names for Configuration enum values.
 */
fun Configuration.displayName(): String {
    return when (this) {
        Configuration.NEEDLE_WEDGE -> "Needle/Wedge"
        Configuration.CONE -> "Cone"
        Configuration.STANDARD -> "Standard"
        Configuration.CLOSE_STRUCTURE -> "Close Structure"
        Configuration.SPHERE -> "Sphere"
        Configuration.DISPERSED_STRUCTURE -> "Dispersed Structure"
        Configuration.PLANETOID -> "Planetoid"
        Configuration.BUFFERED_PLANETOID -> "Buffered Planetoid"
    }
}