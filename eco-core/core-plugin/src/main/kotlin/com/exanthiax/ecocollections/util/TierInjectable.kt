package com.exanthiax.ecocollections.util

import wiki.ecohub.ui.placeholder.InjectablePlaceholder
import wiki.ecohub.ui.placeholder.PlaceholderInjectable
import wiki.ecohub.ui.placeholder.type.StaticPlaceholder

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
