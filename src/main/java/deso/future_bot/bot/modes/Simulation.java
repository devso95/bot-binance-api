package deso.future_bot.bot.modes;

import deso.future_bot.bot.trading.Contract;
import deso.future_bot.bot.trading.LocalAccount;

import java.util.ArrayList;
import java.util.List;

public final class Simulation {
    public static double STARTING_VALUE;
    private static final List<Contract> currencies = new ArrayList<>();
    private static LocalAccount localAccount;

    private Simulation() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Contract> getCurrencies() {
        return currencies;
    }

    public static LocalAccount getAccount() {
        return localAccount;
    }


    public static void init() {
        localAccount = new LocalAccount("Investor Toomas", STARTING_VALUE);

//        for (String arg : ConfigSetup.getCurrencies()) {
//            //The currency class contains all of the method calls that drive the activity of our bot
//            try {
//                currencies.add(new Contract(arg));
//            } catch (BinanceApiException e) {
//                System.out.println("---Could not add " + arg + ConfigSetup.getFiat());
//                System.out.println(e.getMessage());
//            }
//        }
    }
}
