package service;

import model.*;
import lombok.extern.slf4j.Slf4j;
import util.AppConfig;
import util.CsvReader;
import util.CsvTableUtil;

import java.util.*;

@Slf4j
public class DiscoverTransactionProcessor extends TransactionProcessor{
    private static final String FIRST_HEADER = "Trans. Date";
    private static final String SECOND_HEADER = "Post Date";
    private static final String THIRD_HEADER = "Description";
    private static final String FOURTH_HEADER = "Amount";
    private static final String FIFTH_HEADER = "Category";
    private final static String DATE_FORMAT = "MM/dd/yy";
    private final static String FOLDER_PATH_KEY = "discover.folder.path";
    private final static String PAYMENT_TOKEN = "INTERNET PAYMENT";
    private final static String SUGGESTED_CATEGORY_KEY = "discover.suggested.category.key";
    private final static String POST_DATE_KEY = "Post Date";

    private AppConfig config;

    List<String> validHeaders = new ArrayList<>(Arrays.asList(FIRST_HEADER, SECOND_HEADER, THIRD_HEADER, FOURTH_HEADER, FIFTH_HEADER));

    public DiscoverTransactionProcessor(AppConfig config) {
        this.config = config;
    }

    public List<Transaction> readAndProcessRecords() throws Exception {
        log.info("Starting reading Discover transaction files");
        List<CsvTable> tables = CsvReader.readCSVFromFolder(config.getProperties().getProperty(FOLDER_PATH_KEY));
        //validate headers
        for (CsvTable table: tables) {
            if (! table.getHeaders().equals(validHeaders)) {
                log.error("headers does not match. Expected headers: {}, actual headers: {}", validHeaders, table.getHeaders());
                throw new Exception("headers does not match");
            }
        }

        //check for duplicate records
        if (CsvTableUtil.isDuplicateFound(tables)) {
            log.error("Duplicate found in records");
        }
        CsvTableUtil.removeNullValues(tables);
        //create transaction records
        List<DiscoverTransactionRecord> records = new ArrayList<>();
        for (CsvTable table: tables) {
            List<List<String>> rows = table.getRows();
            log.info("No of rows in table: {}", rows.size());
            for (List<String> row: rows) {
                DiscoverTransactionRecord transactionRecord = new DiscoverTransactionRecord();
                transactionRecord.setTransactionDate(convertStringToDate(row.get(0), DATE_FORMAT));
                transactionRecord.setPostDate(convertStringToDate(row.get(1), DATE_FORMAT));
                transactionRecord.setDescription(row.get(2));
                transactionRecord.setAmount(row.get(3).equals("NONE") ? 0.0 : Double.parseDouble(row.get(3)));
                transactionRecord.setCategory(row.get(4));
                records.add(transactionRecord);
            }
        }
        log.info("No of rows in records: {}", records.size());
        return convertToCommonTransactions(records);

    }

    private List<Transaction> convertToCommonTransactions(List<DiscoverTransactionRecord> records) {
        List<Transaction> transactions = new ArrayList<>();
        for (DiscoverTransactionRecord record: records) {
            if (! record.getDescription().contains(PAYMENT_TOKEN)) {
                Transaction transaction = new Transaction.Builder().setTransactionDate(record.getTransactionDate())
                        .setTransactionType(record.getAmount()< 0.0 ? TransactionType.CREDIT : TransactionType.DEBIT)
                        .setAmount(Math.abs(record.getAmount()))
                        .setDescription(record.getDescription())
                        .setSource(TransactionSource.DISCOVER)
                        .addDetails(config.getProperties().getProperty(SUGGESTED_CATEGORY_KEY), record.getCategory()).build();

                transactions.add(transaction);
            }
            else {
                log.info("Internet payment record discarded record description {}",record.getDescription());
            }
        }
        return transactions;
    }
}
