package com.auxilor.ecocollections.util

import com.willfp.eco.core.placeholder.InjectablePlaceholder
import com.willfp.eco.core.placeholder.PlaceholderInjectable
import com.willfp.eco.core.placeholder.StaticPlaceholder

class TierInjectable(
    tier: Int
) : PlaceholderInjectable {
    private val placeholders = listOf(
        StaticPlaceholder("tier") { tier.toString() }
    )

    override fun getPlaceholderInjections(): List<InjectablePlaceholder> = placeholders
    override fun addInjectablePlaceholder(p0: Iterable<InjectablePlaceholder>) = Unit
    override fun clearInjectedPlaceholders() = Unit
}
