package dev.gideonwhite1029.iseeyou.anticheat.listeners

import dev.gideonwhite1029.iseeyou.anticheat.AntiCheatListener
import me.rerere.matrix.api.events.PlayerViolationEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MatrixListener : Listener {
    @EventHandler
    fun onPlayerViolation(e: PlayerViolationEvent) = AntiCheatListener.onAntiCheatAction(e.player)
}