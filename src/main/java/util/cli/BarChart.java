package util.cli;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import util.TransactionSummaryUtil;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
public class BarChart {
    private static final int MARKING_COUNT = 10;
    private static final int DEFAULT_X_AXIS_START_VALUE = 0;
    private static final int DEFAULT_X_AXIS_START = 0;
    private static final int DEFAULT_X_AXIS_END = 500;

    private static final String PERCENTAGE_PHRASE = "PERCENTAGE";
    private static final String COUNT_PHRASE = "COUNT";



    private static final String SPACE = " ";
    //steps
    int xAxisStart, xAxisEnd, markingCount, markingWidth, yAxisLabelWidth;

    //values
    int xAxisStartValue, xAxisEndValue, markingWidthValue;

    //ranges
    int valueRange, stepRange;

    //inputs
    List<String> labels;
    List<Double> data;
    TransactionSummaryUtil.TransactionSummaryProperty property;

    public BarChart(List<String> labels, List<Double> data, TransactionSummaryUtil.TransactionSummaryProperty property) throws Exception {
        this.labels = labels;
        this.data = data;
        this.property = property;
        loadDefaultSettings();
    }

    public String draw() throws Exception {
        StringBuilder chart = new StringBuilder();
        for (int i=0; i<data.size(); i++) {
            chart.append(getLabelAndBar(labels.get(i), getSteps(data.get(i)))).append(getPrintLabel(data.get(i), property)).append("\n");
        }
        chart.append(getXAxisLabel(generateMarkingLabels(markingCount, markingWidthValue, xAxisStartValue, property),
                markingWidth, yAxisLabelWidth));
        return chart.toString();

    }

    private int getSteps(double value) {
        double stepSize = (double) stepRange /(double) valueRange;
        return (int) Math.ceil(value * stepSize);
    }

    private void loadDefaultSettings() throws Exception {
        if (data.size() != labels.size()) {
            throw new Exception("Data and label size must be equal");
        }
        double[] stream = new double[data.size()];
        double[] labelLengths = new double[data.size()];
        for (int i=0; i<data.size(); i++) {
            stream[i] = data.get(i);
        }

        for (int i=0; i<labels.size(); i++) {
            labelLengths[i] = labels.get(i).length();
        }
        DescriptiveStatistics dataStats = new DescriptiveStatistics(stream);
        DescriptiveStatistics labelLengthStat = new DescriptiveStatistics(labelLengths);

        //x axis
        xAxisStart = DEFAULT_X_AXIS_START;
        xAxisEnd = DEFAULT_X_AXIS_END;
        stepRange = xAxisEnd - xAxisStart;
        xAxisStartValue = DEFAULT_X_AXIS_START_VALUE;
        xAxisEndValue = getXAxisEndValue(dataStats.getMax(), property);
        valueRange = xAxisEndValue - xAxisStartValue;
        markingCount = MARKING_COUNT;
        markingWidth = stepRange / markingCount;
        markingWidthValue = valueRange / markingCount;
        yAxisLabelWidth = (int) labelLengthStat.getMax();
        printSettings();
    }

    private void printSettings() {
        log.debug("xAxisStart: {}", xAxisStart);
        log.debug("xAxisEnd: {}", xAxisEnd);
        log.debug("stepRange: {}", stepRange);
        log.debug("xAxisStartValue: {}", xAxisStartValue);
        log.debug("xAxisEndValue: {}", xAxisEndValue);
        log.debug("valueRange: {}", valueRange);
        log.debug("markingCount: {}", markingCount);
        log.debug("markingWidth: {}", markingWidth);
        log.debug("markingWidthValue: {}", markingWidthValue);
        log.debug("yAxisLabelWidth: {}", yAxisLabelWidth);
        log.debug("data: {}", data);
        log.debug("labels: {}", labels);

    }

    private int getXAxisEndValue(double maxVal, TransactionSummaryUtil.TransactionSummaryProperty property) {
        if (property.toString().contains(PERCENTAGE_PHRASE)) {
            return 100;
        }
        int val =(int) Math.ceil(maxVal);
        int res = 1;
        boolean nonZeros = false;
        while(val >= 10) {

            nonZeros = nonZeros || val%10 != 0;
            val = val/10;
            res = res * 10;

        }

        val = nonZeros? val +1 : val;
        res = val * res;
        res = Math.max(10, res);
        return res;

    }

    private String getLabelAndBar(String label, int steps) {
        log.debug("Step size: {}", steps);
        String labelString = String.format("%-" + yAxisLabelWidth + "s |", label);
        String bar = "█".repeat(steps);
        return labelString + CliUtils.getColoredString(bar, CliUtils.ANSI_BLUE);
    }

    private String getXAxisLabel(List<String> markingLabels, int markingWidth, int leftPadding) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ".repeat(leftPadding)).append(" |");
        markingWidth --;
        for (String label: markingLabels) {
            stringBuilder.append(String.format("%-" + markingWidth + "s", SPACE + label));
            stringBuilder.append("|");
        }
        return stringBuilder.toString();
    }

    private List<String> generateMarkingLabels(int markingCount, int markingWidthValue, int xAxisStartValue,
                                               TransactionSummaryUtil.TransactionSummaryProperty property) throws Exception {
        List<String> markingLabels = new ArrayList<>();
        int currentValue = xAxisStartValue;
        for (int i=0; i <= markingCount; i++) {
            markingLabels.add(getPrintLabel(currentValue, property));
            currentValue += markingWidthValue;
        }
        return markingLabels;
    }

    private String getPrintLabel(double labelValue, TransactionSummaryUtil.TransactionSummaryProperty property) throws Exception {
        if (property.toString().contains("TOTAL")) {
            throw new Exception("Property can to be total, as it might contain negative data points. This chart does not support negative data points yet");
        }

        if (property.toString().contains("PERCENTAGE"))
            return  (double) Math.round(labelValue*100)/100 + "%";

        if (property.toString().contains("COUNT")) {
            return  String.valueOf(Math.round(labelValue));
        }
        String sign = labelValue < 0 ? "-" : "";
        return sign + "$" + (double) Math.round(Math.abs(labelValue*100))/100;
    }

}
