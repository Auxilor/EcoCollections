package com.auxilor.ecocollections.commands

import com.auxilor.eco.core.command.impl.PluginCommand
import com.auxilor.eco.core.command.impl.Subcommand
import com.auxilor.eco.core.data.profile
import com.auxilor.eco.util.StringUtils
import com.auxilor.ecocollections.api.giveCollectionCount
import com.auxilor.ecocollections.api.resetCollection
import com.auxilor.ecocollections.api.setCollectionCount
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.ecocollections.collections.CollectionsLeaderboard
import com.auxilor.ecocollections.plugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandEcoCollections : PluginCommand(
    plugin,
    "ecocollections",
    "ecocollections.command.admin",
    false
) {
    init {
        this.addSubcommand(SubcommandGive)
            .addSubcommand(SubcommandSet)
            .addSubcommand(SubcommandSetTier)
            .addSubcommand(SubcommandReset)
            .addSubcommand(SubcommandReload)
            .addSubcommand(SubcommandLeaderboard)
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(StringUtils.format("&eEcoCollections &7- Subcommands: give, set, settier, reset, reload, leaderboard"))
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return emptyList()
    }
}

private object SubcommandGive : Subcommand(
    plugin,
    "give",
    "ecocollections.command.admin",
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

@Suppress("DEPRECATION")
private object SubcommandSet : Subcommand(
    plugin,
    "set",
    "ecocollections.command.admin",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.size < 3) {
            sender.sendMessage(StringUtils.format("&cUsage: /ecocollections set <player> <collection> <amount>"))
            return
        }

        val playerName = args[0]
        val collectionId = args[1].lowercase()
        val amount = args[2].toDoubleOrNull()

        if (amount == null) {
            sender.sendMessage(StringUtils.format("&cInvalid amount: ${args[2]}"))
            return
        }

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

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> Bukkit.getOnlinePlayers().map { it.name }
                .filter { it.lowercase().startsWith(args[0].lowercase()) }
            2 -> Collections.values().map { it.id }
                .filter { it.lowercase().startsWith(args[1].lowercase()) }
            3 -> listOf("0", "1", "10", "100", "1000")
                .filter { it.startsWith(args[2]) }
            else -> emptyList()
        }
    }
}

@Suppress("DEPRECATION")
private object SubcommandSetTier : Subcommand(
    plugin,
    "settier",
    "ecocollections.command.admin",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.size < 3) {
            sender.sendMessage(StringUtils.format("&cUsage: /ecocollections settier <player> <collection> <tier>"))
            return
        }

        val playerName = args[0]
        val collectionId = args[1].lowercase()
        val tier = args[2].toIntOrNull()

        if (tier == null) {
            sender.sendMessage(StringUtils.format("&cInvalid tier: ${args[2]}"))
            return
        }

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

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> Bukkit.getOnlinePlayers().map { it.name }
                .filter { it.lowercase().startsWith(args[0].lowercase()) }
            2 -> Collections.values().map { it.id }
                .filter { it.lowercase().startsWith(args[1].lowercase()) }
            3 -> {
                val collection = Collections.getByID(args[1].lowercase())
                val maxTier = collection?.maxTier ?: 10
                (0..maxTier).map { it.toString() }
                    .filter { it.startsWith(args[2]) }
            }
            else -> emptyList()
        }
    }
}

@Suppress("DEPRECATION")
private object SubcommandReset : Subcommand(
    plugin,
    "reset",
    "ecocollections.command.admin",
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

private object SubcommandReload : Subcommand(
    plugin,
    "reload",
    "ecocollections.command.admin",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        this.plugin.reload()
        sender.sendMessage(
            StringUtils.format(
                plugin.langYml.getString("commands.reload-success")
            )
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return emptyList()
    }
}

private object SubcommandLeaderboard : Subcommand(
    plugin,
    "leaderboard",
    "ecocollections.command.admin",
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
