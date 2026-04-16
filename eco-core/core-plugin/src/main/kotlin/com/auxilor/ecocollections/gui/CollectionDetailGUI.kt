package com.auxilor.ecocollections.gui

import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNumeral
import com.auxilor.ecocollections.api.getCollectionCount
import com.auxilor.ecocollections.api.getCollectionTier
import com.auxilor.ecocollections.collections.Collection
import com.auxilor.ecocollections.plugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object CollectionDetailGUI {

    fun open(player: Player, collection: Collection) {
        val title = StringUtils.format(
            plugin.configYml.getString("gui.detail.title")
                .replace("%collection_name%", collection.name)
        )
        val rows = plugin.configYml.getInt("gui.detail.rows")

        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("gui.detail.mask.materials"))
        val maskPattern = plugin.configYml.getStrings("gui.detail.mask.pattern").toTypedArray()

        val playerTier = player.getCollectionTier(collection)
        val playerCount = player.getCollectionCount(collection)

        val progressionPattern = plugin.configYml.getStrings("gui.detail.progression-slots.pattern")
        val tierPositions = parseProgressionPattern(progressionPattern)

        val centerSlot = buildCenterSlot(player, collection, playerTier, playerCount)

        val backRow = rows
        val backCol = 1

        val backItem = ItemStack(Material.ARROW)
        val backMeta = backItem.itemMeta
        backMeta?.setDisplayName(StringUtils.format("&fBack"))
        backItem.itemMeta = backMeta

        val rankSlot = buildRankSlot(player, collection)

        val theMenu = menu(rows) {
            setTitle(title)

            setMask(
                FillerMask(
                    maskItems,
                    *maskPattern
                )
            )

            setSlot(
                plugin.configYml.getInt("gui.detail.info-icon.location.row"),
                plugin.configYml.getInt("gui.detail.info-icon.location.column"),
                centerSlot
            )

            for (tier in 1..collection.maxTier) {
                val position = tierPositions.getOrNull(tier - 1) ?: continue
                val tierSlotItem = buildTierSlot(collection, tier, playerTier, playerCount)
                setSlot(position.first, position.second, tierSlotItem)
            }

            setSlot(backRow, backCol, slot(backItem) {
                onLeftClick { _, _, _, _ ->
                    val group = collection.group
                    if (group != null) {
                        GroupGUI.open(player, group)
                    } else {
                        CollectionsGUI.open(player)
                    }
                }
            })

            setSlot(backRow, 9, rankSlot)

            for (config in plugin.configYml.getSubsections("gui.detail.custom-slots")) {
                setSlot(
                    config.getInt("row"),
                    config.getInt("column"),
                    ConfigSlot(config)
                )
            }
        }

        theMenu.open(player)
    }

    private fun buildCenterSlot(
        player: Player,
        collection: Collection,
        playerTier: Int,
        playerCount: Double
    ): Slot {
        val iconItem = collection.icon.item.clone()
        val meta = iconItem.itemMeta

        if (meta != null) {
            meta.setDisplayName(StringUtils.format(collection.name))

            val lore = mutableListOf<String>()

            for (line in collection.descriptionLines) {
                lore.add(
                    StringUtils.format(
                        substituteDetailPlaceholders(line, collection, playerTier, playerCount)
                    )
                )
            }

            val rankLine = GroupGUI.formatRankLore(player, collection)
            if (rankLine != null) {
                lore.add("")
                lore.add(StringUtils.format(rankLine))
            }

            meta.lore = lore
            iconItem.itemMeta = meta
        }

        return slot(iconItem)
    }

    private fun buildTierSlot(
        collection: Collection,
        tier: Int,
        playerTier: Int,
        playerCount: Double
    ): Slot {
        val isMaxTier = tier == collection.maxTier
        val requirement = collection.tierRequirements[tier - 1]

        val state = when {
            isMaxTier && playerTier >= tier -> "completed"
            playerTier >= tier -> "reached"
            tier == playerTier + 1 -> "in-progress"
            else -> "locked"
        }

        val configKey = "gui.detail.progression-slots.$state"

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
                StringUtils.format(
                    plugin.configYml.getString("$configKey.name").applyPlaceholders()
                )
            )

            val rewardMessages = collection.getRewardMessages(tier).map {
                StringUtils.format(it.applyPlaceholders())
            }

            val lore = plugin.configYml.getStrings("$configKey.lore").flatMap { line ->
                if (line.contains("%rewards%")) {
                    if (rewardMessages.isEmpty()) {
                        emptyList()
                    } else {
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

    private fun buildRankSlot(player: Player, collection: Collection): Slot {
        val rankItem = ItemStack(Material.PLAYER_HEAD)
        val meta = rankItem.itemMeta

        if (meta != null) {
            meta.setDisplayName(StringUtils.format("&7Your Ranking"))

            val lore = mutableListOf<String>()
            val rankLine = GroupGUI.formatRankLore(player, collection)
            if (rankLine != null) {
                lore.add(StringUtils.format(rankLine))
            }

            meta.lore = lore
            rankItem.itemMeta = meta
        }

        return slot(rankItem)
    }

    private fun parseProgressionPattern(pattern: List<String>): List<Pair<Int, Int>> {
        val slots = mutableListOf<Triple<Int, Int, Int>>() // row, column, order

        for ((rowIndex, line) in pattern.withIndex()) {
            for ((colIndex, char) in line.withIndex()) {
                val order = when (char) {
                    '0' -> continue
                    in '1'..'9' -> char - '0'
                    in 'a'..'z' -> char - 'a' + 10
                    else -> continue
                }
                slots.add(Triple(rowIndex + 1, colIndex + 1, order))
            }
        }

        return slots.sortedBy { it.third }.map { Pair(it.first, it.second) }
    }

    private fun substituteDetailPlaceholders(
        text: String,
        collection: Collection,
        tier: Int,
        count: Double
    ): String {
        val maxTier = collection.maxTier
        val required = if (tier >= maxTier) {
            plugin.langYml.getString("placeholders.max-tier")
        } else {
            collection.tierRequirements[tier].toLong().toString()
        }

        val percent = if (tier >= maxTier) {
            "100"
        } else {
            val req = collection.tierRequirements[tier]
            val prevReq = if (tier > 0) collection.tierRequirements[tier - 1] else 0.0
            ((count - prevReq) / (req - prevReq) * 100)
                .coerceIn(0.0, 100.0)
                .toInt()
                .toString()
        }

        val previousTier = (tier - 1).coerceAtLeast(0)
        val nextTier = (tier + 1).coerceAtMost(maxTier)

        return text
            .replace("%tier%", tier.toString())
            .replace("%tier_numeral%", tier.toNumeral())
            .replace("%previous_tier%", previousTier.toString())
            .replace("%previous_tier_numeral%", previousTier.toNumeral())
            .replace("%next_tier%", nextTier.toString())
            .replace("%next_tier_numeral%", nextTier.toNumeral())
            .replace("%max_tier%", maxTier.toString())
            .replace("%max_tier_numeral%", maxTier.toNumeral())
            .replace("%count%", count.toLong().toString())
            .replace("%required%", required)
            .replace("%percent%", percent)
            .replace("%collection_name%", collection.name)
    }
}
