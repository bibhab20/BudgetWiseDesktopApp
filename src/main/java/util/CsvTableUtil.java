package util;

import model.CsvTable;
import lombok.extern.slf4j.Slf4j;
import util.cli.CliUtils;
import util.filter.AdvanceFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CsvTableUtil {
    private static final String DOUBLE_QUOTE_STRING = "\"\"";
    private static final char HORIZONTAL_LINE = '-';
    private static final char VERTICAL_LINE = '|';
    private static final char CORNER = '+';


    private static Map<List<String>, Integer> getDuplicationCount(List<CsvTable> tables) {
        Map<List<String>, Integer> duplicates = new HashMap<>();
        for (CsvTable table: tables) {
            for (List<String> row : table.getRows()){
                duplicates.put(row, duplicates.getOrDefault(row, -1)+1);
            }
        }
        return duplicates;
    }

    public static boolean isDuplicateFound(List<CsvTable> tables) {
        Map<List<String>, Integer> map = getDuplicationCount(tables);
        for (Map.Entry<List<String>, Integer> entry: map.entrySet()) {
            if (entry.getValue() != 0) {
                log.error("Duplicate found of row; {}, count: {}", entry.getKey(), entry.getValue());
                return true;
            }
        }
        return false;
    }


    public static void removeNullValues(List<CsvTable> tables) {
        log.info("Starting removing null values in the cells and setting it to NONE");
        for (CsvTable table: tables) {
            for (List<String> row: table.getRows()) {
                for (int i=0; i<row.size(); i++) {
                    if (row.get(i).equals(DOUBLE_QUOTE_STRING) || row.get(i).isEmpty()) {
                        row.set(i, "NONE");
                    }
                }
            }
        }
    }

    public static String getCliTable(CsvTable table) {
        int numColumns = table.getHeaders().size();
        int[] columnWidths = new int[numColumns];
        if (table.getRows().isEmpty()) {
            return "Empty Table: This table has no data";
        }
        // Calculate the column widths based on header and row values
        for (int i = 0; i < numColumns; i++) {
            int maxWidth = removeColorCodes(table.getHeaders().get(i)).length();
            for (List<String> row : table.getRows()) {
                if (i < row.size() && row.get(i) != null) {
                    int cellWidth = removeColorCodes(row.get(i)).length();
                    maxWidth = Math.max(maxWidth, cellWidth);
                }
            }
            columnWidths[i] = maxWidth;
        }

        StringBuilder cliTable = new StringBuilder();

        // Generate the table headers
        cliTable.append(generateSeparator(columnWidths)).append("\n");
        cliTable.append(generateRow(table.getHeaders(), columnWidths, table.getMetaData().getHeaderColor()));
        cliTable.append(System.lineSeparator());

        // Generate the table separator
        cliTable.append(generateSeparator(columnWidths));
        cliTable.append(System.lineSeparator());

        // Generate the table rows
        int index = 0;
        for (List<String> row : table.getRows()) {
            String highlightColor = table.getMetaData().getHighlightLineIndices().contains(index) ?
                    table.getMetaData().getHighlightColor() : "";
            cliTable.append(generateRow(row, columnWidths, highlightColor));
            cliTable.append(System.lineSeparator());
            index++;
        }
        cliTable.append(generateSeparator(columnWidths));
        return cliTable.toString();
    }

    private static String generateSeparator(int[] columnWidths) {
        StringBuilder separator = new StringBuilder();
        separator.append(" ").append(CORNER);
        separator.append("-");
        for (int i = 0; i < columnWidths.length; i++) {
            if (i > 0) {
                separator.append(" " + CORNER + " ");
            }
            int visibleLength = columnWidths[i];
            separator.append(String.valueOf(HORIZONTAL_LINE).repeat(visibleLength));
        }
        if (columnWidths.length > 0) {
            separator.append(" " + CORNER);
        }
        return separator.toString();
    }



    private static String removeColorCodes(String input) {
        // Use a regular expression to remove ANSI color codes
        String colorCodePattern = "\\u001B\\[[;\\d]*[ -/]*[@-~]";
        Pattern pattern = Pattern.compile(colorCodePattern);
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }

    private static String generateRow(List<String> cells, int[] columnWidths, String highLightColor) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            row.append(" " + VERTICAL_LINE + " ");
            String cell = cells.get(i);
            int padding = columnWidths[i] - getVisibleLength(cell);
            row.append(cell);
            row.append(" ".repeat(padding));
        }
        row.append(" " + VERTICAL_LINE);
        return (highLightColor == null || highLightColor.isBlank()) ? row.toString() : CliUtils.getColoredString(row.toString(), highLightColor);
    }

    // Helper method to get the visible length of a string (excluding ANSI escape codes)
    private static int getVisibleLength(String input) {
        String colorCodePattern = "\\u001B\\[[;\\d]*[ -/]*[@-~]";
        Pattern pattern = Pattern.compile(colorCodePattern);
        Matcher matcher = pattern.matcher(input);
        String stringWithoutColor = matcher.replaceAll("");
        return stringWithoutColor.length();
    }


}
