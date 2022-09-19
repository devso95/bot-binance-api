package deso.future_bot.bot.modes

import deso.future_bot.bot.binance.model.trade.Order
import deso.future_bot.bot.data.StrategyState
import deso.future_bot.bot.data.TradeType
import deso.future_bot.bot.trading.Contract
import deso.future_bot.mapper.StrategyMapper
import deso.future_bot.model.dto.StrategyDto
import deso.future_bot.model.entity.WaitingOrder
import deso.future_bot.model.rest.StrategyInfo
import deso.future_bot.repository.StrategyRepository
import deso.future_bot.repository.WaitingOrderRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class StrategyManager constructor(
    repository: StrategyRepository,
    mapper: StrategyMapper,
    val waitingOrderRepository: WaitingOrderRepository
) : StrategyManagerInterface {

    private val contracts: MutableList<Contract> = ArrayList()

    init {
        repository.findByState(StrategyState.ACTIVE).map {
            contracts.add(Contract(mapper.toDto(it), this, getWaitingOrder(it.id)))
        }
    }

    fun getContracts(): List<Contract> {
        return contracts
    }

    fun update(strategy: StrategyDto) {
        if (strategy.state == StrategyState.ACTIVE) {
            contracts.firstOrNull { contract -> contract.config.id == strategy.id }.let { contract ->
                contract?.updateConfig(strategy)
                    ?: contracts.add(Contract(strategy, this, getWaitingOrder(strategy.id)))
            }
        } else {
            close(strategy.id)
        }
    }

    fun close(strategyId: Long) {
        contracts.removeIf { it.config.id == strategyId }
    }

    @Scheduled(fixedRate = 30000)
    private fun run() {
        contracts.toMutableList().forEach {
            it.sync()
        }
    }

    override fun saveOrder(order: Order, contract: Contract): Order {
        val waitingOrder = WaitingOrder().apply {
            price = order.price
            amount = order.origQty
            refId = order.refOrderId
            explanation = order.explanation
            strategyId = contract.config.id
            userId = contract.config.userId
            createdDate = Instant.now()
        }
        val entity = waitingOrderRepository.save(waitingOrder)
        return order.apply { orderId = entity.id }
    }

    override fun complete(order: Order) {
        waitingOrderRepository.deleteById(order.orderId)
    }

    override fun remove(contract: Contract) {
        contracts.remove(contract)
    }

    fun getWaitingOrder(strategyId: Long): List<Order> {
        return waitingOrderRepository.findByStrategyId(strategyId)
            .map { waitingOrder ->
                Order().apply {
                    orderId = waitingOrder.id
                    price = waitingOrder.price
                    origQty = waitingOrder.amount
                    refOrderId = waitingOrder.refId
                    explanation = waitingOrder.explanation
                    side = TradeType.BUY
                    status = "WAIT"
                    time = waitingOrder.createdDate.toEpochMilli()
                }
            }
    }

    fun getInfo(coin: String, userId: Long): StrategyInfo? {
        return contracts.firstOrNull { it.config.coin == coin && it.config.userId == userId }?.run {
            val job = this.job
            val info = StrategyInfo(
                id = job.mContract.config.id,
                averageEntry = job.averageEntry,
                currentPosition = job.currentPosition(this),
                quantity = job.mQuantity,
                pair = job.mContract.pair,
                currentValue = job.currentValue(this),
                currentLevel = job.getCurrentLevel(this),
                pnl = job.getPnl(),
                openOrders = job.getOpenOrders(),
                waitingOrders = job.getWaitingOrders(),
                markPrice = job.mContract.price,
                liquidation = job.getLiquidation(),
                errors = job.getError()
            )
            info
        }
    }

    fun getInfo(userId: Long): List<StrategyInfo> {
        return contracts.filter { it.config.userId == userId }.map {
            val job = it.job
            val info = StrategyInfo(
                id = job.mContract.config.id,
                averageEntry = job.averageEntry,
                currentPosition = job.currentPosition(it),
                currentValue = job.currentValue(it),
                quantity = job.mQuantity,
                pair = job.mContract.pair,
                currentLevel = job.getCurrentLevel(it),
                openOrders = job.getOpenOrders(),
                pnl = job.getPnl(),
                waitingOrders = job.getWaitingOrders(),
                markPrice = job.mContract.price,
                liquidation = job.getLiquidation(),
                errors = job.getError()
            )
            info
        }
    }

}

interface StrategyManagerInterface {
    fun saveOrder(order: Order, contract: Contract): Order
    fun complete(order: Order)
    fun remove(contract: Contract)
}