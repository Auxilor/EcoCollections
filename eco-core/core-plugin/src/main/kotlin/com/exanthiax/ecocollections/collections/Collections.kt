package com.exanthiax.ecocollections.collections

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.RegistrableCategory

object Collections : RegistrableCategory<Collection>("collection", "collections") {
    override fun clear(plugin: LibreforgePlugin) {
        for (collection in values()) {
            collection.onRemove()
        }
        registry.clear()
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(Collection(id, config))
    }

    override fun afterReload(plugin: LibreforgePlugin) {
        CollectionsLeaderboard.invalidateAll()
    }
}
