package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.EcoCollectionsPlugin
import wiki.ecohub.core.command.command
import wiki.ecohub.ui.text.StringUtils

internal fun EcoCollectionsPlugin.ecoCollectionsCommand() = command(
    name = "ecocollections",
    permission = "ecocollections.command.ecocollections"
) {
    giveCommand()
    setCommand()
    resetCommand()
    unlockCommand()
    reloadCommand()
    leaderboardCommand()

    executes {
        sender.sendMessage(
            StringUtils.format("&eEcoCollections &7- Subcommands: give, set, reset, unlock, reload, leaderboard")
        )
    }
}
