package com.exanthiax.ecocollections.libreforge.trigger

import com.exanthiax.ecocollections.api.event.PlayerCollectionCompleteEvent
import org.bukkit.event.EventHandler
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.trigger.Trigger
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

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
