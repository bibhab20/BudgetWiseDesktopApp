package service.reader;

import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import service.supplier.ReaderConfigSupplier;
import util.AppConfig;


import java.util.ArrayList;
import java.util.List;


@Slf4j
public class ConfigurableReader extends TransactionReader{
    AppConfig config;
    ReaderConfigSupplier readerConfigSupplier;
    private static final String CSV_FOLDER_PATH_KEY = "csv";

    public ConfigurableReader(AppConfig appConfig, ReaderConfigSupplier readerConfigSupplier) {
        this.config = appConfig;
        this.readerConfigSupplier = readerConfigSupplier;
    }

    @Override
    public List<Transaction> readAndProcessRecords() throws Exception {
        return new ArrayList<>();
    }
}
