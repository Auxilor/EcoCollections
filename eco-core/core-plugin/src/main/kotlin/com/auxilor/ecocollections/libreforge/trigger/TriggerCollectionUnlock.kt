package com.auxilor.ecocollections.libreforge.trigger

import com.auxilor.ecocollections.api.event.PlayerCollectionUnlockEvent
import com.auxilor.libreforge.toDispatcher
import com.auxilor.libreforge.triggers.Trigger
import com.auxilor.libreforge.triggers.TriggerData
import com.auxilor.libreforge.triggers.TriggerParameter
import org.bukkit.event.EventHandler

object TriggerCollectionUnlock : Trigger("unlock_collection") {
    override val parameters = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.LOCATION
    )

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerCollectionUnlockEvent) {
        this.dispatch(
            event.player.toDispatcher(),
            TriggerData(
                player = event.player,
                location = event.player.location,
                event = event,
                value = 1.0
            )
        )
    }
}
