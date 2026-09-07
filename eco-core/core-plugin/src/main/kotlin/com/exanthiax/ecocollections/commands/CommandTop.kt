package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.collections.Collections
import com.exanthiax.ecocollections.plugin
import com.exanthiax.ecocollections.plugin as ecoCollections
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.core.leaderboard.Leaderboards
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

object CommandTop : Subcommand(
    plugin,
    "top",
    "ecocollections.command.top",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val collection = Collections.getByID(args.getOrNull(0))

        // With no collection named, the first argument is the page and the totals leaderboard is
        // shown; with one named, the page moves along to the second argument.
        val pageIndex = if (collection == null) 0 else 1
        val page = args.getOrNull(pageIndex)?.toIntOrNull() ?: 1

        if (
            collection == null
            && args.getOrNull(pageIndex)?.toIntOrNull() == null
            && args.getOrNull(pageIndex)?.isBlank() == false
        ) {
            sender.sendMessage(
                plugin.langYml.getMessage("commands.no-such-collection")
                    .replace("%id%", args[pageIndex])
            )
            return
        }

        val offset = (page - 1) * 10

        val positions = ((offset + 1)..(offset + 10)).toList()

        val leaderboard = if (collection == null) ecoCollections.totalsLeaderboard else collection.leaderboard
        val top = positions.mapNotNull { leaderboard?.getTop(it) }

        val messages = plugin.langYml.getStrings("top.format").toMutableList()
        val lines = mutableListOf<String>()

        for ((index, entry) in top.withIndex()) {
            val line = plugin.langYml.getString("top-line-format")
                .replace("%rank%", (offset + index + 1).toString())
                .replace("%value%", entry.value.toLong().toString())
                .replace("%player%", entry.player.savedDisplayName)

            lines.add(line)
        }

        // An empty leaderboard still shows its header and footer, with the universal
        // "no records" message standing in for the entries.
        if (lines.isEmpty()) {
            lines.add(Leaderboards.getNoRecordsMessage(plugin))
        }

        val linesIndex = messages.indexOf("%lines%")

        if (linesIndex != -1) {
            messages.removeAt(linesIndex)
            messages.addAll(linesIndex, lines)
        }

        for (message in messages) {
            sender.sendMessage(
                message
                    .replace("%collection%", collection?.name ?: plugin.langYml.getString("top.totals-name"))
                    .formatEco(
                        placeholderContext(
                            player = sender as? Player
                        )
                    )
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                listOf(1, 2, 3, 4, 5).map { it.toString() }
                        + Collections.values().map { it.id },
                completions
            )
            return completions
        }

        if (args.size == 2 && Collections.getByID(args[0]) != null) {
            StringUtil.copyPartialMatches(
                args[1],
                listOf(1, 2, 3, 4, 5).map { it.toString() },
                completions
            )
            return completions
        }

        return emptyList()
    }
}
