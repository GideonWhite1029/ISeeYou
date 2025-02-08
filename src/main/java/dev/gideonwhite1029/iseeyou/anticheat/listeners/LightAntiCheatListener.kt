package dev.gideonwhite1029.iseeyou.anticheat.listeners

import dev.gideonwhite1029.iseeyou.anticheat.AntiCheatListener
import me.vekster.lightanticheat.api.event.LACViolationEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener


class LightAntiCheatListener : Listener {
    @EventHandler
    fun onFlag(e: LACViolationEvent) = AntiCheatListener.onAntiCheatAction(e.player)
}