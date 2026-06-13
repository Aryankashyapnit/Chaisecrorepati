package com.example.chaisecrorepati.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class SaveManager(context: Context) {

    private val prefs = context.getSharedPreferences("chai_game_v1", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder().serializeNulls().create()

    fun saveGame(state: GameState) {
        val json = gson.toJson(state)
        prefs.edit()
            .putString("game_state", json)
            .putLong("last_exit", System.currentTimeMillis())
            .apply()
    }

    fun loadGame(): GameState {
        val json = prefs.getString("game_state", null) ?: return GameState()
        return try {
            val loaded = gson.fromJson(json, GameState::class.java)
            // Agar upgrades null aa jaye toh default do
            if (loaded.upgrades.isNullOrEmpty()) {
                loaded.copy(upgrades = defaultUpgrades())
            } else loaded
        } catch (e: Exception) {
            GameState() // corrupt save — fresh start
        }
    }

    fun getExitTime(): Long {
        return prefs.getLong("last_exit", System.currentTimeMillis())
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}