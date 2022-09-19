package deso.future_bot.bot.system;

public enum Mode {
    LIVE,
    SIMULATION,
    BACKTESTING,
    COLLECTION;

    private static Mode state = Mode.LIVE;

    public static Mode get() {
        return state;
    }

    static void set(Mode state) {
        Mode.state = state;
    }
}
