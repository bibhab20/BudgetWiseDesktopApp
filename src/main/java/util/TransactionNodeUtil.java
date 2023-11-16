package util;

import lombok.extern.slf4j.Slf4j;
import model.TransactionNode;
import model.TransactionSummary;
import model.TreeNode;
import util.cli.CliUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static util.TransactionSummaryUtil.SUM;

@Slf4j
public class TransactionNodeUtil {
    private static final int INDENTATION = 100;

    public static TreeNode getTrendTreeNode(TransactionNode root, TransactionSummaryUtil.TransactionSummaryProperty property) {
        if (root == null)
            return null;

        TreeNode node = new TreeNode(generateTreeName(CliUtils.getColoredString(root.getName(), CliUtils.ANSI_BLUE),
                TransactionSummaryUtil.geTrendString(root.getSummaryBucket(),property),
                TransactionSummaryUtil.getBucketStat(root.getSummaryBucket(), property),
                INDENTATION), TransactionSummaryUtil.getSummaryStat(new ArrayList<>(root.getSummaryBucket().values()), property, SUM));
        for (TransactionNode child: root.getChildren().values()) {
            node.addChild(getTrendTreeNode(child, property));
        }
        return node;
    }

    public static TreeNode getTreeNode(TransactionNode root, List<TransactionSummaryUtil.TransactionSummaryProperty> properties) {
        if (root == null)
            return null;
        String name = generateTreeName(root.getName(), TransactionSummaryUtil.getPrintString(root.getSummary(),
                properties), INDENTATION);
        if (root.getName() == null || root.getName().isBlank()) {
            log.error("root name is empty");
        }
        TreeNode node = new TreeNode(name, Math.abs(root.getSummary().getTotal()));
        for (TransactionNode child: root.getChildren().values()) {
            node.addChild(getTreeNode(child, properties));
        }
        return node;
    }

    public static boolean validateSummaryBucket(TransactionNode node, Map<String, TransactionSummary> parentBucket) {
        if (parentBucket != null && node.getSummaryBucket().size() != parentBucket.size()) {
            log.error("Parent bucket size does not match on node: {}", node.getName());
            return false;
        }
        for (TransactionNode child: node.getChildren().values()) {
            if (!validateSummaryBucket(child, node.getSummaryBucket())) {
                return false;
            }
        }
        return true;
    }

    private static String generateTreeName(String label, String trend, String stats, int indentation) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("%-" + indentation + "s", label).replace(' ', '-'));
        result.append("+");
        result.append(trend);
        result.append("------------STATS------------").append(stats);
        return result.toString();
    }

    private static String generateTreeName(String label, String stats, int indentation) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("%-" + indentation + "s", label).replace(' ', '-'));
        result.append("+");
        result.append(stats);
        return result.toString();
    }
}
