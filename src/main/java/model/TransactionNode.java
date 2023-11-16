package model;

import lombok.Getter;

import java.util.*;

@Getter
public class TransactionNode{
    String name;
    TransactionSummary summary;
    LinkedHashMap<String, TransactionSummary> summaryBucket;
    Map<String, TransactionNode> children;

    public TransactionNode(String name) {
        this.name = name;
        this.summary = new TransactionSummary();
        this.summaryBucket = new LinkedHashMap<>();
        this.children = new HashMap<>();
    }

    public TransactionNode(String name, LinkedHashMap<String, TransactionSummary> parentSummary) {
        this(name);
        this.updateSummary(parentSummary);
    }

    public void addTransaction(Transaction transaction, Queue<String> path) {
        this.summary.update(transaction);
        if (path.size() == 0)
            return;
        String childName = path.poll();
        if (!this.children.containsKey(childName)) {
            this.children.put(childName, new TransactionNode(childName));
        }
        this.children.get(childName).addTransaction(transaction, path);
        for (TransactionNode child: this.children.values()) {
            child.getSummary().update(this.summary);
        }
    }

    public void addTransaction(Transaction transaction, Queue<String> path, String bucketName) {
        TransactionSummary summary = this.summaryBucket.getOrDefault(bucketName, new TransactionSummary());
        this.summaryBucket.put(bucketName, summary);
        summary.update(transaction);
        if (path.size() == 0)
            return;
        String childName = path.poll();
        if (!this.children.containsKey(childName)) {
            this.children.put(childName, new TransactionNode(childName, this.summaryBucket));
        }
        this.children.get(childName).addTransaction(transaction, path, bucketName);
        for (TransactionNode child: this.children.values()) {
            child.updateSummary(this.summaryBucket);
        }
    }

    private void updateSummary(LinkedHashMap<String, TransactionSummary> parentBucket) {
        for (Map.Entry<String, TransactionSummary> entry : parentBucket.entrySet()) {
            TransactionSummary summary = this.summaryBucket.getOrDefault(entry.getKey(), new TransactionSummary());
            summary.update(entry.getValue());
            this.summaryBucket.put(entry.getKey(), summary);
            for (TransactionNode child: this.getChildren().values()) {
                child.updateSummary(this.getSummaryBucket());
            }
        }
    }


}
