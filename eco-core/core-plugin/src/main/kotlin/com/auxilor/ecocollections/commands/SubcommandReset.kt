package com.auxilor.ecocollections.commands

import com.auxilor.ecocollections.api.resetCollection
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.ecocollections.plugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@Suppress("DEPRECATION")
object SubcommandReset : Subcommand(
    plugin,
    "reset",
    "ecocollections.command.reset",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.size < 2) {
            sender.sendMessage(StringUtils.format("&cUsage: /ecocollections reset <player> <collection|all>"))
            return
        }

        val playerName = args[0]
        val collectionArg = args[1].lowercase()

        val target = Bukkit.getOfflinePlayer(playerName)
        if (!target.hasPlayedBefore() && !target.isOnline) {
            sender.sendMessage(
                StringUtils.format(
                    plugin.langYml.getString("commands.no-such-player")
                        .replace("%name%", playerName)
                )
            )
            return
        }

        if (collectionArg == "all") {
            for (collection in Collections.values()) {
                target.resetCollection(collection)
            }
            sender.sendMessage(
                StringUtils.format(
                    plugin.langYml.getString("commands.reset-success")
                        .replace("%player%", target.name ?: playerName)
                        .replace("%collection%", "all collections")
                )
            )
        } else {
            val collection = Collections.getByID(collectionArg)
            if (collection == null) {
                sender.sendMessage(
                    StringUtils.format(
                        plugin.langYml.getString("commands.no-such-collection")
                            .replace("%id%", collectionArg)
                    )
                )
                return
            }

            target.resetCollection(collection)
            sender.sendMessage(
                StringUtils.format(
                    plugin.langYml.getString("commands.reset-success")
                        .replace("%player%", target.name ?: playerName)
                        .replace("%collection%", collection.name)
                )
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> Bukkit.getOnlinePlayers().map { it.name }
                .filter { it.lowercase().startsWith(args[0].lowercase()) }
            2 -> {
                val options = mutableListOf("all")
                options.addAll(Collections.values().map { it.id })
                options.filter { it.lowercase().startsWith(args[1].lowercase()) }
            }
            else -> emptyList()
        }
    }
}

