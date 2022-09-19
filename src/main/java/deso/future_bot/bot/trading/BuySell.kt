package deso.future_bot.bot.trading

import com.binance.api.client.domain.OrderStatus
import com.binance.api.client.exception.BinanceApiException
import deso.future_bot.bot.binance.model.enums.*
import deso.future_bot.bot.binance.model.trade.Order
import deso.future_bot.bot.data.TradeType
import deso.future_bot.bot.system.Formatter
import deso.future_bot.bot.system.Mode

class BuySell private constructor() {
    companion object {
        private val LAST_ACTION_PRICE = HashMap<Long, Double>()

        //Used by strategy
        fun open(contract: Contract, explanation: String?, confluence: Double) {
            val currentPrice = contract.price //Current price of the currency
            val lastPrice = getLastPrice(contract)
            if (contract.price < lastPrice * (1 - FutureStrategyJob.RATE_CHANGE)) {
                return
            }
            syncServer(contract)
            val amount = contract.job.getQuantity(TradeType.BUY, contract, confluence)
            if (amount == 0.0 || amount * contract.price < 5) {
                return
            }
            val order: Order?
            var orderId: Long? = 0L
            if (Mode.get() == Mode.LIVE) {
                order = placeOrder(contract, amount, true)
                if (order == null) {
                    return
                }
                orderId = order.orderId
                order.explanation = explanation
                println(
                    "Opened trade at an avg open of " + Formatter.formatDecimal(order.price) + " ("
                            + Formatter.formatPercent((order.price - currentPrice) / order.price)
                            + " from current)"
                )
            } else {
                order = Order(TradeType.BUY, currentPrice, amount, contract.currentTime, explanation)
            }

            //Converting fiat value to coin value
            contract.job.afterTrade(TradeType.BUY, order, orderId)
            putLastPrice(contract, contract.price)
        }

        //Used by trade
        fun close(contract: Contract, confluence: Double, explanation: String?) {
            val order: Order?
            var orderId: Long? = 0L
            syncServer(contract)
            val amount = contract.job.getQuantity(TradeType.SELL, contract, confluence)
            if (amount == 0.0 || amount * contract.price < 5) {
                return
            }
            if (Mode.get() == Mode.LIVE) {
                order = placeOrder(contract, amount, false)
                if (order == null) {
                    return
                }
                orderId = order.orderId
                println(
                    "Closed trade at an avg close of " + Formatter.formatDecimal(contract.price) + " ("
                            + Formatter.formatPercent((contract.price - contract.job.averageEntry) / contract.job.averageEntry)
                            + " from current)"
                )
            } else {
                order = Order(TradeType.SELL, contract.price, amount, contract.currentTime, explanation)
            }
            contract.job.afterTrade(TradeType.SELL, order, orderId)
        }

        fun close() {}
        fun placeOrder(contract: Contract, amount: Double, buy: Boolean): Order? {
            println(
                "---Placing a ${if (buy) "buy" else "sell"} market order for ${contract.pair}"
            )
            return try {
                val order: Order
                order = if (contract.config.coin == "EGLD") {
                    contract.client!!.postOrder(
                        contract.pair, if (buy) OrderSide.BUY else OrderSide.SELL, PositionSide.BOTH,
                        OrderType.LIMIT, TimeInForce.GTC,
                        Formatter.formatDecimal(amount, 1), Formatter.formatDecimal(contract.price),
                        null, null, null, null, NewOrderRespType.RESULT
                    )
                } else {
                    contract.client!!.postOrder(
                        contract.pair, if (buy) OrderSide.BUY else OrderSide.SELL, PositionSide.BOTH,
                        OrderType.LIMIT, TimeInForce.GTC,
                        Formatter.formatDecimal(amount, 2), Formatter.formatDecimal(contract.price),
                        null, null, null, null, NewOrderRespType.RESULT
                    )
                }
                if (order.status != OrderStatus.FILLED.toString()) {
                    println("Order is " + order.status + ", not FILLED!")
                }
                order
            } catch (e: BinanceApiException) {
                println(e.message)
                null
            }
        }

        fun closeIfNeed(contract: Contract, confluence: Double) {
            var confluence = confluence
            val averageEntry = contract.job.averageEntry
            val newPrice = contract.price
            val lastPrice = getLastPrice(contract)
            var close = true
            var explanation = ""
            if (averageEntry > 0 && newPrice > lastPrice * (1 + FutureStrategyJob.RATE_CHANGE) && newPrice > contract.job.averageEntry * 1.01) {
                val tmpConfluence = confluence
                confluence -= contract.job.getSellRate(contract)
                if (confluence >= 0) {
                    return
                }
                explanation += if (tmpConfluence < 0) {
                    "Closed due to: Take profit and Indicator $tmpConfluence"
                } else {
                    "Closed due to: Take profit"
                }
            } else if (newPrice < contract.job.mStopLoss) {
                confluence = 10.0
                explanation += "Closed due to: Trailing SL"
            } else if (Trade.CLOSE_USE_CONFLUENCE && confluence <= -Trade.CLOSE_CONFLUENCE && Math.abs((newPrice - lastPrice) / lastPrice) > FutureStrategyJob.RATE_CHANGE) {
                explanation += "Closed due to: Indicator confluence of $confluence"
            } else {
                close = false
            }
            if (close) {
                putLastPrice(contract, newPrice)
                close(contract, confluence, explanation)
            }
        }

        fun getLastPrice(contract: Contract): Double {
            return LAST_ACTION_PRICE.getOrDefault(contract.config.id, contract.job.averageEntry)
        }

        fun putLastPrice(contract: Contract, price: Double) {
            LAST_ACTION_PRICE[contract.config.id] = price
        }

        fun syncServer(contract: Contract) {
            contract.job.syncServer(contract.client, contract)
        }

        @JvmStatic
        fun clear() {
            LAST_ACTION_PRICE.clear()
        }
    }

    init {
        throw IllegalStateException("Utility class")
    }
}