package util;

import model.CsvTable;
import model.Transaction;
import lombok.extern.slf4j.Slf4j;
import model.TransactionType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TransactionUtil {
    private final static String ID_HEADER_KEY = "transaction.header.id";
    private final static String DATE_HEADER_KEY = "transaction.header.date";
    private final static String SOURCE_HEADER_KEY = "transaction.header.source";
    private final static String TYPE_HEADER_KEY = "transaction.header.type";
    private final static String AMOUNT_HEADER_KEY = "transaction.header.amount";
    private final static String DESCRIPTION_HEADER_KEY = "transaction.header.description";
    private final static String CATEGORY_HEADER_KEY = "transaction.header.category";
    private final static String VENDOR_HEADER_KEY = "transaction.header.vendor";

    //transaction summary constants
    private final static String SUMMARY_CATEGORY_HEADER = "transaction.summary.header.category";
    private final static String SUMMARY_COUNT_HEADER = "transaction.summary.header.count";
    private final static String SUMMARY_AMOUNT_HEADER = "transaction.summary.header.amount";
    private final static String SUMMARY_BALANCE_KEY = "transaction.summary.key.balance";

    private AppConfig config;

    public TransactionUtil(AppConfig config) {
        this.config = config;
    }

    public CsvTable getCsv(List<Transaction> transactions, List<String> additionalDetailsKeys) throws Exception {
        CsvTable table = new CsvTable();
        List<String> headers = new ArrayList<>();
        headers.add(config.getProperties().getProperty(ID_HEADER_KEY));
        headers.add(config.getProperties().getProperty(DATE_HEADER_KEY));
        headers.add(config.getProperties().getProperty(SOURCE_HEADER_KEY));
        headers.add(config.getProperties().getProperty(TYPE_HEADER_KEY));
        headers.add(config.getProperties().getProperty(AMOUNT_HEADER_KEY));
        headers.add(config.getProperties().getProperty(DESCRIPTION_HEADER_KEY));
        headers.add(config.getProperties().getProperty(VENDOR_HEADER_KEY));
        headers.addAll(additionalDetailsKeys);

        table.setHeaders(headers);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        for (Transaction transaction: transactions) {
            List<String> row = new ArrayList<>();
            row.add(transaction.getId().toString());
            row.add(sdf.format(transaction.getTransactionDate()));
            row.add(transaction.getSource().name());
            row.add(transaction.getType().name());
            row.add(String.valueOf(transaction.getAmount()));
            row.add(transaction.getDescription());
            row.add(transaction.getVendor());

            for (String key: additionalDetailsKeys) {
                row.add(transaction.getAdditionalDetails().getOrDefault(key, ""));
            }
            table.addRow(row);
        }

        return table;
    }

    public CsvTable getCsvForFrontEnd(List<Transaction> transactions) throws Exception {
        CsvTable table = new CsvTable();
        List<String> headers = new ArrayList<>();
        headers.add(config.getProperties().getProperty(ID_HEADER_KEY));
        headers.add(config.getProperties().getProperty(DATE_HEADER_KEY));
        headers.add(config.getProperties().getProperty(TYPE_HEADER_KEY));
        headers.add(config.getProperties().getProperty(AMOUNT_HEADER_KEY));
        headers.add(config.getProperties().getProperty(CATEGORY_HEADER_KEY));

        table.setHeaders(headers);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        for (Transaction transaction: transactions) {
            List<String> row = new ArrayList<>();
            row.add(transaction.getId().toString());
            row.add(sdf.format(transaction.getTransactionDate()));
            row.add(transaction.getType().name());
            row.add(String.valueOf(transaction.getAmount()));
            row.add(transaction.getCategory());
            table.addRow(row);
        }

        return table;
    }

    public CsvTable getSummaryTable(List<Transaction> transactions) throws Exception {
        CsvTable table = new CsvTable();
        List<String> headers = new ArrayList<>();
        headers.add(config.getProperties().getProperty(SUMMARY_CATEGORY_HEADER));
        headers.add(config.getProperties().getProperty(SUMMARY_COUNT_HEADER));
        headers.add(config.getProperties().getProperty(SUMMARY_AMOUNT_HEADER));
        table.setHeaders(headers);

        int transactionCount = transactions.size();
        int creditCount =  0, debitCount = 0;
        double balance = 0, creditAmount = 0, debitAmount = 0;
        for (Transaction transaction: transactions) {
            if (transaction.getType().equals(TransactionType.CREDIT)) {
                balance += transaction.getAmount();
                creditCount++;
                creditAmount += transaction.getAmount();
            } else {
                balance -= transaction.getAmount();
                debitCount++;
                debitAmount += transaction.getAmount();
            }
        }
        List<String> creditRow = new ArrayList<>(List.of("Credit", String.valueOf(creditCount), String.valueOf(creditAmount)));
        List<String> debitRow = new ArrayList<>(List.of("Debit", String.valueOf(debitCount), String.valueOf(debitAmount)));
        List<String> totalRow = new ArrayList<>(List.of(config.getProperties().getProperty(SUMMARY_BALANCE_KEY),
                String.valueOf(transactionCount), String.valueOf(balance)));

        table.addRow(creditRow);
        table.addRow(debitRow);
        table.addRow(totalRow);
        return table;
    }

}
