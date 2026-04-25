package com.auxilor.ecocollections.gui

import com.auxilor.ecocollections.api.getCollectionCount
import com.auxilor.ecocollections.api.getCollectionTier
import com.auxilor.ecocollections.collections.Collection
import com.auxilor.ecocollections.plugin
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNumeral
import com.willfp.ecomponent.AutofillComponent
import com.willfp.ecomponent.GUIPosition
import org.bukkit.entity.Player
import kotlin.math.ceil

private val progressionOrder =
    "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

class TierProgressionComponent(
    private val collection: Collection
) : AutofillComponent() {

    private val progressionSlots: Map<GUIPosition, Int>
    val slotsPerPage: Int
    val pages: Int

    init {
        val pattern = plugin.configYml.getStrings("gui.detail.progression-slots.pattern")
        val slots = mutableMapOf<GUIPosition, Int>()

        var x = 0
        for (row in pattern) {
            x++
            var y = 0
            for (char in row) {
                y++
                if (char == '0') continue
                val pos = progressionOrder.indexOf(char)
                if (pos == -1) continue
                slots[GUIPosition(x, y)] = pos + 1
            }
        }

        progressionSlots = slots
        slotsPerPage = slots.size.coerceAtLeast(1)
        pages = ceil(collection.maxTier.toDouble() / slotsPerPage).toInt().coerceAtLeast(1)
    }

    fun getPageOf(tier: Int): Int =
        ceil(tier.toDouble() / slotsPerPage).toInt().coerceAtLeast(1)

    override fun getSlotAt(row: Int, column: Int, player: Player, menu: Menu): Slot? {
        val posIndex = progressionSlots[GUIPosition(row, column)] ?: return null
        val tierIndex = (menu.getPage(player) - 1) * slotsPerPage + posIndex

        if (tierIndex > collection.maxTier) return null

        return buildTierSlot(player, collection, tierIndex)
    }
}

private fun buildTierSlot(player: Player, collection: Collection, tier: Int): Slot {
    val playerTier = player.getCollectionTier(collection)
    val playerCount = player.getCollectionCount(collection)
    val isMaxTier = tier == collection.maxTier
    val requirement = collection.tierRequirements[tier - 1]

    val configKey = when {
        isMaxTier && playerTier >= tier -> "gui.detail.progression-slots.completed"
        playerTier >= tier -> "gui.detail.progression-slots.reached"
        tier == playerTier + 1 -> "gui.detail.progression-slots.in-progress"
        else -> "gui.detail.progression-slots.locked"
    }

    val tierItem = Items.lookup(plugin.configYml.getString("$configKey.item")).item.clone()
    val meta = tierItem.itemMeta

    if (meta != null) {
        val percent = if (requirement > 0) {
            (playerCount / requirement * 100).coerceIn(0.0, 100.0).toInt().toString()
        } else {
            "100"
        }

        fun String.applyPlaceholders() = this
            .replace("%tier%", tier.toString())
            .replace("%tier_numeral%", tier.toNumeral())
            .replace("%count%", playerCount.toLong().toString())
            .replace("%required%", requirement.toLong().toString())
            .replace("%percent%", percent)
            .replace("%collection_name%", collection.name)

        meta.setDisplayName(
            StringUtils.format(plugin.configYml.getString("$configKey.name").applyPlaceholders())
        )

        val rewardMessages = collection.getRewardMessages(tier).map {
            StringUtils.format(it.applyPlaceholders())
        }

        val lore = plugin.configYml.getStrings("$configKey.lore").flatMap { line ->
            if (line.contains("%rewards%")) {
                if (rewardMessages.isEmpty()) emptyList()
                else {
                    val margin = line.length - line.trimStart().length
                    rewardMessages.map { " ".repeat(margin) + it }
                }
            } else {
                listOf(StringUtils.format(line.applyPlaceholders()))
            }
        }

        meta.lore = lore
        tierItem.itemMeta = meta
    }

    return slot(tierItem)
}
