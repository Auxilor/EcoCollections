package com.exanthiax.ecocollections.groups

import wiki.ecohub.config.Config
import wiki.ecohub.config.category.RegistrableCategory
import wiki.ecohub.core.plugin.EcoPlugin

object CollectionGroups : RegistrableCategory<CollectionGroup>("group", "groups") {
    override fun clear(plugin: EcoPlugin) {
        registry.clear()
    }

    override fun acceptConfig(plugin: EcoPlugin, id: String, config: Config) {
        registry.register(CollectionGroup(id, config))
    }
}
