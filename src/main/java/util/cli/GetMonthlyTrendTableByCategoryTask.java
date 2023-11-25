package util.cli;

import model.*;
import service.TransactionSummarizationService;
import util.CsvTableUtil;
import util.TransactionSummaryUtil;

import java.util.*;

public class GetMonthlyTrendTableByCategoryTask extends CliTask{
    Date startDate, endDate;
    String category;
    TransactionSummaryUtil.TransactionSummaryProperty property;
    Map<String, TransactionSummaryUtil.TransactionSummaryProperty> propertyMap;

    TransactionSummarizationService summarizationService;

    public GetMonthlyTrendTableByCategoryTask(String name, TransactionSummarizationService summarizationService) {
        super();
        this.name = name;
        this.description = "This task shows the table with trends and stats for a given date range and category";
        this.summarizationService = summarizationService;
        loadPropertyMap();
        loadParameters();
    }

    @Override
    CliSummary run() {
        StringBuilder message = new StringBuilder();
        message.append(CliUtils.getColoredString("Summary Table:", CliUtils.ANSI_GREEN)).append("\n");
        try {
            CsvTable table = summarizationService.getMonthlyTrendTableByCategory(startDate, endDate,getTransactionType(property),
                    property);
            message.append(CsvTableUtil.getCliTable(table));
        } catch (Exception e) {
            e.printStackTrace();
            return new CliSummary(CliSummary.Status.FAIL, "Failed");
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
        this.property = this.propertyMap.get(parameters.get(2).getValue());
        return superSummary;
    }

    private void loadParameters() {
        List<TaskParameter> parameters = new ArrayList<>();
        parameters.add(new TaskParameter("Start Date", "1/22"));
        parameters.add(new TaskParameter("End Date", "5/22"));
        parameters.add(new TaskParameter("Property",
                TransactionSummaryUtil.TransactionSummaryProperty.DEBIT.name(),
                new ArrayList<>(this.propertyMap.keySet())));
        this.parameterBatches.add(new ParameterBatch("ONE", parameters));
    }

    private void loadPropertyMap() {

        this.propertyMap = new HashMap<>();
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.CREDIT.name(),  TransactionSummaryUtil.TransactionSummaryProperty.CREDIT);
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.DEBIT.name(),  TransactionSummaryUtil.TransactionSummaryProperty.DEBIT);
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.TOTAL.name(),  TransactionSummaryUtil.TransactionSummaryProperty.TOTAL);
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.CREDIT_PERCENTAGE.name(),  TransactionSummaryUtil.TransactionSummaryProperty.CREDIT_PERCENTAGE);
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.DEBIT_PERCENTAGE.name(),  TransactionSummaryUtil.TransactionSummaryProperty.DEBIT_PERCENTAGE);
        propertyMap.put( TransactionSummaryUtil.TransactionSummaryProperty.TOTAL_PERCENTAGE.name(),  TransactionSummaryUtil.TransactionSummaryProperty.TOTAL_PERCENTAGE);
    }

    private TransactionType getTransactionType(TransactionSummaryUtil.TransactionSummaryProperty property) {
        if (property.toString().contains("DEBIT")) {
            return TransactionType.DEBIT;
        }
        if (property.toString().contains("CREDIT")) {
            return TransactionType.CREDIT;
        }
        return TransactionType.ALL;
    }


}
