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

public class GetTransactionsWithMissingVendorConfig implements CliTask{
    String name, description;
    VendorProcessorService processorService;
    List<TaskParameter> parameters;
    TransactionUtil transactionUtil;

    public GetTransactionsWithMissingVendorConfig(String name, VendorProcessorService processorService, TransactionUtil transactionUtil) {
        this.name = name;
        this.processorService = processorService;
        this.transactionUtil = transactionUtil;
        this.description = "This task prints the list of transaction with unknown or multiple vendors";

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CliSummary run() {
        List<Transaction> result = processorService.filterByMissingVendors();
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
        return new CliSummary(CliSummary.Status.PASS, "No parameters present");
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
