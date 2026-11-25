# Draconic Evolution

## Minecraft 26.1.2 · NeoForge

This branch is an unofficial port of Draconic Evolution by brandon3055. It builds on [the original 1.21 branch](https://github.com/Draconic-Inc/Draconic-Evolution/tree/1.21) and [MehanikTMYT’s 26.1.2 migration](https://github.com/MehanikTMYT/Draconic-Evolution/tree/port/26.1.2).

### Installation

Use Java 25 and Minecraft **26.1.2** with NeoForge **26.1.2.126**. Place the following three files together in the instance’s `mods` directory on the client and the dedicated server.

| File | Provides |
| --- | --- |
| `Draconic-Evolution-26.1.2-3.1.4-port.2.jar` | Draconic Evolution |
| `BrandonsCore-3.2.1+26.1.2-port.1.jar` | Brandon’s Core |
| `codechickenlib-4.7.0+26.1.2-port.2.jar` | CodeChicken Lib and its embedded Quack library |

### Tested on 26.1.2

The client entered singleplayer and joined a dedicated NeoForge server with the three jars listed above. Both loaded all 179 Draconic Evolution recipes, including 39 fusion recipes.

Gameplay checks covered crafting a Wyvern Sword using six injectors and eight million OP, installing and charging a Wyvern energy module, using a bound Dislocator, linking energy crystals, and assembling a reactor. A server restart preserved machine inventories, energy and filters. World generation produced Draconium ore in the Overworld and a Chaos Island with its crystal in the End.

### Building from source

Install Git, Python 3.8 or later and a JDK 25. From the root of this branch run the following command.

```sh
python gradle/build-port.py build
```

The script builds the pinned library sources, applies the port’s patches and runs the Draconic Evolution Gradle build. Set `DE_BUILD_ROOT` to choose the working directory. It defaults to `.gradle/port-work` inside the checkout. The finished mod is under `Draconic-Evolution/build/libs` in that directory, and its two required libraries are under `deps`. All three projects retain their original licenses.

[![](http://cf.way2muchnoise.eu/short_draconic-evolution_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/draconic-evolution)
[![](http://cf.way2muchnoise.eu/versions/Available%20for_draconic-evolution_full.svg)](https://www.curseforge.com/minecraft/mc-mods/draconic-evolution/files)
[![](https://img.shields.io/discord/474753901451542531.svg?logo=discord&label=Discord&labelColor=2d2d2d&style=flat)](https://discord.gg/e2HBEtF)
[![](https://img.shields.io/badge/Nexus%20index-maven-blue?style=flat&labelColor=2d2d2d)](https://nexus.covers1624.net/#browse/browse:maven-hosted:com%2Fbrandon3055%2Fdraconicevolution%2FDraconic-Evolution)
[![](https://img.shields.io/badge/Patreon-brandon3055-red?style=flat&labelColor=2d2d2d&logo=patreon)](https://www.patreon.com/brandon3055)

Draconic Evolution adds powerful new tools, Items, weapons and Armor. The premise is the enigmatic ore - Draconium. As you progress you find it has many uses from the mundane such as machines and items, to the near-mystical at the top with Awakened Draconium to make extremely powerful armor and tools.

## License / Use in Modpacks
This mod is licensed under the [**Don't Be a Jerk License**](https://github.com/Draconic-Inc/Draconic-Evolution/blob/HEAD/LICENSE.md) created by CoFH.
I hereby grant permission to use this mod in any mod pack without the need to request permission from myself the owner (brandon3055).
