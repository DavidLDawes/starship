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

#### MVVM + Repository Pattern
```
StarShipScreen (Compose UI) 
  ↓ observes StateFlow
StarShipViewModel (@HiltViewModel)
  ↓ calls repository methods  
StarShipRepository (interface + implementation)
  ↓ uses DAO
StarShipDao (Room DAO)
  ↓ queries
AppDatabase (Room Database)
```

#### Package Structure
- `ui.starship.*` - Compose screens and ViewModels
- `data.*` - Repository implementations and interfaces  
- `data.local.database.*` - Room entities, DAOs, and database
- `data.di.*` - Hilt modules for data layer
- `data.local.di.*` - Hilt modules for database

#### Key Architectural Files
- `StarShipDesigner.kt` - Application class with `@HiltAndroidApp`
- `AppDatabase.kt` - Room database with migration support
- `StarShip.kt` - Room entity and DAO definitions
- `StarShipRepository.kt` - Repository pattern implementation
- `DatabaseModule.kt` & `DataModule.kt` - Hilt dependency injection modules

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

#### UI State
```kotlin
sealed interface StarShipUiState {
    data object Loading : StarShipUiState
    data class Success(val starShips: List<StarShip>) : StarShipUiState
    data class Error(val exception: Throwable) : StarShipUiState
}
```

#### ViewModel Pattern
- Expose `StateFlow<UiState>` for UI observation
- Use `viewModelScope` for coroutine management
- Collect Repository `Flow` and transform to UI state

### Room Database Patterns

#### Entity Design
- Entities use `@PrimaryKey(autoGenerate = true)` with `var uid: Int = 0`
- DAOs return `Flow<List<T>>` for reactive queries
- Use `suspend` functions for write operations

#### Database Configuration
- Schema location: `$projectDir/schemas` for version control
- Database version: Currently 1
- Export schema: `true` for migration tracking

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
5. **Create Pull Request**: Use `gh pr create` or GitHub web interface
6. **Wait for Approval**: Operator reviews and approves the PR
7. **Operator Merges**: Human operator merges the PR on GitHub (closes associated Issue)
8. **Switch to Main**: `git checkout main`
9. **Pull Changes**: `git pull origin main` to get the merged changes locally

#### Why This Process:
- Maintains clean commit history
- Enables code review process
- Automatically links PRs to Issues
- Provides audit trail of all changes
- Ensures human oversight on all merges

## Important Instructions
- Use existing architectural patterns and conventions
- Follow MVVM + Repository pattern with Hilt DI
- Add unit tests with mocking for UI elements and complex logic
- Use Room database patterns for data persistence
- Follow Compose UI patterns with StateFlow observation
- Always run tests before finalizing work
- Each new screen should include the Ship's Summary table, and when the new Screen allows adding items that use Tons and cost MCr, those get summed into the Summary correctly

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