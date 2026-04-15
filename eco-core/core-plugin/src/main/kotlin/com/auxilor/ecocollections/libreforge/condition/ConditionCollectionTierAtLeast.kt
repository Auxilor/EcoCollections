package com.auxilor.ecocollections.libreforge.condition

import com.willfp.eco.core.config.interfaces.Config
import com.auxilor.ecocollections.api.getCollectionTier
import com.auxilor.ecocollections.collections.Collections
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import org.bukkit.entity.Player

object ConditionCollectionTierAtLeast : Condition<NoCompileData>("collection_tier_at_least") {
    override val arguments = arguments {
        require("collection", "You must specify the collection!")
        require("tier", "You must specify the tier!")
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false
        val collection = Collections.getByID(config.getString("collection").lowercase()) ?: return false
        return player.getCollectionTier(collection) >= config.getIntFromExpression("tier", player)
    }
}
