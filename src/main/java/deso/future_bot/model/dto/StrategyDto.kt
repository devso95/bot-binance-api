package deso.future_bot.model.dto

import deso.future_bot.bot.data.StrategyState

data class StrategyDto(
        var id: Long = 0,
        var stopLoss: Double,
        var liquidation: Double,
        var target: Double,
        var state: StrategyState,
        var secretKey: String,
        var apiKey: String,
        var coin: String,
        var userId: Long,
        var crossRate: Double
)
