package util.cli;

import config.CategoryConfig;
import config.CategoryConfigSupplier;
import model.CliSummary;
import model.CsvTable;
import model.TaskParameter;
import model.Transaction;
import service.CategoryProcessorService;
import util.CsvTableUtil;
import util.TransactionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetCategoryTransactionSummary extends CliTask {
    private static final String START_DATE_DEFAULT_VALUE = "1/1/2022";
    private static final String END_DATE_DEFAULT_VALUE = "31/12/2025";
    private static final String CATEGORY_DEFAULT_VALUE = "Food";
    public static final Integer CATEGORY_COLUMN_COUNT = 4;


    String name, description;
    Date startDate, endDate;
    String category;
    List<TaskParameter> parameters;
    CategoryProcessorService processorService;
    TransactionUtil transactionUtil;
    CategoryConfigSupplier configSupplier;

    public GetCategoryTransactionSummary(String name, CategoryProcessorService processorService, TransactionUtil transactionUtil, CategoryConfigSupplier configSupplier) {
        this.name = name;
        this.processorService = processorService;
        this.transactionUtil = transactionUtil;
        this.configSupplier = configSupplier;
        loadParameters();
        loadDescription();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CliSummary run() {
        List<Transaction> result = processorService.getTransactionByDateAndCategory(this.startDate, this.endDate, category);
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
    public String getDescription() {
        return this.description;
    }

    private void loadParameters() {
        this.parameters = new ArrayList<>();
        this.parameters.add(new TaskParameter("Start Date", START_DATE_DEFAULT_VALUE));
        this.parameters.add(new TaskParameter("End Date", END_DATE_DEFAULT_VALUE));
        this.parameters.add(new TaskParameter("Category Name",CATEGORY_DEFAULT_VALUE ));
    }

    private void loadDescription() {
        List<CategoryConfig> categoryConfigs = configSupplier.get();
        List<String> categoryNames = new ArrayList<>();
        for (CategoryConfig config: categoryConfigs) {
            categoryNames.add(config.getName());
        }
        this.description = "Choose the category from the list below.\n" + CliUtils.getCliGrid(categoryNames, CATEGORY_COLUMN_COUNT);
    }
}
