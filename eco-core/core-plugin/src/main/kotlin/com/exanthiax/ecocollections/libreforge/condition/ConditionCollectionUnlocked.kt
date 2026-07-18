package com.exanthiax.ecocollections.libreforge.condition

import com.willfp.eco.core.config.interfaces.Config
import com.exanthiax.ecocollections.api.isCollectionUnlocked
import com.exanthiax.ecocollections.collections.Collections
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import org.bukkit.entity.Player

object ConditionCollectionUnlocked : Condition<NoCompileData>("collection_unlocked") {
    override val description = "Passes when the player has unlocked the specified collection."

    override val categories = setOf("player")

    override val arguments = arguments {
        require("collection", "You must specify the collection!", Config::getString) {
            Collections[it.lowercase()] != null
        }
        describe(
            "collection",
            description = "The collection to check the unlock status of.",
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
        return player.isCollectionUnlocked(collection)
    }
}
