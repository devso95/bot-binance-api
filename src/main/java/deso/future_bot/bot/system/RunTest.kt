package deso.future_bot.bot.system

import deso.future_bot.bot.data.StrategyState
import deso.future_bot.bot.data.StrategyType
import deso.future_bot.bot.modes.Collection
import deso.future_bot.bot.trading.BuySell
import deso.future_bot.bot.trading.Contract
import deso.future_bot.bot.trading.FutureStrategyJob
import deso.future_bot.model.dto.StrategyDto
import java.io.File

object RunTest {
    @JvmStatic
    fun main(args: Array<String>) {
        ConfigSetup.readConfig()
        Mode.set(Mode.BACKTESTING)
        FutureStrategyJob.TEST = true
        applyStrategy(StrategyType.SIDEWAY)
        applyStrategy(StrategyType.BULL)
        applyStrategy(StrategyType.NORMAL)
    }

    fun applyStrategy(strategy: StrategyType) {
        FutureStrategyJob.STRATEGY = strategy
        applyRateChange(0.01)
        applyRateChange(0.02)
        applyRateChange(0.03)
    }

    fun applyRateChange(rateChange: Double) {
        FutureStrategyJob.RATE_CHANGE = rateChange

        applyPriceReplaceRate(0.95, 0.91)
        applyPriceReplaceRate(0.93, 0.88)
    }

    fun applyPriceReplaceRate(priceReplace1: Double, priceReplace2: Double) {
        FutureStrategyJob.PRICE_REPLACE_RATE1 = priceReplace1
        FutureStrategyJob.PRICE_REPLACE_RATE2 = priceReplace2

        test(0.7)
//        test(1.0)
    }

    fun test(rateOpen: Double) {
        val testFiles = Collection.getDataFiles()
        val list = mutableListOf<Contract>()

        println("==========*===========")
        println(
            "STRATEGY ${FutureStrategyJob.STRATEGY}\n" +
                    "RATE_CHANGE ${FutureStrategyJob.RATE_CHANGE}\n" +
                    "REPLACE_RATE ${FutureStrategyJob.REPLACE_RATE1}*${FutureStrategyJob.PRICE_REPLACE_RATE1}/" +
                    "${FutureStrategyJob.REPLACE_RATE2}*${FutureStrategyJob.PRICE_REPLACE_RATE2}\n" +
                    "${FutureStrategyJob.OPEN_ENHANCE} ${FutureStrategyJob.RATE_OPEN}"
        )

        for (i in 1..5) {
            val path = "backtesting/" + testFiles[i - 1]
            var strategy = StrategyDto(0, 50.0, 50.0, 800.0, StrategyState.ACTIVE, "", "", "EGLD", 0, 1.0)
            if (path.contains("BNB")) {
                strategy = StrategyDto(0, 200.0, 200.0, 800.0, StrategyState.ACTIVE, "", "", "BNB", 0, 1.0)
            }
            BuySell.clear()
            val contract = Contract(File(path).name.split("_".toRegex()).toTypedArray()[0], path, strategy, 10000.0)
            BuySell.close(contract, 20.0, "Close")
            list.add(contract)
        }
        list.forEachIndexed { index, contract ->
            println("Index" + index + " " + contract.job.currentValue(contract).toInt())
        }
    }

}