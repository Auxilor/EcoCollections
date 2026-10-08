package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.collections.Collections
import com.exanthiax.ecocollections.plugin
import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object SubcommandReload : Subcommand(
    plugin,
    "reload",
    "ecocollections.command.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (Prerequisite.HAS_FOLIA.isMet && !Bukkit.isGlobalTickThread()) {
            plugin.scheduler.global().run { onExecute(sender, args) }
            return
        }

        sender.sendMessage(
            plugin.langYml.getMessage("commands.reloaded", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%time%", plugin.reloadWithTime().toNiceString())
                .replace("%count%", Collections.values().size.toString())
        )
    }
}
