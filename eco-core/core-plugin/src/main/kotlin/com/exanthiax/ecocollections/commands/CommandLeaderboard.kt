package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.collections.Collections
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.data.playerbase.leaderboard.Leaderboard
import wiki.ecohub.ui.text.StringUtils
import java.util.logging.Level
import com.exanthiax.ecocollections.plugin as ecoCollections

internal fun CommandBuilder<CommandSender>.leaderboardCommand() = subcommand(
    name = "leaderboard",
    permission = "ecocollections.command.leaderboard"
) {
    executes {
        replying {
            if (args.firstOrNull()?.lowercase() != "refresh") {
                reject("&cUsage: /ecocollections leaderboard refresh [collection]")
            }

            val collection = args.getOrNull(1)?.let { collection(it.lowercase()) }
            val refreshed = collection?.let { listOf(it) } ?: Collections.values()

            (refreshed.map { it.leaderboard } + ecoCollections.totalsLeaderboard).forEach { it?.refreshLogged() }

            val reply = if (collection == null) {
                "&aRefreshed all leaderboards."
            } else {
                "&aRefreshed leaderboard for &e${collection.name}&a."
            }

            sender.sendMessage(StringUtils.format(reply))
        }
    }

    completes {
        when (args.size) {
            1 -> listOf("refresh")
            2 -> collectionIds()
            else -> emptyList()
        }
    }
}

private fun Leaderboard.refreshLogged() {
    refresh().exceptionally { error ->
        ecoCollections.logger.log(Level.WARNING, "Failed to refresh a leaderboard", error)
        null
    }
}
