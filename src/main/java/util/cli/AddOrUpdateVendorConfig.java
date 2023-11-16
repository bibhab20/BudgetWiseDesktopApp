package util.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import config.VendorConfig;
import config.VendorTypeConfigSupplier;
import model.*;
import service.VendorProcessorService;
import util.CsvTableUtil;
import util.TransactionUtil;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddOrUpdateVendorConfig extends CliTask{
    private static final String LINE = "\n------------------------------------\n";
    private static final String STAR = "\n*****************************************************************************\n";
    private static final String TAB = "\t";
    VendorTypeConfigSupplier vendorTypeConfigSupplier;
    VendorProcessorService processorService;
    VendorConfig newConfig;
    TransactionUtil transactionUtil;
    List<TransactionUtil.TransactionProperty> columnList;
    List<TaskParameter> parameters;

    public AddOrUpdateVendorConfig(String name, VendorTypeConfigSupplier vendorTypeConfigSupplier, VendorProcessorService processorService, TransactionUtil transactionUtil) {
        this.name = name;
        this.vendorTypeConfigSupplier = vendorTypeConfigSupplier;
        this.processorService = processorService;
        this.transactionUtil = transactionUtil;
        loadParameters();
        loadColumnList();
    }

    @Override
    public String getDescription() {
        loadDescription();
        return this.description;
    }

    @Override
    CliSummary run() {
        /*
        1. Show how many new transactions matched
        2. Show collision
        3.
         */
        List<Transaction> matches = processorService.getNewTransactionMatches(newConfig);
        Map<VendorConfig, List<Transaction>> collisions = processorService.getVendorConfigCollision(newConfig);

        //create match report
        StringBuilder sb = new StringBuilder();
        sb.append(STAR);
        sb.append("New Config:");
        sb.append(LINE);
        sb.append(String.format("Name: %s | contains:(%d) %s | does not contain:(%d) %s", newConfig.getName(),
                newConfig.getContains().size(),
                CliUtils.toCommaDelimitedString(newConfig.getContains()),
                newConfig.getNotContain().size(),
                CliUtils.toCommaDelimitedString(newConfig.getNotContain())));


        //the matches
        sb.append(STAR);
        sb.append("Transactions matched with the new config:");
        sb.append(LINE);
        sb.append(CsvTableUtil.getCliTable(transactionUtil.getCustomColumnCsv(matches, columnList)));

        //the collisions
        sb.append(STAR);
        sb.append("Collisions - Vendor configs with the list of transactions that matched with new config");
        sb.append(LINE);
        if (collisions.size() == 0) {
            sb.append(LINE);
            sb.append("No collisions found");
        }
        else {
            for (VendorConfig vendorConfig: collisions.keySet()) {
                //vendor config
                sb.append("Vendor Config");
                sb.append(String.format("Name: %s | contains: %s | does not contain: %s", vendorConfig.getName(),
                        CliUtils.toCommaDelimitedString(vendorConfig.getContains()),
                        CliUtils.toCommaDelimitedString(vendorConfig.getNotContain())));
                //Matches
                sb.append(TAB);
                sb.append(LINE);
                sb.append(CsvTableUtil.getCliTable(transactionUtil.getCustomColumnCsv(collisions.get(vendorConfig), columnList)));

            }
        }
        sb.append(STAR);
        sb.append("New Vendor Config Json: \n");
        sb.append(convertVendorConfigToJson(newConfig));

        return new CliSummary(CliSummary.Status.PASS, sb.toString());


    }

    @Override
    public CliSummary validateParameters() {
        CliSummary superSummary = super.validateParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        newConfig = new VendorConfig();
        newConfig.setName(parameters.get(0).getValue());
        newConfig.setContains(CliUtils.getListFromCommaSeparatedString(parameters.get(1).getValue()));
        newConfig.setNotContain(CliUtils.getListFromCommaSeparatedString(parameters.get(2).getValue()));
        newConfig.setVendorType(parameters.get(3).getValue());
        newConfig.setTags(new ArrayList<>());
        return superSummary;

    }

    private void loadDescription() {
        StringBuilder description = new StringBuilder("Vendor Types Collection: \n" +
                CliUtils.getCliGrid(vendorTypeConfigSupplier.getVendorTypes(), 6));

        CsvTable noVendorMatchTable = transactionUtil.getCustomColumnCsv(processorService.filterByMissingVendors(), columnList);
        CsvTable multipleVendorMatchesTable = transactionUtil.getCustomColumnCsv(processorService.filterByMultipleVendorMatches(), columnList);
        description.append(String.format("%d transactions with no vendor matches: \n", noVendorMatchTable.getRows().size()));
        description.append(CsvTableUtil.getCliTable(noVendorMatchTable));
        description.append(String.format("%d transaction with MULTIPLE vendor matches: \n", multipleVendorMatchesTable.getRows().size()));
        description.append(CsvTableUtil.getCliTable(multipleVendorMatchesTable));

        this.description = description.toString();

    }
    private void loadParameters() {
        this.parameters = new ArrayList<>();
        parameters.add(new TaskParameter("Vendor Name","UNSPECIFIED"));
        parameters.add(new TaskParameter("Contains",""));
        parameters.add(new TaskParameter("Does not contain",""));
        parameters.add(new TaskParameter("Vendor Type","UNKNOWN"));
        this.parameterBatches.add(new ParameterBatch("ONE", parameters));
    }

    private void loadColumnList() {
        this.columnList = new ArrayList<>();
        columnList.add(TransactionUtil.TransactionProperty.ID);
        columnList.add(TransactionUtil.TransactionProperty.DATE);
        columnList.add(TransactionUtil.TransactionProperty.SOURCE);
        columnList.add(TransactionUtil.TransactionProperty.TYPE);
        columnList.add(TransactionUtil.TransactionProperty.AMOUNT);
        columnList.add(TransactionUtil.TransactionProperty.DESCRIPTION);
        columnList.add(TransactionUtil.TransactionProperty.VENDOR);
        columnList.add(TransactionUtil.TransactionProperty.VENDOR_MATCHES);
    }

    public String convertVendorConfigToJson(VendorConfig vendorConfig) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            // Convert the VendorConfig object to a JSON string
            String json = objectMapper.writeValueAsString(vendorConfig);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null; // Handle the exception as needed
        }
    }
}
