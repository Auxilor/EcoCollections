package com.auxilor.ecocollections.groups

import com.auxilor.eco.core.config.interfaces.Config
import com.auxilor.eco.core.items.Items
import com.auxilor.eco.core.items.TestableItem
import com.auxilor.eco.core.registry.KRegistrable

class CollectionGroup(
    override val id: String,
    val config: Config
) : KRegistrable {

    val name: String = config.getFormattedString("name")

    val permission: String = config.getString("permission")

    val icon: TestableItem = Items.lookup(config.getString("gui.icon"))

    val guiRow: Int = config.getInt("gui.position.row")

    val guiColumn: Int = config.getInt("gui.position.column")

    val guiLore: List<String> = config.getStrings("gui.lore")
}
