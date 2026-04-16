package com.auxilor.ecocollections.gui

import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNumeral
import com.auxilor.ecocollections.api.getCollectionCount
import com.auxilor.ecocollections.api.getCollectionTier
import com.auxilor.ecocollections.api.isCollectionUnlocked
import com.auxilor.ecocollections.collections.Collection
import com.auxilor.ecocollections.collections.CollectionRank
import com.auxilor.ecocollections.collections.CollectionsLeaderboard.getCollectionRank
import com.auxilor.ecocollections.groups.CollectionGroup
import com.auxilor.ecocollections.plugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object GroupGUI {

    fun open(player: Player, group: CollectionGroup, bypassMode: Boolean = false) {
        val title = StringUtils.format(
            plugin.configYml.getString("gui.group.title")
                .replace("%group_name%", group.name)
        )
        val rows = plugin.configYml.getInt("gui.group.rows")

        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("gui.group.mask.materials"))
        val maskPattern = plugin.configYml.getStrings("gui.group.mask.pattern").toTypedArray()

        val collectionsInGroup = CollectionsGUI.getCollectionsInGroup(group)
        val showLeaderboardRank = plugin.configYml.getBool("leaderboards.show-in-group-gui")

        val theMenu = menu(rows) {
            setTitle(title)

            setMask(
                FillerMask(
                    maskItems,
                    *maskPattern
                )
            )

            if (!bypassMode) {
                val backMaterial = plugin.configYml.getString("gui.group.back.material")
                val backName = plugin.configYml.getString("gui.group.back.name")
                val backRow = plugin.configYml.getInt("gui.group.back.location.row")
                val backColumn = plugin.configYml.getInt("gui.group.back.location.column")

                val backItem = ItemStack(Material.matchMaterial(backMaterial.uppercase()) ?: Material.ARROW)
                val backMeta = backItem.itemMeta
                backMeta?.setDisplayName(StringUtils.format(backName))
                backItem.itemMeta = backMeta

                setSlot(backRow, backColumn, slot(backItem) {
                    onLeftClick { _, _, _, _ ->
                        CollectionsGUI.open(player)
                    }
                })
            }

            for (collection in collectionsInGroup) {
                val builtSlot = buildCollectionSlot(player, collection, showLeaderboardRank)
                if (builtSlot != null) {
                    setSlot(collection.guiRow, collection.guiColumn, builtSlot)
                }
            }

            for (config in plugin.configYml.getSubsections("gui.group.custom-slots")) {
                setSlot(
                    config.getInt("row"),
                    config.getInt("column"),
                    ConfigSlot(config)
                )
            }
        }

        theMenu.open(player)
    }

    private fun buildCollectionSlot(
        player: Player,
        collection: Collection,
        showLeaderboardRank: Boolean
    ): Slot? {
        val isUnlocked = player.isCollectionUnlocked(collection)

        if (isUnlocked) {
            val tier = player.getCollectionTier(collection)

            if (collection.hideBeforeTier1 && tier == 0) {
                return null
            }

            return buildUnlockedCollectionSlot(player, collection, tier, showLeaderboardRank)
        } else {
            if (collection.hideWhenLocked) {
                return null
            }

            if (!plugin.configYml.getBool("gui.locked.show-locked-collections")) {
                return null
            }

            return buildLockedCollectionSlot(player, collection)
        }
    }

    private fun buildUnlockedCollectionSlot(
        player: Player,
        collection: Collection,
        tier: Int,
        showLeaderboardRank: Boolean
    ): Slot {
        val iconItem = collection.icon.item.clone()
        val meta = iconItem.itemMeta

        if (meta != null) {
            meta.setDisplayName(StringUtils.format(collection.name))

            val lore = mutableListOf<String>()
            val count = player.getCollectionCount(collection)

            for (line in collection.guiLore) {
                if (line.contains("%description%")) {
                    for (descLine in collection.descriptionLines) {
                        lore.add(StringUtils.format(
                            substituteCollectionPlaceholders(descLine, collection, tier, count)
                        ))
                    }
                } else {
                    lore.add(StringUtils.format(
                        substituteCollectionPlaceholders(line, collection, tier, count)
                    ))
                }
            }

            if (showLeaderboardRank) {
                val rankLine = formatRankLore(player, collection)
                if (rankLine != null) {
                    lore.add(StringUtils.format(rankLine))
                }
            }

            meta.lore = lore
            iconItem.itemMeta = meta
        }

        return slot(iconItem) {
            onLeftClick { _, _, _, _ ->
                CollectionDetailGUI.open(player, collection)
            }
        }
    }

    private fun buildLockedCollectionSlot(
        player: Player,
        collection: Collection
    ): Slot {
        val lockedMaterial = plugin.configYml.getString("gui.locked.icon.material")
        val lockedName = plugin.configYml.getString("gui.locked.icon.name")
        val lockedLoreConfig = plugin.configYml.getStrings("gui.locked.icon.lore")
        val clickSound = PlayableSound.create(plugin.configYml.getSubsection("gui.locked.click-sound"))

        val lockedItem = Items.lookup(lockedMaterial).item.clone()
        val meta = lockedItem.itemMeta

        if (meta != null) {
            meta.setDisplayName(StringUtils.format(lockedName))

            val requirementLineTemplate = plugin.langYml.getString("placeholders.unlock-requirement-line")
            val unlockRequirements = buildUnlockRequirementsLore(collection, requirementLineTemplate)

            val lore = mutableListOf<String>()
            for (line in lockedLoreConfig) {
                if (line.contains("%unlock_requirements%")) {
                    for (reqLine in unlockRequirements) {
                        lore.add(StringUtils.format(reqLine))
                    }
                } else {
                    lore.add(StringUtils.format(line))
                }
            }

            meta.lore = lore
            lockedItem.itemMeta = meta
        }

        return slot(lockedItem) {
            onLeftClick { event, _ ->
                val p = event.whoClicked as? Player ?: return@onLeftClick
                clickSound?.playTo(p)
            }
        }
    }

    private fun buildUnlockRequirementsLore(
        collection: Collection,
        lineTemplate: String
    ): List<String> {
        val conditionSections = collection.config.getSubsections("unlock-conditions")
        val descriptions = conditionSections.map { section ->
            if (section.has("description")) {
                section.getString("description")
            } else {
                section.getString("id")
            }
        }

        return descriptions.map { desc ->
            lineTemplate.replace("%requirement%", desc)
        }
    }

    private fun substituteCollectionPlaceholders(
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

    internal fun formatRankLore(player: Player, collection: Collection): String? {
        val rank = player.getCollectionRank(collection)
        return when (rank) {
            is CollectionRank.Exact -> plugin.langYml.getString("leaderboard-rank-exact")
                .replace("%rank%", rank.rank.toString())

            is CollectionRank.Percent -> plugin.langYml.getString("leaderboard-rank-percent")
                .replace("%percent%", rank.topPercent.toString())

            is CollectionRank.Unranked -> plugin.langYml.getString("leaderboard-rank-unranked")
        }
    }
}
