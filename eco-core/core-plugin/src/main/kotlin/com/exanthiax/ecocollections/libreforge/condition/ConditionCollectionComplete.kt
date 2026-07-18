package com.exanthiax.ecocollections.libreforge.condition

import com.willfp.eco.core.config.interfaces.Config
import com.exanthiax.ecocollections.api.isCollectionComplete
import com.exanthiax.ecocollections.collections.Collections
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import org.bukkit.entity.Player

object ConditionCollectionComplete : Condition<NoCompileData>("collection_complete") {
    override val description = "Passes when the player has completed all tiers of the specified collection."

    override val categories = setOf("player")

    override val arguments = arguments {
        require("collection", "You must specify the collection!", Config::getString) {
            Collections[it.lowercase()] != null
        }
        describe(
            "collection",
            description = "The collection to check for completion.",
            type = ArgType.STRING
        )
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
