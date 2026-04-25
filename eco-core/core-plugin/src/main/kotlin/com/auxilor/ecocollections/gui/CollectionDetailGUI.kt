package com.auxilor.ecocollections.gui

import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNumeral
import com.auxilor.ecocollections.api.getCollectionCount
import com.auxilor.ecocollections.api.getCollectionTier
import com.auxilor.ecocollections.collections.Collection
import com.auxilor.ecocollections.plugin
import org.bukkit.entity.Player

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

        val centerSlot = buildCenterSlot(player, collection, playerTier, playerCount)
        val component = TierProgressionComponent(collection)

        val theMenu = menu(rows) {
            setTitle(title)

            setMask(FillerMask(maskItems, *maskPattern))

            maxPages(component.pages)
            addComponent(1, 1, component)

            defaultPage {
                component.getPageOf(it.getCollectionTier(collection)).coerceAtLeast(1)
            }

            setSlot(
                plugin.configYml.getInt("gui.detail.info-icon.location.row"),
                plugin.configYml.getInt("gui.detail.info-icon.location.column"),
                centerSlot
            )

            addComponent(
                plugin.configYml.getInt("gui.detail.buttons.prev-page.location.row"),
                plugin.configYml.getInt("gui.detail.buttons.prev-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.detail.buttons.prev-page.material")))
                        .setDisplayName(StringUtils.format(plugin.configYml.getString("gui.detail.buttons.prev-page.name")))
                        .build(),
                    PageChanger.Direction.BACKWARDS
                )
            )

            addComponent(
                plugin.configYml.getInt("gui.detail.buttons.next-page.location.row"),
                plugin.configYml.getInt("gui.detail.buttons.next-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.detail.buttons.next-page.material")))
                        .setDisplayName(StringUtils.format(plugin.configYml.getString("gui.detail.buttons.next-page.name")))
                        .build(),
                    PageChanger.Direction.FORWARDS
                )
            )

            setSlot(
                plugin.configYml.getInt("gui.detail.buttons.back.location.row"),
                plugin.configYml.getInt("gui.detail.buttons.back.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.detail.buttons.back.material")))
                        .setDisplayName(StringUtils.format(plugin.configYml.getString("gui.detail.buttons.back.name")))
                        .build()
                ) {
                    onLeftClick { _, _, _, _ ->
                        val group = collection.group
                        if (group != null) GroupGUI.open(player, group) else CollectionsGUI.open(player)
                    }
                }
            )

            if (plugin.configYml.getBool("gui.detail.buttons.rank.enabled")) {
                setSlot(
                    plugin.configYml.getInt("gui.detail.buttons.rank.location.row"),
                    plugin.configYml.getInt("gui.detail.buttons.rank.location.column"),
                    buildRankSlot(player, collection)
                )
            }

            for (config in plugin.configYml.getSubsections("gui.detail.custom-slots")) {
                setSlot(config.getInt("row"), config.getInt("column"), ConfigSlot(config))
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

    private fun buildRankSlot(player: Player, collection: Collection): Slot {
        val rankItem = Items.lookup(plugin.configYml.getString("gui.detail.buttons.rank.material")).item.clone()
        val meta = rankItem.itemMeta

        if (meta != null) {
            meta.setDisplayName(StringUtils.format(plugin.configYml.getString("gui.detail.buttons.rank.name")))

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
