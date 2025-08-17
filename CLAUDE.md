#Claude Instructions

#Baseline
Features of this code:

    Room Database
    Hilt
    ViewModel, read+write
    UI in Compose, list + write (Material3)
    Navigation
    Repository and data source
    Kotlin Coroutines and Flow
    Unit tests
    UI tests using fake data with Hilt

#Workflow Process

##Assignment Phase
1. Operator invokes Claude in a terminal window in the root of the github project for all work
2. Claude gets instructions from stories and bugs as assigned in the terminal ("Read CLAUDE.md and following the instructions there address link-to-story" or "address link-to-bug")

##Implementation Phase
3. Claude does the work normally following the baseline architecture and conventions
4. Claude maintains detailed logs of all work performed including:
   - Analysis of requirements
   - Implementation decisions and rationale
   - Files modified or created
   - Tests written or updated
   - Any issues encountered and resolutions

##Review Phase
5. Logs are copied to the story/issue and Claude asks for approval
6. Human reviews, tests, and either:
   a) Adds requests for fixes, corrections, enhancements, or details needed work back to the story and re-invokes Claude to follow up, OR
   b) Approves work and tells Claude to create a branch and PR for final review, approval and merging

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.
Add unit tests with mocking if needed when adding UI elements and complex logic.

