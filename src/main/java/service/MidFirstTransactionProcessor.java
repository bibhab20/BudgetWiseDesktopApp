package service;

import model.*;
import lombok.extern.slf4j.Slf4j;

import util.AppConfig;
import util.CsvReader;
import util.CsvTableUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static model.TransactionType.CREDIT;
import static model.TransactionType.DEBIT;

@Slf4j
public class MidFirstTransactionProcessor extends TransactionProcessor {

    private static final String FIRST_HEADER = "<Date>";
    private static final String SECOND_HEADER = "<CheckNum>";
    private static final String THIRD_HEADER = "<Description>";
    private static final String FOURTH_HEADER = "<Withdrawal Amount>";
    private static final String FIFTH_HEADER = "<Deposit Amount>";
    private static final String SIXTH_HEADER = "<Additional Info>";
    private final static String DATE_FORMAT = "MM/dd/yyyy";
    private final static String FOLDER_PATH_KEY = "midFirst.folder.path";
    private final static String DISCOVER_PAYMENT_TOKEN = "DISCOVER E-PAYMENT";
    private final static String INCOME_TOKEN = "VMware";
    private final static String CHANNEL_TOKEN = "Channel";
    private final List<String> validHeaders = new ArrayList<>(Arrays.asList(FIRST_HEADER, SECOND_HEADER, THIRD_HEADER, FOURTH_HEADER, FIFTH_HEADER, SIXTH_HEADER));
    private final AppConfig config;

    public MidFirstTransactionProcessor(AppConfig config) {
        this.config = config;
    }

    public List<Transaction> readAndProcessRecords() throws Exception {
        log.info("Starting reading Midfirst transaction files");
        List<CsvTable> tables = CsvReader.readCSVFromFolder(config.getProperties().getProperty(FOLDER_PATH_KEY));

        log.info("{} table created for midFirst.",tables.size());

        for (CsvTable table: tables) {
            if (! table.getHeaders().equals(validHeaders)) {
                log.error("headers do not match headers: {}, valid headers: {}",table.getHeaders(), validHeaders);
                throw new Exception("headers do not match");
            }
        }

        //check for duplicate records
        if (CsvTableUtil.isDuplicateFound(tables)) {
            log.error("Duplicate found in records");
            throw new Exception("Duplicate found in csv records");
        }

        CsvTableUtil.removeNullValues(tables);

        //create transaction records
        List<MidFirstTransactionRecord> records = new ArrayList<>();
        for (CsvTable table: tables) {
            List<List<String>> rows = table.getRows();
            for (List<String> row: rows) {
                MidFirstTransactionRecord transactionRecord = new MidFirstTransactionRecord();
                transactionRecord.setDate(convertStringToDate(row.get(0)));
                transactionRecord.setDescription(row.get(2));
                transactionRecord.setWithdrawalAmount(row.get(3).equals("NONE") ? 0 : Double.parseDouble(row.get(3)));
                transactionRecord.setDepositAmount(row.get(4).equals("NONE") ? 0 : Double.parseDouble(row.get(4)));
                transactionRecord.setAdditionalInfo(row.get(5));
                records.add(transactionRecord);
            }
        }

        return convertToCommonTransactions(records);

    }

    private Date convertStringToDate(String dateString){
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private List<Transaction> convertToCommonTransactions(List<MidFirstTransactionRecord> records) throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        for (MidFirstTransactionRecord record: records) {
            if (! record.getAdditionalInfo().contains(DISCOVER_PAYMENT_TOKEN)) {
                double amount = getAmount(record);
                TransactionType type = amount <0 ? DEBIT : CREDIT;
                Transaction transaction = new Transaction.Builder().setTransactionDate(record.getDate())
                        .setAmount(Math.abs(amount))
                        .setTransactionType(type)
                        .setDescription(record.getAdditionalInfo())
                        .setSource(TransactionSource.MIDFIRST)
                        .addDetails(CHANNEL_TOKEN, record.getDescription())
                        .build();
                if (record.getAdditionalInfo().contains(INCOME_TOKEN)) {
                    transaction.addTag(Tag.INCOME);
                }
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    private Double getAmount(MidFirstTransactionRecord record) throws Exception {
        if (record.getWithdrawalAmount() == 0){
            if (record.getDepositAmount() == 0) {
                log.error("Both deposit and withdrawal amount is zero");
                return 0.0;
            }
            else {
                return record.getDepositAmount();
            }
        }
        else {
            if (record.getDepositAmount() != 0) {
                log.error("Both deposit and withdrawal amount non zero");
                throw new Exception("Both deposit and withdrawal amount non zero");
            }
            else {
                return record.getWithdrawalAmount();
            }
        }
    }

}
