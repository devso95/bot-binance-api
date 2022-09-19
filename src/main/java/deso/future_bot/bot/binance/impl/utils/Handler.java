package deso.future_bot.bot.binance.impl.utils;

@FunctionalInterface
public interface Handler<T> {

  void handle(T t);
}
