package com.exanthiax.ecocollections.collections

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.TestableItem
import com.willfp.eco.core.leaderboard.Leaderboard
import com.willfp.eco.core.leaderboard.Leaderboards
import com.willfp.eco.core.leaderboard.registerStandardPlaceholders
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.exanthiax.ecocollections.api.completedCollectionCount
import com.exanthiax.ecocollections.api.getCollectionCount
import com.exanthiax.ecocollections.api.getCollectionTier
import com.exanthiax.ecocollections.api.isCollectionComplete
import com.exanthiax.ecocollections.api.isCollectionUnlocked
import com.exanthiax.ecocollections.api.totalCollectionTiers
import com.exanthiax.ecocollections.api.unlockedCollectionCount
import com.exanthiax.ecocollections.groups.CollectionGroup
import com.exanthiax.ecocollections.groups.CollectionGroups
import com.exanthiax.ecocollections.plugin
import com.exanthiax.ecocollections.util.InvalidConfigurationException
import com.exanthiax.ecocollections.util.TierInjectable
import com.willfp.eco.core.placeholder.PlayerStaticPlaceholder
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.core.registry.KRegistrable
import com.willfp.eco.util.evaluateExpression
import com.willfp.eco.util.toNumeral
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.ConditionList
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.counters.Counter
import com.willfp.libreforge.counters.Counters
import com.willfp.libreforge.effects.Chain
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.effects.executors.impl.NormalExecutorFactory
import com.willfp.eco.util.formatEco

class Collection(
    override val id: String,
    val config: Config
) : KRegistrable {

    val name: String = config.getFormattedString("name")

    val groupId: String = config.getString("group")

    val group: CollectionGroup? by lazy {
        CollectionGroups.getByID(groupId)
    }

    val tierRequirements: List<Double> = run {
        val formula = config.getStringOrNull("tier-formula")
        if (formula != null) {
            val max = config.getIntOrNull("max-tier")
                ?: throw InvalidConfigurationException("Collection $id has tier-formula but no max-tier")
            (1..max).map { tier ->
                evaluateExpression(formula, placeholderContext(injectable = TierInjectable(tier)))
            }
        } else {
            config.getDoubles("tier-requirements")
        }
    }

    val maxTier: Int = tierRequirements.size

    val manualCollectModeEnabled: Boolean = plugin.configYml.getBool("collections.manual-collect-mode.enabled")

    val countMethods: List<Counter> =
        if (manualCollectModeEnabled) {
            emptyList()
        } else {
            config.getSubsections("count-methods").mapNotNull {
                Counters.compile(
                    it,
                    ViolationContext(plugin, "Collection $id count-methods")
                )
            }
        }

    val unlockConditions: ConditionList = Conditions.compile(
        config.getSubsections("unlock-conditions"),
        ViolationContext(plugin, "Collection $id unlock-conditions")
    )

    val hasUnlockConditions: Boolean = config.getSubsections("unlock-conditions").isNotEmpty()

    val conditions: ConditionList = Conditions.compile(
        config.getSubsections("conditions"),
        ViolationContext(plugin, "Collection $id conditions")
    )

    val hasConditions: Boolean = config.getSubsections("conditions").isNotEmpty()

    val hideWhenLocked: Boolean = config.getBool("hide-when-locked")

    val hideBeforeTier1: Boolean = config.getBool("hide-before-tier-1")

    val descriptionLines: List<String> = config.getStrings("description")

    val icon: TestableItem = Items.lookup(config.getString("gui.icon"))

    val guiRow: Int = config.getInt("gui.position.row")

    val guiColumn: Int = config.getInt("gui.position.column")

    val guiPage: Int = config.getInt("gui.position.page").coerceAtLeast(1)

    val guiLore: List<String> = config.getStrings("gui.lore")

    val manualCollectItems: List<TestableItem> = config.getStrings("manual-collect.items")
        .map { Items.lookup(it) }

    val tierRewards: Map<Int, Chain?>
    val allTierRewards: Chain?

    val completionRewardEffects: Chain? = Effects.compileChain(
        config.getSubsections("completion-effects"),
        NormalExecutorFactory.create(),
        ViolationContext(plugin, "Collection $id completion-effects")
    )

    val countKey = PersistentDataKey(
        plugin.createNamespacedKey("${id}_count"),
        PersistentDataKeyType.DOUBLE,
        0.0
    )

    val tierKey = PersistentDataKey(
        plugin.createNamespacedKey("${id}_tier"),
        PersistentDataKeyType.INT,
        0
    )

    val doneKey = PersistentDataKey(
        plugin.createNamespacedKey("${id}_completed"),
        PersistentDataKeyType.BOOLEAN,
        false
    )

    val unlockedKey = PersistentDataKey(
        plugin.createNamespacedKey("${id}_unlocked"),
        PersistentDataKeyType.BOOLEAN,
        false
    )

    /**
     * The leaderboard ranking players by this collection's count, or null before the plugin has
     * registered its leaderboards.
     */
    var leaderboard: Leaderboard? = null
        private set

    init {
        registerPlaceholders()

        val tierRewardsMutable = mutableMapOf<Int, Chain?>()
        var parsedAllTierRewards: Chain? = null
        for (subsection in config.getSubsections("tier-up-effects")) {
            val tierValue = subsection.getString("tier")
            if (tierValue.equals("all", ignoreCase = true)) {
                parsedAllTierRewards = Effects.compileChain(
                    subsection.getSubsections("effects"),
                    NormalExecutorFactory.create(),
                    ViolationContext(plugin, "Collection $id tier-up-effects (all)")
                )
            } else {
                val tier = tierValue.toIntOrNull()
                if (tier != null) {
                    tierRewardsMutable[tier] = Effects.compileChain(
                        subsection.getSubsections("effects"),
                        NormalExecutorFactory.create(),
                        ViolationContext(plugin, "Collection $id tier-up-effects (tier $tier)")
                    )
                }
            }
        }
        tierRewards = tierRewardsMutable.toMap()
        allTierRewards = parsedAllTierRewards

        checkDupeFilters()
    }

    override fun onRegister() {
        if (manualCollectModeEnabled) return

        val accumulator = CollectionCountAccumulator(this)
        for (counter in countMethods) {
            counter.bind(accumulator)
        }
    }

    override fun onRemove() {
        for (counter in countMethods) {
            counter.unbind()
        }
    }

    /**
     * Register this collection's leaderboard and its placeholders.
     *
     * Called from [com.exanthiax.ecocollections.EcoCollectionsPlugin.handleReload] *after*
     * [Leaderboards.unregisterAll], because eco reloads config categories (recreating every
     * Collection) before it calls handleReload.
     */
    internal fun registerLeaderboard() {
        // Nothing at all is registered when disabled -- no leaderboard, and no placeholders. A
        // leaderboard that ranks nobody would still occupy a slot in every refresh sweep.
        if (!plugin.configYml.getBool("leaderboards.enabled")) {
            leaderboard = null
            return
        }

        // Players with no progress have always been excluded from collection leaderboards, which
        // also keeps them out of the percentile denominator. That is now the rule everywhere:
        // ofKey ranks a player only if their value is strictly above the key's default, and
        // countKey defaults to 0.0, so this is exactly the filter it replaces.
        val leaderboard = Leaderboards.ofKey(plugin, id, countKey)

        this.leaderboard = leaderboard

        leaderboard.registerStandardPlaceholders(
            plugin,
            "${id}_leaderboard",
            plugin.langYml.getString("top.empty-position").formatEco()
        ) { value ->
            value.toLong().toString()
        }
    }

    fun getRewardMessages(tier: Int): List<String> {
        if (!config.has("reward-messages")) return emptyList()

        val messages = mutableListOf<String>()

        if (config.has("reward-messages.all")) {
            messages.addAll(config.getStrings("reward-messages.all"))
        }

        if (config.has("reward-messages.$tier")) {
            messages.addAll(config.getStrings("reward-messages.$tier"))
        }

        return messages
    }

    fun getTierForCount(count: Double): Int {
        for (i in tierRequirements.indices.reversed()) {
            if (count >= tierRequirements[i]) {
                return i + 1
            }
        }
        return 0
    }

    private fun registerPlaceholders() {
        config.injectPlaceholders(
            PlayerStaticPlaceholder("tier") { player ->
                player.getCollectionTier(this).toString()
            },
            PlayerStaticPlaceholder("tier_numeral") { player ->
                player.getCollectionTier(this).toNumeral()
            }
        )

        PlayerPlaceholder(plugin, id) { player ->
            player.getCollectionTier(this).toString()
        }.register()

        PlayerPlaceholder(plugin, "${id}_numeral") { player ->
            player.getCollectionTier(this).toNumeral()
        }.register()

        PlayerPlaceholder(plugin, "${id}_count") { player ->
            player.getCollectionCount(this).toLong().toString()
        }.register()

        PlayerPlaceholder(plugin, "${id}_required") { player ->
            val tier = player.getCollectionTier(this)
            if (tier >= maxTier) {
                plugin.langYml.getString("placeholders.max-tier")
            } else {
                tierRequirements[tier].toLong().toString()
            }
        }.register()

        PlayerPlaceholder(plugin, "${id}_percent") { player ->
            val count = player.getCollectionCount(this)
            val tier = player.getCollectionTier(this)
            if (tier >= maxTier) {
                "100"
            } else {
                val required = tierRequirements[tier]
                val previousRequired = if (tier > 0) tierRequirements[tier - 1] else 0.0
                ((count - previousRequired) / (required - previousRequired) * 100)
                    .coerceIn(0.0, 100.0)
                    .toInt()
                    .toString()
            }
        }.register()

        PlayerPlaceholder(plugin, "${id}_max_tier") { _ ->
            maxTier.toString()
        }.register()

        PlayerPlaceholder(plugin, "${id}_max_tier_numeral") { _ ->
            maxTier.toNumeral()
        }.register()

        PlayerPlaceholder(plugin, "${id}_completed") { player ->
            if (player.isCollectionComplete(this)) {
                plugin.langYml.getString("placeholders.completed-true")
            } else {
                plugin.langYml.getString("placeholders.completed-false")
            }
        }.register()

        PlayerPlaceholder(plugin, "${id}_unlocked") { player ->
            if (player.isCollectionUnlocked(this)) {
                plugin.langYml.getString("placeholders.unlocked-true")
            } else {
                plugin.langYml.getString("placeholders.unlocked-false")
            }
        }.register()

        PlayerPlaceholder(plugin, "${id}_name") { _ ->
            name
        }.register()

        PlayerPlaceholder(plugin, "total_tiers") { player ->
            player.totalCollectionTiers.toString()
        }.register()

        PlayerPlaceholder(plugin, "completed_count") { player ->
            player.completedCollectionCount.toString()
        }.register()

        PlayerPlaceholder(plugin, "unlocked_count") { player ->
            player.unlockedCollectionCount.toString()
        }.register()
    }

    private fun checkDupeFilters() {
        if (!plugin.configYml.getBool("collections.warn-on-missing-dupe-filter")) return
        if (manualCollectModeEnabled) return

        for (subsection in config.getSubsections("count-methods")) {
            val trigger = subsection.getString("trigger").lowercase()
            if (trigger == "mine_block" || trigger == "mine_block_cascade" || trigger == "break_block") {
                val hasPlayerPlacedFilter = subsection.has("filters.player_placed")
                if (!hasPlayerPlacedFilter) {
                    plugin.logger.warning(
                        "Collection '$id' uses trigger '$trigger' without a 'player_placed' filter. " +
                            "This may allow duplication exploits. " +
                            "Add 'player_placed: false' to the filters section."
                    )
                }
            }
        }
    }
}
