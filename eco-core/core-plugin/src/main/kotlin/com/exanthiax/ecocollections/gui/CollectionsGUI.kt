package com.exanthiax.ecocollections.gui

import com.willfp.eco.core.config.BuildableConfig
import com.willfp.eco.core.gui.addPage
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.StringUtils
import com.exanthiax.ecocollections.api.isCollectionComplete
import com.exanthiax.ecocollections.collections.Collection
import com.exanthiax.ecocollections.collections.Collections
import com.exanthiax.ecocollections.groups.CollectionGroup
import com.exanthiax.ecocollections.groups.CollectionGroups
import com.exanthiax.ecocollections.plugin
import com.willfp.eco.core.gui.slot.Slot
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object CollectionsGUI {

    fun open(player: Player) {
        val groups = CollectionGroups.values()

        when {
            groups.isEmpty() -> {
                val syntheticGroup = createSyntheticDefaultGroup()
                GroupGUI.open(player, syntheticGroup, bypassMode = true)
            }

            groups.size == 1 -> {
                GroupGUI.open(player, groups.first(), bypassMode = true)
            }

            else -> {
                openRootGUI(player)
            }
        }
    }

    private fun createSyntheticDefaultGroup(): CollectionGroup {
        val displayName = plugin.langYml.getString("groups.default-group-name")

        val config = BuildableConfig()
        config.set("name", displayName)
        config.set("permission", "")
        config.set("gui.icon", "barrier")
        config.set("gui.position.row", 1)
        config.set("gui.position.column", 1)
        config.set("gui.lore", emptyList<String>())

        return CollectionGroup("default", config)
    }

    private fun openRootGUI(player: Player) {
        val titleTemplate = plugin.configYml.getString("gui.collections.title")
        val rows = plugin.configYml.getInt("gui.collections.rows")

        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("gui.collections.mask.materials"))
        val maskPattern = plugin.configYml.getStrings("gui.collections.mask.pattern").toTypedArray()

        val closeMaterial = plugin.configYml.getString("gui.collections.close.material")
        val closeName = plugin.configYml.getString("gui.collections.close.name")
        val closeRow = plugin.configYml.getInt("gui.collections.close.location.row")
        val closeColumn = plugin.configYml.getInt("gui.collections.close.location.column")

        val closeItem = ItemStack(Material.matchMaterial(closeMaterial.uppercase()) ?: Material.BARRIER)
        val closeMeta = closeItem.itemMeta
        closeMeta?.setDisplayName(StringUtils.format(closeName))
        closeItem.itemMeta = closeMeta

        val visibleGroups = CollectionGroups.values().filter {
            it.permission.isEmpty() || player.hasPermission(it.permission)
        }
        val maxPage = visibleGroups.maxOfOrNull { it.guiPage }?.coerceAtLeast(1) ?: 1

        val formattedTitle = StringUtils.format(titleTemplate)
        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("gui.collections.page-change-sound"))

        val theMenu = menu(rows) {
            title = formattedTitle

            maxPages(maxPage)

            addPageChanger(plugin.configYml, "gui.collections.prev-page", PageChanger.Direction.BACKWARDS, pageChangeSound)
            addPageChanger(plugin.configYml, "gui.collections.next-page", PageChanger.Direction.FORWARDS, pageChangeSound)

            for (page in 1..maxPage) {
                addPage(page) {
                    setMask(
                        FillerMask(
                            maskItems,
                            *maskPattern
                        )
                    )

                    setSlot(closeRow, closeColumn, slot(closeItem) {
                        onLeftClick { event, _ ->
                            event.whoClicked.closeInventory()
                        }
                    })

                    for (group in visibleGroups) {
                        if (group.guiPage != page) {
                            continue
                        }

                        val groupIcon = buildGroupIcon(player, group)
                        setSlot(group.guiRow, group.guiColumn, groupIcon)
                    }

                    for (config in plugin.configYml.getSubsections("gui.collections.custom-slots")) {
                        if (config.getInt("page").coerceAtLeast(1) != page) {
                            continue
                        }

                        setSlot(
                            config.getInt("row"),
                            config.getInt("column"),
                            ConfigSlot(config)
                        )
                    }
                }
            }
        }

        theMenu.open(player)
    }

    private fun buildGroupIcon(player: Player, group: CollectionGroup): Slot {
        val iconItem = group.icon.item.clone()
        val meta = iconItem.itemMeta

        if (meta != null) {
            meta.setDisplayName(StringUtils.format(group.name))

            val collectionsInGroup = getCollectionsInGroup(group)
            val maxedCount = collectionsInGroup.count { player.isCollectionComplete(it) }
            val totalCount = collectionsInGroup.size

            val lore = mutableListOf<String>()
            for (line in group.guiLore) {
                lore.add(StringUtils.format(line))
            }
            lore.add("")
            lore.add(StringUtils.format("&7Progress: &e$maxedCount&7/&e$totalCount &7maxed"))

            meta.lore = lore
            iconItem.itemMeta = meta
        }

        return slot(iconItem) {
            onLeftClick { _, _, _, _ ->
                GroupGUI.open(player, group)
            }
        }
    }

    internal fun getCollectionsInGroup(group: CollectionGroup): List<Collection> {
        if (group.id == "default" && CollectionGroups.values().isEmpty()) {
            return Collections.values().toList()
        }
        return Collections.values().filter { it.group?.id == group.id }
    }
}
