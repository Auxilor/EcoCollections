package com.exanthiax.ecocollections.libreforge.effect

import com.exanthiax.ecocollections.api.giveCollectionCount
import com.exanthiax.ecocollections.collections.Collections
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.arguments
import wiki.ecohub.config.section.getDoubleFromExpression
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.effect.Effect
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object EffectGiveCollectionCount : Effect<NoCompileData>("give_collection_count") {
    override val description = "Gives the player progress towards the specified collection."

    override val categories = setOf("player")

    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require("collection", "You must specify the collection!", Config::getString) {
            Collections[it.lowercase()] != null
        }
        describe(
            "collection",
            description = "The collection to give progress towards.",
            type = ArgType.STRING
        )
        require(
            "amount",
            "You must specify the amount!",
            description = "The amount of progress to give towards the collection. Supports expressions.",
            type = ArgType.EXPRESSION,
            example = "%level% * 10"
        )
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val collection = Collections.getByID(config.getString("collection").lowercase()) ?: return false
        val amount = config.getDoubleFromExpression("amount", data)
        player.giveCollectionCount(collection, amount)
        return true
    }
}
