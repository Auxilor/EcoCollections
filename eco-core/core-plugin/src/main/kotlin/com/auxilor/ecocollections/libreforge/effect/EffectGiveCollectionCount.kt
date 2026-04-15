package com.auxilor.ecocollections.libreforge.effect

import com.willfp.eco.core.config.interfaces.Config
import com.auxilor.ecocollections.api.giveCollectionCount
import com.auxilor.ecocollections.collections.Collections
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.getDoubleFromExpression
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter

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
