package deso.future_bot.model.rest

import deso.future_bot.bot.data.StrategyState
import java.time.Instant

data class StrategyResponse(
        var coin: String,
        var id: Long,
        var stopLoss: Double,
        var liquidation: Double,
        var target: Double,
        var state: StrategyState,
        var createdDate: Instant,
        var crossRate: Double
)
