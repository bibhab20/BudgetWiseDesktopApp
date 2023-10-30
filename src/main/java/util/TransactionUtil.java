package util;

import model.CsvTable;
import model.Transaction;
import lombok.extern.slf4j.Slf4j;
import model.TransactionType;

import java.text.SimpleDateFormat;
import java.util.*;

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
    private final static String VENDOR_MATCHES_HEADER_KEY = "transaction.header.vendor.matches";
    private final static String VENDOR_TYPE_HEADER_KEY = "transaction.header.vendor.type";

    private final static String DEFAULT_DATE_FORMAT_KEY = "default.date.format";

    //transaction summary constants
    private final static String SUMMARY_CATEGORY_HEADER = "transaction.summary.header.category";
    private final static String SUMMARY_COUNT_HEADER = "transaction.summary.header.count";
    private final static String SUMMARY_AMOUNT_HEADER = "transaction.summary.header.amount";
    private final static String SUMMARY_BALANCE_KEY = "transaction.summary.key.balance";

    private AppConfig config;
    private Map<TransactionProperty, String> propertyNameMap;

    public TransactionUtil(AppConfig config) {
        this.config = config;
        loadPropertyNameMap();
    }

    private void loadPropertyNameMap() {
        this.propertyNameMap = new HashMap<>();
        propertyNameMap.put(TransactionProperty.ID, config.getProperties().getProperty(ID_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.DATE, config.getProperties().getProperty(DATE_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.SOURCE, config.getProperties().getProperty(SOURCE_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.TYPE, config.getProperties().getProperty(TYPE_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.AMOUNT, config.getProperties().getProperty(AMOUNT_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.DESCRIPTION, config.getProperties().getProperty(DESCRIPTION_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.CATEGORY, config.getProperties().getProperty(CATEGORY_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.VENDOR, config.getProperties().getProperty(VENDOR_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.VENDOR_MATCHES, config.getProperties().getProperty(VENDOR_MATCHES_HEADER_KEY));
        propertyNameMap.put(TransactionProperty.VENDOR_TYPE, config.getProperties().getProperty(VENDOR_TYPE_HEADER_KEY));

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
        SimpleDateFormat sdf = new SimpleDateFormat(config.getProperties().getProperty(DEFAULT_DATE_FORMAT_KEY));

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
        SimpleDateFormat sdf = new SimpleDateFormat(config.getProperties().getProperty(DEFAULT_DATE_FORMAT_KEY));

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

    public CsvTable getCustomColumnCsv(List<Transaction> transactions, List<TransactionProperty> properties) {
        if (properties == null || properties.size() == 0) {
            log.error("There are no properties in the list returning empty table");
            return new CsvTable();
        }
        List<String> headers = new ArrayList<>();
        CsvTable table = new CsvTable();
        for (TransactionProperty property: properties) {
            headers.add(this.propertyNameMap.get(property));
        }
        table.setHeaders(headers);
        for (Transaction transaction: transactions) {
            List<String> row = new ArrayList<>();
            for (TransactionProperty property: properties) {
                row.add(getTransactionPropertyValue(transaction, property));
            }
            try {
                table.addRow(row);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return table;
    }


    private String getTransactionPropertyValue(Transaction transaction, TransactionProperty property) {
        SimpleDateFormat sdf = new SimpleDateFormat(config.getProperties().getProperty(DEFAULT_DATE_FORMAT_KEY));
        switch (property) {
            case ID:
                return String.valueOf(transaction.getId());
            case DATE:
                return sdf.format(transaction.getTransactionDate());
            case SOURCE:
                return String.valueOf(transaction.getSource());
            case TYPE:
                return String.valueOf(transaction.getType());
            case AMOUNT:
                return String.valueOf(transaction.getAmount());
            case DESCRIPTION:
                return transaction.getDescription();
            case CATEGORY:
                return transaction.getCategory();
            case VENDOR:
                return transaction.getVendor();
            case VENDOR_TYPE:
                return transaction.getVendorType();
            case VENDOR_MATCHES:
                return transaction.getAdditionalDetails().get(config.getProperties().getProperty(VENDOR_MATCHES_HEADER_KEY));
        }
        return "NONE";
    }

    public enum TransactionProperty {
        ID,
        DATE,
        SOURCE,
        TYPE,
        AMOUNT,
        DESCRIPTION,
        CATEGORY,
        VENDOR,
        VENDOR_TYPE,
        VENDOR_MATCHES
    }
}
