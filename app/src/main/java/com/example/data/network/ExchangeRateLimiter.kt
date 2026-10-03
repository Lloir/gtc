package com.example.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Galactic Tycoons Public Exchange API Rate Limiter
 *
 * Enforces official policy:
 * - 100 cost units per 5 minutes (300 seconds) per IP address
 *
 * Endpoint Costs:
 * - GET /public/exchange/mat-prices/{materialId}  : 2 units
 * - GET /public/exchange/mat-prices               : 5 units
 * - GET /public/exchange/mat-details/{materialId} : 5 units
 * - GET /public/exchange/mat-details              : 60 units
 * - GET /public/exchange/mat-history/{materialId} : 5 units
 */
object ExchangeRateLimiter {
    const val MAX_BUDGET = 100
    const val WINDOW_MILLIS = 5 * 60 * 1000L // 5 minutes

    const val COST_MAT_PRICES_ALL = 5
    const val COST_MAT_PRICE_SINGLE = 2
    const val COST_MAT_DETAILS_SINGLE = 5
    const val COST_MAT_DETAILS_ALL = 60
    const val COST_MAT_HISTORY = 5

    private data class CostEntry(val timestamp: Long, val cost: Int)
    private val history = ConcurrentLinkedQueue<CostEntry>()

    private val _remainingUnits = MutableStateFlow(MAX_BUDGET)
    val remainingUnits: StateFlow<Int> = _remainingUnits.asStateFlow()

    private val _secondsUntilReset = MutableStateFlow(0L)
    val secondsUntilReset: StateFlow<Long> = _secondsUntilReset.asStateFlow()

    @Synchronized
    private fun pruneOldEntries(now: Long) {
        val cutoff = now - WINDOW_MILLIS
        while (true) {
            val head = history.peek() ?: break
            if (head.timestamp < cutoff) {
                history.poll()
            } else {
                break
            }
        }
        val spent = history.sumOf { it.cost }
        val remaining = (MAX_BUDGET - spent).coerceAtLeast(0)
        _remainingUnits.value = remaining

        val oldest = history.peek()
        _secondsUntilReset.value = if (oldest != null) {
            val elapsed = now - oldest.timestamp
            ((WINDOW_MILLIS - elapsed) / 1000L).coerceAtLeast(0L)
        } else 0L
    }

    @Synchronized
    fun canSpend(cost: Int): Boolean {
        val now = System.currentTimeMillis()
        pruneOldEntries(now)
        return _remainingUnits.value >= cost
    }

    @Synchronized
    fun trySpend(cost: Int): Boolean {
        val now = System.currentTimeMillis()
        pruneOldEntries(now)
        if (_remainingUnits.value >= cost) {
            history.add(CostEntry(now, cost))
            pruneOldEntries(now)
            return true
        }
        return false
    }

    @Synchronized
    fun forceRecord(cost: Int) {
        val now = System.currentTimeMillis()
        history.add(CostEntry(now, cost))
        pruneOldEntries(now)
    }

    @Synchronized
    fun getWaitTimeSeconds(costNeeded: Int): Long {
        val now = System.currentTimeMillis()
        pruneOldEntries(now)
        if (_remainingUnits.value >= costNeeded) return 0L

        // Find how many entries need to expire to free up costNeeded
        var freed = _remainingUnits.value
        for (entry in history) {
            freed += entry.cost
            if (freed >= costNeeded) {
                val timeToExpiry = (entry.timestamp + WINDOW_MILLIS) - now
                return (timeToExpiry / 1000L).coerceAtLeast(1L)
            }
        }
        return 60L
    }
}
