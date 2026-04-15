package com.auxilor.ecocollections.libreforge.effect

import com.auxilor.eco.core.config.interfaces.Config
import com.auxilor.eco.core.data.profile
import com.auxilor.ecocollections.api.event.PlayerCollectionUnlockEvent
import com.auxilor.ecocollections.collections.Collections
import com.auxilor.libreforge.NoCompileData
import com.auxilor.libreforge.arguments
import com.auxilor.libreforge.effects.Effect
import com.auxilor.libreforge.triggers.TriggerData
import com.auxilor.libreforge.triggers.TriggerParameter
import org.bukkit.Bukkit

object EffectUnlockCollection : Effect<NoCompileData>("unlock_collection") {
    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require("collection", "You must specify the collection!")
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
