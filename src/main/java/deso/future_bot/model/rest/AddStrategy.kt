package deso.future_bot.model.rest

import deso.future_bot.bot.data.StrategyState

data class AddStrategy(
        var stopLoss: Double,
        var liquidation: Double,
        var target: Double,
        var secretKey: String,
        var apiKey: String,
        var state: StrategyState,
        var coin: String,
        var crossRate: Double,
        var refId: Long? = null
)
