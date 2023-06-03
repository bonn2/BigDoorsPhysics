# BigDoorsPhysics

This plugin aims to add collisions to the spigot plugin [BigDoors](https://www.spigotmc.org/resources/big-doors.58669/) through several methods.

## Requirements
- Spigot / Paper >= 1.16.5
- [BigDoors](https://www.spigotmc.org/resources/big-doors.58669/) >= 0.1.8.42
- **Optional** [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)
## The methods
- Shulkers (Recommended)
    - Allows off grid collisions
    - Requires 2 entities per door block
    - Shulker heads may sometimes be visible without client side mod
- Barriers
    - Locked to minecraft grid
    - More solid
    - Completely invisible
    - No extra entities
 
 ## How to build
 1. Pull the project
 2. Run `gradlew build`
 3. Plugin will be placed in `~/build/libs`
