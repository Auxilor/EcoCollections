package com.auxilor.ecocollections.commands

import com.auxilor.ecocollections.collections.Collections
import com.auxilor.ecocollections.collections.CollectionsLeaderboard
import com.auxilor.ecocollections.plugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import org.bukkit.command.CommandSender

object SubcommandLeaderboard : Subcommand(
    plugin,
    "leaderboard",
    "ecocollections.command.leaderboard",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty() || args[0].lowercase() != "refresh") {
            sender.sendMessage(StringUtils.format("&cUsage: /ecocollections leaderboard refresh [collection]"))
            return
        }

        if (args.size >= 2) {
            val collectionId = args[1].lowercase()
            val collection = Collections.getByID(collectionId)
            if (collection == null) {
                sender.sendMessage(
                    StringUtils.format(
                        plugin.langYml.getString("commands.no-such-collection")
                            .replace("%id%", collectionId)
                    )
                )
                return
            }
            CollectionsLeaderboard.invalidate(collection)
            sender.sendMessage(StringUtils.format("&aRefreshed leaderboard for &e${collection.name}&a."))
        } else {
            CollectionsLeaderboard.invalidateAll()
            sender.sendMessage(StringUtils.format("&aRefreshed all leaderboards."))
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> listOf("refresh").filter { it.startsWith(args[0].lowercase()) }
            2 -> Collections.values().map { it.id }
                .filter { it.lowercase().startsWith(args[1].lowercase()) }
            else -> emptyList()
        }
    }
}

