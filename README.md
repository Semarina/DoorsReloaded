# DoorsReloaded

[![Modrinth](https://img.shields.io/modrinth/dt/doorsreloaded?logo=modrinth&label=Modrinth)](https://modrinth.com/plugin/doorsreloaded)
[![GitHub](https://img.shields.io/github/license/Semarina/DoorsReloaded)](LICENSE)

Modern door quality-of-life plugin/mod for Minecraft servers.

## Features

- Double Doors - Open both doors with a single click.
- Knocking - Left-click to knock on doors, trapdoors, and fence gates, with separate configuration controls.
- Access Control - Permission-based manipulation of iron doors and iron trapdoors.
- Auto-Close - Configurable automatic closing delays.
- Redstone Sync - Double doors can sync with redstone signals.

## Supported Platforms

| Platform | Minecraft Version | Status |
|----------|-------------------|--------|
| Paper | 26.1.x | Supported |
| Purpur | 26.1.x | Supported |
| Folia | 26.1.x | Supported (region-aware) |
| Fabric | 26.1.x | Supported |
| Quilt | 26.1.x | Supported |
| Spigot/Bukkit | - | Not supported |

Java 25 or newer is required.

## Installation

### Paper/Purpur/Folia

1. Download `DoorsReloaded-2.3.0-Paper.jar` from [Modrinth](https://modrinth.com/plugin/doorsreloaded).
2. Place it in your server's `plugins/` folder.
3. Restart the server.

### Fabric/Quilt

1. Install [Fabric Loader](https://fabricmc.net/) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Download `DoorsReloaded-2.3.0-Fabric.jar` from [Modrinth](https://modrinth.com/plugin/doorsreloaded).
3. Place it in your `mods/` folder.
4. Launch the game.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `doorsreloaded.doubledoors` | Open double doors with one click | Everyone |
| `doorsreloaded.knock` | Knock on doors | Everyone |
| `doorsreloaded.irondoors` | Open iron doors by hand | OP |
| `doorsreloaded.reload` | Reload configuration | OP |
| `doorsreloaded.notify.update` | Receive update notifications | OP |

## Building

DoorsReloaded uses Gradle. To build both platform jars:

```bash
./gradlew build
```

The root build runs `copyArtifacts` after the subprojects build. Output jars are located in `build/libs/`:

- `DoorsReloaded-2.3.0-Paper.jar` - For Paper/Purpur/Folia.
- `DoorsReloaded-2.3.0-Fabric.jar` - For Fabric/Quilt.

## Credits

- Original author: [mfnalex](https://github.com/mfnalex) / JEFF Media GbR
- Community continuation: [Semarina](https://github.com/Semarina)

## License

See [LICENSE](LICENSE) for details.
