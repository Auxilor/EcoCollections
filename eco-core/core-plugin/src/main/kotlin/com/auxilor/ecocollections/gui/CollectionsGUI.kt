package com.auxilor.ecocollections.gui

import com.willfp.eco.core.config.BuildableConfig
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.util.StringUtils
import com.auxilor.ecocollections.api.isCollectionComplete
import com.auxilor.ecocollections.collections.Collection
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.ecocollections.groups.CollectionGroup
import com.auxilor.ecocollections.groups.CollectionGroups
import com.auxilor.ecocollections.plugin
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
        val title = StringUtils.format(plugin.configYml.getString("gui.root.title"))
        val rows = plugin.configYml.getInt("gui.root.rows")

        val fillerMaterial = plugin.configYml.getString("gui.root.filler.material")
        val fillerMat = Material.matchMaterial(fillerMaterial.uppercase()) ?: Material.BLACK_STAINED_GLASS_PANE

        val closeMaterial = plugin.configYml.getString("gui.root.close.material")
        val closeName = plugin.configYml.getString("gui.root.close.name")
        val closeRow = plugin.configYml.getInt("gui.root.close.location.row")
        val closeColumn = plugin.configYml.getInt("gui.root.close.location.column")

        val closeItem = ItemStack(Material.matchMaterial(closeMaterial.uppercase()) ?: Material.BARRIER)
        val closeMeta = closeItem.itemMeta
        closeMeta?.setDisplayName(StringUtils.format(closeName))
        closeItem.itemMeta = closeMeta

        val theMenu = menu(rows) {
            setTitle(title)

            setMask(
                FillerMask(
                    MaskItems(fillerMat),
                    *Array(rows) { "111111111" }
                )
            )

            setSlot(closeRow, closeColumn, slot(closeItem) {
                onLeftClick { event, _ ->
                    event.whoClicked.closeInventory()
                }
            })

            for (group in CollectionGroups.values()) {
                if (group.permission.isNotEmpty() && !player.hasPermission(group.permission)) {
                    continue
                }

                val groupIcon = buildGroupIcon(player, group)
                setSlot(group.guiRow, group.guiColumn, groupIcon)
            }
        }

        theMenu.open(player)
    }

    private fun buildGroupIcon(player: Player, group: CollectionGroup): com.willfp.eco.core.gui.slot.Slot {
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
