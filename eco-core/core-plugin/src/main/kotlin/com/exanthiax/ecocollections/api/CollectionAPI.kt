@file:JvmName("CollectionAPI")

package com.exanthiax.ecocollections.api

import com.willfp.eco.core.data.profile
import com.willfp.eco.core.progression.ProgressionPlaceholders
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNumeral
import com.exanthiax.ecocollections.api.event.PlayerCollectionCompleteEvent
import com.exanthiax.ecocollections.api.event.PlayerCollectionTierUpEvent
import com.exanthiax.ecocollections.api.event.PlayerCollectionUnlockEvent
import com.exanthiax.ecocollections.collections.Collection
import com.exanthiax.ecocollections.collections.Collections
import com.exanthiax.ecocollections.plugin
import com.exanthiax.ecocollections.libreforge.trigger.TriggerCollectionComplete
import com.exanthiax.ecocollections.libreforge.trigger.TriggerCollectionTierUp
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.toNiceString
import com.willfp.libreforge.EmptyProvidedHolder
import com.willfp.libreforge.levels.LevelUpDispatcher
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.DispatchedTrigger
import com.willfp.libreforge.triggers.TriggerData
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * Fire the tier-up chains for one tier.
 *
 * Called per tier rather than once per grant: crossing several tiers in one submission runs
 * the reward chains once for each, and each run must describe its own tier.
 *
 * `type = "tier"` is the whole reason the shared dispatcher takes a type. Everything a server
 * owner writes here - the config keys, the events, the docs - is phrased in tiers, so the
 * placeholders are `%tier%`, `%tier_2%` and so on rather than `%level%`.
 */
private fun triggerTierUp(player: Player, collection: Collection, tier: Int) {
    for (chain in listOf(collection.allTierRewards, collection.tierRewards[tier])) {
        LevelUpDispatcher.dispatch(
            player.toDispatcher(),
            TriggerCollectionTierUp,
            chain ?: continue,
            tier,
            // value is the tier reached. TriggerCollectionTierUp declares
            // TriggerParameter.VALUE and its own event-driven dispatch sets it, but these
            // manual dispatches did not - so an effect reading the trigger's value got 0.0
            // here and the real tier there, depending only on which path fired it.
            TriggerData(player = player, location = player.location, value = tier.toDouble()),
            type = "tier"
        )
    }
}

fun OfflinePlayer.getCollectionCount(collection: Collection): Double {
    return this.profile.read(collection.countKey)
}

fun OfflinePlayer.getCollectionTier(collection: Collection): Int {
    return this.profile.read(collection.tierKey)
}

fun OfflinePlayer.isCollectionComplete(collection: Collection): Boolean {
    return this.profile.read(collection.doneKey)
}

fun OfflinePlayer.isCollectionUnlocked(collection: Collection): Boolean =
    if (!collection.hasUnlockConditions) {
        true
    } else {
        this.profile.read(collection.unlockedKey)
    }

fun Player.tryUnlockCollection(collection: Collection): Boolean {
    if (this.profile.read(collection.unlockedKey)) {
        return true
    }
    if (collection.hasUnlockConditions && !collection.unlockConditions.areMet(this.toDispatcher(), EmptyProvidedHolder)) {
        return false
    }
    val event = PlayerCollectionUnlockEvent(this, collection)
    Bukkit.getPluginManager().callEvent(event)
    if (event.isCancelled) {
        return false
    }
    this.profile.write(collection.unlockedKey, true)
    sendUnlockMessages(this, collection)
    return true
}

fun OfflinePlayer.setCollectionCount(collection: Collection, count: Double) {
    this.profile.write(collection.countKey, count)
    val newTier = collection.getTierForCount(count)
    // Deliberately fires no tier-up events - this is an admin set, not organic progression.
    this.profile.write(collection.tierKey, newTier)
}

fun Player.giveCollectionCount(collection: Collection, amount: Double) {
    if (amount <= 0) return

    if (!this.tryUnlockCollection(collection)) {
        return
    }

    val conditions = collection.hasConditions
    val met = !collection.conditions.areMet(this.toDispatcher(), EmptyProvidedHolder)
    if (conditions && met) {
        return
    }

    val previousCount = this.profile.read(collection.countKey)
    val newCount = previousCount + amount

    this.profile.write(collection.countKey, newCount)

    sendCountUpMessages(this, collection, amount)

    val previousTier = this.profile.read(collection.tierKey)
    val newTier = collection.getTierForCount(newCount)

    if (newTier == previousTier) return

    var committed = previousTier

    for (t in (previousTier + 1)..newTier) {
        val tierUpEvent = PlayerCollectionTierUpEvent(this, collection, t - 1, t)
        Bukkit.getPluginManager().callEvent(tierUpEvent)

        if (tierUpEvent.isCancelled) {
            // Stop the ladder here. `continue` used to let a later iteration write a higher
            // tier anyway, so the cancelled tier was both skipped and implicitly re-granted -
            // its rewards were lost permanently.
            break
        }

        this.profile.write(collection.tierKey, t)
        committed = t

        // %tier% and %tier_numeral% already resolved in these chains, but from the config
        // placeholders injected in Collection's init - so they read the player's *current*
        // tier rather than the tier this iteration is granting. They agree here, because the
        // tier is committed just above before the chain runs. %previous_tier% and
        // %previous_tier_numeral% had no source at all and simply did not resolve.
        triggerTierUp(this, collection, t)

        sendTierUpMessages(this, collection, t - 1, t)
    }

    if (committed == collection.maxTier && !this.profile.read(collection.doneKey)) {
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
    tier: Int? = null,
    amount: Double? = null
): String {
    var result = message
        .replace("%player%", player.name)
        .replace("%collection_name%", collection.name)
        .replace("%collection_id%", collection.id)
        .replace("%max_tier%", collection.maxTier.toString())
        .replace("%max_tier_numeral%", collection.maxTier.toNumeral())

    // An explicitly passed previousTier wins, since it is a parameter in its own right rather
    // than always tier - 1. In practice every caller passes tier - 1, but the signature allows
    // otherwise and this keeps that promise.
    if (previousTier != null) {
        result = result
            .replace("%previous_tier%", previousTier.toString())
            .replace("%previous_tier_numeral%", previousTier.toNumeral())
    }

    if (tier != null) {
        // %tier%, %tier_numeral%, any remaining %previous_tier*%, and the %tier_N% offsets -
        // the last of which used to be a local copy of a regex that three other plugins also
        // each kept their own copy of. It now lives in eco, with tests.
        result = ProgressionPlaceholders.inject(result, "tier", tier)
    }

    if (amount != null) {
        result = result.replace("%amount%", amount.toLong().toString())
    }

    return result.formatEco(player, formatPlaceholders = true)
}

private fun sendCountUpMessages(player: Player, collection: Collection, amount: Double) {
    if (!plugin.configYml.getBool("messages.count-up.enabled")) return

    if (plugin.configYml.getBool("messages.count-up.chat")) {
        val chatMsg = plugin.langYml.getString("messages.count-up.chat")
        player.sendMessage(applyPlaceholders(chatMsg, player, collection, amount = amount))
    }

    if (plugin.configYml.getBool("messages.count-up.title")) {
        val title = applyPlaceholders(
            plugin.langYml.getString("messages.count-up.title"),
            player, collection, amount = amount
        )
        val subtitle = applyPlaceholders(
            plugin.langYml.getString("messages.count-up.subtitle"),
            player, collection, amount = amount
        )
        player.sendTitle(title, subtitle, 10, 40, 10)
    }

    PlayableSound.create(plugin.configYml.getSubsection("messages.count-up.sound"))
        ?.playTo(player)
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

    PlayableSound.create(plugin.configYml.getSubsection("messages.tier-up.sound"))
        ?.playTo(player)
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

    PlayableSound.create(plugin.configYml.getSubsection("messages.complete.sound"))
        ?.playTo(player)

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

    PlayableSound.create(plugin.configYml.getSubsection("messages.unlock.sound"))
        ?.playTo(player)
}
