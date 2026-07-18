package com.exanthiax.ecocollections.libreforge.trigger

import com.exanthiax.ecocollections.api.event.PlayerCollectionTierUpEvent
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.Trigger
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.event.EventHandler

object TriggerCollectionTierUp : Trigger("tier_up_collection") {
    override val description = "Fires when the player advances to a new tier in a collection."

    override val categories = setOf("player")

    override val parameters = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.LOCATION,
        TriggerParameter.VALUE
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.LOCATION to "The player's location when the new tier was reached.",
        TriggerParameter.VALUE to "The new tier number that the player has reached."
    )

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerCollectionTierUpEvent) {
        this.dispatch(
            event.player.toDispatcher(),
            TriggerData(
                player = event.player,
                location = event.player.location,
                event = event,
                value = event.tier.toDouble()
            )
        )
    }
}
