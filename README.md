# All-Kotoff-Want

Client-side Fabric project targeting Minecraft Java 26.1.2. Requires Java 25 and the Fabric Loader/Fabric API versions listed in `gradle.properties`. The project uses Fabric Loom 1.18.4.

## Included tools
- **Item Workshop** (`R`): set/clear the held item's custom name; open lore, enchantment and attribute editors. Item changes now use Minecraft's normal creative inventory sync path when the player is in Creative mode.
- **Lore Editor**: edit item lore, separating multiple lines with `|` because the current editor uses a single-line input field.
- **Enchantment Lab**: add/remove enchantments with levels up to 32767 (subject to version/game rules and server restrictions).
- **Attribute Lab**: apply a custom pair of main-hand attack damage/speed modifiers. This replaces the stack's current attribute-modifier component; use carefully.
- **Entity Lab**: generates and sends `/data merge entity` for the nearest matching entity, including custom name, No AI, Silent, Glowing, Invulnerable and No Gravity flags.
- **Passenger Builder**: summons a vehicle entity with one passenger.
- **Spawner Lab**: sends a `/data merge block` command to the block one block below the player, setting entity ID and common spawn timing/range values.

## Important prototype limitations
- In Creative mode, item edits are sent through the vanilla creative-slot synchronization path. In Survival/Adventure, edits are local-only and may be overwritten by server inventory updates. A client-only mod cannot force a vanilla server to accept arbitrary item components in those modes; server-side support or commands are required.
- Enchantment levels above vanilla limits are requested in the client item component, but their persistence and behavior depend on server acceptance and version rules.
- Entity/spawner command tools require permission and are not an NBT editor that bypasses server protections.

Entity, passenger and spawner tools use server commands. They require command permissions (for example, operator permissions in a private world/server). They do not bypass server protections. The entity tool affects the nearest matching entity, not necessarily the one under the crosshair. The spawner tool targets the block immediately below the player; stand on/above the intended spawner.

## Build
1. Install a JDK 25 distribution.
2. Install Gradle 9.7.1 (the version configured for this project).
3. From this directory, run `gradle build` (or `build.bat` on Windows / `./build.sh` on Linux and macOS).
4. The mod JAR should appear in `build/libs/` if the project compiles successfully.
5. A GitHub Actions workflow is included at `.github/workflows/build.yml`. After you upload the project to a GitHub repository, open the repository Actions tab and run **Build All-Kotoff-Want** (or push a commit). When the run succeeds, open it and download the artifact named `All-Kotoff-Want-<commit>`. Inside is the installable mod JAR. If the run fails, open the failed step and copy the complete error log so the source can be corrected.

The source ZIP intentionally does not bundle a Gradle wrapper JAR. This environment has only Java 21, no Gradle installation, and no network access, so I cannot run a real Gradle compile here. The build configuration uses the non-remapped Fabric Loom plugin and Loom `modImplementation` dependencies. It still needs a real build to confirm API compatibility. Do not treat this prototype as a verified working JAR yet.

## Status
This is an evolving source prototype, not a tested release. I performed basic JSON and source-structure checks, but it has not been compiled against Minecraft 26.1.2 in this environment. Verify the build and test in a backup world before using it on a main save.
