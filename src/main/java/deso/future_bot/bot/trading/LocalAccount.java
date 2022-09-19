package deso.future_bot.bot.trading;

import com.binance.api.client.domain.account.Account;
import deso.future_bot.bot.data.TradeType;
import deso.future_bot.bot.system.ConfigSetup;
import deso.future_bot.bot.system.Formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalAccount {
    private final String username;
    private Account realAccount;

    //To give the account a specific final amount of money.
    private double fiatValue;
    private double startingValue;
    private final ConcurrentHashMap<Contract, Double> wallet;
    private final List<Trade> tradeHistory;
    private double makerCommission;
    private double takerCommission;
    private double buyerCommission;

    /**
     * Wallet value will most probably be 0 at first, but you could start
     * with an existing wallet value as well.
     */
    public LocalAccount(String username, double startingValue) {
        this.username = username;
        this.startingValue = startingValue;
        fiatValue = startingValue;
        wallet = new ConcurrentHashMap<>();
        tradeHistory = new ArrayList<>();
    }

    public LocalAccount(String apiKey, String secretApiKey) {
        CurrentAPI.login(apiKey, secretApiKey);
        username = "";
        wallet = new ConcurrentHashMap<>();
        tradeHistory = new ArrayList<>();
        realAccount = CurrentAPI.get().getAccount();
        if (!realAccount.isCanTrade()) {
            System.out.println("Can't trade!");
        }
        makerCommission = realAccount.getMakerCommission(); //Maker fees are
        // paid when you add liquidity to our order book
        // by placing a limit order below the ticker price for buy, and above the ticker price for sell.
        takerCommission = realAccount.getTakerCommission();//Taker fees are paid when you remove
        // liquidity from our order book by placing any order that is executed against an order on the order book.
        buyerCommission = realAccount.getBuyerCommission();

        //Example: If the current market/ticker price is $2000 for 1 BTC and you market buy bitcoins starting at the market price of $2000, then you will pay the taker fee. In this instance, you have taken liquidity/coins from the order book.
        //
        //If the current market/ticker price is $2000 for 1 BTC and you
        //place a limit buy for bitcoins at $1995, then
        //you will pay the maker fee IF the market/ticker price moves into your limit order at $1995.
        fiatValue = Double.parseDouble(realAccount.getAssetBalance(ConfigSetup.getFiat()).getFree());
        System.out.println("---Starting FIAT: " + Formatter.formatDecimal(fiatValue) + " " + ConfigSetup.getFiat());
    }

    public Account getRealAccount() {
        return realAccount;
    }

    public List<Trade> getTradeHistory() {
        return tradeHistory;
    }

    public void setStartingValue(double startingValue) {
        this.startingValue = startingValue;
    }

    public String getUsername() {
        return username;
    }

    public double getFiat() {
        return fiatValue;
    }

    public void setFiat(double fiatValue) {
        this.fiatValue = fiatValue;
    }

    public double getTotalValue() {
        double value = 0;
        for (Map.Entry<Contract, Double> entry : wallet.entrySet()) {
            Contract contract = entry.getKey();
            Double amount = entry.getValue();
            value += amount * contract.getPrice();
        }
        return value + fiatValue;
    }

    public void addToFiat(double amount) {
        fiatValue += amount;
    }

    public ConcurrentHashMap<Contract, Double> getWallet() {
        return wallet;
    }

    public double getProfit() {
        return (getTotalValue() - startingValue) / startingValue;
    }

    public void addToWallet(Contract key, double value) {
        if (wallet.containsKey(key)) {
            wallet.put(key, wallet.get(key) + value);
        } else {
            wallet.put(key, value);
        }

    }

    /**
     * Method allows to remove values from keys.
     **/
    public void removeFromWallet(Contract key, double value) {
        wallet.put(key, wallet.get(key) - value);
    }

    public double getMakerCommission() {
        return makerCommission;
    }

    public double getTakerCommission() {
        return takerCommission;
    }

    public double getBuyerCommission() {
        return buyerCommission;
    }

}
