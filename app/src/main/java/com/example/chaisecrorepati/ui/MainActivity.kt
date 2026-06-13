package com.example.chaisecrorepati.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chaisecrorepati.R
import com.example.chaisecrorepati.game.GameManager
import com.example.chaisecrorepati.utils.formatRupees

class MainActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager

    private lateinit var tvRupees: TextView
    private lateinit var tvIncomeRate: TextView
    private lateinit var tvBetterTeaName: TextView
    private lateinit var tvFasterServiceName: TextView
    private lateinit var tvMoreCustomersName: TextView
    private lateinit var btnBetterTea: Button
    private lateinit var btnFasterService: Button
    private lateinit var btnMoreCustomers: Button
    private lateinit var btnWatchAd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Views bind karo
        tvRupees           = findViewById(R.id.tvRupees)
        tvIncomeRate       = findViewById(R.id.tvIncomeRate)
        tvBetterTeaName    = findViewById(R.id.tvBetterTeaName)
        tvFasterServiceName= findViewById(R.id.tvFasterServiceName)
        tvMoreCustomersName= findViewById(R.id.tvMoreCustomersName)
        btnBetterTea       = findViewById(R.id.btnBetterTea)
        btnFasterService   = findViewById(R.id.btnFasterService)
        btnMoreCustomers   = findViewById(R.id.btnMoreCustomers)
        btnWatchAd         = findViewById(R.id.btnWatchAd)

        // GameManager init
        gameManager = GameManager.getInstance(this)

        // Game start karo — offline earnings bhi milegi
        val offlineEarned = gameManager.onAppStart { currentRupees ->
            // ye har second chalega — UI update
            runOnUiThread { updateUI() }
        }

        // Offline popup dikhao agar kuch mila
        if (offlineEarned > 0) {
            Toast.makeText(
                this,
                "Aapki dukaan chalta raha! +${formatRupees(offlineEarned)} mila!",
                Toast.LENGTH_LONG
            ).show()
        }

        // Upgrade buttons
        btnBetterTea.setOnClickListener {
            val success = gameManager.purchaseUpgrade("better_tea")
            if (!success) Toast.makeText(this, "Paisa kam hai!", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        btnFasterService.setOnClickListener {
            val success = gameManager.purchaseUpgrade("faster_service")
            if (!success) Toast.makeText(this, "Paisa kam hai!", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        btnMoreCustomers.setOnClickListener {
            val success = gameManager.purchaseUpgrade("more_customers")
            if (!success) Toast.makeText(this, "Paisa kam hai!", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // Ad button (abhi ke liye 2x booster directly denge — Day 9 pe real ad lagega)
        btnWatchAd.setOnClickListener {
            gameManager.incomeController.activateBooster(1800)
            Toast.makeText(this, "2× Income activated for 30 min! 🔥", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        updateUI()
    }

    private fun updateUI() {
        val state = gameManager.gameState
        val income = gameManager.incomeController

        // Rupees update
        tvRupees.text = formatRupees(state.rupees)

        // Income rate
        val rate = income.calculateIncomePerSecond()
        val boostText = if (income.isBoosterActive()) " 🔥 2×" else ""
        tvIncomeRate.text = "${formatRupees(rate)}/sec$boostText"

        // Upgrade buttons + names update
        val upgrades = state.upgrades

        val betterTea = upgrades.find { it.id == "better_tea" }!!
        val betterTeaCost = income.calculateUpgradeCost("better_tea")
        tvBetterTeaName.text = "🍵 Better Tea (Lv ${betterTea.level})"
        btnBetterTea.text = formatRupees(betterTeaCost)
        btnBetterTea.alpha = if (state.rupees >= betterTeaCost) 1.0f else 0.5f

        val fasterService = upgrades.find { it.id == "faster_service" }!!
        val fasterCost = income.calculateUpgradeCost("faster_service")
        tvFasterServiceName.text = "⚡ Faster Service (Lv ${fasterService.level})"
        btnFasterService.text = formatRupees(fasterCost)
        btnFasterService.alpha = if (state.rupees >= fasterCost) 1.0f else 0.5f

        val moreCustomers = upgrades.find { it.id == "more_customers" }!!
        val moreCost = income.calculateUpgradeCost("more_customers")
        tvMoreCustomersName.text = "👥 More Customers (Lv ${moreCustomers.level})"
        btnMoreCustomers.text = formatRupees(moreCost)
        btnMoreCustomers.alpha = if (state.rupees >= moreCost) 1.0f else 0.5f
    }

    override fun onStop() {
        super.onStop()
        gameManager.onAppStop()
    }
}