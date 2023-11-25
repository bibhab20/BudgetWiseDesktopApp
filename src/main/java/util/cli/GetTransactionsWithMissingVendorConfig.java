package util.cli;

import model.CliSummary;
import model.CsvTable;
import model.TaskParameter;
import model.Transaction;
import service.VendorProcessorService;
import util.CsvTableUtil;
import util.TransactionUtil;

import java.util.ArrayList;
import java.util.List;

public class GetTransactionsWithMissingVendorConfig extends CliTask {
    String name, description;
    VendorProcessorService processorService;
    List<TaskParameter> parameters;
    TransactionUtil transactionUtil;
    List<TransactionUtil.TransactionProperty> columnList;

    public GetTransactionsWithMissingVendorConfig(String name, VendorProcessorService processorService, TransactionUtil transactionUtil) {
        super();
        this.name = name;
        this.processorService = processorService;
        this.transactionUtil = transactionUtil;
        this.description = "This task prints the list of transaction with unknown or multiple vendors";
        this.parameters = new ArrayList<>();
        loadColumnList();

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CliSummary run() {
        List<Transaction> result = processorService.filterByMissingVendors();
        if (result.size() == 0) {
            return new CliSummary(CliSummary.Status.PASS, "No transactions found");
        }
        CsvTable table;
        try {
            table = transactionUtil.getCustomColumnCsv(result, this.columnList);
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
    public CliSummary saveParameters() {
        return new CliSummary(CliSummary.Status.PASS, "No parameters present");
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    private void loadColumnList() {
        this.columnList = new ArrayList<>();
        columnList.add(TransactionUtil.TransactionProperty.DATE);
        columnList.add(TransactionUtil.TransactionProperty.SOURCE);
        columnList.add(TransactionUtil.TransactionProperty.TYPE);
        columnList.add(TransactionUtil.TransactionProperty.AMOUNT);
        columnList.add(TransactionUtil.TransactionProperty.DESCRIPTION);
        columnList.add(TransactionUtil.TransactionProperty.VENDOR);
        columnList.add(TransactionUtil.TransactionProperty.VENDOR_MATCHES);
    }
}
