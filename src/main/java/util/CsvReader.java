package util;

import lombok.extern.slf4j.Slf4j;
import model.CsvTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads CSV files given by the path
 */
@Slf4j
public class CsvReader {

    public static List<CsvTable> readCSVFromFolder(String folderPath) throws Exception {
        File folder = new File(folderPath);
        List<CsvTable> tables = new ArrayList<>();
        if (!folder.isDirectory()) {
            log.error("folder path is not valid. It's not a directory {}", folder.getAbsolutePath());
            throw new Exception("folder path is not valid, it's not a directory");
        }
        log.info("Folder path is valid and it's a directory");

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            log.error("There is no files in the folder");
            throw new Exception("There are no files in the folder");
        }
        log.info("{} file(s) found in the folder", files.length);


        log.info("Starting reading csv files");
        for (File file : files) {
            if (file.getName().contains("csv")) {
                CsvTable table = readCSVFile(file.getAbsolutePath());
                tables.add(table);
                log.debug("Read file {} and {} transactions detected", file.getName(), table.getRows().size());
            }
            else {
                log.error("Non csv file found in the directory. File name: {}, path: {}",file.getName(), file.getAbsolutePath());
            }
        }

        return tables;
    }

    public static CsvTable readCSVFile(String filePath) throws Exception {
        List<List<String>> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                List<String> row = readCsvLine(line);
                data.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        CsvTable table = new CsvTable();
        table.setHeaders(data.remove(0));
        for (List<String> line: data){
            table.addRow(line);
        }
        return table;
    }


    public static List<String> readCsvLine(String line) {
        List<String> result = new ArrayList<>();

        boolean insideQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (c == ',' && !insideQuotes) {
                result.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }

        // Add the last field, if any
        if (currentField.length() > 0) {
            result.add(currentField.toString().trim());
        }

        return result;
    }

}
