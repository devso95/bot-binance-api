package deso.future_bot.bot.binance.impl;

import deso.future_bot.bot.binance.impl.utils.JsonWrapper;

@FunctionalInterface
public interface RestApiJsonParser<T> {

  T parseJson(JsonWrapper json);
}
