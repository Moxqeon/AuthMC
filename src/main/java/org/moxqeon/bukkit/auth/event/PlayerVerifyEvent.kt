package org.moxqeon.bukkit.auth.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerVerifyEvent(who: Player, val isSuccess: Boolean) : PlayerEvent(who) {
    override fun getHandlers() = HANDLERS

    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}