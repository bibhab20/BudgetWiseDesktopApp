package util.cli;

import config.VendorConfig;
import config.VendorConfigSupplier;
import model.CliSummary;
import model.CsvTable;
import model.TaskParameter;
import model.Transaction;
import service.VendorProcessorService;
import util.AppConfig;
import util.CsvTableUtil;
import util.TransactionUtil;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GetVendorTransactionSummaryTask implements CliTask {
    private static final String SIMPLE_DATE_FORMAT_CONFIG = "default.date.format";
    private static final String START_DATE = "Start Date";
    private static final String END_DATE = "End Date";
    private static final String VENDOR_NAME = "Vendor Name";
    public static final String NONE = "none";

    public static final Integer VENDOR_NAME_COLUMN_COUNT = 4;
    public static final String BALANCE = "Balance";

    String name, description;
    Map<String, String> parameterMap;
    List<TaskParameter> parameters;
    VendorProcessorService processorService;
    VendorConfigSupplier vendorConfigSupplier;
    SimpleDateFormat dateFormat;
    Date startDate, endDate;
    AppConfig appConfig;
    String vendorName;
    TransactionUtil transactionUtil;

    public GetVendorTransactionSummaryTask(String name, AppConfig appConfig, TransactionUtil transactionUtil,
                                           VendorConfigSupplier vendorConfigSupplier, VendorProcessorService processorService) {
        this.name = name;
        this.appConfig = appConfig;
        this.dateFormat = new SimpleDateFormat(appConfig.getProperties().getProperty(SIMPLE_DATE_FORMAT_CONFIG));
        this.parameterMap = new HashMap<>();
        this.transactionUtil = transactionUtil;
        this.vendorConfigSupplier = vendorConfigSupplier;
        this.processorService = processorService;
        this.parameterMap.put(START_DATE, NONE);
        this.parameterMap.put(END_DATE, NONE);
        this.parameterMap.put(VENDOR_NAME, NONE);

        //new parameter object
        this.parameters = new ArrayList<>();
        parameters.add(new TaskParameter(START_DATE, "1/1/2022"));
        parameters.add(new TaskParameter(END_DATE, "1/1/2024"));
        parameters.add(new TaskParameter(VENDOR_NAME, "Uber"));

        this.description = loadDescription();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CliSummary run() {
        List<Transaction> result = this.processorService.filterByVendorNameAndDate(startDate, endDate, vendorName);
        if (result.size() == 0) {
            return new CliSummary(CliSummary.Status.FAIL, "The transactions found for the given vendor");
        }
        CsvTable table;
        try {
            table = transactionUtil.getCsv(result, new ArrayList<>());
        } catch (Exception e) {
            return new CliSummary(CliSummary.Status.FAIL, e.getMessage());
        }

        CliSummary summary = new CliSummary(CliSummary.Status.PASS, CsvTableUtil.getCliTable(table));
        try {
            summary.appendMessage(CsvTableUtil.getCliTable(transactionUtil.getSummaryTable(result)));
        } catch (Exception e) {
            return new CliSummary(CliSummary.Status.FAIL, "Unknown Error happened");
        }
        return summary;

    }

    @Override
    public List<TaskParameter> getParameters() {
        return this.parameters;
    }


    @Override
    public CliSummary validateParameters() {
        for (TaskParameter parameter: this.parameters) {
            if (parameter.getValue() == null || parameter.getValue().isBlank()) {
                parameter.setValue(parameter.getDefaultValue());
            }
        }
        try {
            this.startDate = dateFormat.parse(parameters.get(0).getValue());
            this.endDate = dateFormat.parse(parameters.get(1).getValue());
            this.vendorName = parameters.get(2).getValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return new CliSummary(CliSummary.Status.FAIL, e.getMessage());
        }
        return new CliSummary(CliSummary.Status.PASS, "Validation Success");
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    private boolean checkForNullAndEmpty() {
        for (String value: this.parameterMap.values()) {
            if (value == null || value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String loadDescription() {
        List<String> vendorNames = new ArrayList<>();
        if (vendorConfigSupplier == null) {
            System.out.println("vendor config supplier is null");
        }
        for (VendorConfig vendorConfig: this.vendorConfigSupplier.get()) {
            vendorNames.add(vendorConfig.getName());
        }
        return getCliGrid(vendorNames, VENDOR_NAME_COLUMN_COUNT);

    }

    public static String getCliGrid(List<String> strings, int columns) {
        if (strings == null || strings.isEmpty() || columns <= 0) {
            return "";
        }

        int maxStringLength = 0;

        // Find the length of the longest string
        for (String str : strings) {
            maxStringLength = Math.max(maxStringLength, str.length());
        }

        // Calculate the column width based on the longest string
        int columnWidth = maxStringLength + 2; // Add some spacing between columns

        StringBuilder result = new StringBuilder();

        int rows = (int) Math.ceil((double) strings.size() / columns);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row + col * rows;
                if (index < strings.size()) {
                    String str = strings.get(index);
                    result.append(String.format("%-" + columnWidth + "s", str));

                    if (col < columns - 1) {
                        // Add some spacing between columns
                        result.append("  ");
                    }
                }
            }
            result.append("\n");
        }

        return result.toString();
    }


}
