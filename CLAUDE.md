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
- `./gradlew.bat :app:testDebugUnitTest --tests "*.StarShipViewModelTest"` - Run single test class

### Development Commands
- `./gradlew.bat installDebug` - Install debug APK to connected device
- `./gradlew.bat uninstallDebug` - Uninstall debug APK

## Architecture Overview

### Technology Stack
- **Database**: Room with SQLite
- **DI**: Hilt for dependency injection
- **Async**: Kotlin Coroutines with Flow
- **Testing**: JUnit 4, Hilt Testing, Compose Testing

### Code Architecture

#### Current Screens & ViewModels

#### Package Structure
- `ui.components.*` - Shared UI components
- `ui.theme.*` - Material3 theming
- `data.*` - Repository implementations, interfaces, and services (including CrewCalculationService)
- `data.local.database.*` - Room entities, DAOs, and database
- `data.di.*` - Hilt modules for data layer
- `data.local.di.*` - Hilt modules for database

#### Key Architectural Files

### Testing Strategy

#### Unit Tests (`/test/`)
- Use fake implementations:
- Test ViewModels with `TestDispatcher` and `runTest`
- Mock external dependencies, use fakes for internal components

#### Integration Tests (`/androidTest/`)
- Use `HiltTestRunner` as custom test runner
- Replace production modules with `@TestInstallIn(replaces = [DatabaseModule::class])`
- Test Compose UI with `createComposeRule()` and semantic testing
- Use in-memory Room database for isolation

### State Management Pattern

#### UI State Patterns

#### ViewModel Pattern

#### Common ViewModel Structure

### Room Database Patterns

#### Entity Design
- Entities use `@PrimaryKey(autoGenerate = true)` with `var uid: Int = 0`
- DAOs return `Flow<List<T>>` for reactive queries and `suspend fun` for write operations
- Foreign key relationships with CASCADE delete to StarShip
- Business logic methods in entities for calculations

#### Current Database Entities

#### Database Configuration
- Schema location: `$projectDir/schemas` for version control
- **Database version: Currently 1, empty
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
6. Human reviews and either requests changes or approves for branch/PR creation; apply steps 3 & 4 for changes.

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