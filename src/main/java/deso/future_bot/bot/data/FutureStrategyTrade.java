package deso.future_bot.bot.data;

import deso.future_bot.bot.trading.Contract;
import deso.future_bot.bot.trading.Trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FutureStrategyTrade {

    private double quantity;

    private double closePrice;

    private HashMap<Double, Double> nextEntry;

    public FutureStrategyTrade(double amount, double price, HashMap<Double, Double> nextEntry) {
        this.quantity = amount;
        this.closePrice = price;
        this.nextEntry = nextEntry;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public HashMap<Double, Double> getNextEntry() {
        return nextEntry;
    }

    public void setNextEntry(HashMap<Double, Double> nextEntry) {
        this.nextEntry = nextEntry;
    }

    public Stream<Trade> getTrade(Contract contract) {
        var trades = new ArrayList<Trade>();
        for (Map.Entry<Double, Double> entry : nextEntry.entrySet()) {
            if (entry.getKey() >= contract.getPrice()) {
                trades.add(new Trade(contract, TradeType.BUY, contract.getPrice(), entry.getValue(), "Match open trade"));
                entry.setValue(0.0);
            }
        }
        nextEntry.values().removeIf(val -> 0.0 == val);
        return trades.stream();
    }

    public boolean isComplete() {
        return nextEntry.isEmpty();
    }

    public double getOpenValue() {
        var openValue = 0.0;
        for (Map.Entry<Double, Double> entry : nextEntry.entrySet()) {
            openValue = openValue + entry.getValue() * entry.getKey();
        }
        return openValue;
    }
}