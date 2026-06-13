package com.example.chaisecrorepati.game

import android.content.Context
import com.example.chaisecrorepati.ads.AdManager
import com.example.chaisecrorepati.data.GameState
import com.example.chaisecrorepati.data.SaveManager

class GameManager private constructor(context: Context) {

    val saveManager      = SaveManager(context)
    var gameState        : GameState = saveManager.loadGame()
    val incomeController = IdleIncomeController(gameState)
    val adManager        = AdManager(context)

    companion object {
        @Volatile private var instance: GameManager? = null

        fun getInstance(context: Context): GameManager {
            return instance ?: synchronized(this) {
                instance ?: GameManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    fun onAppStart(onTick: (Double) -> Unit): Double {
        val lastExit = saveManager.getExitTime()
        val offlineEarnings = incomeController.calculateOfflineEarnings(lastExit)
        if (offlineEarnings > 0) {
            gameState.rupees += offlineEarnings
            gameState.totalEarned += offlineEarnings
        }
        incomeController.start(onTick)
        return offlineEarnings
    }

    fun purchaseUpgrade(upgradeId: String): Boolean {
        val cost = incomeController.calculateUpgradeCost(upgradeId)
        if (gameState.rupees < cost) return false
        gameState.rupees -= cost
        gameState.upgrades.find { it.id == upgradeId }?.let {
            it.level += 1
        }
        saveManager.saveGame(gameState)
        return true
    }

    fun onAppStop() {
        incomeController.stop()
        saveManager.saveGame(gameState)
    }
}