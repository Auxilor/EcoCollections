package com.exanthiax.ecocollections.gui

import com.willfp.eco.core.gui.addPage
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.onRightClick
import com.willfp.eco.core.gui.onShiftRightClick
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.leaderboard.LeaderboardRank
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNumeral
import com.willfp.libreforge.EmptyProvidedHolder
import com.willfp.libreforge.toDispatcher
import com.exanthiax.ecocollections.api.getCollectionCount
import com.exanthiax.ecocollections.api.getCollectionTier
import com.exanthiax.ecocollections.api.giveCollectionCount
import com.exanthiax.ecocollections.api.isCollectionUnlocked
import com.exanthiax.ecocollections.collections.Collection
import com.exanthiax.ecocollections.collections.canGainCollectionProgress
import com.exanthiax.ecocollections.groups.CollectionGroup
import com.exanthiax.ecocollections.plugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object GroupGUI {

    fun open(player: Player, group: CollectionGroup, bypassMode: Boolean = false, page: Int = 1) {
        val titleTemplate = plugin.configYml.getString("gui.group.title")
            .replace("%group_name%", group.name)
        val rows = plugin.configYml.getInt("gui.group.rows")

        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("gui.group.mask.materials"))
        val maskPattern = plugin.configYml.getStrings("gui.group.mask.pattern").toTypedArray()

        val collectionsInGroup = CollectionsGUI.getCollectionsInGroup(group)
        val showLeaderboardRank = plugin.configYml.getBool("leaderboards.show-in-group-gui")

        val maxPage = collectionsInGroup.maxOfOrNull { it.guiPage }?.coerceAtLeast(1) ?: 1
        val targetPage = page.coerceIn(1, maxPage)

        val formattedTitle = StringUtils.format(titleTemplate)
        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("gui.group.page-change-sound"))

        val theMenu = menu(rows) {
            title = formattedTitle

            maxPages(maxPage)
            defaultPage { targetPage }

            addPageChanger(plugin.configYml, "gui.group.prev-page", PageChanger.Direction.BACKWARDS, pageChangeSound)
            addPageChanger(plugin.configYml, "gui.group.next-page", PageChanger.Direction.FORWARDS, pageChangeSound)

            for (page in 1..maxPage) {
                addPage(page) {
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
                        if (collection.guiPage != page) {
                            continue
                        }

                        val builtSlot = buildCollectionSlot(player, group, bypassMode, collection, showLeaderboardRank)
                        if (builtSlot != null) {
                            setSlot(collection.guiRow, collection.guiColumn, builtSlot)
                        }
                    }

                    for (config in plugin.configYml.getSubsections("gui.group.custom-slots")) {
                        if (config.getInt("page").coerceAtLeast(1) != page) {
                            continue
                        }

                        setSlot(
                            config.getInt("row"),
                            config.getInt("column"),
                            ConfigSlot(config)
                        )
                    }

                    if (plugin.configYml.getBool("collections.manual-collect-mode.enabled")) {
                        val unlockedCollectionsInGroup = collectionsInGroup.filter { player.isCollectionUnlocked(it) }
                        setSlot(
                            plugin.configYml.getInt("gui.group.collect-all.location.row"),
                            plugin.configYml.getInt("gui.group.collect-all.location.column"),
                            buildCollectAllSlot(player, group, bypassMode, unlockedCollectionsInGroup)
                        )
                    }
                }
            }
        }

        theMenu.open(player)
    }

    private fun buildCollectAllSlot(
        player: Player,
        group: CollectionGroup,
        bypassMode: Boolean,
        collectionsInGroup: List<Collection>
    ): Slot {
        val collectAllMaterial = plugin.configYml.getString("gui.group.collect-all.material")
        val collectAllName = plugin.configYml.getString("gui.group.collect-all.name")

        val collectAllButtonItem = Items.lookup(collectAllMaterial).item.clone()
        val meta = collectAllButtonItem.itemMeta

        if (meta != null) {
            meta.setDisplayName(StringUtils.format(collectAllName))
            collectAllButtonItem.itemMeta = meta
        }

        return slot(collectAllButtonItem) {
            onLeftClick { _, _, _, menu ->
                manualCollectAllItems(player, group, bypassMode, collectionsInGroup, menu.getPage(player))
            }
        }
    }

    private fun buildCollectionSlot(
        player: Player,
        group: CollectionGroup,
        bypassMode: Boolean,
        collection: Collection,
        showLeaderboardRank: Boolean
    ): Slot? {
        val isUnlocked = player.isCollectionUnlocked(collection)

        if (isUnlocked) {
            val tier = player.getCollectionTier(collection)

            if (collection.hideBeforeTier1 && tier == 0) {
                return null
            }

            return buildUnlockedCollectionSlot(player, group, bypassMode, collection, tier, showLeaderboardRank)
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
        group: CollectionGroup,
        bypassMode: Boolean,
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

            if (plugin.configYml.getBool("collections.manual-collect-mode.enabled")) {
                for (line in plugin.langYml.getStrings("lore.manual-collect-one")) lore.add(StringUtils.format(line))
                for (line in plugin.langYml.getStrings("lore.manual-collect-all")) lore.add(StringUtils.format(line))
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

            if (plugin.configYml.getBool("collections.manual-collect-mode.enabled")) {
                onRightClick { _, _, _, menu ->
                    removeItemAndGiveCollectionCount(player, group, bypassMode, collection, false, menu.getPage(player))
                }
                onShiftRightClick { _, _, _, menu ->
                    removeItemAndGiveCollectionCount(player, group, bypassMode, collection, true, menu.getPage(player))
                }
            }
        }
    }

    private fun removeItemAndGiveCollectionCount(
        player: Player,
        group: CollectionGroup,
        bypassMode: Boolean,
        collection: Collection,
        removeAll: Boolean,
        page: Int
    ) {
        if (!player.canGainCollectionProgress()) {
            sendManualCollectDeniedMessage(player)
            return
        }

        if (collection.hasConditions && !collection.conditions.areMet(player.toDispatcher(), EmptyProvidedHolder)) {
            sendManualCollectDeniedMessage(player)
            return
        }

        val removed = removeManualCollectItemsFromInventory(player, collection, removeAll)
        if (removed <= 0) {
            return
        }

        player.giveCollectionCount(collection, removed.toDouble())
        open(player, group, bypassMode, page)
    }

    private fun manualCollectAllItems(
        player: Player,
        group: CollectionGroup,
        bypassMode: Boolean,
        collectionsInGroup: List<Collection>,
        page: Int
    ) {
        if (!player.canGainCollectionProgress()) {
            sendManualCollectDeniedMessage(player)
            return
        }

        var collected = false
        for (collection in collectionsInGroup) {
            if (collection.hasConditions && !collection.conditions.areMet(player.toDispatcher(), EmptyProvidedHolder)) continue

            val removed = removeManualCollectItemsFromInventory(player, collection, true)
            if (removed <= 0) continue

            collected = true
            player.giveCollectionCount(collection, removed.toDouble())
        }

        if (collected) {
            open(player, group, bypassMode, page)
        }
    }

    private fun sendManualCollectDeniedMessage(player: Player) {
        if (!plugin.configYml.getBool("messages.manual-collect-denied.enabled")) return

        if (plugin.configYml.getBool("messages.manual-collect-denied.chat")) {
            val message = plugin.langYml.getString("messages.manual-collect-denied.chat")
            player.sendMessage(StringUtils.format(message))
        }

        if (plugin.configYml.getBool("messages.manual-collect-denied.title")) {
            val title = plugin.langYml.getString("messages.manual-collect-denied.title")
            val subtitle = plugin.langYml.getString("messages.manual-collect-denied.subtitle")
            player.sendTitle(StringUtils.format(title), StringUtils.format(subtitle), 10, 40, 10)
        }

        PlayableSound.create(plugin.configYml.getSubsection("messages.manual-collect-denied.sound"))
            ?.playTo(player)
    }

    private fun removeManualCollectItemsFromInventory(
        player: Player,
        collection: Collection,
        removeAll: Boolean
    ): Int {
        val preventOverCount = plugin.configYml.getBool("collections.manual-collect-mode.prevent-over-count")
        val currentCount = player.getCollectionCount(collection)
        val maxCount = if (preventOverCount) {
            collection.tierRequirements.lastOrNull() ?: return 0
        } else {
            null
        }

        if (maxCount != null && currentCount >= maxCount) {
            return 0
        }

        var removed = 0
        val inventory = player.inventory
        val storageSize = inventory.storageContents.size

        for (slot in 0 until storageSize) {
            if (!removeAll && removed >= 1) {
                break
            }

            if (maxCount != null && currentCount + removed >= maxCount) {
                break
            }

            val item = inventory.getItem(slot) ?: continue
            if (item.type.isAir || collection.manualCollectItems.none { item.isSimilar(it.item) }) {
                continue
            }

            val remainingCap = if (maxCount != null) (maxCount - currentCount - removed).toInt().coerceAtLeast(0) else Int.MAX_VALUE
            val take = minOf(item.amount, if (removeAll) remainingCap else 1)
            item.amount -= take
            removed += take

            if (item.amount <= 0) {
                inventory.setItem(slot, null)
            } else {
                inventory.setItem(slot, item)
            }
        }

        return removed
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
        val rank = collection.leaderboard?.getRank(player.uniqueId) ?: LeaderboardRank.unranked()
        return when {
            rank.isExact -> plugin.langYml.getString("leaderboard-rank-exact")
                .replace("%rank%", rank.rank.toString())

            rank.isPercent -> plugin.langYml.getString("leaderboard-rank-percent")
                .replace("%percent%", rank.percent.toString())

            else -> plugin.langYml.getString("leaderboard-rank-unranked")
        }
    }
}
