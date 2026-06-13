package com.example.chaisecrorepati.game

import android.os.Handler
import android.os.Looper
import com.example.chaisecrorepati.data.GameState
import kotlin.math.min
import kotlin.math.pow

class IdleIncomeController(private val gameState: GameState) {

    private val handler = Handler(Looper.getMainLooper())
    private var onTickListener: ((Double) -> Unit)? = null
    private var isRunning = false

    companion object {
        const val BASE_INCOME         = 1.0
        const val INCOME_PER_LEVEL    = 0.25
        const val OFFLINE_EFFICIENCY  = 0.5
        const val MAX_OFFLINE_SECONDS = 28800L  // 8 ghante
        const val TICK_INTERVAL_MS    = 1000L
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            val earned = calculateIncomePerSecond()
            gameState.rupees += earned
            gameState.totalEarned += earned
            onTickListener?.invoke(gameState.rupees)
            handler.postDelayed(this, TICK_INTERVAL_MS)
        }
    }

    fun start(onTick: (Double) -> Unit) {
        if (isRunning) return   // double start se bachao
        onTickListener = onTick
        isRunning = true
        handler.post(tickRunnable)
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(tickRunnable)
    }

    fun calculateIncomePerSecond(): Double {
        val totalLevels = gameState.upgrades.sumOf { it.level }
        val boostMultiplier = if (isBoosterActive()) 2.0 else 1.0
        return BASE_INCOME * (1.0 + INCOME_PER_LEVEL * totalLevels) * boostMultiplier
    }

    fun calculateOfflineEarnings(lastExitTime: Long): Double {
        val now = System.currentTimeMillis()
        if (lastExitTime <= 0) return 0.0
        val secondsAway = min((now - lastExitTime) / 1000, MAX_OFFLINE_SECONDS)
        if (secondsAway <= 5) return 0.0   // 5 sec se kam — ignore
        // booster offline mein count nahi hota
        val offlineIncome = BASE_INCOME *
                (1.0 + INCOME_PER_LEVEL * gameState.upgrades.sumOf { it.level })
        return offlineIncome * secondsAway * OFFLINE_EFFICIENCY
    }

    fun calculateUpgradeCost(upgradeId: String): Double {
        val upgrade = gameState.upgrades.find { it.id == upgradeId } ?: return 0.0
        return upgrade.baseCost * upgrade.growthFactor.pow(upgrade.level.toDouble())
    }

    fun isBoosterActive(): Boolean {
        return System.currentTimeMillis() < gameState.boosterEndTime
    }

    fun activateBooster(durationSeconds: Long) {
        gameState.boosterEndTime =
            System.currentTimeMillis() + (durationSeconds * 1000L)
    }

    fun getRemainingBoosterSeconds(): Long {
        if (!isBoosterActive()) return 0L
        return (gameState.boosterEndTime - System.currentTimeMillis()) / 1000L
    }
}