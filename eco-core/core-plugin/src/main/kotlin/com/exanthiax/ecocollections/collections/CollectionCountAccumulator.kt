package com.exanthiax.ecocollections.collections

import com.willfp.eco.core.integrations.afk.AFKManager
import com.exanthiax.ecocollections.api.giveCollectionCount
import com.exanthiax.ecocollections.plugin
import com.willfp.libreforge.counters.Accumulator
import org.bukkit.GameMode
import org.bukkit.entity.Player

class CollectionCountAccumulator(
    private val collection: Collection
) : Accumulator {
    override fun accept(player: Player, count: Double) {
        if (!player.canGainCollectionProgress()) return

        player.giveCollectionCount(collection, count)
    }
}

fun Player.canGainCollectionProgress(): Boolean {
    if (plugin.configYml.getStrings("collections.disabled-worlds").contains(world.name)) return false
    if (plugin.configYml.getBool("collections.prevent-while-creative") && gameMode in setOf(GameMode.CREATIVE, GameMode.SPECTATOR)) return false
    if (plugin.configYml.getBool("collections.prevent-while-afk") && AFKManager.isAfk(this)) return false

    return true
}
