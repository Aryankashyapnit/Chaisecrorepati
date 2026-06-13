package com.example.chaisecrorepati.game

import android.content.Context
import com.example.chaisecrorepati.data.GameState
import com.example.chaisecrorepati.data.SaveManager

class GameManager private constructor(context: Context) {

    val saveManager = SaveManager(context)
    var gameState: GameState = saveManager.loadGame()
    val incomeController = IdleIncomeController(gameState)

    companion object {
        @Volatile
        private var instance: GameManager? = null

        fun getInstance(context: Context): GameManager {
            return instance ?: synchronized(this) {
                instance ?: GameManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // app khulne pe
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

    // upgrade kharidna
    fun purchaseUpgrade(upgradeId: String): Boolean {
        val cost = incomeController.calculateUpgradeCost(upgradeId)
        if (gameState.rupees < cost) return false

        gameState.rupees -= cost

        // level badhao
        val upgrade = gameState.upgrades.find { it.id == upgradeId }
        upgrade?.level = (upgrade?.level ?: 0) + 1

        saveManager.saveGame(gameState)
        return true
    }

    // app band hone pe
    fun onAppStop() {
        incomeController.stop()
        saveManager.saveGame(gameState)
    }

    // total income per second (UI ke liye)
    fun getIncomePerSecond(): Double {
        return incomeController.calculateIncomePerSecond()
    }
}