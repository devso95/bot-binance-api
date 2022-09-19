package deso.future_bot.bot.trading

import deso.future_bot.bot.binance.SyncRequestClient
import deso.future_bot.bot.binance.model.trade.AccountBalance
import deso.future_bot.bot.binance.model.trade.Order
import deso.future_bot.bot.binance.model.trade.PositionRisk
import deso.future_bot.bot.data.StrategyType
import deso.future_bot.bot.data.TradeType
import deso.future_bot.bot.system.Formatter
import deso.future_bot.bot.system.Mode
import deso.future_bot.model.dto.StrategyDto
import deso.future_bot.model.rest.OrderResponse
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FutureStrategyJob {
    companion object {
        private const val MAX_LEVEL = 10.0
        private const val COMMISSION = 0.0004
        const val REPLACE_RATE1 = 0.5
        const val REPLACE_RATE2 = 0.5
        var PRICE_REPLACE_RATE1 = 0.94
        var PRICE_REPLACE_RATE2 = 0.88
        var RATE_CHANGE = 0.02
        var STRATEGY = StrategyType.SIDEWAY
        var TEST = false

        const val RATE_OPEN = 0.25
        const val OPEN_ENHANCE = 1
    }

    private var mLiquidation: Double
    var mStopLoss: Double
    private var mTarget: Double
    private var mCrossRate: Double = 1.0
    var level = 0.0
    private var mSuggestLevel = 0.0
    var averageEntry = 0.0
    private var mLastPrice = 0.0
    private var closed = false
    var mContract: Contract
    var mQuantity = 0.0
    private var mFiat = 0.0
    private var openOrders: MutableList<Order> = ArrayList()
    private val waitingOrder: MutableList<Order> = ArrayList()
    private val errors = mutableListOf<String>()

    constructor(strategy: StrategyDto, contract: Contract, waitingOrder: List<Order>) {
        mLiquidation = strategy.liquidation
        mStopLoss = strategy.stopLoss
        mTarget = strategy.target
        mCrossRate = strategy.crossRate
        this.mContract = contract
        this.waitingOrder.addAll(waitingOrder)
        syncServer(contract.client, contract)
    }

    constructor(liquidation: Double, stopLoss: Double, target: Double, fiat: Double, contract: Contract) {
        mLiquidation = liquidation
        mTarget = target
        mFiat = fiat
        this.mStopLoss = stopLoss
        this.mContract = contract
    }

    fun updateConfig(strategy: StrategyDto) {
        mLiquidation = strategy.liquidation
        mStopLoss = strategy.stopLoss
        mTarget = strategy.target
        if (strategy.crossRate > mCrossRate) {
            BuySell.open(mContract, "Update cross rate", 1.0)
        }
        mCrossRate = strategy.crossRate
    }

    fun getQuantity(type: TradeType, contract: Contract, confluence: Double): Double {
        if (closed) {
            return 0.0
        }
        updateSuggestLevel(contract)
        val amount: Double
        if (type == TradeType.BUY && mQuantity == 0.0) {
            level = contract.price / (contract.price - mLiquidation)
            amount = currentValue(mContract) / (contract.price - mLiquidation) * (1 - COMMISSION)
        } else {
            if (type == TradeType.BUY) {
                amount = getSuggestOpenAmount(contract, confluence)
            } else {
                if (confluence > 10) {
                    closed = true
                    openOrders.clear()
                    return mQuantity
                }
                amount = getSuggestSellAmount(contract, confluence)
            }
            if (amount * contract.price / currentValue(contract) < 0.01) {
                return 0.0
            }
        }
        return (amount * 100).toInt() / 100.0
    }

    private fun updateSuggestLevel(contract: Contract) {
        var liquidationPnl = mQuantity * (contract.price - mLiquidation)
        for (order in openOrders) {
            if (order.side == TradeType.BUY) {
                liquidationPnl += (order.origQty - order.executedQty) * (order.price - mLiquidation)
            }
        }
        for (order in waitingOrder) {
            if (order.side == TradeType.BUY) {
                liquidationPnl += order.origQty * (order.price - mLiquidation)
            }
        }
        val currentValue = currentValue(contract)
        // Tuyen tinh phan tien co the mat khi giam ve liquidation
        // Tinh ra tong so luong co the mua o gia hien tai
        var rate = (contract.price - mLiquidation) / (mTarget - mLiquidation)
        if (STRATEGY == StrategyType.SIDEWAY) {
            rate = 1 - rate
        } else if (STRATEGY == StrategyType.BULL) {
            rate = 1 - rate * rate * rate
        } else {
            rate = 1 - rate * rate
        }
        val result =
            (((currentValue - liquidationPnl) / (contract.price - mLiquidation) + mQuantity) * contract.price + getOpenValue()) / currentValue * rate
        mSuggestLevel = max(0.0, min(MAX_LEVEL, result))
    }

    private fun getSuggestOpenAmount(contract: Contract, confluence: Double): Double {
        val availableAmount =
            currentValue(contract) * mSuggestLevel * OPEN_ENHANCE - (contract.price * mQuantity + getOpenValue())
        if (availableAmount < 0) {
            return 0.0
        } else {
            return availableAmount / contract.price * confluence * RATE_OPEN
        }
    }

    private fun getSuggestSellAmount(contract: Contract, confluence: Double): Double {
        val amount = min(mQuantity * 0.05 * abs(confluence), getSellAvailableAmount(contract))
        return if (amount * contract.price / currentValue(contract) < 0.01) {
            0.0
        } else amount
    }

    private fun getSellAvailableAmount(contract: Contract): Double {
        val quantity = mQuantity - (currentValue(contract) * mSuggestLevel - getOpenValue()) / contract.price
        return max(quantity, 0.0)
    }

    fun afterTrade(type: TradeType, order: Order, orderId: Long?) {
        if (Mode.get() == Mode.BACKTESTING) {
            if (type == TradeType.BUY) {
                averageEntry = (averageEntry * mQuantity + order.price * order.origQty) / (mQuantity + order.origQty)
                mQuantity += order.origQty
                mFiat -= order.origQty * order.price * 0.0004
            } else {
                mQuantity -= order.origQty
                mFiat += (order.price - averageEntry) * order.origQty
                mFiat -= order.origQty * order.price * 0.0004
            }
            if (!closed) {
                if (type == TradeType.SELL) {
                    addReplaceOrder(
                        openOrders,
                        (mContract.price * PRICE_REPLACE_RATE1).toLong(),
                        REPLACE_RATE1 * order.origQty,
                        orderId,
                        order.time
                    )
                    addReplaceOrder(
                        openOrders,
                        (mContract.price * PRICE_REPLACE_RATE2).toLong(),
                        REPLACE_RATE2 * order.origQty,
                        orderId,
                        order.time
                    )

//                    var sell1 = getSellAvailableAmount(mContract)
//                    mContract.price = mContract.price - 30
//                    var lastLevel = mSuggestLevel
//                    updateSuggestLevel(mContract)
//                    var sell = getSellAvailableAmount(mContract)
//                    var open = getSuggestOpenAmount(mContract)
//                    mContract.price = mContract.price + 30
//                    println("$lastLevel $mSuggestLevel $sell1 $sell $open")
                }
            }
        } else {
            openOrders.add(order)
            if (!closed) {
                if (type == TradeType.SELL && orderId != null) {
                    addReplaceOrder(
                        waitingOrder,
                        (mContract.price * PRICE_REPLACE_RATE1).toLong(),
                        REPLACE_RATE1 * order.origQty,
                        orderId,
                        order.time
                    )
                    addReplaceOrder(
                        waitingOrder,
                        (mContract.price * PRICE_REPLACE_RATE2).toLong(),
                        REPLACE_RATE2 * order.origQty,
                        orderId,
                        order.time
                    )
                }
            }
        }
        val mess = print(order, mContract)
        if (Mode.get() == Mode.BACKTESTING) mContract.appendLogLine(mess)
    }

    private fun addReplaceOrder(
        orders: MutableList<Order>,
        price: Long,
        quantity: Double,
        reference: Long?,
        time: Long
    ) {
        if (price > mLiquidation) {
            val order = Order(price, quantity, reference, time, "Replace Sell")
            mContract.manager?.saveOrder(
                order = Order(price, quantity, reference, time, "Replace Sell"),
                contract = mContract
            )?.let {
                order.orderId = it.orderId
            }
            orders.add(order)
        }
    }

    fun currentValue(contract: Contract?): Double {
        return mQuantity * (contract!!.price - averageEntry) + mFiat
    }

    fun currentPosition(contract: Contract?): Double {
        return mQuantity * contract!!.price + getOpenValue()
    }

    private fun getOpenValue(): Double {
        var openValue = 0.0
        for (order in openOrders) {
            if (order.side == TradeType.BUY) {
                openValue += (order.origQty - order.executedQty) * order.price
            }
        }
        for (order in waitingOrder) {
            if (order.side == TradeType.BUY) {
                openValue += order.origQty * order.price
            }
        }
        return openValue
    }

    fun close() {
        mFiat += (mLastPrice - averageEntry) * mQuantity
        mQuantity = 0.0
    }

    val isFirstTrade: Boolean
        get() = mQuantity == 0.0

    private val openTrade: String
        get() = (openOrders.size + waitingOrder.size).toString()

    fun getSellRate(contract: Contract): Double {
        updateSuggestLevel(contract)
        val minQuantity = currentValue(contract) * mSuggestLevel / contract.price
        return max(mQuantity - minQuantity, 0.0) / mQuantity * 20
    }

    fun getCurrentLevel(contract: Contract?): Double {
        if (currentValue(contract) == 0.0) {
            return 0.0;
        }
        return currentPosition(contract) / currentValue(contract)
    }

    fun getPnl(): Double {
        return mQuantity * (mContract.price - averageEntry)
    }

    var count = 0

    fun syncServerIfNeed(client: SyncRequestClient?, contract: Contract) {
        if (count > 5) {
            syncServer(client, contract)
            count = 0
        } else {
            count++
        }
    }

    fun syncServer(client: SyncRequestClient?, contract: Contract) {
        try {
            this.mContract = contract
            mLastPrice = this.mContract.price
            var lastActionPrice = 100000000000.0
            var isFilledOrder = false
            if (Mode.get() == Mode.BACKTESTING) {
                for (order in ArrayList(openOrders)) {
                    if (order.price >= this.mContract.price) {
                        afterTrade(TradeType.BUY, order, null)
                        openOrders.remove(order)
                        lastActionPrice = min(lastActionPrice, order.price)
                        isFilledOrder = true
                    }
                }
                level = currentPosition(contract) / currentValue(contract)
            } else if (client != null) {
                val balance =
                    client.balance.stream().filter { accountBalance: AccountBalance -> accountBalance.asset == "USDT" }
                        .findFirst().orElse(null)
                if (balance != null) {
                    mFiat = balance.balance.toDouble() * mCrossRate
                }
                openOrders.forEach { order ->
                    if (order.price > contract.price) {
                        lastActionPrice = min(lastActionPrice, order.price)
                        isFilledOrder = true
                    }
                }
                openOrders = client.getOpenOrders(contract.pair)
                val positions = client.getPositionRisk(contract.pair)
                positions.stream().findFirst().ifPresent { position: PositionRisk ->
                    mQuantity = position.positionAmt.toDouble()
                    averageEntry = position.entryPrice.toDouble()
                }
                level = currentPosition(contract) / currentValue(contract)
                val histories = HashMap<Long, Order?>()
                for (order in ArrayList(waitingOrder)) {
                    val openOrder = openOrders.stream()
                        .filter { order1: Order -> order1.orderId == order.refOrderId }
                    if (openOrder.findAny().isPresent) {
                        continue
                    }
                    var historyOrder = histories[order.refOrderId]
                    if (historyOrder == null) {
                        historyOrder = client.getOrder(contract.pair, order.refOrderId, null)
                        histories[order.refOrderId] = historyOrder
                    }
                    if (historyOrder!!.status == "FILLED") {
                        val newOrder = BuySell.placeOrder(contract, order.origQty, true)
                        if (newOrder != null) {
                            print(newOrder, this.mContract)
                            afterTrade(TradeType.BUY, newOrder, newOrder.orderId)
                            waitingOrder.remove(order)
                            contract.manager?.complete(order)
                        }
                    } else {
                        waitingOrder.remove(order)
                        contract.manager?.complete(order)
                    }
                }
            }
            if (isFilledOrder) {
                BuySell.putLastPrice(contract, lastActionPrice)
            }
        } catch (e: Exception) {
            errors.add(e.printStackTrace().toString())
            e.printStackTrace()
            closed = true
        }
    }

    private fun print(order: Order, contract: Contract?): String {
        if (!TEST) {
            val message =
                "-${Formatter.formatDate(order.time)} ${order.side} ${Formatter.formatDecimal(order.origQty)}, at ${order.price.toInt()}, Entry ${averageEntry.toInt()} Level ${
                    Formatter.formatDecimal(level)
                } - ${Formatter.formatDecimal(getCurrentLevel(contract))}\n---${order.explanation}"
            println(message)
            println(
                "----- Current ${currentValue(contract).toInt()} Amount ${Formatter.formatDecimal(mQuantity)} Position ${
                    currentPosition(
                        contract
                    ).toInt()
                } - OpenOrder $openTrade\n"
            )
            return message
        }
        return ""
    }

    fun getOpenOrders(): List<OrderResponse> {
        return openOrders.map {
            OrderResponse(
                side = it.side,
                price = it.price,
                qty = it.origQty,
                executedQty = it.executedQty,
                status = it.status,
                time = it.time,
                explanation = it.explanation
            )
        }
    }

    fun getWaitingOrders(): List<OrderResponse> {
        return waitingOrder.map {
            OrderResponse(
                side = it.side,
                price = it.price,
                qty = it.origQty,
                executedQty = it.executedQty,
                status = it.status,
                time = it.time,
                explanation = it.explanation
            )
        }
    }

    fun getLiquidation(): Double {
        var arg1 = mQuantity * mContract.price
        var arg2 = mQuantity
        openOrders.forEach {
            if (TradeType.BUY == it.side) {
                arg1 += it.price * it.origQty
                arg2 += it.origQty
            }
        }
        waitingOrder.forEach {
            if (TradeType.BUY == it.side) {
                arg1 += it.price * it.origQty
                arg2 += it.origQty
            }
        }
        return (arg1 - currentValue(mContract)) / arg2
    }

    fun getError(): String {
        return errors.toString();
    }

}