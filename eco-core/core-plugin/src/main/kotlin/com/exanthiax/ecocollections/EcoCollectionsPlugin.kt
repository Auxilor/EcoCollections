package com.exanthiax.ecocollections

import com.willfp.eco.core.Eco
import com.willfp.eco.core.bstats.EcoMetricsChart
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.leaderboard.Leaderboard
import com.willfp.eco.core.leaderboard.Leaderboards
import com.willfp.eco.core.leaderboard.registerCategoryTopPlaceholders
import com.willfp.eco.core.leaderboard.registerTopPlaceholders
import com.exanthiax.ecocollections.collections.Collections
import com.exanthiax.ecocollections.commands.CommandCollections
import com.exanthiax.ecocollections.commands.CommandEcoCollections
import com.exanthiax.ecocollections.groups.CollectionGroups
import com.exanthiax.ecocollections.libreforge.condition.ConditionCollectionComplete
import com.exanthiax.ecocollections.libreforge.condition.ConditionCollectionTierAtLeast
import com.exanthiax.ecocollections.libreforge.condition.ConditionCollectionUnlocked
import com.exanthiax.ecocollections.libreforge.effect.EffectGiveCollectionCount
import com.exanthiax.ecocollections.libreforge.effect.EffectUnlockCollection
import com.exanthiax.ecocollections.libreforge.trigger.TriggerCollectionComplete
import com.exanthiax.ecocollections.libreforge.trigger.TriggerCollectionTierUp
import com.exanthiax.ecocollections.libreforge.trigger.TriggerCollectionUnlock
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.triggers.Triggers
import org.bukkit.event.Listener
import java.util.UUID
import com.willfp.eco.util.formatEco


lateinit var plugin: EcoCollectionsPlugin
    internal set

class EcoCollectionsPlugin : LibreforgePlugin() {
    /**
     * The leaderboard ranking players by their total collection tiers, or null before the first
     * reload has registered it.
     */
    var totalsLeaderboard: Leaderboard? = null
        private set

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

    override fun handleReload() {
        // Config categories are reloaded at LifecyclePosition.START, which eco runs before
        // handleReload, so every Collection already exists by the time this runs. Everything is
        // unregistered here and registered again below, so a renamed or deleted collection does
        // not leave a leaderboard behind.
        Leaderboards.unregisterAll(this)

        for (collection in Collections.values()) {
            collection.registerLeaderboard()
        }

        totalsLeaderboard = Leaderboards.register(this, "total_tiers") { uuids ->
            if (!this.configYml.getBool("leaderboards.enabled")) {
                emptyMap()
            } else {
                val totals = HashMap<UUID, Double>()

                // One bulk read per collection, summed - not one profile read per player per
                // collection, which is what this replaced.
                for (collection in Collections.values()) {
                    for ((uuid, tier) in Eco.get().readAllProfileValues(uuids, collection.tierKey)) {
                        totals.merge(uuid, tier.toDouble(), Double::plus)
                    }
                }

                totals.filterValues { it > 0.0 }
            }
        }

        if (!this.configYml.getBool("leaderboards.enabled")) {
            return
        }

        val emptyPosition = this.langYml.getString("top.empty-position").formatEco()

        totalsLeaderboard?.registerTopPlaceholders(
            this,
            emptyPosition,
            listOf("tiers", "amount")
        )

        // Registered once for every collection at once: the lookup resolves the ID when the
        // placeholder is read, so a collection added or renamed in a config needs nothing here.
        registerCategoryTopPlaceholders(
            this,
            emptyPosition,
            listOf("count", "amount")
        ) { Collections.getByID(it)?.leaderboard }
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

    override fun getCustomCharts() = listOf(
        EcoMetricsChart.SingleLine("total_collections") { Collections.values().size },
        EcoMetricsChart.SingleLine("total_collection_groups") { CollectionGroups.values().size },
        EcoMetricsChart.SimplePie("prevent_while_afk") {
            if (configYml.getBool("collections.prevent-while-afk")) "enabled" else "disabled"
        },
        EcoMetricsChart.SimplePie("prevent_while_creative") {
            if (configYml.getBool("collections.prevent-while-creative")) "enabled" else "disabled"
        },
        EcoMetricsChart.SimplePie("warn_on_missing_dupe_filter") {
            if (configYml.getBool("collections.warn-on-missing-dupe-filter")) "enabled" else "disabled"
        },
        EcoMetricsChart.SingleLine("disabled_worlds") {
            configYml.getStrings("collections.disabled-worlds").size
        }
    )
}
