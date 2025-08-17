# starship
Android based Traveller SRD Starship Designer
# Derivation
Started with [Google's Android Architecture Template](https://github.com/android/architecture-templates/tree/base). Following those instructions I ran:
```bash
git clone https://github.com/android/architecture-templates.git --branch base
cd architecture-templates
./customizer.sh starship.virtualsoundnw.com StarShip StarShipDesigner
```
Then I created a [starship repository](https://github.com/DavidLDawes/starship) with an Android .gitignore, MIT License, a trivial README.md, and not much else. I cloned that locally and then copied the updated files from the template over that, resulting in this combined image so far.

With the nicely architected and approved trivial UI in place, I can start building out the required screens:
* Ship (name, description tonnage, TL)
* Engines (Power Plant, Maneuver, Jump)
* Fittings (Bridge, Sensors, Launch Tubes)
* Weapons (Turrets, Bays,...)
* Defenses (Sandcasters, Point Defense Lasers, Armor, Shields)
* Cargo (Cargo Bay, Freezer Bay, Secure Storage, Data Storage)
* Vehicles (Assorted, including fighters ideally)
* Drones (Assorted)
* Berths (Staterooms, Lux Staterooms, Low Berths)
* Ship Design (Item, mass and cost summary, Save, Copy as text (CSV), Print)

I'm largely doing this to get used to using Claude in my Android development.

## Development Workflow

This project uses a structured workflow combining GitHub Projects for backlog management and Claude AI for development work.

### Backlog Management Loop

Use the [DavidLDawes Starship Project](https://github.com/users/DavidLDawes/projects/1) for all backlog management:

1. **Create backlog entries** - Ensure entry requirements are met, include boilerplate Claude section that refers to the CLAUDE.md file for Claude's guidance & instructions including logging requirements, unit tests, etc.
2. **Add new stories** as needed 
3. **Open bugs** as needed, including them in the backlog with bug CLAUDE boilerplate referring to CLAUDE.md guidance
4. **Organize, prioritize, clarify and groom** backlog items
5. **Add structure** - group items into features, features into releases
6. **Repeat** steps 2-5 on a regular basis (ideally weekly sprint reviews)

### Production Software Engineering Loop

For executing work on stories and bugs:

1. **Operator invokes Claude** in a terminal window in the root of the github project
2. **Claude gets instructions** from assigned stories/bugs using terminal commands like:
   - `"Read CLAUDE.md and following the instructions there address [link-to-story]"`
   - `"Read CLAUDE.md and following the instructions there address [link-to-bug]"`
3. **Claude does the work** following baseline architecture and conventions
4. **Logs are copied** to the story/issue and Claude asks for approval
5. **Human reviews and tests**, then either:
   - **a)** Adds requests for fixes, corrections, enhancements back to the story and re-invokes Claude, OR
   - **b)** Approves work and tells Claude to create a branch and PR for final review, approval and merging

### Architecture

See [CLAUDE.md](CLAUDE.md) for detailed AI instructions and baseline architecture information.

