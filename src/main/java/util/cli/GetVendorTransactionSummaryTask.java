package util.cli;

import config.VendorConfigSupplier;
import lombok.extern.slf4j.Slf4j;
import model.*;
import service.VendorProcessorService;
import util.AppConfig;
import util.CsvTableUtil;
import util.TransactionUtil;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class GetVendorTransactionSummaryTask extends CliTask {
    private static final String SIMPLE_DATE_FORMAT_CONFIG = "default.date.format";
    private static final String START_DATE = "Start Date";
    private static final String END_DATE = "End Date";
    private static final String VENDOR_NAME = "Vendor Name";
    public static final String NONE = "none";
    private static final String DESCRIPTION = "Given a date range and vendor name, it shows the list of transactions";

    public static final Integer VENDOR_NAME_COLUMN_COUNT = 4;
    public static final String BALANCE = "Balance";

    VendorProcessorService processorService;
    VendorConfigSupplier vendorConfigSupplier;
    SimpleDateFormat dateFormat;
    Date startDate, endDate;
    AppConfig appConfig;
    String vendorName;
    TransactionUtil transactionUtil;
    List<TaskParameter> parameters;

    public GetVendorTransactionSummaryTask(String name, AppConfig appConfig, TransactionUtil transactionUtil,
                                           VendorConfigSupplier vendorConfigSupplier, VendorProcessorService processorService) {
        super();
        this.name = name;
        this.appConfig = appConfig;
        this.dateFormat = new SimpleDateFormat(appConfig.getProperties().getProperty(SIMPLE_DATE_FORMAT_CONFIG));
        this.transactionUtil = transactionUtil;
        this.vendorConfigSupplier = vendorConfigSupplier;
        this.processorService = processorService;
        //new parameter object
        this.parameters = new ArrayList<>();
        parameters.add(new TaskParameter(START_DATE, "1/1/2022"));
        parameters.add(new TaskParameter(END_DATE, "1/5/2022"));
        List<String> vendorNames = vendorConfigSupplier.getNames();
        vendorNames.sort((String::compareTo));
        parameters.add(new TaskParameter(VENDOR_NAME, "Uber", vendorNames));
        this.getParameterBatches().add(new ParameterBatch("ONE", parameters));

        this.description = loadDescription();
    }


    @Override
    public CliSummary run() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        List<Transaction> result = this.processorService.filterByVendorNameAndDate(startDate, endDate, vendorName);
        if (result.size() == 0) {
            return new CliSummary(CliSummary.Status.FAIL, String.format("No transactions found for the given vendor %s, from  %s, to %s",
                    vendorName, sdf.format(startDate), sdf.format(endDate) ));
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
    public CliSummary saveParameters() {
        CliSummary superSummary = super.saveParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        for (TaskParameter parameter: this.parameters) {
            if (parameter.getValue() == null || parameter.getValue().isBlank()) {
                parameter.setValue(parameter.getDefaultValue());
            }
        }
        this.startDate = CliUtils.parseDate(parameters.get(0).getValue());
        this.endDate = CliUtils.parseEndDate(parameters.get(1).getValue());
        this.vendorName = parameters.get(2).getValue();
        return new CliSummary(CliSummary.Status.PASS, superSummary.getMessage());
    }

    private String loadDescription() {
        return DESCRIPTION;

    }


}
