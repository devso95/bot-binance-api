package deso.future_bot.bot.trading

import deso.future_bot.bot.binance.SyncRequestClient
import deso.future_bot.bot.binance.model.enums.CandlestickInterval
import deso.future_bot.bot.binance.model.market.Candlestick
import deso.future_bot.bot.binance.model.trade.Order
import deso.future_bot.bot.data.PriceBean
import deso.future_bot.bot.data.PriceReader
import deso.future_bot.bot.indicators.DBB
import deso.future_bot.bot.indicators.Indicator
import deso.future_bot.bot.indicators.MACD
import deso.future_bot.bot.indicators.RSI
import deso.future_bot.bot.modes.StrategyManagerInterface
import deso.future_bot.bot.system.Formatter
import deso.future_bot.bot.system.Mode
import deso.future_bot.model.dto.StrategyDto
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import java.util.stream.Collectors

class Contract {

    companion object {
        @JvmField
        var CONFLUENCE_TARGET = 0
    }

    val pair: String
    private var candleTime: Long = 0
    private val indicators: MutableList<Indicator> = ArrayList()
    private val currentlyCalculating = AtomicBoolean(false)
    var client: SyncRequestClient? = null
    var price = 0.0
    var currentTime: Long = 0
        private set
    val job: FutureStrategyJob

    private val log = StringBuilder()
    var config: StrategyDto
    var manager: StrategyManagerInterface? = null

    //Used for SIMULATION and LIVE
    constructor(config: StrategyDto, manager: StrategyManagerInterface, waitingOrder: List<Order>) {
        this.config = config
        this.manager = manager

        client = SyncRequestClient.create(config.apiKey, config.secretKey)
        pair = config.coin + "USDT"

        job = FutureStrategyJob(config, this, waitingOrder)
        val history = client!!.getCandlestick(pair, CandlestickInterval.FIVE_MINUTES,
                System.currentTimeMillis() - 480 * 60 * 1000, System.currentTimeMillis(), 100)
        val closingPrices = history.stream().map { candle: Candlestick -> candle.close.toDouble() }.collect(Collectors.toList())
        indicators.add(RSI(closingPrices, 14))
        indicators.add(MACD(closingPrices, 12, 26, 9))
        indicators.add(DBB(closingPrices, 20))

        //We set the initial values to check against in onMessage based on the latest candle in history
        currentTime = System.currentTimeMillis()
        candleTime = history[history.size - 1].closeTime
        println("---SETUP DONE FOR $this")
    }

    fun sync() {
        val tickerPrice = client!!.get24hrTickerPriceChange(pair)
        val newPrice = tickerPrice.lastPrice.toDouble()
        val newTime = System.currentTimeMillis()
        if (price == newPrice && newTime <= candleTime && job.currentValue(this) == 0.0) {
            return
        }
        println("""Check price $newPrice
   --- Level ${Formatter.formatDecimal(job.mQuantity)} (x${Formatter.formatDecimal(job.level)}) $pair
  ---- Value ${Formatter.formatDecimal(job.currentValue(this))}/${Formatter.formatDecimal(job.currentPosition(this))} USDT
 ----- Open  ${job.getOpenOrders().size} Waiting ${job.getWaitingOrders().size}""")
        if (newTime > candleTime) {
            accept(PriceBean(candleTime, newPrice, true))
            candleTime += 300000L
        } else {
            accept(PriceBean(newTime, newPrice))
        }
    }

    constructor(pair: String, filePath: String?, strategy: StrategyDto, fiat: Double) {
        job = FutureStrategyJob(strategy.stopLoss, strategy.liquidation, strategy.target, fiat, this)
        config = strategy
        this.pair = pair
        try {
            PriceReader(filePath).use { reader ->
                var bean = reader.readPrice()
                val closingPrices: MutableList<Double> = ArrayList()
                while (bean!!.isClosing) {
                    closingPrices.add(bean.price)
                    bean = reader.readPrice()
                }
                //TODO: Fix slight mismatch between MACD backtesting and server values.
                indicators.add(RSI(closingPrices, 14))
//                indicators.add(MACD(closingPrices, 12, 26, 9))
//                indicators.add(DBB(closingPrices, 20))
                while (bean != null) {
                    accept(bean)
                    bean = reader.readPrice()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun accept(bean: PriceBean) {
        //Make sure we dont get concurrency issues
        if (currentlyCalculating.get()) {
            System.out.println("------------WARNING, NEW THREAD STARTED ON " + pair + " MESSAGE DURING UNFINISHED PREVIOUS MESSAGE CALCULATIONS");
        }
        price = bean.price
        currentTime = bean.timestamp
        if (bean.isClosing) {
            indicators.forEach(Consumer { indicator: Indicator -> indicator.update(bean.price) })
            if (Mode.get() == Mode.BACKTESTING) {
                appendLogLine(Formatter.formatDate(currentTime) + "  ")
            }
        }

        if (!currentlyCalculating.get()) {
            currentlyCalculating.set(true);
            //We can disable the strategy and deso.future_bot.bot.trading logic to only check indicator and price accuracy
            val confluence: Double = check() //0 Confluence should be reserved in the config for doing nothing
            if (confluence >= CONFLUENCE_TARGET || job.isFirstTrade) {
                BuySell.open(this@Contract, "Trade opened due to: $explanations", confluence)
            } else {
                BuySell.closeIfNeed(this@Contract, confluence)
            }
            job.syncServerIfNeed(client, this)
            currentlyCalculating.set(false);
        }
    }

    fun check(): Double {
        return indicators.stream().mapToDouble { indicator: Indicator -> indicator.check(price) }.sum()
    }

    private val explanations: String
        get() {
            val builder = StringBuilder()
            for (indicator in indicators) {
                var explanation = indicator.explanation
                if (explanation == null) explanation = ""
                builder.append(if (explanation == "") "" else explanation + "\t")
            }
            return builder.toString()
        }

    fun appendLogLine(s: String?) {
        log.append(s).append("\n")
    }

    fun log(path: String?) {
        try {
            FileWriter(path).use { writer ->
                writer.write("""Test ended ${Formatter.formatDate(LocalDateTime.now())} """)
                writer.write("\n\nCONFIG:\n")
                //            writer.write(ConfigSetup.getSetup());
//            writer.write("\n\nMarket performance: " + deso.future_bot.bot.system.Formatter.formatPercent((currentPrice - firstBean.getPrice()) / firstBean.getPrice()));
//            if (!tradeHistory.isEmpty()) {
//                tradeHistory.sort(Comparator.comparingDouble(Trade::getProfit));
//                double maxLoss = tradeHistory.get(0).getProfit();
//                double maxGain = tradeHistory.get(tradeHistory.size() - 1).getProfit();
//                int lossTrades = 0;
//                double lossSum = 0;
//                int gainTrades = 0;
//                double gainSum = 0;
//                long tradeDurs = 0;
//                for (Trade trade : tradeHistory) {
//                    double profit = trade.getProfit();
//                    if (profit < 0) {
//                        lossTrades += 1;
//                        lossSum += profit;
//                    } else if (profit > 0) {
//                        gainTrades += 1;
//                        gainSum += profit;
//                    }
//                    tradeDurs += trade.getDuration();
//                }

//                double tradePerWeek = 604800000.0 / (((double) currentTime - firstBean.getTimestamp()) / tradeHistory.size());
//
//                writer.write("\nBot performance: " + deso.future_bot.bot.system.Formatter.formatPercent(BuySell.getAccount().getProfit()) + "\n\n");
//                writer.write(BuySell.getAccount().getTradeHistory().size() + " closed trades"
//                        + " (" + deso.future_bot.bot.system.Formatter.formatDecimal(tradePerWeek) + " trades per week) with an average holding length of "
//                        + deso.future_bot.bot.system.Formatter.formatDuration(Duration.of(tradeDurs / tradeHistory.size(), ChronoUnit.MILLIS)) + " hours");
//                if (lossTrades != 0) {
//                    writer.write("\nLoss trades:\n");
//                    writer.write(lossTrades + " trades, " + deso.future_bot.bot.system.Formatter.formatPercent(lossSum / (double) lossTrades) + " average, " + deso.future_bot.bot.system.Formatter.formatPercent(maxLoss) + " max");
//                }
//                if (gainTrades != 0) {
//                    writer.write("\nProfitable trades:\n");
//                    writer.write(gainTrades + " trades, " + deso.future_bot.bot.system.Formatter.formatPercent(gainSum / (double) gainTrades) + " average, " + deso.future_bot.bot.system.Formatter.formatPercent(maxGain) + " max");
//                }
//                writer.write("\n\nClosed trades (least to most profitable):\n");
//                for (Trade trade : tradeHistory) {
//                    writer.write(trade.toString() + "\n");
//                }
//            } else {
//                writer.write("\n(Not trades made)\n");
//                System.out.println("---No trades made in the time period!");
//            }
                writer.write("\n\nFULL LOG:\n\n")
                writer.write(log.toString())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        println("---Log file generated at " + File(path).absolutePath)
    }

    override fun toString(): String {
        val s = StringBuilder("$pair price: $price")
        if (currentTime == candleTime) indicators.forEach(Consumer { indicator: Indicator -> s.append(", ").append(indicator.javaClass.simpleName).append(": ").append(Formatter.formatDecimal(indicator.get())) }) else indicators.forEach(Consumer { indicator: Indicator -> s.append(", ").append(indicator.javaClass.simpleName).append(": ").append(Formatter.formatDecimal(indicator.getTemp(price))) })
        return s.toString()
    }

    override fun hashCode(): Int {
        return pair.hashCode()
    }

    fun updateConfig(strategy: StrategyDto) {
        job.updateConfig(strategy)
        job.syncServer(client, this)
    }

}