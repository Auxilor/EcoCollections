package com.exanthiax.ecocollections.libreforge.condition

import com.exanthiax.ecocollections.api.getCollectionTier
import com.exanthiax.ecocollections.collections.Collections
import org.bukkit.entity.Player
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.arguments
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.condition.Condition
import wiki.ecohub.rules.holder.Dispatcher
import wiki.ecohub.rules.holder.ProvidedHolder
import wiki.ecohub.rules.holder.get

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
            type = ArgType.EXPRESSION,
            example = "5 + %level%"
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
