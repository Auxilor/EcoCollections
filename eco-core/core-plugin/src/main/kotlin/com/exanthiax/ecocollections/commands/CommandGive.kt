package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.api.giveCollectionCount
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.game.entity.runOwned

private val amountCompletions = listOf("1", "10", "100", "1000")

private const val AMOUNT_ARGS = 3

internal fun CommandBuilder<CommandSender>.giveCommand() = subcommand(
    name = "give",
    permission = "ecocollections.command.give"
) {
    executes {
        replying {
            if (args.size < AMOUNT_ARGS) {
                reject("&cUsage: /ecocollections give <player> <collection> <amount>")
            }

            val amount = args[2].toDoubleOrNull() ?: reject("&cInvalid amount: ${args[2]}")
            val target = onlineTarget(args[0])
            val collection = collection(args[1].lowercase())

            target.runOwned {
                target.giveCollectionCount(collection, amount)
                sender.sendMessage(
                    message(
                        "commands.give-success",
                        "amount" to amount.toLong().toString(),
                        "player" to target.name,
                        "collection" to collection.name
                    )
                )
            }
        }
    }

    completes {
        when (args.size) {
            1 -> playerNames()
            2 -> collectionIds()
            AMOUNT_ARGS -> amountCompletions
            else -> emptyList()
        }
    }
}
