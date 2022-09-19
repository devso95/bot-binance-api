package deso.future_bot.bot.binance.model.enums;

import deso.future_bot.bot.binance.impl.utils.EnumLookup;

/**
 * buy, sell.
 */
public enum TradeDirection {
  BUY("buy"),
  SELL("sell");

  private final String code;

  TradeDirection(String side) {
    this.code = side;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<TradeDirection> lookup = new EnumLookup<>(TradeDirection.class);

  public static TradeDirection lookup(String name) {
    return lookup.lookup(name);
  }
}
