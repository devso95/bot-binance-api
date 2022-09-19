package deso.future_bot.model.rest

import deso.future_bot.bot.data.StrategyState

data class UpdateStrategy(
        var id: Long,
        var stopLoss: Double,
        var target: Double,
        var secretKey: String? = null,
        var apiKey: String? = null,
        var liquidation: Double,
        var coin: String,
        var state: StrategyState,
        var crossRate: Double
)
