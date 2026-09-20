package com.exanthiax.ecocollections.collections

import com.exanthiax.ecocollections.plugin
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.leaderboard.LeaderboardEntry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.RegistrableCategory

object Collections : RegistrableCategory<Collection>("collection", "collections") {
    fun getTop(position: Int): LeaderboardEntry? {
        require(position > 0) { "Position must be greater than 0" }

        return plugin.totalsLeaderboard?.getTop(position)
    }

    override fun clear(plugin: LibreforgePlugin) {
        for (collection in values()) {
            collection.onRemove()
        }
        registry.clear()
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(Collection(id, config))
    }
}
