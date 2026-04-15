package com.auxilor.ecocollections.groups

import com.auxilor.eco.core.config.interfaces.Config
import com.auxilor.libreforge.loader.LibreforgePlugin
import com.auxilor.libreforge.loader.configs.RegistrableCategory

object CollectionGroups : RegistrableCategory<CollectionGroup>("group", "groups") {
    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(CollectionGroup(id, config))
    }
}
