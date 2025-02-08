package dev.gideonwhite1029.iseeyou

import dev.gideonwhite1029.iseeyou.anticheat.AntiCheatListener
import dev.gideonwhite1029.iseeyou.anticheat.EventListener
import dev.gideonwhite1029.iseeyou.anticheat.suspiciousPhotographers
import dev.gideonwhite1029.iseeyou.utils.ConfigData
import dev.gideonwhite1029.iseeyou.utils.InstantReplayManager
import dev.gideonwhite1029.iseeyou.utils.TomlEx
import dev.gideonwhite1029.horizon.replay.Photographer
import dev.gideonwhite1029.iseeyou.anticheat.listeners.GrimACListener
import dev.gideonwhite1029.iseeyou.anticheat.listeners.LightAntiCheatListener
import dev.gideonwhite1029.iseeyou.anticheat.listeners.MatrixListener
import dev.gideonwhite1029.iseeyou.anticheat.listeners.NegativityListener
import dev.gideonwhite1029.iseeyou.anticheat.listeners.SpartanListener
import dev.gideonwhite1029.iseeyou.anticheat.listeners.ThemisListener
import dev.gideonwhite1029.iseeyou.anticheat.listeners.VulcanListener
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.io.path.isDirectory
import kotlin.math.pow

var toml: TomlEx<ConfigData>? = null
var photographers = mutableMapOf<String, Photographer>()
var highSpeedPausedPhotographers = mutableSetOf<Photographer>()
var instance: JavaPlugin? = null

@Suppress("unused")
class ISeeYou : JavaPlugin(), CommandExecutor {
    private var outdatedRecordRetentionDays: Int = 0
    private val commandPhotographersNameUUIDMap = mutableMapOf<String, String>() // Name => UUID

    override fun onLoad() = CommandAPI.onLoad(
        CommandAPIBukkitConfig(this)
            .verboseOutput(false)
            .silentLogs(true)
    )

    override fun onEnable() {
        instance = this
        CommandAPI.onEnable()
        registerCommand()
        setupConfig()

        logInfo(" ___ ____          __   __          ")
        logInfo("|_ _/ ___|  ___  __\\ \\ / /__  _   _ ")
        logInfo(" | |\\___ \\ / _ \\/ _ \\ V / _ \\| | | |")
        logInfo(" | | ___) |  __/  __/| | (_) | |_| |")
        logInfo("|___|____/ \\___|\\___||_|\\___/ \\__,_|")

        if (toml != null) {
            if (toml!!.data.deleteTmpFileOnLoad) {
                try {
                    Files.walk(Paths.get(toml!!.data.recordPath), Int.MAX_VALUE, FileVisitOption.FOLLOW_LINKS).use { paths ->
                        paths.filter { it.isDirectory() && it.fileName.toString().endsWith(".tmp") }
                            .forEach { deleteTmpFolder(it) }
                    }
                } catch (_: IOException) {
                }
            }

            EventListener.pauseRecordingOnHighSpeedThresholdPerTickSquared = (toml!!.data.pauseRecordingOnHighSpeed.threshold / 20).pow(2.0)

            if (toml!!.data.clearOutdatedRecordFile.enabled) {
                cleanOutdatedRecordings()
                var interval = toml!!.data.clearOutdatedRecordFile.interval
                if (interval !in 1..24) {
                    interval = 24
                    logWarning("Loading the cleanup interval parameter failed and has been reset to the default value of 24.")
                }
                object : BukkitRunnable() {
                    override fun run() = cleanOutdatedRecordings()
                }.runTaskTimer(this, 0, 20 * 60 * 60 * interval.toLong())
            }

            Bukkit.getPluginManager().registerEvents(EventListener, this)
        } else {
            logError("Configuration initialization failed and the plugin cannot be enabled.")
            Bukkit.getPluginManager().disablePlugin(this)
        }

        Bukkit.getPluginManager().registerEvents(AntiCheatListener, this)

        registerThirdPartyListeners()
    }

    private fun registerThirdPartyListeners() {
        if (Bukkit.getPluginManager().isPluginEnabled("Themis") && toml!!.data.recordSuspiciousPlayer.enableThemisIntegration) {
            Bukkit.getPluginManager().registerEvents(ThemisListener(), this)
            logInfo("Register the Themis listener...")
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Matrix") && toml!!.data.recordSuspiciousPlayer.enableMatrixIntegration) {
            Bukkit.getPluginManager().registerEvents(MatrixListener(), this)
            logInfo("Register a Matrix listener...")
        }

        if (Bukkit.getPluginManager()
                .isPluginEnabled("Vulcan") && toml!!.data.recordSuspiciousPlayer.enableVulcanIntegration
        ) {
            Bukkit.getPluginManager().registerEvents(VulcanListener(), this)
            logInfo("Register the Vulcan listener...")
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Negativity") && toml!!.data.recordSuspiciousPlayer.enableNegativityIntegration) {
            Bukkit.getPluginManager().registerEvents(NegativityListener(), this)
            logInfo("Register the Negativity listener...")
        }

        if (Bukkit.getPluginManager().isPluginEnabled("GrimAC") && toml!!.data.recordSuspiciousPlayer.enableGrimACIntegration) {
            Bukkit.getPluginManager().registerEvents(GrimACListener(), this)
            logInfo("Register a GrimAC listener...")
        }

        if (Bukkit.getPluginManager()
                .isPluginEnabled("LightAntiCheat") && toml!!.data.recordSuspiciousPlayer.enableLightAntiCheatIntegration
        ) {
            Bukkit.getPluginManager().registerEvents(LightAntiCheatListener(), this)
            logInfo("Register the LightAntiCheat listener...")
        }

        if (Bukkit.getPluginManager()
                .isPluginEnabled("Spartan") && toml!!.data.recordSuspiciousPlayer.enableSpartanIntegration
        ) {
            Bukkit.getPluginManager().registerEvents(SpartanListener(), this)
            logInfo("Register the Spartan listener...")
        }
    }

    private fun registerCommand() {
        commandTree("photographer") {
            literalArgument("create") {
                stringArgument("name") {
                    playerExecutor { player, args ->
                        val location = player.location
                        val name = args["name"] as String
                        if (name.length !in 4..16) {
                            player.sendMessage("Camera name length must be between 4-16！")
                            return@playerExecutor
                        }
                        createPhotographer(name, location)
                        player.sendMessage("Camera created successfully：$name")
                    }
                    locationArgument("location") {
                        anyExecutor { sender, args ->
                            val location = args["location"] as Location
                            val name = args["name"] as String
                            if (name.length !in 4..16) {
                                sender.sendMessage("Camera name length must be between 4-16！")
                                return@anyExecutor
                            }
                            createPhotographer(name, location)
                            sender.sendMessage("Camera created successfully：$name")
                        }
                    }
                }
            }
            literalArgument("remove") {
                stringArgument("name") {
                    replaceSuggestions(ArgumentSuggestions.strings(commandPhotographersNameUUIDMap.keys.toList())) // 这不会工作
                    anyExecutor { sender, args ->
                        val name = args["name"] as String
                        val uuid = commandPhotographersNameUUIDMap[name] ?: run {
                            sender.sendMessage("The camera does not exist！")
                            return@anyExecutor
                        }
                        photographers[uuid]?.stopRecording(toml!!.data.asyncSave)
                        sender.sendMessage("Camera removed successfully：$name")
                    }
                }
            }
            literalArgument("list") {
                anyExecutor { sender, _ ->
                    val photographerNames = commandPhotographersNameUUIDMap.keys.joinToString(", ")
                    sender.sendMessage("Camera list：$photographerNames")
                }
            }
        }
        commandTree("instantreplay") {
            playerExecutor { player, _ ->
                if (InstantReplayManager.replay(player)) {
                    player.sendMessage("Instant replay created successfully")
                } else {
                    player.sendMessage("The operation was too fast and instant playback creation failed.！")
                }
            }
            playerArgument("player") {

            }
        }
    }

    private fun createPhotographer(name: String, location: Location) {
        val photographer = Bukkit
            .getPhotographerManager()
            .createPhotographer(name, location)
        if (photographer == null) throw RuntimeException("Failed to create a camera: $name")
        val uuid = UUID.randomUUID().toString()

        photographer.teleport(location)
        photographers[uuid] = photographer
        commandPhotographersNameUUIDMap[name] = uuid
        val currentTime = LocalDateTime.now()
        val recordPath: String = toml!!.data.recordPath
            .replace("\${name}", "$name@Command")
            .replace("\${uuid}", uuid)
        File(recordPath).mkdirs()
        val recordFile = File(recordPath + "/" + currentTime.format(EventListener.DATE_FORMATTER) + ".mcpr")
        if (recordFile.exists()) recordFile.delete()
        recordFile.createNewFile()
        photographer.setRecordFile(recordFile)
    }

    private fun setupConfig() {
        toml = TomlEx("plugins/ISeeYou/config.toml", ConfigData::class.java)
        val errMsg = toml!!.data.isConfigValid()
        if (errMsg != null) {
            throw InvalidConfigurationException(errMsg)
        }
        toml!!.data.setConfig()
        outdatedRecordRetentionDays = toml!!.data.clearOutdatedRecordFile.days
        toml!!.save()
    }

    private fun deleteTmpFolder(folderPath: Path) {
        try {
            Files.walkFileTree(folderPath, EnumSet.noneOf(FileVisitOption::class.java), Int.MAX_VALUE, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                }
            })
        } catch (e: IOException) {
            logSevere("An error occurred while deleting a temporary folder: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun cleanOutdatedRecordings() {
        try {
            val recordPathA: String = toml!!.data.recordPath
            val recordingsDirA = Paths.get(recordPathA).parent
            val recordingsDirB: Path? =
                if (toml!!.data.recordSuspiciousPlayer.enableMatrixIntegration || toml!!.data.recordSuspiciousPlayer.enableThemisIntegration) {
                    Paths.get(toml!!.data.recordSuspiciousPlayer.recordPath).parent
                } else {
                    null
                }

            logInfo("Start deleting expired record files in $recordingsDirA and $recordingsDirB")
            var deletedCount = 0

            deletedCount += deleteFilesInDirectory(recordingsDirA)
            recordingsDirB?.let {
                deletedCount += deleteFilesInDirectory(it)
            }

            logInfo("Deleted expired record files, deleted $deletedCount files")
        } catch (e: IOException) {
            logSevere("An error occurred while cleaning out expiration records: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun deleteFilesInDirectory(directory: Path): Int {
        var count = 0
        Files.walk(directory).use { paths ->
            paths.filter { Files.isDirectory(it) && it.parent == directory }
                .forEach { folder ->
                    count += deleteRecordingFiles(folder)
                }
        }
        return count
    }

    private fun deleteRecordingFiles(folderPath: Path): Int {
        var deletedCount = 0
        var fileCount = 0
        try {
            val currentDate = LocalDate.now()
            Files.walk(folderPath).use { paths ->
                paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".mcpr") }
                    .forEach { file ->
                        fileCount++
                        val fileName = file.fileName.toString()
                        val creationDateStr = fileName.substringBefore('@')
                        val creationDate = LocalDate.parse(creationDateStr)
                        val daysSinceCreation = Duration.between(creationDate.atStartOfDay(), currentDate.atStartOfDay()).toDays()
                        if (daysSinceCreation > outdatedRecordRetentionDays) {
                            val executor = Executors.newSingleThreadExecutor()
                            val future = executor.submit(Callable {
                                try {
                                    Files.delete(file)
                                    logInfo("Deleted record file: $fileName")
                                    true
                                } catch (e: IOException) {
                                    logSevere("An error occurred while deleting the record file: $fileName, error: ${e.message}")
                                    e.printStackTrace()
                                    false
                                }
                            })

                            try {
                                if (future.get(2, TimeUnit.SECONDS)) {
                                    deletedCount++
                                }
                            } catch (e: TimeoutException) {
                                logWarning("Delete file timeout: $fileName. Skip this file...")
                                future.cancel(true)
                            } finally {
                                executor.shutdown()
                            }
                        }
                    }
            }
            if (fileCount == 0 || deletedCount == 0) {
                logInfo("The expired record file that needs to be deleted was not found.")
            }
        } catch (e: IOException) {
            logSevere("An error occurred while processing folders: $folderPath, error: ${e.message}")
            e.printStackTrace()
        }
        return deletedCount
    }

    override fun onDisable() {
        CommandAPI.onDisable()
        for (photographer in photographers.values) {
            photographer.stopRecording(toml!!.data.asyncSave)
        }
        photographers.clear()
        highSpeedPausedPhotographers.clear()
        suspiciousPhotographers.clear()
        instance = null
    }

    private fun logInfo(message: String) {
        logger.info("\u001B[32m[INFO] $message\u001B[0m")
    }

    private fun logWarning(message: String) {
        logger.warning("\u001B[33m[WARNING] $message\u001B[0m")
    }

    private fun logSevere(message: String) {
        logger.severe("\u001B[31m[DANGER] $message\u001B[0m")
    }

    private fun logError(message: String) {
        logger.severe("\u001B[31m[ERROR] $message\u001B[0m")
    }
}