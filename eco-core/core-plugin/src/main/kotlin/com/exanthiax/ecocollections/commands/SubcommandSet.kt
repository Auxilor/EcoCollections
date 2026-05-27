package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.api.setCollectionCount
import com.exanthiax.ecocollections.collections.Collections
import com.exanthiax.ecocollections.plugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.core.data.profile
import com.willfp.eco.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@Suppress("DEPRECATION")
object SubcommandSet : Subcommand(
    plugin,
    "set",
    "ecocollections.command.set",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.size < 4) {
            sender.sendMessage(StringUtils.format("&cUsage: /ecocollections set <count|tier> <player> <collection> <value>"))
            return
        }

        val mode = args[0].lowercase()
        if (mode != "count" && mode != "tier") {
            sender.sendMessage(StringUtils.format("&cUsage: /ecocollections set <count|tier> <player> <collection> <value>"))
            return
        }

        val playerName = args[1]
        val collectionId = args[2].lowercase()
        val value = args[3]

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

        when (mode) {
            "count" -> {
                val amount = value.toDoubleOrNull()
                if (amount == null) {
                    sender.sendMessage(StringUtils.format("&cInvalid amount: $value"))
                    return
                }

                target.setCollectionCount(collection, amount)
                sender.sendMessage(
                    StringUtils.format(
                        plugin.langYml.getString("commands.set-success")
                            .replace("%amount%", amount.toLong().toString())
                            .replace("%player%", target.name ?: playerName)
                            .replace("%collection%", collection.name)
                    )
                )
            }

            "tier" -> {
                val tier = value.toIntOrNull()
                if (tier == null) {
                    sender.sendMessage(StringUtils.format("&cInvalid tier: $value"))
                    return
                }

                target.profile.write(collection.tierKey, tier.coerceIn(0, collection.maxTier))
                sender.sendMessage(
                    StringUtils.format(
                        plugin.langYml.getString("commands.settier-success")
                            .replace("%tier%", tier.toString())
                            .replace("%player%", target.name ?: playerName)
                            .replace("%collection%", collection.name)
                    )
                )
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> listOf("count", "tier")
                .filter { it.lowercase().startsWith(args[0].lowercase()) }
            2 -> Bukkit.getOnlinePlayers().map { it.name }
                .filter { it.lowercase().startsWith(args[1].lowercase()) }
            3 -> Collections.values().map { it.id }
                .filter { it.lowercase().startsWith(args[2].lowercase()) }
            4 -> when (args[0].lowercase()) {
                "count" -> listOf("0", "1", "10", "100", "1000")
                "tier" -> {
                    val collection = Collections.getByID(args[2].lowercase())
                    val maxTier = collection?.maxTier ?: 10
                    (0..maxTier).map { it.toString() }
                }
                else -> emptyList()
            }.filter { it.startsWith(args[3]) }
            else -> emptyList()
        }
    }
}

