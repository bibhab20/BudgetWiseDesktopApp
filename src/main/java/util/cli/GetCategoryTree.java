package util.cli;

import config.*;
import lombok.extern.slf4j.Slf4j;
import model.*;
import service.PersistenceService;
import util.CsvTableUtil;
import util.TransactionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class GetCategoryTree extends CliTask{
    List<TaskParameter> parameters;
    CategoryConfigSupplier categoryConfigSupplier;
    VendorTypeConfigSupplier vendorTypeConfigSupplier;
    PersistenceService persistenceService;
    TransactionUtil transactionUtil;
    int level;

    public GetCategoryTree(String name, CategoryConfigSupplier categoryConfigSupplier,
                           VendorTypeConfigSupplier vendorTypeConfigSupplier, PersistenceService persistenceService,
                           TransactionUtil transactionUtil) {
        super();
        this.name = name;
        this.categoryConfigSupplier = categoryConfigSupplier;
        this.vendorTypeConfigSupplier = vendorTypeConfigSupplier;
        this.persistenceService = persistenceService;
        this.transactionUtil = transactionUtil;
        loadDescription();
        loadParameters();
        log.debug("Inside GetCategoryTree Constructor and the name is: {}", this.getName());
    }

    @Override
    CliSummary run() {
        Map<String, List<VendorConfig>> vendorType2VendorMap = vendorTypeConfigSupplier.getVendorTypeConfigs();

        //Create the tree
        SimpleTreeNode root = new SimpleTreeNode("ROOT");
        for (CategoryConfig config: categoryConfigSupplier.get()) {
            SimpleTreeNode categoryNode = new SimpleTreeNode(config.getName());
            root.addChild(categoryNode);
            for (String vendorType: config.getVendorTypes()) {
                SimpleTreeNode vendorTypeNode = new SimpleTreeNode(vendorType);
                categoryNode.addChild(vendorTypeNode);
                for (VendorConfig vendor: vendorType2VendorMap.get(vendorType)) {
                    SimpleTreeNode vendorNode = new SimpleTreeNode(vendor.getName());
                    vendorTypeNode.addChild(vendorNode);
                }

            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(gethashCollisionSummary()).append("\n");
        printTreeRecursive(root, sb, "", true, level + 1);
        return new CliSummary(CliSummary.Status.PASS, sb.toString());
    }


    private void loadParameters() {
        this.parameters = new ArrayList<>();
        parameters.add(new TaskParameter("No of levels", "3"));
        this.getParameterBatches().add(new ParameterBatch("ONE", parameters));
    }

    @Override
    public CliSummary validateParameters() {
        CliSummary superSummary = super.validateParameters();
        if (superSummary.getStatus().equals(CliSummary.Status.FAIL)) {
            return superSummary;
        }
        try {
            level = Integer.parseInt(parameters.get(0).getValue());
        } catch (Exception e) {
            return new CliSummary(CliSummary.Status.FAIL, "Expecting a number");
        }
        if (level < 1  || level >3) {
            return new CliSummary(CliSummary.Status.FAIL, "The level value should be between 1 and 3 included");
        }
        return new CliSummary(CliSummary.Status.PASS, superSummary.getMessage());
    }

    private void loadDescription() {
        this.description = "This task tries to draw a shitty tree";
    }

    private void printTreeRecursive(SimpleTreeNode node, StringBuilder result, String prefix, boolean isLast, int level) {
        if (level == 0)
            return;
        result.append(prefix);
        result.append(isLast ? "+---" : "+---");

        result.append(CliUtils.getColoredString(node.getValue(), getColor(level)));
        result.append("\n");

        List<SimpleTreeNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            SimpleTreeNode child = children.get(i);
            boolean isLastChild = (i == children.size() - 1);
            String childPrefix = prefix + (isLast ? "    " : "|   ");
            printTreeRecursive(child, result, childPrefix, isLastChild, level -1);
        }
    }

    private String getColor(int level) {
        switch (level) {
            case 1:
                return CliUtils.ANSI_GREEN;
            case 2:
                return CliUtils.ANSI_MAGENTA;
            case 3:
                return CliUtils.ANSI_BLUE;
        }
        return CliUtils.ANSI_YELLOW;
    }

    private String gethashCollisionSummary() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, List<Transaction>> map = persistenceService.getHashCollisions();
        if (map.size() > 0) {
            stringBuilder.append("Hash collisions found for " + map.size() + " hash keys");
            for (String hashKey: map.keySet()) {
                stringBuilder.append("Hash Key: ").append(hashKey).append("\n");
                try {
                    stringBuilder.append(CsvTableUtil.getCliTable(transactionUtil.getCsv(map.get(hashKey), new ArrayList<>())));
                    stringBuilder.append("\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

}
