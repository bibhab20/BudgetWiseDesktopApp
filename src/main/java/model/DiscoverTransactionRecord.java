package model;

import java.util.Date;

public class DiscoverTransactionRecord {

    private Date transactionDate;
    private Date postDate;
    private String description;
    private Double amount;
    private String Category;

    public DiscoverTransactionRecord(Date transactionDate, Date postDate, String description, Double amount, String category) {
        this.transactionDate = transactionDate;
        this.postDate = postDate;
        this.description = description;
        this.amount = amount;
        Category = category;
    }

    public DiscoverTransactionRecord() {
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

}
