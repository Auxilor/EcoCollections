package com.exanthiax.ecocollections.libreforge.effect

import com.exanthiax.ecocollections.api.forceUnlockCollection
import com.exanthiax.ecocollections.collections.Collections
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.arguments
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.game.entity.player.profile
import wiki.ecohub.rules.effect.Effect
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object EffectUnlockCollection : Effect<NoCompileData>("unlock_collection") {
    override val description = "Unlocks the specified collection for the player if it is not already unlocked."

    override val categories = setOf("player")

    override val additionalInfo = listOf(
        "Has no effect if the player has already unlocked the collection.",
        "Has no effect if the unlock event is cancelled."
    )

    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require("collection", "You must specify the collection!", Config::getString) {
            Collections[it.lowercase()] != null
        }
        describe(
            "collection",
            description = "The collection to unlock for the player.",
            type = ArgType.STRING
        )
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false
        val collection = Collections.getByID(config.getString("collection").lowercase()) ?: return false
        if (player.profile.read(collection.unlockedKey)) return false
        return player.forceUnlockCollection(collection)
    }
}
