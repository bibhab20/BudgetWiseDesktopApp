package util;

import model.CsvTable;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        log.info("Starting removing ");
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

        // Calculate the column widths based on header and row values
        for (int i = 0; i < numColumns; i++) {
            int maxWidth = table.getHeaders().get(i).length();
            for (List<String> row : table.getRows()) {
                if (i < row.size()) {
                    maxWidth = Math.max(maxWidth, row.get(i).length());
                }
            }
            columnWidths[i] = maxWidth;
        }

        StringBuilder cliTable = new StringBuilder();

        // Generate the table headers
        cliTable.append(generateRow(table.getHeaders(), columnWidths));
        cliTable.append(System.lineSeparator());

        // Generate the table separator
        cliTable.append(generateSeparator(columnWidths));
        cliTable.append(System.lineSeparator());

        // Generate the table rows
        for (List<String> row : table.getRows()) {
            cliTable.append(generateRow(row, columnWidths));
            cliTable.append(System.lineSeparator());
        }

        return cliTable.toString();
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

    private static String generateSeparator(int[] columnWidths) {
        StringBuilder separator = new StringBuilder();
        for (int width : columnWidths) {
            separator.append(" " + CORNER + " ");
            separator.append(String.valueOf(HORIZONTAL_LINE).repeat(width + 1));
        }
        separator.append(" " + CORNER);
        return separator.toString();
    }


}
