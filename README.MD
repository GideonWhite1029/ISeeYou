# ISeeYou

<img src="https://picdm.sunbangyan.cn/2024/02/20/b7ea08f832b52a6dcbc215947d849425.jpeg" alt="Logo" width="100" height="100">

> [!WARNING]
> This plugin can only run on servers using the [Horizon](https://github.com/GideonWhite1029/Horizon) core. It is not compatible with other server cores!
>
> The developers only guarantee that the **latest stable version** of the ISeeYou plugin will work properly on the **latest version** of the Horizon core server. Compatibility with older versions is not maintained.

ISeeYou utilizes the Replay API provided by the [Horizon](https://github.com/GideonWhite1029/Horizon) core to record player actions in `.mcpr` format.

## Thank you

**This plugin is an outcome of the original [ISeeYou](https://github.com/MC-XiaoHei/ISeeYou). If you are looking for a plugin to use [Leaves](https://leavesmc.org/) or [Leaf](https://github.com/Winds-Studio/Leaf) cores, go to the original project**

## Features

- **Automatic Recording**: No manual operation required. By default, the plugin automatically records all players.
- **Flexible Configuration**: You can configure whitelists, blacklists, and recording paths through configuration files.
- **Anti-Cheat Support**: Compatible with multiple anti-cheat plugins. It automatically starts recording when a
  suspicious player is detected.
- **Instant Replay**: You can use commands to review recent gameplay and save it in `.mcpr` format to the server's hard
  drive.

## Anti-Cheat Compatibility

| Name                                                                                                                                          | Availability |
|-----------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| [Themis Anti Cheat](https://www.spigotmc.org/resources/themis-anti-cheat-1-17-1-20-bedrock-support-paper-compatibility-free-optimized.90766/) | ✅ Available  |
| [Matrix](https://matrix.rip/)                                                                                                                 | ✅ Available  |
| [Vulcan Anti-Cheat](https://www.spigotmc.org/resources/vulcan-anti-cheat-advanced-cheat-detection-1-7-1-20-4.83626/)                          | ⚠️ Beta      |
| [AC - Negativity](https://www.spigotmc.org/resources/ac-negativity-spigot-1-8-1-20-bungeecord-velocity.48399/)                                | ⚠️ Beta      |
| [Grim Anticheat](https://www.spigotmc.org/resources/grim-anticheat.99923/)                                                                    | ⚠️ Beta      |
| [LightAntiCheat](https://github.com/tiredvekster/LightAntiCheat)                                                                              | ⚠️ Beta      |
| [SpartanAntiCheat](https://www.spigotmc.org/resources/spartan-anti-cheat-for-java-advanced-anticheat-detection-1-7-1-21-4.25638/)             | ⚠️ Beta      |

> [!TIP]
> If you would like us to add support for more anti-cheat plugins, please create
> an [issue](https://github.com/GideonWhite1029/Horizon/issues) and let us know!

## Usage

### Dependencies

| **Dependency**                                                                                                                                     |   **Optional**   |
|:---------------------------------------------------------------------------------------------------------------------------------------------------|:----------------:|
| Server [**Horizon**](https://github.com/GideonWhite1029/Horizon)                                                                                   |   **Required**   |
| [Themis Anti Cheat](https://www.spigotmc.org/resources/themis-anti-cheat-1-17-1-20-bedrock-support-paper-compatibility-free-optimized.90766/)      |   💡 Optional    |
| [Matrix AntiCheat](https://matrix.rip/)                                                                                                            |   💡 Optional    |
| [Vulcan Anti-Cheat](https://www.spigotmc.org/resources/vulcan-anti-cheat-advanced-cheat-detection-1-7-1-20-4.83626/)                               |   💡 Optional    |
| [AC - Negativity](https://www.spigotmc.org/resources/ac-negativity-spigot-1-8-1-20-bungeecord-velocity.48399/)                                     |   💡 Optional    |
| [Grim Anticheat](https://www.spigotmc.org/resources/grim-anticheat.99923/)                                                                         |   💡 Optional    |
| [LightAntiCheat](https://github.com/tiredvekster/LightAntiCheat)                                                                                   |   💡 Optional    |
| [SpartanAntiCheat](https://www.spigotmc.org/resources/spartan-anti-cheat-for-java-advanced-anticheat-detection-1-7-1-21-4.25638/)                  |   💡 Optional    |

> [!IMPORTANT]
> Before installing an anti-cheat plugin, please ensure that its required dependencies are also installed to avoid
> compatibility issues.

### How to install

1. **Install the Plugin**: Place the plugin file in the plugins directory of the Leaves or Leaf server, and restart the
   server.

2. **Configure Settings**: Edit the `plugins/ISeeYou/config.toml` file to adjust recording parameters and anti-cheat
   settings.

## Config

```toml
# Configuration file comments:
deleteTmpFileOnLoad = true # Delete temporary files on load
pauseInsteadOfStopRecordingOnPlayerQuit = false # Pause recording on player quit instead of stopping it
# Recording save path template. Use ${name} and ${uuid} variables to replace with player name and UUID.
recordPath = "replay/player/${name}@${uuid}"
asyncSave = false # Whether to enable asynchronous saving

# [pauseRecordingOnHighSpeed] Pause recording when the player is moving at high speeds
[pauseRecordingOnHighSpeed]
enabled = false  # Whether to pause recording during high-speed movement
threshold = 20.0  # Speed threshold (in meters/second)

# [filter] Player recording filter settings
[filter]
checkBy = "name"  # Filter by player name ("name") or UUID ("uuid")
recordMode = "blacklist"  # Record mode: either blacklist or whitelist ("whitelist")
blacklist = []  # List of players not to be recorded
whitelist = []  # List of players to be recorded

# [clearOutdatedRecordFile] Clear outdated recording files
[clearOutdatedRecordFile]
enabled = false  # Whether to enable automatic clearing of outdated files
interval = 24  # Interval in hours to clear outdated files
days = 7  # Number of days to retain outdated files

# [recordSuspiciousPlayer] Automatically record suspicious players
[recordSuspiciousPlayer]
enableThemisIntegration = false  # Enable recording of suspicious players reported by Themis
enableMatrixIntegration = false  # Enable recording of suspicious players reported by Matrix
enableVulcanIntegration = false  # Enable recording of suspicious players reported by Vulcan
enableNegativityIntegration = false  # Enable recording of suspicious players reported by Negativity
enableGrimACIntegration = false  # Enable recording of suspicious players reported by GrimAC
enableLightAntiCheatIntegration = false  # Enable recording of suspicious players reported by LightAntiCheat
enableSpartanIntegration = false # Enable recording of suspicious players reported by SpartanAntiCheat
recordMinutes = 5  # Duration to record suspicious players (in minutes)
recordPath = "replay/suspicious/${name}@${uuid}"  # Path template for suspicious player recordings

# [instantReplay] Instant replay settings
[instantReplay]
enabled = false  # Whether to enable instant replay
replayMinutes = 5  # Duration of the replay (in minutes)
createMinutes = 1  # Interval to create a camera for replay (in minutes)
# Instant replay creates a camera every `createMinutes` and removes it after `replayMinutes`.
recordPath = "replay/instant/${name}@${uuid}"  # Path template for instant replay recordings
```

> [!WARNING]
> Enabling features like instant replay or recording suspicious players may increase server performance load.

## Commands

> **`photographer create <name>` and `/instantreplay` commands** can only be executed by players in the game, not
> through the server console.

> **`photographer create <name> [location]` command** can be executed from the console.

> **Before using the `/instantreplay` command**, ensure that the feature is enabled in the plugin's configuration file.

| Command                                 | Description                                                                                                                                                                      | Example                                                                        |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| `photographer create <name> [location]` | Creates a camera<br>`<name>`: Camera name (5–16 characters)<br>`[location]` (optional): Camera position in x y z format. If not provided, the player's current location is used. | `/photographer create MyCamera`<br>`/photographer create MyCamera <x> <y> <z>` |
| `photographer remove <name>`            | Removes a camera<br>`<name>`: The name of the camera to be removed                                                                                                               | `/photographer remove MyCamera`                                                |
| `photographer list`                     | Displays the list of all cameras                                                                                                                                                 | `/photographer list`                                                           |
| `instantreplay`                         | Creates an instant replay for the player executing the command                                                                                                                   | `/instantreplay`                                                               |

## Download

- **Stable Versions**
  - [GitHub Releases](https://github.com/GideonWhite1029/ISeeYou/releases)
 
- **Development Versions**
  - [GitHub Actions CI](https://github.com/GideonWhite1029/ISeeYou/actions/workflows/dev-build.yml?query=is%3Asuccess)

### Building From Scratch

#### 1. Clone the project repository

```shell
git clone https://github.com/GideonWhite1029/ISeeYou.git
```

#### 2. Compile and Build

```shell
./gradlew shadowJar
```

After the build is complete, you will find the `ISeeYou.jar` file in the `build/libs` directory.

## Notes

- This plugin can only run on servers using the [Horizon](https://github.com/GideonWhite1029/Horizon) core. It is not supported on other Spigot or
  its derivatives (such as Paper, Purpur, etc.).
- Please carefully read and configure the `config.toml` file before using the plugin to ensure it operates correctly.
- Although there have been no reports of bugs caused by reloads, it is recommended not to use plugins like `Plugman` to
  hot-reload this plugin, as this may cause unknown issues!