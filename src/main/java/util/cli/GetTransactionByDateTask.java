package util.cli;

import model.*;
import service.TransactionProcessorService;
import util.CsvTableUtil;
import util.TransactionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetTransactionByDateTask extends CliTask{
    List<TaskParameter> parameters;
    TransactionProcessorService processorService;
    TransactionUtil transactionUtil;
    Date startDate, endDate;
    int sortAlgo;
    List<TransactionUtil.TransactionProperty> columnList;

    public GetTransactionByDateTask(String name, TransactionProcessorService processorService, TransactionUtil transactionUtil) {
        super();
        this.name = name;
        this.processorService = processorService;
        this.transactionUtil = transactionUtil;
        loadColumnList();
        loadParameters();
        this.description = "This task does what it says";
    }

    @Override
    CliSummary run() {
        List<Transaction> result = processorService.getTransactionsByDateRange(startDate, endDate, sortAlgo);
        CsvTable table = transactionUtil.getCustomColumnCsv(result, this.columnList);

        return new CliSummary(CliSummary.Status.PASS, CsvTableUtil.getCliTable(table));
    }

    @Override
    public CliSummary saveParameters() {
        CliSummary superSummary = super.saveParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        this.sortAlgo = TransactionUtil.DATE_ASC;
        this.startDate = CliUtils.parseDate(this.parameters.get(0).getValue());
        this.endDate = CliUtils.parseEndDate(this.parameters.get(1).getValue());
        return new CliSummary(CliSummary.Status.PASS, superSummary.getMessage());
    }

    private void loadParameters() {
        this.parameters = new ArrayList<>();
        parameters.add(new TaskParameter("Start Date", "1/22"));
        parameters.add(new TaskParameter("End Date", "1/22"));
        this.parameterBatches.add(new ParameterBatch("ONE", parameters));
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
