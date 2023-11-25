package model;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Transaction {
    private Date transactionDate;
    private double amount;
    private TransactionType type;
    private String description;
    @Getter
    @Setter
    private String category;
    private Map<String, String> additionalDetails;
    private TransactionSource source;
    private Set<Tag> tags;

    @Getter
    @Setter
    private String id;
    private String vendor;

    @Getter
    @Setter
    private String vendorType;

    @Getter
    @Setter
    private boolean enriched;

    @Getter
    @Setter
    private List<String> enrichmentList;

    public Transaction(Date transactionDate, double amount, TransactionType type, String description, Map<String, String> additionalDetails, TransactionSource source, Set<Tag> tags, String vendor) {
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.additionalDetails = additionalDetails;
        this.source = source;
        this.tags = tags;
        this.vendor = vendor;
        this.enrichmentList = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(Map<String, String> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public void addAdditionalDetails(String key, String value) {
        this.additionalDetails.put(key, value);
    }

    public TransactionSource getSource() {
        return source;
    }

    public void setSource(TransactionSource source) {
        this.source = source;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionDate=" + transactionDate +
                ", amount=" + amount +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", source=" + source +
                ", tags=" + tags +
                ", id='" + id + '\'' +
                ", vendor='" + vendor + '\'' +
                ", vendorType='" + vendorType + '\'' +
                ", enriched=" + enriched +
                ", enrichmentList=" + enrichmentList +
                '}';
    }

    public static class Builder {
        private Date transactionDate;
        private double amount;
        private TransactionType type;
        private String description;
        private String category;
        private Map<String, String> additionalDetails = new HashMap<>();
        private TransactionSource source;
        private Set<Tag> tags = new HashSet<>();
        private String vendor;


        public Builder setTransactionDate(Date transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }
        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }
        public Builder setTransactionType(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addDetails(String key, String value) {
            this.additionalDetails.put(key, value);
            return this;
        }

        public Builder setSource(TransactionSource source) {
            this.source = source;
            return this;
        }

        public Builder addTag(Tag tag) {
            this.tags.add(tag);
            return this;
        }

        public Builder setVendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Transaction build() {
            return new Transaction(transactionDate, amount, type, description, additionalDetails, source, tags, vendor);
        }
    }
}
