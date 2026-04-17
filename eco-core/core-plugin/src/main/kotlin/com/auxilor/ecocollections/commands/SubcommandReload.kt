package com.auxilor.ecocollections.commands

import com.auxilor.ecocollections.plugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import org.bukkit.command.CommandSender

object SubcommandReload : Subcommand(
    plugin,
    "reload",
    "ecocollections.command.reload",
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

