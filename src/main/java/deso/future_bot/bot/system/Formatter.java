package deso.future_bot.bot.system;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class Formatter {
    private static final SimpleDateFormat SIMPLE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final SimpleDateFormat ONLY_DATE_FORMATTER = new SimpleDateFormat("yyyy.MM.dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final NumberFormat PERCENT_FORMAT = new DecimalFormat("0.000%");

    public static SimpleDateFormat getSimpleFormatter() {
        return SIMPLE_FORMATTER;
    }

    public static String formatDate(LocalDateTime date) {
        return DATE_TIME_FORMATTER.format(date);
    }

    public static String formatDate(long timestamp) {
        return SIMPLE_FORMATTER.format(new Date(timestamp));
    }

    public static String formatOnlyDate(long timestamp) {
        return ONLY_DATE_FORMATTER.format(new Date(timestamp));
    }

    public static String formatPercent(double percentage) {
        return PERCENT_FORMAT.format(percentage);
    }

    //TODO: This adds an "?" to the MACD value in simulation in the exe for some reason
    public static String formatDecimal(double decimal) {
        return formatDecimal(decimal, 2);
    }

    public static String formatDecimal(double decimal, int decimalCount) {
        if (decimalCount == 1) {
            return String.format("%.1f", decimal);
        } else {
            return String.format("%.2f", decimal);
        }
    }

    public static String formatLarge(long large) {
        String s = String.valueOf(large);
        StringBuilder builder = new StringBuilder();
        int count = 0;
        char[] chars = s.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            builder.append(chars[i]);
            count++;
            if (count == 3 && i != 0) {
                count = 0;
                builder.append(" ");
            }
        }
        return builder.reverse().toString();
    }


    public static String formatDuration(long duration) {
        if (duration < 1000) {
            return duration + " ms";
        }
        if (duration > 86400000L) {
            return formatDecimal(duration / 86400000.0) + " days";
        }
        return formatDuration(Duration.of(duration, ChronoUnit.MILLIS));
    }

    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }
}
