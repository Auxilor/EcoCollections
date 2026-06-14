package com.exanthiax.ecocollections.libreforge.trigger

import com.exanthiax.ecocollections.api.event.PlayerCollectionCompleteEvent
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.Trigger
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.event.EventHandler

object TriggerCollectionComplete : Trigger("complete_collection") {
    override val description = "Fires when the player completes all tiers of a collection."

    override val categories = setOf("player")

    override val parameters = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.LOCATION
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.LOCATION to "The player's location when the collection was completed."
    )

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerCollectionCompleteEvent) {
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
