package dev.gideonwhite1029.iseeyou.anticheat

import dev.gideonwhite1029.iseeyou.instance
import dev.gideonwhite1029.iseeyou.toml
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

val suspiciousPhotographers: MutableMap<String, SuspiciousPhotographer> = mutableMapOf()

object AntiCheatListener : Listener {
    init {
        object : BukkitRunnable() {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                suspiciousPhotographers.entries.removeIf {
                    if (currentTime - it.value.lastTagged >
                        Duration.ofMinutes(toml!!.data.recordSuspiciousPlayer.recordMinutes).toMillis()
                    ) {
                        it.value.photographer.stopRecording(toml!!.data.asyncSave)
                        return@removeIf true
                    } else false
                }
            }
        }.runTaskTimer(instance!!, 0, 20 * 60 * 5)
    }

    fun onAntiCheatAction(player: Player) {
        val suspiciousPhotographer = suspiciousPhotographers[player.name]
        if (suspiciousPhotographer != null) {
            suspiciousPhotographers[player.name] =
                suspiciousPhotographer.copy(lastTagged = System.currentTimeMillis())
        } else {
            val photographer = Bukkit
                .getPhotographerManager()
                .createPhotographer(
                    (player.name + "_sus_" + UUID.randomUUID().toString().replace("-".toRegex(), ""))
                        .substring(0, 16),
                    player.location
                )
            if (photographer == null) {
                throw RuntimeException(
                    "Error on create suspicious photographer for player: {name: " + player.name + " , UUID:" + player.uniqueId + "}"
                )
            }
            photographer.setFollowPlayer(player)
            val currentTime = LocalDateTime.now()
            val recordPath: String = toml!!.data.recordSuspiciousPlayer.recordPath
                .replace("\${name}", player.name)
                .replace("\${uuid}", player.uniqueId.toString())
            File(recordPath).mkdirs()
            val recordFile =
                File(recordPath + "/" + currentTime.format(EventListener.DATE_FORMATTER) + ".mcpr")
            if (recordFile.exists()) {
                recordFile.delete()
            }
            recordFile.createNewFile()
            photographer.setRecordFile(recordFile)
            suspiciousPhotographers[player.name] = SuspiciousPhotographer(
                photographer = photographer,
                name = player.name,
                lastTagged = System.currentTimeMillis()
            )
        }
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val suspiciousPhotographer = suspiciousPhotographers[e.player.name]
        if (suspiciousPhotographer != null) {
            suspiciousPhotographer.photographer.stopRecording(toml!!.data.asyncSave)
            suspiciousPhotographers.remove(e.player.name)
        }
    }
}