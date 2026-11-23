package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.api.resetCollection
import com.exanthiax.ecocollections.collections.Collections
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext

internal fun CommandBuilder<CommandSender>.resetCommand() = subcommand(
    name = "reset",
    permission = "ecocollections.command.reset"
) {
    executes {
        replying { reset() }
    }

    completes {
        when (args.size) {
            1 -> playerNames()
            2 -> listOf("all") + collectionIds()
            else -> emptyList()
        }
    }
}

private fun CommandContext<CommandSender>.reset() {
    if (args.size < 2) {
        reject("&cUsage: /ecocollections reset <player> <collection|all>")
    }

    val target = knownTarget(args[0])
    val all = args[1].equals("all", ignoreCase = true)
    val collections = if (all) Collections.values() else listOf(collection(args[1].lowercase()))

    for (collection in collections) {
        target.resetCollection(collection)
    }

    sender.sendMessage(
        message(
            "commands.reset-success",
            "player" to (target.name ?: args[0]),
            "collection" to if (all) "all collections" else collections.first().name
        )
    )
}
