package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.api.forceUnlockCollection
import com.exanthiax.ecocollections.api.isCollectionUnlocked
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.game.entity.player.profile
import wiki.ecohub.game.entity.runOwned

/**
 * An offline player is unlocked by writing their profile; an online one goes through the unlock
 * event on their region, which can cancel it.
 */
internal fun CommandBuilder<CommandSender>.unlockCommand() = subcommand(
    name = "unlock",
    permission = "ecocollections.command.unlock"
) {
    executes {
        replying { unlock() }
    }

    completes {
        when (args.size) {
            1 -> playerNames()
            2 -> collectionIds()
            else -> emptyList()
        }
    }
}

private fun CommandContext<CommandSender>.unlock() {
    if (args.size < 2) {
        reject("&cUsage: /ecocollections unlock <player> <collection>")
    }

    val target = knownTarget(args[0])
    val collection = collection(args[1].lowercase())
    val playerName = target.name ?: args[0]

    if (target.isCollectionUnlocked(collection)) {
        reject(message("commands.already-unlocked", "player" to playerName, "collection" to collection.name))
    }

    val onlineTarget = target.player

    if (onlineTarget == null) {
        target.profile.write(collection.unlockedKey, true)
        sender.sendMessage(message("commands.unlock-success", "player" to playerName, "collection" to collection.name))
        return
    }

    onlineTarget.runOwned {
        val key = if (onlineTarget.forceUnlockCollection(collection)) {
            "commands.unlock-success"
        } else {
            "commands.unlock-cancelled"
        }

        sender.sendMessage(message(key, "player" to onlineTarget.name, "collection" to collection.name))
    }
}
