package util.cli;

import config.CategoryConfig;
import config.CategoryConfigSupplier;
import lombok.extern.slf4j.Slf4j;
import model.*;
import service.CategoryProcessorService;
import util.CsvTableUtil;
import util.TransactionUtil;
import util.filter.AdvanceFilter;
import util.filter.GreaterThanAmountFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class GetCategoryTransactionSummary extends CliTask {
    private static final String START_DATE_DEFAULT_VALUE = "1/1/22";
    private static final String END_DATE_DEFAULT_VALUE = "12/31/23";
    private static final String CATEGORY_DEFAULT_VALUE = "ALL";

    Date startDate, endDate;
    String category;
    CategoryProcessorService processorService;
    TransactionUtil transactionUtil;
    CategoryConfigSupplier configSupplier;
    double highlightThreshold;
    List<TransactionUtil.TransactionProperty> columnList;
    List<TaskParameter> parameters;

    public GetCategoryTransactionSummary(String name, CategoryProcessorService processorService, TransactionUtil transactionUtil, CategoryConfigSupplier configSupplier) {
        super();
        this.name = name;
        this.processorService = processorService;
        this.transactionUtil = transactionUtil;
        this.configSupplier = configSupplier;
        loadParameters();
        loadDescription();
        loadColumnList();
        log.debug("Inside GetCategoryTransactionSummary Constructor and the name is: {}", this.getName());
    }

    @Override
    public CliSummary run() {
        List<Transaction> result = processorService.getTransactionByDateAndCategory(this.startDate, this.endDate, category);
        if (result.size() == 0) {
            return new CliSummary(CliSummary.Status.PASS, "No transactions found for the given Category");
        }
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new GreaterThanAmountFilter(this.highlightThreshold)).build();
        CsvTable table;
        try {
            table = transactionUtil.getCsv(result, this.columnList, filter, CliUtils.ANSI_BLUE, CliUtils.ANSI_YELLOW_BACKGROUND);
        } catch (Exception e) {
            return new CliSummary(CliSummary.Status.FAIL, e.getMessage());
        }

        CliSummary summary = new CliSummary(CliSummary.Status.PASS, String.format("%d transactions found", result.size()));
        summary.appendMessage(String.format("Start date: %s, end date: %s", CliUtils.getDateString(startDate),
                CliUtils.getDateString(endDate)));
        summary.appendMessage(CsvTableUtil.getCliTable(table));
        try {
            summary.appendMessage(CsvTableUtil.getCliTable(transactionUtil.getSummaryTable(result)));
        } catch (Exception e) {
            return new CliSummary(CliSummary.Status.FAIL, "Unknown Error happened");
        }
        return summary;
    }

    @Override
    public CliSummary validateParameters() {
        CliSummary superSummary = super.validateParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        this.startDate = CliUtils.parseDate(parameters.get(0).getValue());
        this.endDate = CliUtils.parseEndDate(parameters.get(1).getValue());
        this.category = parameters.get(2).getValue();
        this.highlightThreshold = Double.parseDouble(parameters.get(3).getValue());
        return new CliSummary(CliSummary.Status.PASS, superSummary.getMessage());
    }

    private void loadParameters() {
        this.parameters = new ArrayList<>();
        this.parameters.add(new TaskParameter("Start Date", START_DATE_DEFAULT_VALUE));
        this.parameters.add(new TaskParameter("End Date", END_DATE_DEFAULT_VALUE));

        List<CategoryConfig> categoryConfigs = configSupplier.get();
        List<String> categoryNames = new ArrayList<>();
        for (CategoryConfig config: categoryConfigs) {
            categoryNames.add(config.getName());
        }
        this.parameters.add(new TaskParameter("Category Name",CATEGORY_DEFAULT_VALUE, categoryNames));
        this.parameters.add(new TaskParameter("Highlight Amount threshold", "1000"));
        this.getParameterBatches().add(new ParameterBatch("ONE", parameters));
    }

    private void loadDescription() {
        this.description = "Enter start date, end date and category name. Get the list of transactions and their summary";
    }

    private void loadColumnList() {
        this.columnList = new ArrayList<>();
        columnList.add(TransactionUtil.TransactionProperty.DATE);
        columnList.add(TransactionUtil.TransactionProperty.SOURCE);
        columnList.add(TransactionUtil.TransactionProperty.TYPE);
        columnList.add(TransactionUtil.TransactionProperty.AMOUNT);
        columnList.add(TransactionUtil.TransactionProperty.DESCRIPTION);
        columnList.add(TransactionUtil.TransactionProperty.VENDOR);
        columnList.add(TransactionUtil.TransactionProperty.VENDOR_TYPE);
        columnList.add(TransactionUtil.TransactionProperty.CATEGORY);
    }
}
