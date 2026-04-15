@file:JvmName("CollectionAPI")

package com.auxilor.ecocollections.api

import com.auxilor.eco.core.data.profile
import com.auxilor.eco.util.StringUtils
import com.auxilor.eco.util.toNumeral
import com.auxilor.ecocollections.api.event.PlayerCollectionCompleteEvent
import com.auxilor.ecocollections.api.event.PlayerCollectionTierUpEvent
import com.auxilor.ecocollections.api.event.PlayerCollectionUnlockEvent
import com.auxilor.ecocollections.collections.Collection
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.ecocollections.plugin
import com.auxilor.ecocollections.libreforge.trigger.TriggerCollectionComplete
import com.auxilor.ecocollections.libreforge.trigger.TriggerCollectionTierUp
import com.auxilor.libreforge.EmptyProvidedHolder
import com.auxilor.libreforge.toDispatcher
import com.auxilor.libreforge.triggers.DispatchedTrigger
import com.auxilor.libreforge.triggers.TriggerData
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

fun OfflinePlayer.getCollectionCount(collection: Collection): Double {
    return this.profile.read(collection.countKey)
}

fun OfflinePlayer.getCollectionTier(collection: Collection): Int {
    return this.profile.read(collection.tierKey)
}

fun OfflinePlayer.isCollectionComplete(collection: Collection): Boolean {
    return this.profile.read(collection.doneKey)
}

fun OfflinePlayer.isCollectionUnlocked(collection: Collection): Boolean {
    if (!collection.hasUnlockConditions) return true
    return this.profile.read(collection.unlockedKey)
}

fun Player.tryUnlockCollection(collection: Collection): Boolean {
    if (!collection.hasUnlockConditions) return false
    if (this.profile.read(collection.unlockedKey)) return false

    if (!collection.unlockConditions.areMet(this.toDispatcher(), EmptyProvidedHolder)) {
        return false
    }

    val event = PlayerCollectionUnlockEvent(this, collection)
    Bukkit.getPluginManager().callEvent(event)
    if (event.isCancelled) return false

    this.profile.write(collection.unlockedKey, true)
    sendUnlockMessages(this, collection)
    return true
}

fun OfflinePlayer.setCollectionCount(collection: Collection, count: Double) {
    this.profile.write(collection.countKey, count)
    val newTier = collection.getTierForCount(count)
    this.profile.write(collection.tierKey, newTier)
}

fun Player.giveCollectionCount(collection: Collection, amount: Double) {
    if (amount <= 0) return

    if (!this.tryUnlockCollection(collection)) return

    if (collection.hasConditions) {
        if (!collection.conditions.areMet(this.toDispatcher(), EmptyProvidedHolder)) {
            return
        }
    }

    val previousCount = this.profile.read(collection.countKey)
    val newCount = previousCount + amount

    this.profile.write(collection.countKey, newCount)

    val previousTier = this.profile.read(collection.tierKey)
    val newTier = collection.getTierForCount(newCount)

    if (newTier == previousTier) return

    for (t in (previousTier + 1)..newTier) {
        val tierUpEvent = PlayerCollectionTierUpEvent(this, collection, t - 1, t)
        Bukkit.getPluginManager().callEvent(tierUpEvent)
        if (tierUpEvent.isCancelled) continue

        this.profile.write(collection.tierKey, t)

        collection.allTierRewards?.trigger(
            DispatchedTrigger(
                this.toDispatcher(),
                TriggerCollectionTierUp,
                TriggerData(player = this, location = this.location)
            )
        )

        collection.tierRewards[t]?.trigger(
            DispatchedTrigger(
                this.toDispatcher(),
                TriggerCollectionTierUp,
                TriggerData(player = this, location = this.location)
            )
        )

        sendTierUpMessages(this, collection, t - 1, t)
    }

    if (newTier == collection.maxTier && !this.profile.read(collection.doneKey)) {
        val completeEvent = PlayerCollectionCompleteEvent(this, collection)
        Bukkit.getPluginManager().callEvent(completeEvent)
        if (!completeEvent.isCancelled) {
            this.profile.write(collection.doneKey, true)

            collection.completionRewardEffects?.trigger(
                DispatchedTrigger(
                    this.toDispatcher(),
                    TriggerCollectionComplete,
                    TriggerData(player = this, location = this.location)
                )
            )

            sendCompletionMessages(this, collection)
        }
    }
}

fun OfflinePlayer.resetCollection(collection: Collection) {
    this.profile.write(collection.countKey, 0.0)
    this.profile.write(collection.tierKey, 0)
    this.profile.write(collection.doneKey, false)
    this.profile.write(collection.unlockedKey, false)
}

val OfflinePlayer.totalCollectionTiers: Int
    get() = Collections.values().sumOf { this.getCollectionTier(it) }

val OfflinePlayer.unlockedCollectionCount: Int
    get() = Collections.values().count { this.isCollectionUnlocked(it) }

val OfflinePlayer.completedCollectionCount: Int
    get() = Collections.values().count { this.isCollectionComplete(it) }


private fun applyPlaceholders(
    message: String,
    player: Player,
    collection: Collection,
    previousTier: Int? = null,
    tier: Int? = null
): String {
    var result = message
        .replace("%player%", player.name)
        .replace("%collection_name%", collection.name)
        .replace("%collection_id%", collection.id)
        .replace("%max_tier%", collection.maxTier.toString())
        .replace("%max_tier_numeral%", collection.maxTier.toNumeral())

    if (tier != null) {
        result = result
            .replace("%tier%", tier.toString())
            .replace("%tier_numeral%", tier.toNumeral())
    }

    if (previousTier != null) {
        result = result
            .replace("%previous_tier%", previousTier.toString())
            .replace("%previous_tier_numeral%", previousTier.toNumeral())
    }

    return StringUtils.format(result)
}

private fun sendTierUpMessages(player: Player, collection: Collection, previousTier: Int, tier: Int) {
    if (!plugin.configYml.getBool("messages.tier-up.enabled")) return

    if (plugin.configYml.getBool("messages.tier-up.chat")) {
        val chatMsg = plugin.langYml.getString("messages.tier-up.chat")
        player.sendMessage(applyPlaceholders(chatMsg, player, collection, previousTier, tier))
    }

    if (plugin.configYml.getBool("messages.tier-up.title")) {
        val title = applyPlaceholders(
            plugin.langYml.getString("messages.tier-up.title"),
            player, collection, previousTier, tier
        )
        val subtitle = applyPlaceholders(
            plugin.langYml.getString("messages.tier-up.subtitle"),
            player, collection, previousTier, tier
        )
        player.sendTitle(title, subtitle, 10, 40, 10)
    }

    if (plugin.configYml.getBool("messages.tier-up.sound")) {
        val soundString = plugin.langYml.getString("messages.tier-up.sound")
        player.playSound(player.location, soundString, 1.0f, 1.0f)
    }
}

private fun sendCompletionMessages(player: Player, collection: Collection) {
    if (!plugin.configYml.getBool("messages.complete.enabled")) return

    if (plugin.configYml.getBool("messages.complete.chat")) {
        val chatMsg = plugin.langYml.getString("messages.complete.chat")
        player.sendMessage(applyPlaceholders(chatMsg, player, collection))
    }

    if (plugin.configYml.getBool("messages.complete.title")) {
        val title = applyPlaceholders(
            plugin.langYml.getString("messages.complete.title"),
            player, collection
        )
        val subtitle = applyPlaceholders(
            plugin.langYml.getString("messages.complete.subtitle"),
            player, collection
        )
        player.sendTitle(title, subtitle, 10, 40, 10)
    }

    if (plugin.configYml.getBool("messages.complete.sound")) {
        val soundString = plugin.langYml.getString("messages.complete.sound")
        player.playSound(player.location, soundString, 1.0f, 1.0f)
    }

    if (plugin.configYml.getBool("messages.complete.broadcast")) {
        val broadcastMsg = applyPlaceholders(
            plugin.langYml.getString("messages.complete.broadcast"),
            player, collection
        )
        Bukkit.broadcastMessage(broadcastMsg)
    }
}

private fun sendUnlockMessages(player: Player, collection: Collection) {
    if (!plugin.configYml.getBool("messages.unlock.enabled")) return

    if (plugin.configYml.getBool("messages.unlock.chat")) {
        val chatMsg = plugin.langYml.getString("messages.unlock.chat")
        player.sendMessage(applyPlaceholders(chatMsg, player, collection))
    }

    if (plugin.configYml.getBool("messages.unlock.title")) {
        val title = applyPlaceholders(
            plugin.langYml.getString("messages.unlock.title"),
            player, collection
        )
        val subtitle = applyPlaceholders(
            plugin.langYml.getString("messages.unlock.subtitle"),
            player, collection
        )
        player.sendTitle(title, subtitle, 10, 40, 10)
    }

    if (plugin.configYml.getBool("messages.unlock.sound")) {
        val soundString = plugin.langYml.getString("messages.unlock.sound")
        player.playSound(player.location, soundString, 1.0f, 1.0f)
    }
}
