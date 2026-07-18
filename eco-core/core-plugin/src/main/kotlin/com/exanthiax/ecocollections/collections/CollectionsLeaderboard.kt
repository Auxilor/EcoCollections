package com.exanthiax.ecocollections.collections

import com.willfp.eco.core.cache.EcoCache
import com.willfp.eco.core.data.profile
import com.exanthiax.ecocollections.api.totalCollectionTiers
import com.exanthiax.ecocollections.plugin
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

sealed class CollectionRank {
    data class Exact(val rank: Int) : CollectionRank()
    data class Percent(val topPercent: Double) : CollectionRank()
    data object Unranked : CollectionRank()
}

data class LeaderboardEntry(val player: OfflinePlayer, val value: Number)

internal data class LeaderboardSnapshot(
    val sortedEntries: List<Pair<UUID, Double>>,
    val rankMap: Map<UUID, Int>,
    val totalTrackedPlayers: Int
)

object CollectionsLeaderboard {

    private val perCollectionCaches = ConcurrentHashMap<String, EcoCache<Boolean, LeaderboardSnapshot>>()

    private val totalsCache: EcoCache<Boolean, LeaderboardSnapshot> by lazy {
        buildCache()
    }

    private fun getRefreshDuration(): Duration {
        val intervalStr = plugin.configYml.getString("leaderboards.refresh-interval")
        return parseDuration(intervalStr)
    }

    private fun parseDuration(str: String): Duration {
        val trimmed = str.trim().lowercase()
        return when {
            trimmed.endsWith("h") -> Duration.ofHours(trimmed.removeSuffix("h").trim().toLongOrNull() ?: 6)
            trimmed.endsWith("m") -> Duration.ofMinutes(trimmed.removeSuffix("m").trim().toLongOrNull() ?: 360)
            trimmed.endsWith("s") -> Duration.ofSeconds(trimmed.removeSuffix("s").trim().toLongOrNull() ?: 21600)
            else -> Duration.ofSeconds(trimmed.toLongOrNull() ?: 21600)
        }
    }

    private fun buildCache(): EcoCache<Boolean, LeaderboardSnapshot> {
        return EcoCache.builder<Boolean, LeaderboardSnapshot>()
            .expireAfterWrite(getRefreshDuration())
            .build()
    }

    private fun getOrCreateCollectionCache(collection: Collection): EcoCache<Boolean, LeaderboardSnapshot> {
        return perCollectionCaches.computeIfAbsent(collection.id) {
            buildCache()
        }
    }

    private fun buildCollectionSnapshot(collection: Collection): LeaderboardSnapshot {
        val entries = mutableListOf<Pair<UUID, Double>>()
        for (offlinePlayer in Bukkit.getOfflinePlayers()) {
            val count = offlinePlayer.profile.read(collection.countKey)
            if (count > 0.0) {
                entries.add(offlinePlayer.uniqueId to count)
            }
        }
        entries.sortByDescending { it.second }

        val rankMap = mutableMapOf<UUID, Int>()
        for ((index, entry) in entries.withIndex()) {
            rankMap[entry.first] = index + 1
        }

        return LeaderboardSnapshot(
            sortedEntries = entries,
            rankMap = rankMap,
            totalTrackedPlayers = entries.size
        )
    }

    private fun buildTotalsSnapshot(): LeaderboardSnapshot {
        val entries = mutableListOf<Pair<UUID, Double>>()
        for (offlinePlayer in Bukkit.getOfflinePlayers()) {
            val totalTiers = offlinePlayer.totalCollectionTiers
            if (totalTiers > 0) {
                entries.add(offlinePlayer.uniqueId to totalTiers.toDouble())
            }
        }
        entries.sortByDescending { it.second }

        val rankMap = mutableMapOf<UUID, Int>()
        for ((index, entry) in entries.withIndex()) {
            rankMap[entry.first] = index + 1
        }

        return LeaderboardSnapshot(
            sortedEntries = entries,
            rankMap = rankMap,
            totalTrackedPlayers = entries.size
        )
    }

    fun OfflinePlayer.getCollectionRank(collection: Collection): CollectionRank {
        val snapshot = try {
            getOrCreateCollectionCache(collection).get(true) { buildCollectionSnapshot(collection) }
        } catch (e: Exception) {
            return CollectionRank.Unranked
        }

        val rank = snapshot.rankMap[this.uniqueId] ?: return CollectionRank.Unranked

        val cutoff = plugin.configYml.getInt("leaderboards.exact-rank-cutoff")

        if (cutoff == 0 || rank <= cutoff) {
            return CollectionRank.Exact(rank)
        }

        if (snapshot.totalTrackedPlayers == 0) {
            return CollectionRank.Unranked
        }

        val decimalPlaces = plugin.configYml.getInt("leaderboards.percent-decimal-places")
        val percent = (rank.toDouble() / snapshot.totalTrackedPlayers.toDouble()) * 100.0
        val factor = Math.pow(10.0, decimalPlaces.toDouble())
        val roundedPercent = Math.round(percent * factor) / factor

        return CollectionRank.Percent(roundedPercent)
    }

    fun Collection.getTopPlayers(n: Int): List<LeaderboardEntry> {
        val clampedN = n.coerceIn(1, 10)
        val snapshot = try {
            getOrCreateCollectionCache(this).get(true) { buildCollectionSnapshot(this@getTopPlayers) }
        } catch (e: Exception) {
            return emptyList()
        }

        return snapshot.sortedEntries.take(clampedN).map { (uuid, count) ->
            LeaderboardEntry(
                player = Bukkit.getOfflinePlayer(uuid),
                value = count
            )
        }
    }

    fun getTopByTotal(position: Int): LeaderboardEntry? {
        val snapshot = try {
            totalsCache.get(true) { buildTotalsSnapshot() }
        } catch (e: Exception) {
            return null
        }

        val index = position - 1
        if (index < 0 || index >= snapshot.sortedEntries.size) return null

        val (uuid, value) = snapshot.sortedEntries[index]
        return LeaderboardEntry(
            player = Bukkit.getOfflinePlayer(uuid),
            value = value.toInt()
        )
    }

    fun getPositionByTotal(uuid: UUID): Int? {
        val snapshot = try {
            totalsCache.get(true) { buildTotalsSnapshot() }
        } catch (e: Exception) {
            return null
        }

        return snapshot.rankMap[uuid]
    }

    fun getTop(collection: Collection, position: Int): LeaderboardEntry? {
        val snapshot = try {
            getOrCreateCollectionCache(collection).get(true) { buildCollectionSnapshot(collection) }
        } catch (e: Exception) {
            return null
        }

        val index = position - 1
        if (index < 0 || index >= snapshot.sortedEntries.size) return null

        val (uuid, value) = snapshot.sortedEntries[index]
        return LeaderboardEntry(
            player = Bukkit.getOfflinePlayer(uuid),
            value = value
        )
    }

    fun getPosition(collection: Collection, uuid: UUID): Int? {
        val snapshot = try {
            getOrCreateCollectionCache(collection).get(true) { buildCollectionSnapshot(collection) }
        } catch (e: Exception) {
            return null
        }

        return snapshot.rankMap[uuid]
    }

    fun invalidateAll() {
        perCollectionCaches.values.forEach { it.invalidateAll() }
        perCollectionCaches.clear()
        totalsCache.invalidateAll()
    }

    fun invalidate(collection: Collection) {
        perCollectionCaches[collection.id]?.invalidateAll()
        perCollectionCaches.remove(collection.id)
        totalsCache.invalidateAll()
    }
}
