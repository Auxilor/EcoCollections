package com.exanthiax.ecocollections.commands

import com.exanthiax.ecocollections.collections.Collection
import com.exanthiax.ecocollections.collections.Collections
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.ui.text.StringUtils

/**
 * Thrown by [reject] to stop a command with a message that is not a lang key.
 */
internal class CommandRejection(val reply: String) : RuntimeException(reply, null, false, false)

/**
 * Stop the command and send [message], colour codes translated.
 */
internal fun reject(message: String): Nothing = throw CommandRejection(StringUtils.format(message))

/**
 * Run [block], sending the message of any [reject] it calls.
 */
internal inline fun CommandContext<*>.replying(block: () -> Unit) {
    try {
        block()
    } catch (rejection: CommandRejection) {
        sender.sendMessage(rejection.reply)
    }
}

/**
 * The lang string [key] with [replacements], colour codes translated.
 */
internal fun CommandContext<*>.message(key: String, vararg replacements: Pair<String, String>): String =
    StringUtils.format(
        replacements.fold(plugin.langYml.getString(key)) { text, (placeholder, value) ->
            text.replace(oldValue = "%$placeholder%", newValue = value)
        }
    )

internal fun CommandContext<*>.onlineTarget(name: String): Player =
    Bukkit.getPlayer(name) ?: reject(noSuchPlayer(name))

internal fun CommandContext<*>.knownTarget(name: String): OfflinePlayer {
    @Suppress("DEPRECATION")
    val target = Bukkit.getOfflinePlayer(name)

    if (!target.hasPlayedBefore() && !target.isOnline) {
        reject(noSuchPlayer(name))
    }

    return target
}

internal fun CommandContext<*>.collection(id: String): Collection =
    Collections.getByID(id)
        ?: reject(plugin.langYml.getString("commands.no-such-collection").replace(oldValue = "%id%", newValue = id))

private fun CommandContext<*>.noSuchPlayer(name: String): String =
    plugin.langYml.getString("commands.no-such-player").replace(oldValue = "%name%", newValue = name)

internal fun collectionIds(): List<String> = Collections.values().map { it.id }
