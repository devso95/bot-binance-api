package deso.future_bot.model.rest

import deso.future_bot.bot.data.TradeType

class OrderResponse(val side: TradeType,
                    var price: Double,
                    val qty: Double,
                    val executedQty: Double,
                    val status: String?,
                    val time: Long?,
                    val explanation: String?)