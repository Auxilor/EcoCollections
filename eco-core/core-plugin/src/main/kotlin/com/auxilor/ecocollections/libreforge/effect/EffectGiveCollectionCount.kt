package com.auxilor.ecocollections.libreforge.effect

import com.auxilor.eco.core.config.interfaces.Config
import com.auxilor.ecocollections.api.giveCollectionCount
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.libreforge.NoCompileData
import com.auxilor.libreforge.arguments
import com.auxilor.libreforge.effects.Effect
import com.auxilor.libreforge.getDoubleFromExpression
import com.auxilor.libreforge.triggers.TriggerData
import com.auxilor.libreforge.triggers.TriggerParameter

object EffectGiveCollectionCount : Effect<NoCompileData>("give_collection_count") {
    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require("collection", "You must specify the collection!")
        require("amount", "You must specify the amount!")
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val collection = Collections.getByID(config.getString("collection").lowercase()) ?: return false
        val amount = config.getDoubleFromExpression("amount", data)
        player.giveCollectionCount(collection, amount)
        return true
    }
}
