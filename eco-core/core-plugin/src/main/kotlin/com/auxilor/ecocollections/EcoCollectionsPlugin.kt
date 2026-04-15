package com.auxilor.ecocollections

import com.willfp.eco.core.command.impl.PluginCommand
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.ecocollections.commands.CommandCollections
import com.auxilor.ecocollections.commands.CommandEcoCollections
import com.auxilor.ecocollections.groups.CollectionGroups
import com.auxilor.ecocollections.libreforge.condition.ConditionCollectionComplete
import com.auxilor.ecocollections.libreforge.condition.ConditionCollectionTierAtLeast
import com.auxilor.ecocollections.libreforge.condition.ConditionCollectionUnlocked
import com.auxilor.ecocollections.libreforge.effect.EffectGiveCollectionCount
import com.auxilor.ecocollections.libreforge.effect.EffectUnlockCollection
import com.auxilor.ecocollections.libreforge.trigger.TriggerCollectionComplete
import com.auxilor.ecocollections.libreforge.trigger.TriggerCollectionTierUp
import com.auxilor.ecocollections.libreforge.trigger.TriggerCollectionUnlock
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.triggers.Triggers
import org.bukkit.event.Listener


lateinit var plugin: EcoCollectionsPlugin
    internal set

class EcoCollectionsPlugin : LibreforgePlugin() {
    init {
        plugin = this
    }

    override fun loadConfigCategories(): List<ConfigCategory> = listOf(
        CollectionGroups,
        Collections
    )

    override fun handleEnable() {
        Triggers.register(TriggerCollectionTierUp)
        Triggers.register(TriggerCollectionComplete)
        Triggers.register(TriggerCollectionUnlock)
        Conditions.register(ConditionCollectionTierAtLeast)
        Conditions.register(ConditionCollectionComplete)
        Conditions.register(ConditionCollectionUnlocked)
        Effects.register(EffectGiveCollectionCount)
        Effects.register(EffectUnlockCollection)
    }

    override fun loadPluginCommands(): List<PluginCommand> = listOf(
        CommandEcoCollections,
        CommandCollections
    )

    override fun loadListeners(): List<Listener> = listOf(
        TriggerCollectionTierUp,
        TriggerCollectionComplete,
        TriggerCollectionUnlock
    )
}
