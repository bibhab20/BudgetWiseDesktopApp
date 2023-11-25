package util.cli;

import config.AppConstants;
import config.CategoryConfigSupplier;
import model.CliSummary;
import model.ParameterBatch;
import model.TaskParameter;
import service.TransactionSummarizationService;
import util.TransactionSummaryUtil;

import java.util.*;

public class GetMonthlyCategoryWiseChartTask extends CliTask{
    Date startDate, endDate;
    String category;
    TransactionSummaryUtil.TransactionSummaryProperty property;
    Map<String, TransactionSummaryUtil.TransactionSummaryProperty> propertyMap;

    CategoryConfigSupplier categoryConfigSupplier;
    TransactionSummarizationService summarizationService;

    public GetMonthlyCategoryWiseChartTask(String name, CategoryConfigSupplier categoryConfigSupplier, TransactionSummarizationService summarizationService) {
        super();
        this.name = name;
        this.description = "This task shows the bar chart with month in the Y axis and values on the x axis for a given date range and category";
        this.categoryConfigSupplier = categoryConfigSupplier;
        this.summarizationService = summarizationService;
        loadPropertyMap();
        loadParameters();
    }

    @Override
    CliSummary run() {
        StringBuilder message = new StringBuilder();
        try {
            BarChart chart = summarizationService.getBarChartByCategory(startDate, endDate, category, property);
            message.append(CliUtils.getColoredString("Bar Chart for category: ", CliUtils.ANSI_CYAN)).append(CliUtils.getColoredString(category, CliUtils.ANSI_MAGENTA)).append("\n");
            message.append(chart.draw());
        } catch (Exception e) {
            e.printStackTrace();
            return new CliSummary(CliSummary.Status.FAIL, CliUtils.getColoredString("Something went wrong. Contact developer", CliUtils.ANSI_RED));
        }
        return new CliSummary(CliSummary.Status.PASS, message.toString());
    }

    @Override
    public CliSummary saveParameters() {
        CliSummary superSummary = super.saveParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        List<TaskParameter> parameters = this.parameterBatches.get(0).getParameters();
        this.startDate = CliUtils.parseDate(parameters.get(0).getValue());
        this.endDate = CliUtils.parseEndDate(parameters.get(1).getValue());
        this.category = parameters.get(2).getValue();
        this.property = this.propertyMap.get(parameters.get(3).getValue());
        return superSummary;
    }

    private void loadParameters() {
        List<TaskParameter> parameters = new ArrayList<>();
        parameters.add(new TaskParameter("Start Date", "1/22"));
        parameters.add(new TaskParameter("End Date", "5/22"));
        parameters.add(new TaskParameter("Category", AppConstants.ALL_PASS_FILTER, categoryConfigSupplier.getSortedNames()));
        parameters.add(new TaskParameter("Property", TransactionSummaryUtil.TransactionSummaryProperty.DEBIT.name(),
                new ArrayList<>(this.propertyMap.keySet())));
        this.parameterBatches.add(new ParameterBatch("ONE", parameters));
    }

    private void loadPropertyMap() {

        this.propertyMap = new HashMap<>();
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.CREDIT.name(),  TransactionSummaryUtil.TransactionSummaryProperty.CREDIT);
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.DEBIT.name(),  TransactionSummaryUtil.TransactionSummaryProperty.DEBIT);
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.CREDIT_PERCENTAGE.name(),  TransactionSummaryUtil.TransactionSummaryProperty.CREDIT_PERCENTAGE);
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.DEBIT_PERCENTAGE.name(),  TransactionSummaryUtil.TransactionSummaryProperty.DEBIT_PERCENTAGE);
    }
}
