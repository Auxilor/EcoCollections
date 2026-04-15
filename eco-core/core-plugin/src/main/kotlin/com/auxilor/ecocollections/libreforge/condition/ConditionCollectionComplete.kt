package com.auxilor.ecocollections.libreforge.condition

import com.auxilor.eco.core.config.interfaces.Config
import com.auxilor.ecocollections.api.isCollectionComplete
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.libreforge.Dispatcher
import com.auxilor.libreforge.NoCompileData
import com.auxilor.libreforge.ProvidedHolder
import com.auxilor.libreforge.arguments
import com.auxilor.libreforge.conditions.Condition
import com.auxilor.libreforge.get
import org.bukkit.entity.Player

object ConditionCollectionComplete : Condition<NoCompileData>("collection_complete") {
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
        return player.isCollectionComplete(collection)
    }
}
