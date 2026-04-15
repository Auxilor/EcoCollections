package com.auxilor.ecocollections.libreforge.condition

import com.willfp.eco.core.config.interfaces.Config
import com.auxilor.ecocollections.api.isCollectionUnlocked
import com.auxilor.ecocollections.collections.Collections
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import org.bukkit.entity.Player

object ConditionCollectionUnlocked : Condition<NoCompileData>("collection_unlocked") {
    override val arguments = arguments {
        require("collection", "You must specify the collection!")
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false
        val collection = Collections.getByID(config.getString("collection").lowercase()) ?: return false
        return player.isCollectionUnlocked(collection)
    }
}
