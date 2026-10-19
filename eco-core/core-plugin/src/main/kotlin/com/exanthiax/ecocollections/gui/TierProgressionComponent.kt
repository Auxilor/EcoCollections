package com.exanthiax.ecocollections.gui

import com.exanthiax.ecocollections.api.getCollectionCount
import com.exanthiax.ecocollections.api.getCollectionTier
import com.exanthiax.ecocollections.collections.Collection
import com.exanthiax.ecocollections.plugin
import com.willfp.eco.core.gui.component.LevelComponent
import com.willfp.eco.core.gui.component.LevelState
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.items.Items
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNumeral
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

class TierProgressionComponent(
    private val collection: Collection
) : LevelComponent() {

    override val pattern: List<String> = plugin.configYml.getStrings("gui.detail.progression-slots.pattern")

    override val maxLevel = collection.maxTier

    /** Page count with at least one slot per page, as before the eco level component. */
    val menuPages: Int
        get() = ceil(maxLevel.toDouble() / levelsPerPage.coerceAtLeast(1)).toInt().coerceAtLeast(1)

    /** Page of [tier] with at least one slot per page, as before the eco level component. */
    fun getMenuPageOf(tier: Int): Int =
        ceil(tier.toDouble() / levelsPerPage.coerceAtLeast(1)).toInt().coerceAtLeast(1)

    override fun getLevelState(player: Player, level: Int): LevelState =
        LevelState.of(level, player.getCollectionTier(collection))

    override fun getLevelItem(player: Player, menu: Menu, level: Int, levelState: LevelState): ItemStack {
        val playerCount = player.getCollectionCount(collection)
        val requirement = collection.tierRequirements[level - 1]

        val configKey = when (levelState) {
            LevelState.UNLOCKED -> if (level == collection.maxTier) {
                "gui.detail.progression-slots.completed"
            } else {
                "gui.detail.progression-slots.reached"
            }

            LevelState.IN_PROGRESS -> "gui.detail.progression-slots.in-progress"
            LevelState.LOCKED -> "gui.detail.progression-slots.locked"
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
                .replace("%tier%", level.toString())
                .replace("%tier_numeral%", level.toNumeral())
                .replace("%count%", playerCount.toLong().toString())
                .replace("%required%", requirement.toLong().toString())
                .replace("%percent%", percent)
                .replace("%collection_name%", collection.name)

            meta.setDisplayName(
                StringUtils.format(plugin.configYml.getString("$configKey.name").applyPlaceholders())
            )

            val rewardMessages = collection.getRewardMessages(level).map {
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

        return tierItem
    }
}
