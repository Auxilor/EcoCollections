package com.exanthiax.ecocollections.libreforge.effect

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.profile
import com.exanthiax.ecocollections.api.event.PlayerCollectionUnlockEvent
import com.exanthiax.ecocollections.collections.Collections
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.Bukkit

object EffectUnlockCollection : Effect<NoCompileData>("unlock_collection") {
    override val description = "Unlocks the specified collection for the player if it is not already unlocked."

    override val categories = setOf("player")

    override val additionalInfo = listOf(
        "Has no effect if the player has already unlocked the collection."
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
        Bukkit.getPluginManager().callEvent(PlayerCollectionUnlockEvent(player, collection))
        player.profile.write(collection.unlockedKey, true)
        return true
    }
}
