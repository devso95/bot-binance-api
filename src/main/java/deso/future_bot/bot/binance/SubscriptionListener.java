package deso.future_bot.bot.binance;

/**
 * You must implement the SubscriptionListener interface. <br> The server will push any update to
 * the client. if client get the update, the onReceive method will be called.
 *
 * @param <T> The type of received deso.future_bot.bot.data.
 */
@FunctionalInterface
public interface SubscriptionListener<T> {

  /**
   * onReceive will be called when get the deso.future_bot.bot.data sent by server.
   *
   * @param data The deso.future_bot.bot.data send by server.
   */
  void onReceive(T data);
}
