# BigDoorsPhysics

This plugin aims to add collisions to the spigot plugin [BigDoors](https://www.spigotmc.org/resources/big-doors.58669/) through several methods.

## Minecraft Version
Built and tested on [PaperMC](https://papermc.io) 1.19, may work on older versions but also may not

## The methods
- Shulkers
    - Allows off grid collisions
    - Requires 2 entities per door block
- Barriers
    - Locked to minecraft grid
    - More solid


 
 ## How to build
 1. Pull the project
 2. Create the directory `~/libs/` in the project
 3. Add the latest release of [BigDoors](https://www.spigotmc.org/resources/big-doors.58669/) to the newly created folder
 4. Run `gradlew build`
 5. Plugin will be placed in `~/build/libs`
 
