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
