package dev.gideonwhite1029.iseeyou.anticheat.listeners

import dev.gideonwhite1029.iseeyou.anticheat.AntiCheatListener
import com.gmail.olexorus.themis.api.ActionEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ThemisListener : Listener {
    @EventHandler
    fun onAction(e: ActionEvent) = AntiCheatListener.onAntiCheatAction(e.player)
}