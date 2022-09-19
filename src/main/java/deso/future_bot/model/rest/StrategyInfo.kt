package deso.future_bot.model.rest

data class StrategyInfo(
        var id: Long,
        var averageEntry: Double,
        val quantity: Double,
        val pair: String,
        val currentPosition: Double,
        val currentValue: Double,
        val currentLevel: Double,
        val markPrice: Double,
        val liquidation: Double,
        val pnl: Double,
        val openOrders: List<OrderResponse>,
        val waitingOrders: List<OrderResponse>,
        val errors: String? = null
) {
    val openOrderCount = openOrders.size
    val waitingOrderCount = waitingOrders.size
}
