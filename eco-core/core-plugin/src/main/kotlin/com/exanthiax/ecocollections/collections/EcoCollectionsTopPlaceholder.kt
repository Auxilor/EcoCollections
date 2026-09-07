package com.exanthiax.ecocollections.collections

import com.exanthiax.ecocollections.plugin
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.placeholder.RegistrablePlaceholder
import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.util.savedDisplayName
import java.util.regex.Pattern

object EcoCollectionsCollectionTopPlaceholder : RegistrablePlaceholder {
    private val pattern = Pattern.compile("top_([a-z0-9_]+)_(\\d+)_(name|count|amount)")

    override fun getPattern(): Pattern = pattern
    override fun getPlugin(): EcoPlugin = com.exanthiax.ecocollections.plugin

    override fun getValue(params: String, ctx: PlaceholderContext): String? {
        val emptyPosition: String = plugin.langYml.getString("top.empty-position")
        val matcher = pattern.matcher(params)

        if (!matcher.matches()) return null

        val collectionId = matcher.group(1) // Collection ID (allows underscores)
        val place = matcher.group(2).toIntOrNull() ?: return null
        val type = matcher.group(3)

        val collection = Collections.getByID(collectionId) ?: return null

        return when (type) {
            "name" -> collection.leaderboard?.getTop(place)?.player?.savedDisplayName ?: emptyPosition
            "count", "amount" -> collection.leaderboard?.getTop(place)?.value?.toInt()?.toString() ?: emptyPosition
            else -> null
        }
    }
}

object EcoCollectionsTopPlaceholder : RegistrablePlaceholder {
    private val pattern = Pattern.compile("top_(\\d+)_(name|tiers|amount)")

    override fun getPattern(): Pattern = pattern
    override fun getPlugin(): EcoPlugin = com.exanthiax.ecocollections.plugin

    override fun getValue(params: String, ctx: PlaceholderContext): String? {
        val emptyPosition: String = plugin.langYml.getString("top.empty-position")
        val matcher = pattern.matcher(params)

        if (!matcher.matches()) return null

        val place = matcher.group(1).toIntOrNull() ?: return null
        val type = matcher.group(2)

        return when (type) {
            "name" -> Collections.getTop(place)?.player?.savedDisplayName ?: emptyPosition
            "tiers", "amount" -> Collections.getTop(place)?.value?.toInt()?.toString() ?: emptyPosition
            else -> null
        }
    }
}
