package util.cli;

import config.AppConstants;
import config.CategoryConfigSupplier;
import config.VendorConfigSupplier;
import config.VendorTypeConfigSupplier;
import model.*;
import util.CsvTableUtil;
import util.TransactionUtil;
import util.filter.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenericTransactionQueryTask extends CliTask{
    List<TaskParameter> filterParameters;
    List<TaskParameter> highlightParameters;
    private static final String MIDFIRST_OPTION = "Midfirst";
    private static final String DISCOVER_OPTION = "Discover";
    Date startDate, endDate;
    String category, vendor, vendorType, descriptionString;
    String highlightCategory, highlightVendor, highlightVendorType;
    double highlightAmountAbove, getHighlightAmountBelow;

    TransactionSource source, highlightSource;
    TransactionType type, highlightType;
    List<TransactionUtil.TransactionProperty> columnList;

    CategoryConfigSupplier categoryConfigSupplier;
    VendorConfigSupplier vendorConfigSupplier;
    VendorTypeConfigSupplier vendorTypeConfigSupplier;
    TransactionRepository repository;
    TransactionUtil transactionUtil;

    public GenericTransactionQueryTask(String name, CategoryConfigSupplier categoryConfigSupplier, VendorConfigSupplier vendorConfigSupplier, VendorTypeConfigSupplier vendorTypeConfigSupplier, TransactionRepository repository, TransactionUtil transactionUtil) {
        super();
        this.name = name;
        this.categoryConfigSupplier = categoryConfigSupplier;
        this.vendorConfigSupplier = vendorConfigSupplier;
        this.vendorTypeConfigSupplier = vendorTypeConfigSupplier;
        this.repository = repository;
        this.transactionUtil = transactionUtil;
        loadColumnList();
        loadParameters();
        this.description = "You can query based on all available filters and highlight specific transactions too";
    }

    @Override
    CliSummary run() {
        AdvanceFilter filter = new AdvanceFilter.Builder()
                .addFilter(new AfterDateFilter(startDate))
                .addFilter(new BeforeDateFilter(endDate))
                .addFilter(new CategoryMatchOrFilter(List.of(category)))
                .addFilter(new VendorTypeMatchOrFilter(List.of(vendorType)))
                .addFilter(new VendorMatchOrFilter(List.of(vendor)))
                .addFilter(new ContainsDescriptionFilter(List.of(descriptionString)))
                .addFilter(new SourceFilter(source))
                .addFilter(new TransactionTypeFilter(type))
                .build();
        List<Transaction> result = filter.filter(repository.getAll());

        //highlights
        List<TransactionFilter> highlightFilters = new ArrayList<>();
        if (!highlightCategory.equals(AppConstants.NO_PASS_FILTER)) {
            highlightFilters.add(new CategoryMatchOrFilter(List.of(highlightCategory)));
        }

        if (!highlightVendorType.equals(AppConstants.NO_PASS_FILTER)) {
            highlightFilters.add(new VendorTypeMatchOrFilter(List.of(highlightVendorType)));
        }
        if (!highlightVendor.equals(AppConstants.NO_PASS_FILTER)) {
            highlightFilters.add(new VendorMatchOrFilter(List.of(highlightVendor)));
        }

        if (!highlightSource.equals(TransactionSource.ALL)) {
            highlightFilters.add(new SourceFilter(highlightSource));
        }

        if (!highlightType.equals(TransactionType.ALL)) {
            highlightFilters.add(new TransactionTypeFilter(highlightType));
        }
        AdvanceFilter highlightFilter = new AdvanceFilter(highlightFilters);
        StringBuilder message = new StringBuilder(CliUtils.getColoredString("Filter Result:", CliUtils.ANSI_YELLOW));
        message.append("\n").append(CsvTableUtil.getCliTable(transactionUtil.getCsv(result, columnList, highlightFilter,
                CliUtils.ANSI_CYAN, CliUtils.ANSI_YELLOW_BACKGROUND)));
        try {
            message.append("\n").append(transactionUtil.getSummaryTable(result));
        } catch (Exception e) {
            e.printStackTrace();
            return new CliSummary(CliSummary.Status.FAIL, CliUtils.getColoredString("Something went wrong. Contact developer", CliUtils.ANSI_RED));
        }
        return new CliSummary(CliSummary.Status.PASS, message.toString());
    }

    @Override
    public CliSummary validateParameters(){
        CliSummary superSummary = super.validateParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }

        //filters
        this.startDate = CliUtils.parseDate(filterParameters.get(0).getValue());
        this.endDate = CliUtils.parseEndDate(filterParameters.get(1).getValue());
        this.category = filterParameters.get(2).getValue();
        this.vendorType = filterParameters.get(3).getValue();
        this.vendor = filterParameters.get(4).getValue();
        this.descriptionString = filterParameters.get(5).getValue();
        this.source = getSource(filterParameters.get(6).getValue());
        this.type = getType(filterParameters.get(7).getValue());

        //highlights
        this.highlightCategory = highlightParameters.get(0).getValue();
        this.highlightVendorType = highlightParameters.get(1).getValue();
        this.highlightVendor = highlightParameters.get(2).getValue();
        this.highlightSource = getSource(highlightParameters.get(3).getValue());
        this.highlightType = getType(highlightParameters.get(4).getValue());

        return new CliSummary(CliSummary.Status.PASS, superSummary.getMessage());
    }

    private void loadParameters() {
        this.filterParameters = new ArrayList<>();
        filterParameters.add(new TaskParameter("Start Date", "1/22"));
        filterParameters.add(new TaskParameter("End Date","12/22"));
        filterParameters.add(new TaskParameter("Category", AppConstants.ALL_PASS_FILTER, categoryConfigSupplier.getNames()));
        filterParameters.add(new TaskParameter("Vendor Type", AppConstants.ALL_PASS_FILTER, vendorTypeConfigSupplier.getVendorTypes()));
        filterParameters.add(new TaskParameter("Vendor", AppConstants.ALL_PASS_FILTER, vendorConfigSupplier.getNames()));
        filterParameters.add(new TaskParameter("Description Containing", AppConstants.ALL_PASS_FILTER));
        filterParameters.add(new TaskParameter("Source", AppConstants.ALL_PASS_FILTER, new ArrayList<>(List.of(AppConstants.ALL_PASS_FILTER, MIDFIRST_OPTION, DISCOVER_OPTION))));
        filterParameters.add(new TaskParameter("Type", AppConstants.ALL_PASS_FILTER, new ArrayList<>(List.of(AppConstants.ALL_PASS_FILTER, "Credit", "Debit"))));
        this.parameterBatches.add(new ParameterBatch("Filters", filterParameters));

        highlightParameters = new ArrayList<>();
        highlightParameters.add(new TaskParameter("Category", AppConstants.NO_PASS_FILTER, categoryConfigSupplier.getNames()));
        highlightParameters.add(new TaskParameter("Vendor Type", AppConstants.NO_PASS_FILTER, vendorTypeConfigSupplier.getVendorTypes()));
        highlightParameters.add(new TaskParameter("Vendor", AppConstants.NO_PASS_FILTER, vendorConfigSupplier.getNames()));
        highlightParameters.add(new TaskParameter("Source", AppConstants.NO_PASS_FILTER, new ArrayList<>(List.of(AppConstants.NO_PASS_FILTER, MIDFIRST_OPTION, DISCOVER_OPTION))));
        highlightParameters.add(new TaskParameter("Type", AppConstants.NO_PASS_FILTER, new ArrayList<>(List.of(AppConstants.NO_PASS_FILTER, "Credit", "Debit"))));
        highlightParameters.add(new TaskParameter("Amount above", "10000"));
        highlightParameters.add(new TaskParameter("Amount above", "0"));
        this.parameterBatches.add(new ParameterBatch("Highlight Filters", highlightParameters));

    }

    private TransactionSource getSource(String name) {
        if (name.equals(MIDFIRST_OPTION))
            return TransactionSource.MIDFIRST;
        if (name.equals(DISCOVER_OPTION))
            return TransactionSource.DISCOVER;
        return TransactionSource.ALL;
    }

    private TransactionType getType(String type) {
        if (type.equals("Credit"))
            return TransactionType.CREDIT;
        if (type.equals("Debit"))
            return TransactionType.DEBIT;
        return TransactionType.ALL;
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
