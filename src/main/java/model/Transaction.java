package model;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class Transaction {
    @Setter
    private Date transactionDate;
    @Setter
    private double amount;
    @Setter
    private TransactionType type;
    @Setter
    private String description;
    @Setter
    private String category;
    @Setter
    private Map<String, String> additionalDetails;
    @Setter
    private TransactionSource source;
    private Set<Tag> tags;

    @Setter
    private String id;
    @Setter
    private String vendor;

    @Setter
    private String vendorType;

    @Setter
    private boolean enriched;

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

    public void addAdditionalDetails(String key, String value) {
        this.additionalDetails.put(key, value);
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
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
