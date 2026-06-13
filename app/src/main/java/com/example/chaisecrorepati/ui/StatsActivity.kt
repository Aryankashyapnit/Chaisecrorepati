package com.example.chaisecrorepati.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.chaisecrorepati.R
import com.example.chaisecrorepati.game.GameManager
import com.example.chaisecrorepati.utils.formatRupees

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val gameManager = GameManager.getInstance(this)
        val state = gameManager.gameState
        val income = gameManager.incomeController
        val upgrades = state.upgrades

        // Total earned
        findViewById<TextView>(R.id.tvTotalEarned).text =
            formatRupees(state.totalEarned)

        // Income per sec
        findViewById<TextView>(R.id.tvStatsIncome).text =
            "${formatRupees(income.calculateIncomePerSecond())}/sec"

        // Current rupees
        findViewById<TextView>(R.id.tvStatsRupees).text =
            formatRupees(state.rupees)

        // Upgrade levels
        findViewById<TextView>(R.id.tvLvBetterTea).text =
            "Level ${upgrades.find { it.id == "better_tea" }?.level ?: 0}"
        findViewById<TextView>(R.id.tvLvFasterService).text =
            "Level ${upgrades.find { it.id == "faster_service" }?.level ?: 0}"
        findViewById<TextView>(R.id.tvLvMoreCustomers).text =
            "Level ${upgrades.find { it.id == "more_customers" }?.level ?: 0}"
        findViewById<TextView>(R.id.tvLvPrimeLocation).text =
            "Level ${upgrades.find { it.id == "prime_location" }?.level ?: 0}"

        // Back button
        findViewById<Button>(R.id.btnBackToGame).setOnClickListener {
            finish()
        }
    }
}