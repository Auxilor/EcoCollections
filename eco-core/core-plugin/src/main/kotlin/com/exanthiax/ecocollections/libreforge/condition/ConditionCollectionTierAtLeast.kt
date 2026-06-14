package com.exanthiax.ecocollections.libreforge.condition

import com.willfp.eco.core.config.interfaces.Config
import com.exanthiax.ecocollections.api.getCollectionTier
import com.exanthiax.ecocollections.collections.Collections
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import org.bukkit.entity.Player

object ConditionCollectionTierAtLeast : Condition<NoCompileData>("collection_tier_at_least") {
    override val description = "Passes when the player's tier in the specified collection is at or above the given tier."

    override val categories = setOf("player")

    override val arguments = arguments {
        require("collection", "You must specify the collection!", Config::getString) {
            Collections[it.lowercase()] != null
        }
        describe(
            "collection",
            description = "The collection to check the player's tier in.",
            type = ArgType.STRING
        )
        require(
            "tier",
            "You must specify the tier!",
            description = "The minimum tier the player must have reached in the collection. Supports expressions.",
            type = ArgType.EXPRESSION
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
        return player.getCollectionTier(collection) >= config.getIntFromExpression("tier", player)
    }
}
