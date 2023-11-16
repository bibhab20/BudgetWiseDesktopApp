package util;

import lombok.extern.slf4j.Slf4j;
import model.TransactionSummary;
import model.TransactionType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import util.cli.CliUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TransactionSummaryUtil {
    public static final char VERTICAL_LINE = '|';
    private static final char UP_ARROW = '\u2191'; // Unicode for up arrow
    private static final char DOWN_ARROW = '\u2193'; // Unicode for down arrow
    private static final String SPACE = " ";

    public static final int MEAN = 0;
    public static final int MAX = 2;
    public static final int MIN = 3;
    public static final int SUM = 4;
    public static final int SD = 5;

    public static String getPrintString(TransactionSummary summary, List<TransactionSummaryProperty> properties){
        List<String> labels = new ArrayList<>();
        for (TransactionSummaryProperty property: properties) {
           labels.add(property.toString() + ": " + getSummaryPropertyValue(summary, property));
        }
        return generateRow(labels, 35);
    }

    public static String getTrendString(double currentValue, double diff, TransactionSummaryProperty property) {
        String value = CliUtils.getPrintString(currentValue, property);
        ColorScheme scheme = getColorScheme(property);
        log.debug("currentValue: {} , diff: {}, colorScheme: {}", currentValue, diff, getColorScheme(property));
        return CliUtils.getColoredString(value + getArrow(diff, scheme), getColor(property, diff, currentValue));
    }

    public static String geTrendString(LinkedHashMap<String, TransactionSummary> bucket,
                                       TransactionSummaryProperty property) {
        List<String> labels = new ArrayList<>();
        double lastValue = 0;
        int count = 0;
        for (Map.Entry<String, TransactionSummary> entry: bucket.entrySet()) {
            StringBuilder result = new StringBuilder();
            double currentValue = getNumericalSummaryPropertyValue(entry.getValue(), property);

            //append the name
            result.append(entry.getKey()).append(SPACE);
            //append the value with color, if increasing in green, stagnant in yellow and decreasing in red
            String value = getSummaryPropertyValue(entry.getValue(), property);
            double diff = count == 0? 0: currentValue - lastValue;
            ColorScheme scheme = getColorScheme(property);
            result.append(CliUtils.getColoredString(value + getArrow(diff, scheme), getColor(property, diff, currentValue)));

            labels.add(result.toString());
            lastValue = currentValue;
            count++;
        }
        return generateRow(labels, 35);
    }

    public static TransactionType getTransactionType(TransactionSummaryUtil.TransactionSummaryProperty property) {
        if (property.toString().contains("DEBIT")) {
            return TransactionType.DEBIT;
        }
        if (property.toString().contains("CREDIT")) {
            return TransactionType.CREDIT;
        }
        return TransactionType.ALL;
    }

    public static String getBucketStat(LinkedHashMap<String, TransactionSummary> bucket,
                                                   TransactionSummaryProperty property) {
        StringBuilder result = new StringBuilder(geTrendString(bucket,property));
        List<String> statsList = new ArrayList<>();
        int size = bucket.size();
        double[] data = new double[size];
        int count = 0;
        double sum = 0;
        for (TransactionSummary summary: bucket.values()) {
            data[count] = getNumericalSummaryPropertyValue(summary, property);
            sum += data[count];
            count++;
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(data);
        if (!property.toString().contains("PERCENTAGE")) {
            statsList.add(CliUtils.getColoredString("Total: ", CliUtils.ANSI_YELLOW) + Math.abs(Math.round(sum * 100.0) / 100.0));
        }
        statsList.add(CliUtils.getColoredString("Mean: ", CliUtils.ANSI_YELLOW) + Math.abs(Math.round((stats.getMean()) * 100.0) / 100.0));
        statsList.add(CliUtils.getColoredString("SD: ", CliUtils.ANSI_YELLOW) + Math.abs(Math.round((stats.getStandardDeviation()) * 100.0) / 100.0));
        return generateRow(statsList, 30);
    }



    public static String getSummaryPropertyValue(TransactionSummary summary, TransactionSummaryProperty property) {
        switch (property) {
            case CREDIT:
                return getAmountString(summary.getCredit());

            case DEBIT:
                return getAmountString(summary.getDebit());

            case TOTAL:
                return getAmountString(summary.getTotal());

            case CREDIT_COUNT:
                return String.valueOf(summary.getCreditCount());

            case DEBIT_COUNT:
                return String.valueOf(summary.getDebitCount());

            case TOTAL_COUNT:
                return String.valueOf(summary.getTotalCount());

            case CREDIT_PERCENTAGE:
                return getPercentageString(summary.getCreditPercentage());

            case DEBIT_PERCENTAGE:
                return getPercentageString(summary.getDebitPercentage());

            case TOTAL_PERCENTAGE:
                return getPercentageString(summary.getTotalPercentage());

            case CREDIT_COUNT_PERCENTAGE:
                return getPercentageString(summary.getCreditCountPercentage());

            case DEBIT_COUNT_PERCENTAGE:
                return getPercentageString(summary.getDebitCountPercentage());

            case TOTAL_COUNT_PERCENTAGE:
                return getPercentageString(summary.getTotalCountPercentage());
        }
        return null;
    }

    public static double getNumericalSummaryPropertyValue(TransactionSummary summary, TransactionSummaryProperty property) {
        switch (property) {
            case CREDIT:
                return summary.getCredit();

            case DEBIT:
                return summary.getDebit();

            case TOTAL:
                return summary.getTotal();

            case CREDIT_COUNT:
                return summary.getCreditCount();

            case DEBIT_COUNT:
                return summary.getDebitCount();

            case TOTAL_COUNT:
                return summary.getTotalCount();

            case CREDIT_PERCENTAGE:
                return summary.getCreditPercentage();

            case DEBIT_PERCENTAGE:
                return summary.getDebitPercentage();

            case TOTAL_PERCENTAGE:
                return summary.getTotalPercentage();

            case CREDIT_COUNT_PERCENTAGE:
                return summary.getCreditCountPercentage();

            case DEBIT_COUNT_PERCENTAGE:
                return summary.getDebitCountPercentage();

            case TOTAL_COUNT_PERCENTAGE:
                return summary.getTotalCountPercentage();
        }
        return 0.0;
    }

    private static String getAmountString(double amount) {
        String sign = amount<0? "-" : "";
        return sign + "$" + Math.abs(Math.round((amount) * 100.0) / 100.0);
    }

    private static String getPercentageString(double percentage) {
        return percentage + "%";
    }


    public enum TransactionSummaryProperty {
        CREDIT, DEBIT, TOTAL,
        CREDIT_COUNT, DEBIT_COUNT, TOTAL_COUNT,

        CREDIT_PERCENTAGE, DEBIT_PERCENTAGE, TOTAL_PERCENTAGE,
        CREDIT_COUNT_PERCENTAGE, DEBIT_COUNT_PERCENTAGE, TOTAL_COUNT_PERCENTAGE,
    }

    private static String generateRow(List<String> cells, int columnWidth) {
        StringBuilder row = new StringBuilder();
        for (String cell : cells) {
            row.append(" " + VERTICAL_LINE + " ");
            row.append(String.format("%-" + columnWidth + "s", cell));
            row.append(" ");
        }
        row.append(" " + VERTICAL_LINE);
        return row.toString();
    }

    public enum ColorScheme {
        SIGN_BASED,
        TREND_BASED,
        INVERTED_TREND_BASED,
        NEUTRAL
    }

    public static ColorScheme getColorScheme(TransactionSummaryProperty property) {
        if (property.toString().contains("DEBIT")) {
            return ColorScheme.INVERTED_TREND_BASED;
        }
        if (property.toString().contains("CREDIT")) {
            return ColorScheme.TREND_BASED;
        }
        if (property.toString().contains("COUNT")) {
            return ColorScheme.NEUTRAL;
        }
        return ColorScheme.SIGN_BASED;

    }


    public static String getColor(TransactionSummaryProperty property, double difference, double value) {
        ColorScheme colorScheme = getColorScheme(property);
        if (colorScheme.equals(ColorScheme.NEUTRAL)) {
            return "";
        }
        if (colorScheme.equals(ColorScheme.SIGN_BASED)) {
            if (value == 0)
                return CliUtils.ANSI_YELLOW;
            if (value < 0)
                return CliUtils.ANSI_RED;
            return CliUtils.ANSI_GREEN;
        }
        if (colorScheme.equals(ColorScheme.TREND_BASED)) {
            if (difference == 0) {
                return CliUtils.ANSI_YELLOW;
            } else if (difference < 0) {
                return CliUtils.ANSI_RED;
            }
            return CliUtils.ANSI_GREEN;
        }
        if (difference == 0) {
            return CliUtils.ANSI_YELLOW;
        }
        if (difference > 0) {
            return CliUtils.ANSI_RED;
        }
        return CliUtils.ANSI_GREEN;
    }

    public static char getArrow(double diff, ColorScheme scheme) {
        if (!scheme.toString().contains("TREND") || diff == 0) {
            return ' ';
        }
        return diff < 0 ? DOWN_ARROW : UP_ARROW;
    }

    public static double getSummaryStat(List<TransactionSummary> summaries,TransactionSummaryProperty property,  int type) {
        double[] data = new double[summaries.size()];
        for (int i = 0; i<summaries.size(); i++) {
            data[i] = getNumericalSummaryPropertyValue(summaries.get(i), property);
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(data);
        switch (type) {
            case MEAN:
                return stats.getMean();
            case MAX:
                return stats.getMax();
            case SUM:
                return stats.getSum();
            case MIN:
                return stats.getMin();
            case SD:
                return stats.getStandardDeviation();
        }
        return 0;
    }

    public static DescriptiveStatistics getSummaryStat(List<TransactionSummary> summaries, TransactionSummaryProperty property) {
        double[] data = new double[summaries.size()];
        for (int i = 0; i<summaries.size(); i++) {
            data[i] = getNumericalSummaryPropertyValue(summaries.get(i), property);
        }
        return new DescriptiveStatistics(data);
    }

    private static String generateTreeName(String label, String stats, int indentation) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("%-" + indentation + "s", label).replace(' ', '-'));
        result.append("+");
        result.append(stats);
        return result.toString();
    }


}
