package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.collections.Collection
import com.exanthiax.ecocollections.collections.Collections
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.data.playerbase.leaderboard.Leaderboards
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.ui.placeholder.placeholderContext
import wiki.ecohub.ui.text.formatEco
import com.exanthiax.ecocollections.plugin as ecoCollections

private val pageCompletions = listOf("1", "2", "3", "4", "5")

private const val PAGE_SIZE = 10

/**
 * `top [page]` shows the totals leaderboard, `top <collection> [page]` one collection's.
 */
internal fun CommandBuilder<CommandSender>.topCommand() = subcommand(
    name = "top",
    permission = "ecocollections.command.top"
) {
    executes {
        replying {
            val collection = Collections.getByID(args.getOrNull(0))
            sendTop(collection, pageOf(collection))
        }
    }

    completes {
        when {
            args.size == 1 -> pageCompletions + collectionIds()
            args.size == 2 && Collections.getByID(args[0]) != null -> pageCompletions
            else -> emptyList()
        }
    }
}

private fun CommandContext<CommandSender>.pageOf(collection: Collection?): Int {
    val pageArg = args.getOrNull(if (collection == null) 0 else 1)

    val badPage = pageArg?.takeIf { it.isNotBlank() && it.toIntOrNull() == null }

    if (collection == null && badPage != null) {
        reject(
            plugin.langYml.getFormattedString("commands.no-such-collection")
                .replace(oldValue = "%id%", newValue = badPage)
        )
    }

    return pageArg?.toIntOrNull() ?: 1
}

private fun CommandContext<CommandSender>.sendTop(collection: Collection?, page: Int) {
    val offset = (page - 1) * PAGE_SIZE
    val leaderboard = if (collection == null) ecoCollections.totalsLeaderboard else collection.leaderboard
    val top = (offset + 1..offset + PAGE_SIZE).mapNotNull { leaderboard?.getTop(it) }

    val lines = top.map { entry ->
        plugin.langYml.getString("top-line-format")
            .replace(oldValue = "%rank%", newValue = entry.position.toString())
            .replace(oldValue = "%value%", newValue = entry.value.toLong().toString())
            .replace(oldValue = "%player%", newValue = PlayerUtils.getSavedDisplayName(entry.player))
    }.ifEmpty { listOf(Leaderboards.getNoRecordsMessage(plugin)) }

    val messages = plugin.langYml.getStrings("top.format").toMutableList()
    val linesIndex = messages.indexOf("%lines%")

    if (linesIndex != -1) {
        messages.removeAt(linesIndex)
        messages.addAll(linesIndex, lines)
    }

    val name = collection?.name ?: plugin.langYml.getString("top.totals-name")
    val context = placeholderContext(player = sender as? Player)

    for (message in messages) {
        sender.sendMessage(message.replace(oldValue = "%collection%", newValue = name).formatEco(context))
    }
}
