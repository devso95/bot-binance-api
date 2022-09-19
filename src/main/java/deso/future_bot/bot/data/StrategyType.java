package deso.future_bot.bot.data;

/**
 * The Gender enumeration.
 */
public enum StrategyType {
    SIDEWAY("SIDEWAY"),
    BULL("BULL"),
    BEAR("BEAR"),
    NORMAL("NORMAL");

    private final String value;

    StrategyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
