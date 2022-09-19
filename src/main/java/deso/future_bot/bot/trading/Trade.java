package deso.future_bot.bot.trading;

import deso.future_bot.bot.binance.model.trade.Order;
import deso.future_bot.bot.data.TradeType;

public class Trade extends Order {

    public static double TRAILING_SL; //It's in percentages, but using double for comfort.
    public static double TAKE_PROFIT; //It's in percentages, but using double for comfort.
    public static boolean CLOSE_USE_CONFLUENCE;
    public static int CLOSE_CONFLUENCE;

    private final long time;
    private final Contract contract;
    private double amount;
    private String explanation;
    private TradeType type;

    public Trade(Contract contract, TradeType type, double price, double amount, String explanation) {
        this.contract = contract;
        this.price = price;
        this.amount = amount;
        this.explanation = explanation;
        this.type = type;
        time = contract.getCurrentTime();
    }

    public Trade(Contract contract, TradeType type, double price, double amount, boolean applied, String explanation) {
        this.contract = contract;
        this.price = price;
        this.amount = amount;
        this.explanation = explanation;
        this.type = type;
        time = contract.getCurrentTime();
    }

    //Getters and setters


    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Contract getCurrency() { //for getting the currency to calculate what the price is now.
        return contract;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Long getTime() {
        return time;
    }

}
