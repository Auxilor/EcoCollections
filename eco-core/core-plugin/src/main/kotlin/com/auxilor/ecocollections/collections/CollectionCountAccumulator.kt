package com.auxilor.ecocollections.collections

import com.auxilor.eco.core.integrations.afk.AFKManager
import com.auxilor.ecocollections.api.giveCollectionCount
import com.auxilor.ecocollections.plugin
import com.auxilor.libreforge.counters.Accumulator
import org.bukkit.GameMode
import org.bukkit.entity.Player

class CollectionCountAccumulator(
    private val collection: Collection
) : Accumulator {
    override fun accept(player: Player, count: Double) {
        if (player.isInDisabledWorld) return
        if (player.gameMode in setOf(GameMode.CREATIVE, GameMode.SPECTATOR)) return
        if (plugin.configYml.getBool("collections.prevent-while-afk") && AFKManager.isAfk(player)) return

        player.giveCollectionCount(collection, count)
    }
}

private val Player.isInDisabledWorld: Boolean
    get() = plugin.configYml.getStrings("collections.disabled-worlds").contains(world.name)
