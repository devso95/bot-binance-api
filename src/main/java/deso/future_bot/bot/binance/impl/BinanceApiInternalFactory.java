package deso.future_bot.bot.binance.impl;

import deso.future_bot.bot.binance.SubscriptionClient;
import deso.future_bot.bot.binance.SubscriptionOptions;
import deso.future_bot.bot.binance.RequestOptions;
import deso.future_bot.bot.binance.SyncRequestClient;

import java.net.URI;

public final class BinanceApiInternalFactory {

    private static final BinanceApiInternalFactory instance = new BinanceApiInternalFactory();

    public static BinanceApiInternalFactory getInstance() {
        return instance;
    }

    private BinanceApiInternalFactory() {
    }

    public SyncRequestClient createSyncRequestClient(String apiKey, String secretKey, RequestOptions options) {
        RequestOptions requestOptions = new RequestOptions(options);
        RestApiRequestImpl requestImpl = new RestApiRequestImpl(apiKey, secretKey, requestOptions);
        return new SyncRequestImpl(requestImpl);
    }

    public SubscriptionClient createSubscriptionClient(SubscriptionOptions options) {
        SubscriptionOptions subscriptionOptions = new SubscriptionOptions(options);
        RequestOptions requestOptions = new RequestOptions();
        try {
            String host = new URI(options.getUri()).getHost();
            requestOptions.setUrl("https://" + host);
        } catch (Exception e) {
            System.out.println("ERROR SOCKET " + e);
        }
        return new WebSocketStreamClientImpl(subscriptionOptions);
    }

}
