package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.api.setCollectionCount
import com.exanthiax.ecocollections.api.setCollectionTier
import com.exanthiax.ecocollections.collections.Collections
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

private const val USAGE = "&cUsage: /ecocollections set <count|tier> <player> <collection> <value>"

private const val VALUE_INDEX = 3

private const val DEFAULT_MAX_TIER = 10

private val countCompletions = listOf("0", "1", "10", "100", "1000")

internal fun CommandBuilder<CommandSender>.setCommand() = subcommand(
    name = "set",
    permission = "ecocollections.command.set"
) {
    executes {
        replying {
            val mode = args.getOrNull(0)?.lowercase()

            if (args.size <= VALUE_INDEX || mode !in listOf("count", "tier")) {
                reject(USAGE)
            }

            val target = knownTarget(args[1])
            val collection = collection(args[2].lowercase())
            val value = args[VALUE_INDEX]
            val playerName = target.name ?: args[1]

            if (mode == "count") {
                val amount = value.toDoubleOrNull() ?: reject("&cInvalid amount: $value")
                target.setCollectionCount(collection, amount)
                sender.sendMessage(
                    message(
                        "commands.set-success",
                        "amount" to amount.toLong().toString(),
                        "player" to playerName,
                        "collection" to collection.name
                    )
                )
            } else {
                val tier = value.toIntOrNull() ?: reject("&cInvalid tier: $value")
                target.setCollectionTier(collection, tier)
                sender.sendMessage(
                    message(
                        "commands.settier-success",
                        "tier" to tier.toString(),
                        "player" to playerName,
                        "collection" to collection.name
                    )
                )
            }
        }
    }

    completes {
        when (args.size) {
            1 -> listOf("count", "tier")
            2 -> playerNames()
            VALUE_INDEX -> collectionIds()
            VALUE_INDEX + 1 -> valueCompletions(mode = args[0].lowercase(), collectionId = args[2].lowercase())
            else -> emptyList()
        }
    }
}

private fun valueCompletions(mode: String, collectionId: String): List<String> = when (mode) {
    "count" -> countCompletions
    "tier" -> (0..(Collections.getByID(collectionId)?.maxTier ?: DEFAULT_MAX_TIER)).map { it.toString() }
    else -> emptyList()
}
