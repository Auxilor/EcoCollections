package com.exanthiax.ecocollections

import wiki.ecohub.core.Eco
import wiki.ecohub.core.plugin.EcoPlugin
import wiki.ecohub.data.playerbase.leaderboard.Leaderboard
import wiki.ecohub.data.playerbase.leaderboard.LeaderboardEntry
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.ui.placeholder.type.DynamicPlaceholder
import java.util.regex.Pattern

private val LeaderboardEntry.shownName: String
    get() = if (Eco.get().getEcoPlugin().configYml.getBool("leaderboards.use-display-names")) {
        PlayerUtils.getSavedDisplayName(player)
    } else {
        PlayerUtils.getSavedName(player)
    }

/**
 * `%<plugin>_top_<N>_name%` and `%<plugin>_top_<N>_<alias>%` for this leaderboard, as eco's
 * removed `Leaderboard.registerTopPlaceholders` did.
 */
internal fun Leaderboard.registerTopPlaceholders(
    plugin: EcoPlugin,
    emptyText: String,
    valueAliases: Collection<String>
) {
    val leaderboard = this
    val types = (listOf("name") + valueAliases).joinToString("|") { Pattern.quote(it) }
    val pattern = Pattern.compile("top_(\\d+)_($types)")

    DynamicPlaceholder(plugin, pattern, "top_<N>_<${(listOf("name") + valueAliases).joinToString("|")}>") { args ->
        val matcher = pattern.matcher(args)
        val entry = matcher.takeIf { it.matches() }
            ?.group(1)
            ?.toIntOrNull()
            ?.let { leaderboard.getTop(it) }

        when {
            entry == null -> emptyText
            matcher.group(2) == "name" -> entry.shownName
            else -> entry.value.toInt().toString()
        }
    }.register()
}
