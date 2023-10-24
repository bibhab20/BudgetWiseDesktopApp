package controller;

import lombok.extern.slf4j.Slf4j;
import model.CsvTable;
import model.Transaction;
import model.TransactionRepository;
import util.AppConfig;
import util.CsvWriter;
import util.TransactionUtil;
import util.enrichment.EnrichmentManager;
import util.filter.AdvanceFilter;
import util.filter.AfterDateFilter;
import util.filter.BeforeDateFilter;
import util.filter.ContainsVendorFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class CsvOutController {
    private TransactionRepository repository;
    AppConfig config;
    TransactionUtil transactionUtil;
    CsvWriter writer;
    List<String> additionalColumnKeys;
    private static final String SUGGESTED_CATEGORY_KEY = "discover.suggested.category.key";
    private static final String OUTPUT_FILE_PATH = "output.folder.path";

    public CsvOutController(TransactionRepository repository, AppConfig config, TransactionUtil transactionUtil,
                            CsvWriter writer) {
        this.repository = repository;
        this.config = config;
        this.transactionUtil = transactionUtil;
        this.writer = writer;
        additionalColumnKeys = new ArrayList<>(List.of(config.getProperties().getProperty(SUGGESTED_CATEGORY_KEY), "Vendor matches"));
    }

    public void getDebitsWithDateRange(Date beforeDate, Date afterDate) {
        List<Transaction> allTransactions = this.repository.getDebits();
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new BeforeDateFilter(beforeDate))
                .addFilter(new AfterDateFilter(afterDate))
                .build();

        List<Transaction> result = filter.filter(allTransactions);
        try {
            CsvTable table = transactionUtil.getCsv(result, additionalColumnKeys);
            writer.writeToFile(table, config.getProperties().getProperty(OUTPUT_FILE_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllWithDateRange(Date afterDate, Date beforeDate) {
        List<Transaction> allTransactions = repository.getAll();
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new BeforeDateFilter(beforeDate))
                .addFilter(new AfterDateFilter(afterDate))
                .build();

        List<Transaction> result = filter.filter(allTransactions);
        log.info("No of transactions filters out: {}", result.size());

        try {
            CsvTable table = transactionUtil.getCsv(result, additionalColumnKeys);
            writer.writeToFile(table, config.getProperties().getProperty(OUTPUT_FILE_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllTransactionAfterDate(Date afterDate) {
        List<Transaction> allTransactions = this.repository.getAll();
        AdvanceFilter filter = new AdvanceFilter.Builder()
                .addFilter(new AfterDateFilter(afterDate))
                .build();

        List<Transaction> result = filter.filter(allTransactions);
        log.info("No of transactions filters out: {}", result.size());
        try {
            CsvTable table = transactionUtil.getCsv(result, additionalColumnKeys);
            writer.writeToFile(table, config.getProperties().getProperty(OUTPUT_FILE_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllTransactions() {
        List<Transaction> allTransactions = repository.getAll();
        try {
            CsvTable table = transactionUtil.getCsv(allTransactions, additionalColumnKeys);
            writer.writeToFile(table, config.getProperties().getProperty(OUTPUT_FILE_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getTransactionsForFrontEnd(Date afterDate, Date beforeDate) {
        List<Transaction> allTransactions = repository.getAll();
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new BeforeDateFilter(beforeDate))
                .addFilter(new AfterDateFilter(afterDate))
                .build();

        List<Transaction> result = filter.filter(allTransactions);
        log.info("No of transactions filters out: {}", result.size());


        try {
            CsvTable table = transactionUtil.getCsvForFrontEnd(result);
            writer.writeToFile(table, config.getProperties().getProperty(OUTPUT_FILE_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTransactionsWithVendorEnrichmentFailures(Date afterDate, Date beforeDate) {
        List<Transaction> allTransactions = repository.getAll();
        List<Transaction> result = allTransactions;
        if (afterDate != null && beforeDate != null) {
            AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new BeforeDateFilter(beforeDate))
                    .addFilter(new AfterDateFilter(afterDate))
                    .build();
            result = filter.filter(allTransactions);
            log.info("No of transactions filters out in the date filter: {}", result.size());
        }


        AdvanceFilter vendorFilter = new AdvanceFilter.Builder().addFilter(new ContainsVendorFilter(List.of("UNKNOWN", "ERROR"))).build();
        result = vendorFilter.filter(result);
        log.info("No of transactions filters out in the vendor filter: {}", result.size());

        try {
            CsvTable table = transactionUtil.getCsv(result, additionalColumnKeys);
            writer.writeToFile(table, config.getProperties().getProperty(OUTPUT_FILE_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
