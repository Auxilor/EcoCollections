package com.exanthiax.ecocollections.groups

import wiki.ecohub.config.Config
import wiki.ecohub.core.registry.KRegistrable
import wiki.ecohub.game.item.Items
import wiki.ecohub.game.item.lookup.TestableItem

class CollectionGroup(
    override val id: String,
    val config: Config
) : KRegistrable {

    val name: String = config.getFormattedString("name")

    val permission: String = config.getString("permission")

    val icon: TestableItem = Items.lookup(config.getString("gui.icon"))

    val guiRow: Int = config.getInt("gui.position.row")

    val guiColumn: Int = config.getInt("gui.position.column")

    val guiPage: Int = config.getInt("gui.position.page").coerceAtLeast(1)

    val guiLore: List<String> = config.getStrings("gui.lore")
}
