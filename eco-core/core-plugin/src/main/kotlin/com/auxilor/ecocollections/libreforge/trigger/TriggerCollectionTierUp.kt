package com.auxilor.ecocollections.libreforge.trigger

import com.auxilor.ecocollections.api.event.PlayerCollectionTierUpEvent
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.Trigger
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.event.EventHandler

object TriggerCollectionTierUp : Trigger("tier_up_collection") {
    override val parameters = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.LOCATION
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
