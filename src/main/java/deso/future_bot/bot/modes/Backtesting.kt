package deso.future_bot.bot.modes

import deso.future_bot.bot.data.StrategyState
import deso.future_bot.bot.trading.BuySell
import deso.future_bot.bot.trading.Contract
import deso.future_bot.bot.trading.FutureStrategyJob
import deso.future_bot.bot.trading.LocalAccount
import deso.future_bot.model.dto.StrategyDto
import java.io.File
import java.util.*

class Backtesting private constructor() {
    companion object {
        private var localAccount: LocalAccount? = null

        @JvmStatic
        fun startBackTesting() {
            val backtestingFiles = Collection.getDataFiles()
            if (backtestingFiles.size == 0) {
                println("No backtesting files detected!")
                System.exit(0)
            }
            localAccount = LocalAccount("Investor Toomas", Simulation.STARTING_VALUE)
            val sc = Scanner(System.`in`)
            while (true) {
                println("\nBacktesting deso.future_bot.bot.data files:\n")
                for (i in backtestingFiles.indices) {
                    println("[" + (i + 1) + "] " + backtestingFiles[i])
                }
                println("\nEnter a number to select the backtesting data file")
                val input = sc.nextLine()
                if (!input.matches(Regex("\\d+"))) continue
                val index = input.toInt()
                if (index > backtestingFiles.size) {
                    continue
                }
                var path = "backtesting/" + backtestingFiles[index - 1]
                try {
                    println("\n---Setting up...")
                    var strategy = StrategyDto(0, 50.0, 50.0, 800.0, StrategyState.ACTIVE, "", "", "EGLD", 0, 1.0)
                    if (path.contains("BNB")) {
                        strategy = StrategyDto(0, 200.0, 200.0, 800.0, StrategyState.ACTIVE, "", "", "BNB", 0, 1.0)
                    }
                    println(
                        "STRATEGY ${FutureStrategyJob.STRATEGY}\n" +
                                "RATE_CHANGE ${FutureStrategyJob.RATE_CHANGE}\n" +
                                "REPLACE_RATE ${FutureStrategyJob.REPLACE_RATE1}*${FutureStrategyJob.PRICE_REPLACE_RATE1}/" +
                                "${FutureStrategyJob.REPLACE_RATE2}*${FutureStrategyJob.PRICE_REPLACE_RATE2}");
                    BuySell.clear()
                    val contract = Contract(File(path).name.split("_".toRegex()).toTypedArray()[0], path, strategy, 10000.0)
                    BuySell.close(contract, 20.0, "Close")
//                    var i = 1
//                    path = path.replace("backtesting", "log")
//                    var resultPath = path.replace(".dat", "_run_$i.txt")
//                    while (File(resultPath).exists()) {
//                        i++
//                        resultPath = path.replace(".dat", "_run_$i.txt")
//                    }
                    //                new File("log").mkdir();
//
//                contract.log(resultPath);
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Testing failed, try again")
                }
            }
        }
    }

}