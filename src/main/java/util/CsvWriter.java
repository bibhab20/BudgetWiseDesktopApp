package util;

import lombok.extern.slf4j.Slf4j;
import model.CsvTable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvWriter {
    private AppConfig config;
    public CsvWriter(AppConfig config) {
        this.config = config;
    }

    public void writeToFile(CsvTable csvTable, String filePath) {
        List<List<String>> table = new ArrayList<>();
        table.add(csvTable.getHeaders());
        table.addAll(csvTable.getRows());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (List<String> row : table) {
                StringBuilder sb = new StringBuilder();
                for (String cell : row) {
                    sb.append(escapeSpecialCharacters(cell)).append(",");
                }
                // Remove the last comma and write the row to the file
                writer.write(sb.deleteCharAt(sb.length() - 1).toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to escape special characters if needed
    private String escapeSpecialCharacters(String input) {
        if (input == null)
            return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            input = input.replace("\"", "\"\"");
            input = "\"" + input + "\"";
        }
        return input;
    }
}

