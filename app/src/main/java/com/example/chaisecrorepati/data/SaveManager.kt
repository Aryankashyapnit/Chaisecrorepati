package com.example.chaisecrorepati.data

import android.content.Context
import com.google.gson.Gson

class SaveManager(context: Context) {

    private val prefs = context.getSharedPreferences("chai_game", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveGame(state: GameState) {
        val json = gson.toJson(state)
        prefs.edit().putString("game_state", json).apply()
        saveExitTime()
    }

    fun loadGame(): GameState {
        val json = prefs.getString("game_state", null)
        return if (json != null) {
            gson.fromJson(json, GameState::class.java)
        } else {
            GameState() // pehli baar game khula — fresh state
        }
    }

    fun saveExitTime() {
        prefs.edit().putLong("last_exit", System.currentTimeMillis()).apply()
    }

    fun getExitTime(): Long {
        return prefs.getLong("last_exit", System.currentTimeMillis())
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}