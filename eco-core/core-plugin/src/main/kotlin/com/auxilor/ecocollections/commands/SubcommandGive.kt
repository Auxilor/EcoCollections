package com.auxilor.ecocollections.commands

import com.auxilor.ecocollections.api.giveCollectionCount
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.ecocollections.plugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object SubcommandGive : Subcommand(
    plugin,
    "give",
    "ecocollections.command.give",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.size < 3) {
            sender.sendMessage(StringUtils.format("&cUsage: /ecocollections give <player> <collection> <amount>"))
            return
        }

        val playerName = args[0]
        val collectionId = args[1].lowercase()
        val amount = args[2].toDoubleOrNull()

        if (amount == null) {
            sender.sendMessage(StringUtils.format("&cInvalid amount: ${args[2]}"))
            return
        }

        val target = Bukkit.getPlayer(playerName)
        if (target == null) {
            sender.sendMessage(
                StringUtils.format(
                    plugin.langYml.getString("commands.no-such-player")
                        .replace("%name%", playerName)
                )
            )
            return
        }

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

        target.giveCollectionCount(collection, amount)
        sender.sendMessage(
            StringUtils.format(
                plugin.langYml.getString("commands.give-success")
                    .replace("%amount%", amount.toLong().toString())
                    .replace("%player%", target.name)
                    .replace("%collection%", collection.name)
            )
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> Bukkit.getOnlinePlayers().map { it.name }
                .filter { it.lowercase().startsWith(args[0].lowercase()) }
            2 -> Collections.values().map { it.id }
                .filter { it.lowercase().startsWith(args[1].lowercase()) }
            3 -> listOf("1", "10", "100", "1000")
                .filter { it.startsWith(args[2]) }
            else -> emptyList()
        }
    }
}

