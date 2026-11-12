package com.exanthiax.ecocollections.collections

import com.exanthiax.ecocollections.plugin
import wiki.ecohub.config.Config
import wiki.ecohub.config.category.RegistrableCategory
import wiki.ecohub.core.plugin.EcoPlugin
import wiki.ecohub.data.playerbase.leaderboard.LeaderboardEntry

object Collections : RegistrableCategory<Collection>("collection", "collections") {
    fun getTop(position: Int): LeaderboardEntry? {
        require(position > 0) { "Position must be greater than 0" }

        return plugin.totalsLeaderboard?.getTop(position)
    }

    override fun clear(plugin: EcoPlugin) {
        for (collection in values()) {
            collection.onRemove()
        }
        registry.clear()
    }

    override fun acceptConfig(plugin: EcoPlugin, id: String, config: Config) {
        registry.register(Collection(id, config))
    }
}
