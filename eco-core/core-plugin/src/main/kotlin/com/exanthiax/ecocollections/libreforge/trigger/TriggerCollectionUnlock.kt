package com.exanthiax.ecocollections.libreforge.trigger

import com.exanthiax.ecocollections.api.event.PlayerCollectionUnlockEvent
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.Trigger
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.event.EventHandler

object TriggerCollectionUnlock : Trigger("unlock_collection") {
    override val description = "Fires when the player unlocks a collection."

    override val categories = setOf("player")

    override val parameters = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.LOCATION
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.LOCATION to "The player's location when the collection was unlocked."
    )

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerCollectionUnlockEvent) {
        this.dispatch(
            event.player.toDispatcher(),
            TriggerData(
                player = event.player,
                location = event.player.location,
                event = event
            )
        )
    }
}
