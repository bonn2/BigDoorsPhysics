# BigDoorsPhysics

This plugin aims to add collisions to the spigot plugin [BigDoors](https://www.spigotmc.org/resources/big-doors.58669/) through several methods.

## Requirements
- Paper >= 1.19
- BigDoors >= 0.1.8.42 (Currently only avalible as a dev build [on pim16aap2's official site](https://pim16aap2.nl/BigDoors/))

## The methods
- Shulkers (Recommended)
    - Allows off grid collisions
    - Requires 2 entities per door block
    - Shulker heads may sometimes be visible without client side mode
- Barriers
    - Locked to minecraft grid
    - More solid
    - Completely invisible
    - No extra entities
 
 ## How to build
 1. Pull the project
 2. Create the directory `~/libs/` in the project
 3. Add the latest release of [BigDoors](https://www.spigotmc.org/resources/big-doors.58669/) to the newly created folder
 4. Run `gradlew build`
 5. Plugin will be placed in `~/build/libs`
