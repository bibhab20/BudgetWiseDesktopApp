package controller;

import lombok.extern.slf4j.Slf4j;
import util.AppConfig;
import util.CsvWriter;

import java.util.Date;

@Slf4j
public class CategoryController {
    private static final String OUTPUT_FILE_PATH = "vendor-types.output.path";
    AppConfig config;
    CsvWriter writer;

    public void writeCategoryEnrichedTransactionsToCsv(Date afterDate, Date beforeDate) {

    }

}
