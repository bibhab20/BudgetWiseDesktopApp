package util.cli;

import model.*;
import service.TransactionSummarizationService;
import util.TransactionNodeUtil;
import util.TransactionSummaryUtil;

import java.util.*;

public class GetMonthlyTrend extends CliTask{
    List<TaskParameter> parameters;
    TransactionSummarizationService summarizationService;
    Date startDate, endDate;
    int levels;
    Map<String, TransactionSummaryUtil.TransactionSummaryProperty> propertyMap;
    TransactionSummaryUtil.TransactionSummaryProperty property;

    public GetMonthlyTrend(String name, TransactionSummarizationService summarizationService) {
        super();
        this.name = name;
        this.summarizationService = summarizationService;
        loadDescription();
        loadPropertyMap();
        loadParameters();
    }

    @Override
    CliSummary run() {
        StringBuilder sb = new StringBuilder();
        //get the transactions with the date filter
        TransactionNode root = summarizationService.getMonthlyTrend(startDate, endDate, levels, getTransactionType(property));
        if (!TransactionNodeUtil.validateSummaryBucket(root, null)) {
            return new CliSummary(CliSummary.Status.FAIL, "Node validation failed. Contact Developer");
        }
        TreeNode node = TransactionNodeUtil.getTrendTreeNode(root, this.property);
        sb.append(CliUtils.getCliTransactionSummaryTree(node));

        try {

        } catch (Exception e) {
            e.printStackTrace();
            return new CliSummary(CliSummary.Status.FAIL, "Failed");
        }
        return new CliSummary(CliSummary.Status.PASS, sb.toString());
    }

    @Override
    public CliSummary saveParameters() {
        CliSummary superSummary = super.saveParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        this.startDate = CliUtils.parseDate(parameters.get(0).getValue());
        this.endDate = CliUtils.parseDate(parameters.get(1).getValue());
        this.levels = Integer.parseInt(parameters.get(2).getValue());
        this.property = propertyMap.get(parameters.get(3).getValue());
        if (this.property == null) {
            return new CliSummary(CliSummary.Status.FAIL, "Entered invalid property value");
        }
        return new CliSummary(CliSummary.Status.PASS, superSummary.getMessage());
    }

    private void loadDescription() {
        this.description = "Enter the starting and ending month in MM/yy format and it will show you the trend of expenses";
    }

    private void loadParameters() {
        this.parameters = new ArrayList<>();
        parameters.add(new TaskParameter("Start Date","1/22"));
        parameters.add(new TaskParameter("End Date", "12/22"));
        parameters.add(new TaskParameter("levels", "2"));
        parameters.add(new TaskParameter("Property",
                TransactionSummaryUtil.TransactionSummaryProperty.TOTAL.name(), new ArrayList<>(this.propertyMap.keySet())));
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
