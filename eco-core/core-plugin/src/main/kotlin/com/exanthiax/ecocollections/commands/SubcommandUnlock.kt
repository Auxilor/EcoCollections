package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.api.forceUnlockCollection
import com.exanthiax.ecocollections.api.isCollectionUnlocked
import com.exanthiax.ecocollections.collections.Collections
import com.exanthiax.ecocollections.plugin
import com.exanthiax.ecocollections.runOwned
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.core.data.profile
import com.willfp.eco.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@Suppress("DEPRECATION")
object SubcommandUnlock : Subcommand(
    plugin,
    "unlock",
    "ecocollections.command.unlock",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.size < 2) {
            sender.sendMessage(StringUtils.format("&cUsage: /ecocollections unlock <player> <collection>"))
            return
        }

        val playerName = args[0]
        val collectionId = args[1].lowercase()

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

        if (target.isCollectionUnlocked(collection)) {
            sender.sendMessage(
                StringUtils.format(
                    plugin.langYml.getString("commands.already-unlocked")
                        .replace("%player%", target.name ?: playerName)
                        .replace("%collection%", collection.name)
                )
            )
            return
        }

        val onlineTarget = target.player
        if (onlineTarget == null) {
            target.profile.write(collection.unlockedKey, true)
            sendResult(sender, "commands.unlock-success", target.name ?: playerName, collection.name)
            return
        }

        onlineTarget.runOwned {
            val key = if (onlineTarget.forceUnlockCollection(collection)) {
                "commands.unlock-success"
            } else {
                "commands.unlock-cancelled"
            }
            sendResult(sender, key, onlineTarget.name, collection.name)
        }
    }

    private fun sendResult(sender: CommandSender, key: String, playerName: String, collectionName: String) {
        sender.sendMessage(
            StringUtils.format(
                plugin.langYml.getString(key)
                    .replace("%player%", playerName)
                    .replace("%collection%", collectionName)
            )
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> Bukkit.getOnlinePlayers().map { it.name }
                .filter { it.lowercase().startsWith(args[0].lowercase()) }
            2 -> Collections.values().map { it.id }
                .filter { it.lowercase().startsWith(args[1].lowercase()) }
            else -> emptyList()
        }
    }
}

