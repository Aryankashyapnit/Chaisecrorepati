package com.example.chaisecrorepati.data

data class UpgradeData(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: Double,
    val growthFactor: Double,
    var level: Int = 0
)

data class GameState(
    var rupees: Double = 0.0,
    var gems: Int = 0,
    var totalEarned: Double = 0.0,
    var lastExitTime: Long = 0L,
    var boosterEndTime: Long = 0L,
    var upgrades: List<UpgradeData> = defaultUpgrades(),
    var version: Int = 1
)

fun defaultUpgrades() = listOf(
    UpgradeData("better_tea",     "Better Tea",     "+25% income per level",  50.0,   1.15),
    UpgradeData("faster_service", "Faster Service", "+25% income per level",  200.0,  1.18),
    UpgradeData("more_customers", "More Customers", "+25% income per level",  800.0,  1.20),
    UpgradeData("prime_location", "Prime Location", "+25% income per level",  3000.0, 1.22)
)