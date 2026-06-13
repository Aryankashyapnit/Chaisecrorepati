package com.example.chaisecrorepati.game

import android.os.Handler
import android.os.Looper
import com.example.chaisecrorepati.data.GameState

class IdleIncomeController(private val gameState: GameState) {

    private val handler = Handler(Looper.getMainLooper())
    private var onTickListener: ((Double) -> Unit)? = null
    private var isRunning = false

    companion object {
        const val BASE_INCOME = 1.0          // ₹1 per second base
        const val INCOME_PER_LEVEL = 0.25    // har level +25%
        const val OFFLINE_EFFICIENCY = 0.5   // offline 50% income
        const val MAX_OFFLINE_SECONDS = 28800L // 8 ghante max
    }

    // har second ye runnable chalta hai
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val earned = calculateIncomePerSecond()
                gameState.rupees += earned
                gameState.totalEarned += earned
                onTickListener?.invoke(gameState.rupees)
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun start(onTick: (Double) -> Unit) {
        onTickListener = onTick
        isRunning = true
        handler.post(tickRunnable)
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(tickRunnable)
    }

    fun calculateIncomePerSecond(): Double {
        val totalUpgradeLevels = gameState.upgrades.sumOf { it.level }
        val boostMultiplier = if (isBoosterActive()) 2.0 else 1.0
        return BASE_INCOME * (1 + INCOME_PER_LEVEL * totalUpgradeLevels) * boostMultiplier
    }

    fun calculateOfflineEarnings(lastExitTime: Long): Double {
        val now = System.currentTimeMillis()
        val secondsAway = ((now - lastExitTime) / 1000).coerceAtMost(MAX_OFFLINE_SECONDS)
        return calculateIncomePerSecond() * secondsAway * OFFLINE_EFFICIENCY
    }

    fun calculateUpgradeCost(upgradeId: String): Double {
        val upgrade = gameState.upgrades.find { it.id == upgradeId } ?: return 0.0
        return upgrade.baseCost * Math.pow(upgrade.growthFactor, upgrade.level.toDouble())
    }

    fun isBoosterActive(): Boolean {
        return System.currentTimeMillis() < gameState.boosterEndTime
    }

    fun activateBooster(durationSeconds: Long) {
        gameState.boosterEndTime = System.currentTimeMillis() + (durationSeconds * 1000)
    }
}

