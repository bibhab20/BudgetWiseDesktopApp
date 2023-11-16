package util.cli;

import lombok.Getter;
import lombok.Setter;
import model.*;
import service.TransactionSummarizationService;
import util.TransactionNodeUtil;
import util.TransactionSummaryUtil;
import util.filter.AdvanceFilter;
import util.filter.AfterDateFilter;
import util.filter.BeforeDateFilter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetMonthlySummary extends CliTask{
    List<TaskParameter> parameters;
    List<MonthConfig> monthConfigs;
    TransactionRepository repository;
    TransactionSummarizationService summarizationService;
    int levels;

    private static final List<TransactionSummaryUtil.TransactionSummaryProperty> CREDIT_PROPERTIES = new ArrayList<>(List.of(
            TransactionSummaryUtil.TransactionSummaryProperty.CREDIT,
            TransactionSummaryUtil.TransactionSummaryProperty.CREDIT_PERCENTAGE
    ));
    private static final List<TransactionSummaryUtil.TransactionSummaryProperty> DEBIT_PROPERTIES = new ArrayList<>(List.of(
            TransactionSummaryUtil.TransactionSummaryProperty.DEBIT,
            TransactionSummaryUtil.TransactionSummaryProperty.DEBIT_PERCENTAGE
    ));

    public GetMonthlySummary(String name, TransactionRepository repository, TransactionSummarizationService summarizationService) {
        super();
        this.name = name;
        this.repository = repository;
        this.summarizationService = summarizationService;
        loadDescription();
        loadParameters();
    }

    @Override
    CliSummary run() {
        StringBuilder sb = new StringBuilder();
        sb.append(CliUtils.getColoredString(String.format("\nPrinting summary for %d months\n", this.monthConfigs.size()),
                CliUtils.ANSI_YELLOW));
        //get the transactions with the date filter
        for (MonthConfig config: this.monthConfigs) {
            AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new AfterDateFilter(config.getStartDate()))
                    .addFilter(new BeforeDateFilter(config.getEndDate())).build();

            List<Transaction> debits = new ArrayList<>(), credits = new ArrayList<>();
            for (Transaction transaction: repository.getAll()) {
                if (filter.pass(transaction)) {
                   if (transaction.getType().equals(TransactionType.CREDIT)) {
                       credits.add(transaction);
                   } else {
                       debits.add(transaction);
                   }
                }
            }
            sb.append("\n");
            sb.append(CliUtils.getColoredString("-".repeat(95) + config.getName() + "-".repeat(95),
                    CliUtils.ANSI_RED)).append("\n");
            sb.append(CliUtils.getColoredString("Credit Summary", CliUtils.ANSI_CYAN)).append("\n");
            TreeNode creditNode = TransactionNodeUtil.getTreeNode(summarizationService.getTransactionSummarizationTree(credits, this.levels), CREDIT_PROPERTIES);
            TreeNode debitNode = TransactionNodeUtil.getTreeNode(summarizationService.getTransactionSummarizationTree(debits, this.levels), DEBIT_PROPERTIES);
            sb.append(CliUtils.getCliTreeWithLevelColoring(creditNode, this.levels, getLevelColors()));
            sb.append(CliUtils.getColoredString("Debit Summary", CliUtils.ANSI_CYAN)).append("\n");
            sb.append(CliUtils.getCliTreeWithLevelColoring(debitNode, this.levels, getLevelColors()));

        }

        return new CliSummary(CliSummary.Status.PASS, sb.toString());

    }

    private void loadDescription(){
        this.description = "This task sucks";
    }

    @Override
    public CliSummary validateParameters() {
        CliSummary superSummary = super.validateParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        List<String> months = CliUtils.getListFromCommaSeparatedString(parameters.get(0).getValue());
        this.levels = Integer.parseInt(parameters.get(1).getValue());
        this.monthConfigs = new ArrayList<>();
        for (String monthString: months) {
            try {
                monthConfigs.add(new MonthConfig(monthString));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return new CliSummary(CliSummary.Status.PASS, "Fuck yeah");
    }

    private void loadParameters() {
        this.parameters = new ArrayList<>();
        parameters.add(new TaskParameter("Comma Separated list of month","1/22"));
        parameters.add(new TaskParameter("No of levels", "3"));
        this.getParameterBatches().add(new ParameterBatch("ONE", parameters));
    }

    @Getter
    @Setter
    public class MonthConfig {
        String name;
        String dateString;
        Date startDate, endDate;

        public MonthConfig(String dateString) throws ParseException {
            this.dateString = dateString;
            List<Date> dateRange = CliUtils.getDateRangeOfMonth(dateString);
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMM yy");
            this.name = outputDateFormat.format(dateRange.get(0));
            this.startDate = dateRange.get(0);
            this.endDate = dateRange.get(1);
        }
    }

    private List<String> getLevelColors() {
        return new ArrayList<>(List.of(CliUtils.ANSI_YELLOW, CliUtils.ANSI_MAGENTA, CliUtils.ANSI_BLUE, CliUtils.ANSI_RED));
    }
}
