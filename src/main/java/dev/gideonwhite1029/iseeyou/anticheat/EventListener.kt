package dev.gideonwhite1029.iseeyou.anticheat

import dev.gideonwhite1029.iseeyou.utils.InstantReplayManager
import dev.gideonwhite1029.iseeyou.highSpeedPausedPhotographers
import dev.gideonwhite1029.iseeyou.photographers
import dev.gideonwhite1029.iseeyou.toml
import dev.gideonwhite1029.horizon.replay.Photographer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.pow

/**
 * Event listener object, used to listen for player join, move and exit events
 */
object EventListener : Listener {
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH-mm-ss")
    var pauseRecordingOnHighSpeedThresholdPerTickSquared = 0.00

    /**
     * Listen for player joining events
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    @Throws(IOException::class)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerUniqueId = player.uniqueId.toString()
        if (!toml!!.data.shouldRecordPlayer(player)) {
            return
        }
        if (toml!!.data.pauseInsteadOfStopRecordingOnPlayerQuit && photographers.containsKey(playerUniqueId)) {
            val photographer: Photographer = photographers[playerUniqueId]!!
            photographer.resumeRecording()
            photographer.setFollowPlayer(player)
            return
        }
        if (toml!!.data.instantReplay.enabled) {
            InstantReplayManager.watch(player)
        }
        var prefix = player.name
        if (prefix.length > 10) {
            prefix = prefix.substring(0, 10)
        }
        if (prefix.startsWith(".")) { // fix Floodgate
            prefix = prefix.replace(".", "_")
        }
        val photographer = Bukkit
            .getPhotographerManager()
            .createPhotographer(
                (prefix + "_" + UUID.randomUUID().toString().replace("-".toRegex(), "")).substring(0, 16),
                player.location
            )
        if (photographer == null) {
            throw RuntimeException(
                "Error on create photographer for player: {name: " + player.name + " , UUID:" + playerUniqueId + "}"
            )
        }

        val currentTime = LocalDateTime.now()
        val recordPath: String = toml!!.data.recordPath
            .replace("\${name}", player.name)
            .replace("\${uuid}", playerUniqueId)
        File(recordPath).mkdirs()
        val recordFile = File(recordPath + "/" + currentTime.format(DATE_FORMATTER) + ".mcpr")
        if (recordFile.exists()) {
            recordFile.delete()
        }
        recordFile.createNewFile()
        photographer.setRecordFile(recordFile)

        photographers[playerUniqueId] = photographer
        photographer.setFollowPlayer(player)
    }

    /**
     * Listen for player movement events
     */
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val photographer: Photographer = photographers[event.player.uniqueId.toString()] ?: return
        val velocity = event.player.velocity
        if (toml!!.data.pauseRecordingOnHighSpeed.enabled &&
            velocity.x.pow(2.0) + velocity.z.pow(2.0) > pauseRecordingOnHighSpeedThresholdPerTickSquared &&
            !highSpeedPausedPhotographers.contains(photographer)
        ) {
            photographer.pauseRecording()
            highSpeedPausedPhotographers.add(photographer)
        }
        photographer.resumeRecording()
        photographer.setFollowPlayer(event.player)
        highSpeedPausedPhotographers.remove(photographer)
    }

    /**
     * Listen for player exit events
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (toml!!.data.instantReplay.enabled) {
            InstantReplayManager.taskMap[player.uniqueId.toString()]?.cancel()
            InstantReplayManager.taskMap.remove(player.uniqueId.toString())
            InstantReplayManager.player2photographerUUIDMap[player.uniqueId.toString()]?.forEach { uuid ->
                InstantReplayManager.photographerMap[uuid]?.stopRecording(false,false)
            }
        }
        val photographer: Photographer = photographers[player.uniqueId.toString()] ?: return
        highSpeedPausedPhotographers.remove(photographer)
        if (toml!!.data.pauseInsteadOfStopRecordingOnPlayerQuit) {
            photographer.resumeRecording()
        } else {
            photographer.stopRecording(toml!!.data.asyncSave)
            photographers.remove(player.uniqueId.toString())
        }
    }
}
