package com.auxilor.ecocollections.commands

import com.auxilor.ecocollections.collections.Collections
import com.auxilor.ecocollections.plugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import org.bukkit.command.CommandSender

object SubcommandReload : Subcommand(
    plugin,
    "reload",
    "ecocollections.command.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("commands.reloaded", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%time%", plugin.reloadWithTime().toNiceString())
                .replace("%count%", Collections.values().size.toString())
        )
    }
}

