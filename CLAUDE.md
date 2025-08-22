# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build Commands
- `./gradlew.bat build` - Full project build and verification
- `./gradlew.bat assembleDebug` - Build debug APK
- `./gradlew.bat assembleRelease` - Build release APK
- `./gradlew.bat clean` - Clean build artifacts

### Testing Commands
- `./gradlew.bat test` - Run all unit tests
- `./gradlew.bat testDebugUnitTest` - Run debug unit tests only
- `./gradlew.bat connectedAndroidTest` - Run instrumentation tests (requires device/emulator)
- `./gradlew.bat :app:testDebugUnitTest --tests "*.StarShipViewModelTest"` - Run single test class

### Development Commands
- `./gradlew.bat installDebug` - Install debug APK to connected device
- `./gradlew.bat uninstallDebug` - Uninstall debug APK

## Architecture Overview

### Technology Stack
- **Android**: Target SDK 35, Min SDK 21
- **UI**: Jetpack Compose with Material3
- **Database**: Room with SQLite
- **DI**: Hilt for dependency injection
- **Async**: Kotlin Coroutines with Flow
- **Testing**: JUnit 4, Hilt Testing, Compose Testing

### Code Architecture

#### MVVM + Repository Pattern + Service Layer
```
Screen (Compose UI)
  ↓ observes StateFlow
ViewModel (@HiltViewModel)
  ↓ calls repository methods + ShipSummaryService
Repository (interface + implementation) 
  ↓ uses DAO
DAO (Room DAO)
  ↓ queries
AppDatabase (Room Database)
```

#### Current Screens & ViewModels
- **StarShipScreen/ViewModel** - Main ship selection and creation
- **EnginesScreen/ViewModel** - Engine configuration (Power Plants, Jump/Maneuver Drives)
- **WeaponsScreen/ViewModel** - Weapon systems and turrets
- **DefensesScreen/ViewModel** - Armor and defensive screens
- **FittingsScreen/ViewModel** - Sensors and computer systems
- **CargoScreen/ViewModel** - Cargo allocation (5 types: Cargo, Spares, Cold Storage, Secured, Xeno)
- **VehiclesScreen/ViewModel** - Vehicle bay configuration

#### Package Structure
- `ui.starship.*` - Main ship selection screen
- `ui.engines.*` - Engine configuration screen and logic
- `ui.weapons.*` - Weapons configuration screen and logic  
- `ui.defenses.*` - Defenses configuration screen and logic
- `ui.fittings.*` - Fittings configuration screen and logic
- `ui.cargo.*` - Cargo configuration screen and logic
- `ui.vehicles.*` - Vehicles configuration screen and logic
- `ui.components.*` - Shared UI components (ComprehensiveShipSummaryPanel)
- `ui.theme.*` - Material3 theming
- `data.*` - Repository implementations, interfaces, and services
- `data.local.database.*` - Room entities, DAOs, and database
- `data.di.*` - Hilt modules for data layer
- `data.local.di.*` - Hilt modules for database

#### Key Architectural Files
- `StarShipDesigner.kt` - Application class with `@HiltAndroidApp`
- `AppDatabase.kt` - Room database with migration support (currently version 10)
- `StarShip.kt`, `Engine.kt`, `Weapon.kt`, `Defense.kt`, `Fitting.kt`, `Cargo.kt` - Room entities and DAOs
- `StarShipRepository.kt` + domain-specific repositories - Repository pattern implementation
- `ShipSummaryService.kt` - Centralized service for comprehensive ship data aggregation
- `ShipSummaryPanel.kt` - Shared UI component for consistent ship summaries across all screens
- `DatabaseModule.kt` & `DataModule.kt` - Hilt dependency injection modules
- `Navigation.kt` - Compose navigation setup

#### Ship Summary Architecture (Cross-Screen Feature)

The ship summary system provides consistent, comprehensive ship data across all configuration screens:

**Components:**
- `ShipSummaryService` - Centralized service that aggregates data from all repositories
- `ComprehensiveShipSummaryPanel` - Shared UI component for displaying complete ship information  
- `ShipSummaryData` - UI data class for comprehensive ship summary
- `ShipSummary` - Domain data class from service layer

**Data Flow:**
```
Screen requests ship summary
  ↓
ViewModel calls ShipSummaryService.getComprehensiveShipSummary()
  ↓  
Service combines data from all repositories (engines, weapons, defenses, fittings, cargo)
  ↓
Service calculates totals (tonnage, costs, fuel requirements)
  ↓
Returns ShipSummary to ViewModel
  ↓
ViewModel converts to ShipSummaryData for UI
  ↓
Screen displays ComprehensiveShipSummaryPanel
```

**Key Features:**
- **Centralized Data**: Single source of truth for ship totals across all systems
- **Real-time Updates**: Reactive flows ensure summaries update when any system changes
- **Consistent UI**: Same summary component used across all configuration screens
- **Complete Information**: Shows engines, weapons, defenses, fittings, cargo with costs/tonnage
- **Calculated Values**: Remaining tonnage, total costs, fuel requirements, service intervals

**Usage Pattern:** All configuration screens (Engines, Weapons, Defenses, Fittings, Cargo, Vehicles) use this system to show users the complete ship state while configuring individual systems.

### Testing Strategy

#### Unit Tests (`/test/`)
- Use fake implementations: `FakeStarShipRepository`, `FakeStarShipDao`
- Test ViewModels with `TestDispatcher` and `runTest`
- Mock external dependencies, use fakes for internal components

#### Integration Tests (`/androidTest/`)
- Use `HiltTestRunner` as custom test runner
- Replace production modules with `@TestInstallIn(replaces = [DatabaseModule::class])`
- Test Compose UI with `createComposeRule()` and semantic testing
- Use in-memory Room database for isolation

### State Management Pattern

#### UI State Patterns
Each screen uses a data class-based UiState pattern:

```kotlin
data class EnginesUiState(
    val ship: StarShip? = null,
    val engines: List<Engine> = emptyList(),
    val powerPlants: List<Engine> = emptyList(),
    val jumpDrives: List<Engine> = emptyList(),
    val maneuverDrives: List<Engine> = emptyList(),
    val fitting: Fitting? = null,
    val weapons: List<Weapon> = emptyList(),
    val shipSummary: ShipSummary? = null,  // Comprehensive ship data
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    // Helper methods for calculations
    fun getTotalEngineTonnage(): Float { ... }
    fun hasRequiredEngines(): Boolean { ... }
}
```

#### ViewModel Pattern
- Expose `StateFlow<UiState>` for UI observation  
- Use `viewModelScope` for coroutine management
- Combine multiple repository flows using `combine()`
- Include `ShipSummaryService` for comprehensive ship data
- Transform domain data to UI state

#### Common ViewModel Structure
```kotlin
@HiltViewModel
class ScreenViewModel @Inject constructor(
    private val specificRepository: SpecificRepository,
    private val starShipRepository: StarShipRepository,
    private val shipSummaryService: ShipSummaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenUiState(isLoading = true))
    val uiState: StateFlow<ScreenUiState> = _uiState.asStateFlow()

    fun loadDataForShip(shipId: Int) {
        viewModelScope.launch {
            combine(
                starShipRepository.starShips,
                specificRepository.getDataForShip(shipId),
                shipSummaryService.getComprehensiveShipSummary(shipId)
            ) { ships, specificData, shipSummary ->
                // Transform to UiState
            }.collect { _uiState.value = it }
        }
    }
}
```

### Room Database Patterns

#### Entity Design
- Entities use `@PrimaryKey(autoGenerate = true)` with `var uid: Int = 0`
- DAOs return `Flow<List<T>>` for reactive queries and `suspend fun` for write operations
- Foreign key relationships with CASCADE delete to StarShip
- Business logic methods in entities for calculations

#### Current Database Entities
- **StarShip** - Core ship definition (name, tons, tech level, configuration)
- **Engine** - Power plants, jump drives, maneuver drives with performance ratings
- **Weapon** - Weapon systems and turret configurations
- **Defense** - Armor protection and defensive screens (nuclear damper, meson screen, black globe)
- **Fitting** - Sensors and computer systems
- **Cargo** - Five cargo types (Cargo, Spares, Cold Storage, Secured Cargo, Xeno Cargo)

#### Database Configuration
- Schema location: `$projectDir/schemas` for version control
- **Database version: Currently 10** (was migrated from 9 for Cargo enhancements)
- Export schema: `true` for migration tracking
- Uses `fallbackToDestructiveMigration()` for development flexibility

### Workflow Process

#### Assignment Phase
1. Operator invokes Claude in terminal at project root
2. Claude gets instructions from github project issues. also called stories or bugs, referred to by the issue number: "Address Starship #105"

#### Implementation Phase  
3. Claude follows baseline architecture and conventions
4. Claude maintains detailed logs including analysis, decisions, files modified, tests, and issues

#### Review Phase
5. Logs copied to story/issue, Claude asks for approval
6. Human reviews and either requests changes or approves for branch/PR creation

### Git Workflow - NEVER Merge Locally

**CRITICAL**: Never use `git merge` locally. Always use GitHub Pull Requests for all merges to main.

#### Proper Git Workflow:
1. **Create Feature Branch**: `git checkout -b feature/issue-XX-description`
2. **Implement Changes**: Make all code changes, add tests, ensure functionality works
3. **Commit Changes**: `git add .` and `git commit -m "description"`
4. **Push Branch**: `git push -u origin feature/issue-XX-description`
5. **Create Pull Request**: Use `gh pr create` with issue linking keywords:
   - Include "Closes #XX", "Fixes #XX", or "Resolves #XX" in PR title or body
   - Example: `gh pr create --title "Fix navigation bug (Fixes #42)" --body "..."`
   - This automatically links and closes the issue when PR is merged
6. **Wait for Approval**: Operator reviews and approves the PR
7. **Operator Merges**: Human operator merges the PR on GitHub (automatically closes linked Issue)
8. **Switch to Main**: `git checkout main`
9. **Pull Changes**: `git pull origin main` to get the merged changes locally

#### Why This Process:
- Maintains clean commit history
- Enables code review process
- Automatically links and closes Issues when PRs are merged (using "Closes #XX" keywords)
- Provides audit trail of all changes
- Ensures human oversight on all merges

## Important Instructions
- Use existing architectural patterns and conventions
- Follow MVVM + Repository + Service pattern with Hilt DI
- Add unit tests with mocking for UI elements and complex logic
- Use Room database patterns for data persistence
- Follow Compose UI patterns with StateFlow observation
- Always run tests before finalizing work

### Ship Summary Requirements (Critical)
- **ALL configuration screens MUST use `ComprehensiveShipSummaryPanel`** - No local summary implementations
- **Include `ShipSummaryService`** in ViewModels for comprehensive ship data
- **Add `shipSummary: ShipSummary?`** field to UiState for complete ship information
- **Use `combine()`** to collect data from specific repository + `ShipSummaryService`
- **Show complete data**: engines, weapons, defenses, fittings, cargo with costs and tonnage
- **Real-time updates**: Summary must reflect all system changes immediately
- **Consistent positioning**: Summary panels should appear at consistent locations across screens

### Copyright Management

When working with files that have copyright headers, follow these rules:

#### For Files with Existing "Copyright (C) 2022 The Android Open Source Project" Headers:
1. **If the file is unchanged since the original template**: Leave the copyright as-is
2. **If the file has been modified since the original template**: Update to dual copyright:
   ```
   * Copyright (C) 2022 The Android Open Source Project, 2025 David L. Dawes
   * Notice: As this license requires, be aware this file has been changed by David L. Dawes since cloning it from github.
   ```

#### For New Files Created:
Use single copyright for David L. Dawes:
```
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
```

**Note**: When modifying any file for the first time that still has the old single copyright, update it to the dual copyright format before making other changes.