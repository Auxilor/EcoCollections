package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.EcoCollectionsPlugin
import com.exanthiax.ecocollections.gui.CollectionsGUI
import org.bukkit.entity.Player
import wiki.ecohub.core.command.command

internal fun EcoCollectionsPlugin.collectionsCommand() = command(
    name = "collections",
    permission = "ecocollections.command.collections"
) {
    topCommand()

    executes {
        CollectionsGUI.open(sender as? Player ?: fail("not-player"))
    }
}
