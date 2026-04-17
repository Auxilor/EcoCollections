package com.auxilor.ecocollections.commands

import com.auxilor.ecocollections.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.util.StringUtils
import org.bukkit.command.CommandSender

object CommandEcoCollections : PluginCommand(
    plugin,
    "ecocollections",
    "ecocollections.command.ecocollections",
    false
) {
    init {
        this.addSubcommand(SubcommandGive)
            .addSubcommand(SubcommandSet)
            .addSubcommand(SubcommandReset)
            .addSubcommand(SubcommandUnlock)
            .addSubcommand(SubcommandReload)
            .addSubcommand(SubcommandLeaderboard)
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(StringUtils.format("&eEcoCollections &7- Subcommands: give, set, reset, unlock, reload, leaderboard"))
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return emptyList()
    }
}

