package util.cli;

import com.sun.source.tree.Tree;
import model.*;
import util.CsvTableUtil;
import util.TransactionSummaryUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CliUtils {
    private static final char VERTICAL_LINE = '|';
    private static final String PERCENTAGE_PHRASE = "PERCENTAGE";
    private static final String COUNT_PHRASE = "COUNT";
    // ANSI escape codes for text color
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_MAGENTA = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static final String ANSI_RED_BACKGROUND = "\u001B[41M";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_MAGENTA_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";

    /**
     * Parses date string smart.
     * @param dateString String entered for date by the CLI user
     * @return Date object after parsing
     */
    public static Date parseDate(String dateString) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM/yy");

        try {
            // Attempt to parse as mm/dd/yyyy
            Date date1 = sdf1.parse(dateString);
            if (date1 != null) {
                return date1;
            }
        } catch (ParseException e1) {
            try {
                // Attempt to parse as mm/yyyy
                Date date2 = sdf2.parse(dateString);
                if (date2 != null) {
                    return date2;
                }
            } catch (ParseException e2) {
                // Unable to parse in either format
                return null;
            }
        }

        return null;
    }

    public static Date parseEndDate(String dateString) {
        if (dateString.length() > 5) {
            return parseDate(dateString);
        }
        return getLastDayOfMonth(parseDate(dateString));
    }

    public static List<Date> getDateRangeOfMonth(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
        Date startDate = sdf.parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Set the day of the month to the last day of the month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();
        return List.of(startDate, endDate);

    }

    public static String getCliGrid(List<String> strings, int columns) {
        if (strings == null || strings.isEmpty() || columns <= 0) {
            return "";
        }

        int maxStringLength = 0;

        // Find the length of the longest string
        for (String str : strings) {
            maxStringLength = Math.max(maxStringLength, str.length());
        }

        // Calculate the column width based on the longest string
        int columnWidth = maxStringLength + 2; // Add some spacing between columns

        StringBuilder result = new StringBuilder();

        int rows = (int) Math.ceil((double) strings.size() / columns);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row + col * rows;
                if (index < strings.size()) {
                    String str = strings.get(index);
                    result.append(String.format("%-" + columnWidth + "s", str));

                    if (col < columns - 1) {
                        // Add some spacing between columns
                        result.append("  ");
                    }
                }
            }
            result.append("\n");
        }

        return result.toString();
    }

    public static String getCliGridWithNumbers(List<String> strings, int columns) {
        if (strings == null || strings.isEmpty() || columns <= 0) {
            return "";
        }
        List<String> result = new ArrayList<>();
        for (int i=0; i<strings.size(); i++) {
            result.add(String.format("%d. %s", i+1, strings.get(i)));
        }
        return getCliGrid(result, columns);
    }

    private static Date getLastDayOfMonth(Date inputDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inputDate);

        // Set the day of the month to the last day of the month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Reset the time fields to midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static String toCommaDelimitedString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s: list) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    public static List<String> getListFromCommaSeparatedString(String input) {
        String[] arr = input.trim().split(",");
        List<String> result = new ArrayList<>();
        for (String s: arr) {
            if (!s.isBlank()) {
                result.add(s);
            }
        }
        return result;
    }

    private static void appendTransactionSummaryTree(TreeNode node, StringBuilder result, String prefix, boolean isLast) {
        result.append(ANSI_YELLOW).append(prefix).append("+---").append(ANSI_RESET);
        /*
        result.append(ANSI_GREEN).append(getSummaryString(node.getSummary(), transactionType)).append(ANSI_RESET);
        result.append(ANSI_BLUE).append(node.getName()).append(ANSI_RESET);
        result.append("\n");
         */
        List<TreeNode> children = new ArrayList<>(node.getChildren());
        //children.sort(Comparator.comparing(TreeNode::getName));
        int count = children.size();
        for (TreeNode childNode: children) {
            count--;
            boolean isLastChild = count == 0;
            String childPrefix = prefix + (isLast ? "    " : "|   ");
            appendTransactionSummaryTree(childNode, result, childPrefix, isLastChild);
        }

    }

    public static String getCliTransactionSummaryTree(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        printTreeRecursive(root,sb,"", true);
        return sb.toString();
    }

    public static String getCliTreeWithLevelColoring(TreeNode root, int level, List<String> colors) {
        StringBuilder stringBuilder = new StringBuilder();
        printTreeRecursiveWithLevelColoring(root, stringBuilder, "", colors, level, true);
        return stringBuilder.toString();
    }

    public static String getDateString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");
        return simpleDateFormat.format(date);
    }

    public static String getColoredString(String input, String color){
        return color + input + ANSI_RESET;
    }

    public static String getParameterDefaultValueTable(List<TaskParameter> parameters) throws Exception {
        CsvTable table = new CsvTable();
        List<String> headers = new ArrayList<>();
        headers.add("Parameter Name");
        headers.add("Default Value");
        table.setHeaders(headers);
        for (TaskParameter parameter: parameters) {
            List<String> row = new ArrayList<>();
            row.add(parameter.getName());
            row.add(parameter.getDefaultValue());
            table.addRow(row);
        }
        return CsvTableUtil.getCliTable(table);
    }

    private static void printTreeRecursive(TreeNode node, StringBuilder result, String prefix, boolean isLast) {
        result.append(prefix);
        result.append("+---");

        result.append(node.getName());
        result.append("\n");

        List<TreeNode> children = node.getChildren();
        children.sort((c1, c2) -> (int) (c2.getSortingValue() - c1.getSortingValue()));
        for (int i = 0; i < children.size(); i++) {
            TreeNode child = children.get(i);
            boolean isLastChild = (i == children.size() - 1);
            String childPrefix = prefix + (isLast ? "    " : "|   ");
            printTreeRecursive(child, result, childPrefix, isLastChild);
        }
    }

    private static void printTreeRecursiveWithLevelColoring(TreeNode node, StringBuilder result, String prefix,
                                                            List<String> colors, int level, boolean isLast) {
        if (level == 0)
            return;
        result.append(prefix);
        result.append("+---");

        result.append(CliUtils.getColoredString(node.getName(), colors.get(level-1)));
        result.append("\n");

        List<TreeNode> children = node.getChildren();
        children.sort((c1, c2) -> (int) (c2.getSortingValue() - c1.getSortingValue()));
        for (int i = 0; i < children.size(); i++) {
            TreeNode child = children.get(i);
            boolean isLastChild = (i == children.size() - 1);
            String childPrefix = prefix + (isLast ? "    " : "|   ");
            printTreeRecursiveWithLevelColoring(child, result, childPrefix, colors, level-1,  isLastChild);
        }
    }

    private static String generateRow(List<String> cells, int[] columnWidths) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            row.append(" " + VERTICAL_LINE + " ");
            String cell = cells.get(i);
            row.append(String.format("%-" + columnWidths[i] + "s", cell));
            row.append(" ");
        }
        row.append(" " + VERTICAL_LINE);
        return row.toString();
    }

    public static String getPrintString(double labelValue, TransactionSummaryUtil.TransactionSummaryProperty property) {
        if (property.toString().contains("PERCENTAGE"))
            return  (double)Math.round(labelValue*100)/100 + "%";

        if (property.toString().contains("COUNT")) {
            return  String.valueOf(Math.round(labelValue));
        }
        String sign = labelValue < 0 ? "-" : " ";
        return sign + "$" + (double)Math.abs(Math.round(labelValue*100)/100);
    }
}
