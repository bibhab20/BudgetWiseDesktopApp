package service;

import config.CategoryConfig;
import config.CategoryConfigSupplier;
import config.VendorTypeConfigSupplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import util.TransactionSummaryUtil;
import util.TransactionUtil;
import util.cli.BarChart;
import util.cli.CliUtils;
import util.filter.*;

import java.util.*;

import static util.TransactionSummaryUtil.*;

@Slf4j
public class TransactionSummarizationService {
    CategoryConfigSupplier categoryConfigSupplier;
    VendorTypeConfigSupplier vendorTypeConfigSupplier;
    TransactionUtil transactionUtil;
    TransactionRepository repository;
    public static final List<TransactionUtil.TransactionProperty> PATHS = new ArrayList<>(List.of(
            TransactionUtil.TransactionProperty.CATEGORY,
            TransactionUtil.TransactionProperty.VENDOR_TYPE,
            TransactionUtil.TransactionProperty.VENDOR,
            TransactionUtil.TransactionProperty.DESCRIPTION));

    public TransactionSummarizationService(CategoryConfigSupplier categoryConfigSupplier,
                                           VendorTypeConfigSupplier vendorTypeConfigSupplier,
                                           TransactionUtil transactionUtil, TransactionRepository repository) {
        this.categoryConfigSupplier = categoryConfigSupplier;
        this.vendorTypeConfigSupplier = vendorTypeConfigSupplier;
        this.transactionUtil = transactionUtil;
        this.repository = repository;
    }


    public TransactionNode getTransactionSummarizationTree(List<Transaction> transactions) {
        TransactionNode root = new TransactionNode("ROOT");
        for (Transaction transaction: transactions) {
            root.addTransaction(transaction, new LinkedList<>(List.of(transaction.getCategory(), transaction.getVendorType()
            ,transaction.getVendor())));
        }
        return root;
    }

    public TransactionNode getTransactionSummarizationTree(List<Transaction> transactions, int levels) {
        levels = Math.min(levels, PATHS.size());
        TransactionNode root = new TransactionNode("ROOT");
        for (Transaction transaction: transactions) {
            Queue<String> path = new LinkedList<>();
            for(int i = 0; i < levels; i++) {
                path.add(transactionUtil.getTransactionPropertyValue(transaction, PATHS.get(i)));
            }
            root.addTransaction(transaction, path);
        }
        return root;
    }

    public TransactionNode getMonthlyTrend(Date startDate, Date endDate, int levels, TransactionType transactionType) {
        LinkedHashMap<String, Date[]> dateRangeMap = getDateRangeMap(startDate, endDate);
        TransactionNode root = new TransactionNode("ROOT");
        for (Map.Entry<String, Date[]> entry: dateRangeMap.entrySet()) {
            String title = entry.getKey();
            Date monthStartDate = dateRangeMap.get(title)[0];
            Date monthEndDate = dateRangeMap.get(title)[1];
            AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new AfterDateFilter(monthStartDate))
                    .addFilter(new BeforeDateFilter(monthEndDate)).build();
            log.debug("Processing transaction for {}", title);
            for (Transaction transaction: repository.get(transactionType)) {
                if (filter.pass(transaction)) {
                    Queue<String> path = new LinkedList<>();
                    for(int i = 0; i < levels; i++) {
                        path.add(transactionUtil.getTransactionPropertyValue(transaction, PATHS.get(i)));
                    }
                    root.addTransaction(transaction, path, title);
                }

            }
        }
        return root;
    }

    public static LinkedHashMap<String, Date[]> getDateRangeMap(Date startDate, Date endDate) {
        LinkedHashMap<String, Date[]> dateRangeMap = new LinkedHashMap<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
            String monthYear = String.format("%1$tB %1$ty", calendar);
            Date[] dateRange = {calendar.getTime(), null};
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            dateRange[1] = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            dateRangeMap.put(monthYear, dateRange);
        }

        return dateRangeMap;
    }

    public CsvTable getMonthlyTrendTableByCategory(Date startDate, Date endDate, TransactionType transactionType,
                                                   TransactionSummaryUtil.TransactionSummaryProperty property) throws Exception {
        List<CategoryConfig> categories = categoryConfigSupplier.get();
        LinkedHashMap<String, Date[]> dateRangeMap = getDateRangeMap(startDate, endDate);
        Map<String, List<TransactionSummary>> categoryMap = new HashMap<>();
        Map<String, List<TransactionSummary>> monthSummaryMap = new HashMap<>();
        Map<String, TransactionSummary> parentSummaryMap = new HashMap<>();
        List<String> months = new ArrayList<>();
        int index = 0;

        //populate category map with all summaries for every month
        for (CategoryConfig config: categories) {
            categoryMap.put(config.getName(), new ArrayList<>());
            List<TransactionSummary> summaries = categoryMap.get(config.getName());
            for (int i=0; i<dateRangeMap.size(); i++) {
                summaries.add(new TransactionSummary());
            }
        }

        //Filter out transactions for each month and add update the corresponding summary in categoryMap
        for (Map.Entry<String, Date[]> entry: dateRangeMap.entrySet()) {
            months.add(entry.getKey());
            Date monthStartDate = entry.getValue()[0];
            Date monthEndDate = entry.getValue()[1];

            //create a new entry for monthly summary in monthlySummaryMap
            monthSummaryMap.put(entry.getKey(), new ArrayList<>());
            parentSummaryMap.put(entry.getKey(), new TransactionSummary());
            AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new AfterDateFilter(monthStartDate))
                    .addFilter(new BeforeDateFilter(monthEndDate)).build();
            List<Transaction> result = filter.filter(repository.get(transactionType));
            for (Transaction transaction: result) {
                parentSummaryMap.get(entry.getKey()).update(transaction);

                if (categoryMap.containsKey(transaction.getCategory())) {
                    categoryMap.get(transaction.getCategory()).get(index).update(transaction);
                } else {
                    log.error("Found invalid category {} in transaction with id {}", transaction.getCategory(), transaction.getId());
                }
                monthSummaryMap.get(entry.getKey()).add(categoryMap.get(transaction.getCategory()).get(index));
            }
            index++;
        }

        //For each month update all the children summaries with parent summary
        for (String month: months) {
            TransactionSummary parentSummary = parentSummaryMap.get(month);
            for (TransactionSummary summary: monthSummaryMap.get(month)) {
                summary.update(parentSummary);
            }
        }
        Map<String , DescriptiveStatistics> categorySortingMap = new HashMap<>();
        for (CategoryConfig config: categories) {
            DescriptiveStatistics stat = TransactionSummaryUtil.getSummaryStat(categoryMap.get(config.getName()), property);
            categorySortingMap.put(config.getName(), stat);
        }
        List<Map.Entry<String, DescriptiveStatistics>> entryList = new ArrayList<>(categorySortingMap.entrySet());
        entryList.sort(Map.Entry.comparingByValue((n1, n2) -> (int) (n2.getSum()-n1.getSum())));
        List<String> sortedCategoryList = new ArrayList<>();
        for (Map.Entry<String, DescriptiveStatistics> entry: entryList) {
            sortedCategoryList.add(entry.getKey());
        }

        //create data rows
        CsvTable table = new CsvTable();
        table.getMetaData().setHeaderColor(CliUtils.ANSI_BLUE);
        List<String> headers = new ArrayList<>();
        headers.add("Month");
        headers.addAll(sortedCategoryList);
        headers.add(CliUtils.getColoredString("Sum", CliUtils.ANSI_GREEN));
        headers.add(CliUtils.getColoredString("Mean", CliUtils.ANSI_MAGENTA));
        table.setHeaders(headers);


        double[] sumArray = new double[months.size()];
        for (int i=0; i<months.size(); i++) {
            List<String> row = new ArrayList<>();
            row.add(months.get(i));
            List<TransactionSummary> monthSummaries = new ArrayList<>();
            for (String category: sortedCategoryList) {
                TransactionSummary summary = categoryMap.get(category).get(i);
                TransactionSummary lastSummary = i==0 ? new TransactionSummary(): categoryMap.get(category).get(i-1);
                double currentValue = TransactionSummaryUtil.getNumericalSummaryPropertyValue(summary, property);
                double lastValue = TransactionSummaryUtil.getNumericalSummaryPropertyValue(lastSummary, property);
                double diff = i == 0? 0: currentValue - lastValue;
                row.add(TransactionSummaryUtil.getTrendString(currentValue, diff, property));
                monthSummaries.add(summary);
            }
            DescriptiveStatistics stat = TransactionSummaryUtil.getSummaryStat(monthSummaries, property);
            row.add(CliUtils.getPrintString(stat.getSum(), property));
            row.add(CliUtils.getPrintString(stat.getMean(), property));
            table.addRow(row);
            sumArray[i] = stat.getSum();
        }

        //Add category summary in the last row
        List<String> sumRow = new ArrayList<>();
        List<String> meanRow = new ArrayList<>();
        sumRow.add(CliUtils.getColoredString("Sum", CliUtils.ANSI_GREEN));
        meanRow.add(CliUtils.getColoredString("Mean", CliUtils.ANSI_MAGENTA));
        for (String category: sortedCategoryList) {
            DescriptiveStatistics stats = TransactionSummaryUtil.getSummaryStat(categoryMap.get(category), property);
            sumRow.add(CliUtils.getPrintString(stats.getSum(), property));
            meanRow.add(CliUtils.getPrintString(stats.getMean(), property));
        }
        DescriptiveStatistics sumStat = new DescriptiveStatistics(sumArray);
        sumRow.add(CliUtils.getPrintString(sumStat.getSum(), property));
        sumRow.add(CliUtils.getColoredString("NA", CliUtils.ANSI_RED));
        meanRow.add(CliUtils.getPrintString(sumStat.getMean(), property));
        meanRow.add(CliUtils.getColoredString("NA", CliUtils.ANSI_RED));
        table.addRow(sumRow);
        table.addRow(meanRow);
        return table;
    }


    public BarChart getBarChartByCategory(Date startDate, Date endDate, String category, TransactionSummaryProperty property) throws Exception {
        TransactionType transactionType = getTransactionType(property);
        LinkedHashMap<String, Date[]> dateRangeMap = getDateRangeMap(startDate, endDate);
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        Map<String, TransactionSummary> childSummaries = new HashMap<>();

        for (Map.Entry<String, Date[]> entry: dateRangeMap.entrySet()) {
            String month = entry.getKey();
            labels.add(month);
            Date monthStartDate = entry.getValue()[0];
            Date monthEndDate = entry.getValue()[1];
            AdvanceFilter dateFilter = new AdvanceFilter.Builder().addFilter(new AfterDateFilter(monthStartDate))
                    .addFilter(new BeforeDateFilter(monthEndDate))
                    .addFilter(new TransactionTypeFilter(transactionType)).build();
            List<Transaction> monthTransactions = dateFilter.filter(repository.getAll());
            TransactionSummary parentSummary = new TransactionSummary();
            TransactionSummary childSummary = new TransactionSummary();
            for (Transaction transaction: monthTransactions) {
                parentSummary.update(transaction);
                if (transaction.getCategory().equals(category)) {
                    childSummary.update(transaction);
                }
            }
            childSummary.update(parentSummary);
            childSummaries.put(month, childSummary);
        }

        for (String month: labels) {
            data.add(TransactionSummaryUtil.getNumericalSummaryPropertyValue(childSummaries.get(month), property));
        }
        return new BarChart(labels, data, property);

    }



    @Getter
    public class Trend {
        TransactionNode root;
        List<String> orderedBucketList;

        public Trend(TransactionNode root, List<String> orderedBucketList) {
            this.root = root;
            this.orderedBucketList = orderedBucketList;
        }
    }


}
