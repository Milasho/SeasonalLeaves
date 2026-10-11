# Seasonal Leaves (NeoForge 26.3)

A client-side mod that visually hides leaves according to Serene Seasons. It never removes or edits world blocks. Poplar leaf saturation is adjusted per pixel at resource load time; hue, brightness, and transparency are preserved.

Author: Toucanio

## Intended behavior

- Mid/Late Spring and Summer: leaves visible, with poplar saturation gradually moving from 70% to 85%.
- Early and Mid Autumn: leaves visible; poplar saturation peaks at 100% in Mid Autumn.
- Late Autumn: progressive shedding based on season-cycle ticks.
- Early Winter: mostly bare.
- Mid/Late Winter: all leaves hidden; poplar saturation is 50% in Mid Winter.
- Early Spring: leaves gradually return.

Vanilla and modded leaves are detected through `minecraft:leaves`, the `LeavesBlock` class, or a block registry ID containing `leaf`. The red, orange, and yellow vanilla poplar leaf textures are adjusted in memory without distributing modified copies of Minecraft assets. Their saturation multipliers by sub-season are: Early/Mid/Late Spring 60/70/78%, Early/Mid/Late Summer 82/85/92%, Early/Mid/Late Autumn 96/100/95%, and Early/Mid/Late Winter 70/50/55%. Custom leaf blocks with unrelated IDs and custom rendering pipelines may need a dedicated integration.

## Build requirements

- Java 25 (Minecraft 26.3 requirement)
- Gradle 8.12+ or the Gradle version required by ModDevGradle
- Internet access for Gradle to download Minecraft/NeoForge dependencies
- NeoForge 26.3 development environment

## Build on Windows

1. Install a JDK matching the NeoForge 26.3 MDK requirement.
2. Open this folder in IntelliJ IDEA, or open PowerShell in this folder.
3. Run `gradle build` (if you have Gradle installed). If Gradle reports that the selected `neo_version` does not exist, copy the exact NeoForge version string from the official NeoForge 26.3 MDK and replace `neo_version` in `gradle.properties`.
4. After a successful build, the JAR will be in `build/libs/seasonalleaves-0.1.0.jar`.
5. Put the JAR into the client instance's `mods` folder together with Serene Seasons and its required dependencies.
6. Test in a disposable world, switch seasons using Serene Seasons' controls/commands, and watch `latest.log` for Mixin errors.

## Testing checklist

- Confirm mod loads with Serene Seasons present.
- Confirm no crash on entering a world or rebuilding chunks.
- Confirm progressive late-autumn shedding, no leaves in midwinter, and gradual early-spring restoration.
- Check poplar leaves and other modded leaves, especially those with custom rendering.
- Check graphics mods and shaders, since custom render pipelines may bypass this hook.

## Known limitations

- This is client-side rendering only. The server and world save remain unchanged.
- The mod hides an entire leaf block's render, not individual pixels of a leaf texture.
- Render geometry is invalidated at 1/16 shedding increments so changes appear without manual chunk rebuilds; this can briefly increase chunk rebuild load.
- Poplar saturation changes at sub-season boundaries by updating the in-memory block atlas; no resource-pack reload or Mojang loading screen is used.
- The reflection-based reader deliberately fails open (leaves remain visible) if Serene Seasons is absent or its API cannot be read.
- No Gradle wrapper is bundled in this generated archive; use the official NeoForge 26.3 MDK wrapper or installed Gradle.
