package com.example.chaisecrorepati.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.chaisecrorepati.R
import com.example.chaisecrorepati.game.GameManager
import com.example.chaisecrorepati.utils.formatRupees
import com.example.chaisecrorepati.utils.formatTime

class MainActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager

    // Views
    private lateinit var tvRupees: TextView
    private lateinit var tvIncomeRate: TextView
    private lateinit var tvBoosterTimer: TextView
    private lateinit var tvStallLevel: TextView
    private lateinit var tvBetterTeaName: TextView
    private lateinit var tvFasterServiceName: TextView
    private lateinit var tvMoreCustomersName: TextView
    private lateinit var tvPrimeLocationName: TextView
    private lateinit var btnBetterTea: Button
    private lateinit var btnFasterService: Button
    private lateinit var btnMoreCustomers: Button
    private lateinit var btnPrimeLocation: Button
    private lateinit var btnWatchAd: Button
    private lateinit var btnStats: Button

    // Booster timer handler
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateBoosterTimer()
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        gameManager = GameManager.getInstance(this)

        val offlineEarned = gameManager.onAppStart { _ ->
            runOnUiThread { updateUI() }
        }

        if (offlineEarned > 1.0) {
            showOfflineDialog(offlineEarned)
        }

        setupButtons()
        updateUI()
        timerHandler.post(timerRunnable)

        btnStats.setOnClickListener {
            startActivity(android.content.Intent(this, StatsActivity::class.java))
        }
    }

    private fun bindViews() {
        tvRupees            = findViewById(R.id.tvRupees)
        tvIncomeRate        = findViewById(R.id.tvIncomeRate)
        tvBoosterTimer      = findViewById(R.id.tvBoosterTimer)
        tvStallLevel        = findViewById(R.id.tvStallLevel)
        tvBetterTeaName     = findViewById(R.id.tvBetterTeaName)
        tvFasterServiceName = findViewById(R.id.tvFasterServiceName)
        tvMoreCustomersName = findViewById(R.id.tvMoreCustomersName)
        tvPrimeLocationName = findViewById(R.id.tvPrimeLocationName)
        btnBetterTea        = findViewById(R.id.btnBetterTea)
        btnFasterService    = findViewById(R.id.btnFasterService)
        btnMoreCustomers    = findViewById(R.id.btnMoreCustomers)
        btnPrimeLocation    = findViewById(R.id.btnPrimeLocation)
        btnWatchAd          = findViewById(R.id.btnWatchAd)
        btnStats = findViewById(R.id.btnStats)

    }

    private fun setupButtons() {
        btnBetterTea.setOnClickListener {
            if (!gameManager.purchaseUpgrade("better_tea")) showNoMoneyToast()
            updateUI()
        }
        btnFasterService.setOnClickListener {
            if (!gameManager.purchaseUpgrade("faster_service")) showNoMoneyToast()
            updateUI()
        }
        btnMoreCustomers.setOnClickListener {
            if (!gameManager.purchaseUpgrade("more_customers")) showNoMoneyToast()
            updateUI()
        }
        btnPrimeLocation.setOnClickListener {
            if (!gameManager.purchaseUpgrade("prime_location")) showNoMoneyToast()
            updateUI()
        }
        btnWatchAd.setOnClickListener {
            gameManager.incomeController.activateBooster(1800)
            Toast.makeText(this,
                "🔥 2× Income 30 min ke liye ON!",
                Toast.LENGTH_LONG).show()
            updateUI()
        }
    }

    private fun updateUI() {
        val state    = gameManager.gameState
        val income   = gameManager.incomeController
        val upgrades = state.upgrades

        // Rupees counter
        tvRupees.text = formatRupees(state.rupees)

        // Income rate
        val rate = income.calculateIncomePerSecond()
        tvIncomeRate.text = "${formatRupees(rate)}/sec"

        // Stall level
        val totalLevels = upgrades.sumOf { it.level }
        tvStallLevel.text = "Roadside Chai Stall — Level ${(totalLevels / 3) + 1}"

        // Upgrades
        updateUpgradeRow(
            upgrades.find { it.id == "better_tea" }!!,
            tvBetterTeaName, btnBetterTea,
            "🍵 Better Tea", "better_tea",
            state.rupees, income
        )
        updateUpgradeRow(
            upgrades.find { it.id == "faster_service" }!!,
            tvFasterServiceName, btnFasterService,
            "⚡ Faster Service", "faster_service",
            state.rupees, income
        )
        updateUpgradeRow(
            upgrades.find { it.id == "more_customers" }!!,
            tvMoreCustomersName, btnMoreCustomers,
            "👥 More Customers", "more_customers",
            state.rupees, income
        )
        updateUpgradeRow(
            upgrades.find { it.id == "prime_location" }!!,
            tvPrimeLocationName, btnPrimeLocation,
            "📍 Prime Location", "prime_location",
            state.rupees, income
        )
    }

    private fun updateUpgradeRow(
        upgrade: com.example.chaisecrorepati.data.UpgradeData,
        nameView: TextView,
        btn: Button,
        label: String,
        upgradeId: String,
        currentRupees: Double,
        income: com.example.chaisecrorepati.game.IdleIncomeController
    ) {
        val cost = income.calculateUpgradeCost(upgradeId)
        nameView.text = "$label (Lv ${upgrade.level})"
        btn.text = formatRupees(cost)
        val canAfford = currentRupees >= cost
        btn.alpha = if (canAfford) 1.0f else 0.4f
        btn.isEnabled = canAfford
    }

    // Booster timer — har second update hota hai
    private fun updateBoosterTimer() {
        val income = gameManager.incomeController
        if (income.isBoosterActive()) {
            val remaining = (gameManager.gameState.boosterEndTime
                    - System.currentTimeMillis()) / 1000
            tvBoosterTimer.visibility = View.VISIBLE
            tvBoosterTimer.text = "🔥 2× Boost: ${formatTime(remaining)} baki"
        } else {
            tvBoosterTimer.visibility = View.GONE
        }
    }

    private fun showOfflineDialog(earned: Double) {
        AlertDialog.Builder(this)
            .setTitle("🎉 Dukaan chalta raha!")
            .setMessage(
                "Aap nahi the phir bhi chai biki!\n\n" +
                        "Kamaai: ${formatRupees(earned)}\n\n" +
                        "Watch Ad karke double karao!"
            )
            .setPositiveButton("Collect ✅") { d, _ -> d.dismiss() }
            .setNegativeButton("2× Collect 📺") { d, _ ->
                gameManager.gameState.rupees += earned
                gameManager.gameState.totalEarned += earned
                gameManager.incomeController.activateBooster(1800)
                Toast.makeText(this,
                    "Double mila + 🔥 2× Booster ON!",
                    Toast.LENGTH_LONG).show()
                d.dismiss()
                updateUI()
            }
            .setCancelable(false)
            .show()
    }

    private fun showNoMoneyToast() {
        Toast.makeText(this, "💸 Paisa kam hai!", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        timerHandler.removeCallbacks(timerRunnable)
        gameManager.onAppStop()
    }
}