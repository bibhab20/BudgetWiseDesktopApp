package model;

import lombok.Getter;

@Getter
public class TransactionSummary {
    private double credit, debit, total;
    private int creditCount, debitCount, totalCount;

    private double creditPercentage, debitPercentage, totalPercentage;
    private double creditCountPercentage, debitCountPercentage, totalCountPercentage;

    public TransactionSummary() {
        this.credit = 0;
        this.debit = 0;
        this.total = 0;
        this.creditCount = 0;
        this.debitCount = 0;
        this.totalCount = 0;
    }

    public void update(Transaction transaction) {
        if (transaction.getType().equals(TransactionType.CREDIT)) {
            this.credit += transaction.getAmount();
            this.creditCount++;
            this.total += transaction.getAmount();
        } else {
            this.debit += transaction.getAmount();
            this.debitCount++;
            this.total -= transaction.getAmount();
        }
        this.totalCount++;
    }

    public void update(TransactionSummary parentSummary) {
        this.creditPercentage = getPercentage(this.credit, parentSummary.getCredit());
        this.debitPercentage = getPercentage(this.debit, parentSummary.getDebit());
        this.totalPercentage = getPercentage(this.total, parentSummary.getTotal());

        this.creditCountPercentage = getPercentage(this.creditCount, parentSummary.getCreditCount());
        this.debitCountPercentage = getPercentage(this.debitCount, parentSummary.getDebitCount());
        this.totalCountPercentage = getPercentage(this.totalCount, parentSummary.getTotalCount());
    }

    private double getPercentage(double childValue, double parentValue) {
        if (childValue == 0)
            return 0;
        return Math.abs(Math.round((childValue/parentValue) * 10000.0) / 100.0);
    }

    private double getPercentage(int childValue, int parentValue) {
        if (childValue == 0)
            return 0;
        return Math.abs(Math.round((childValue/parentValue) * 10000.0) / 100.0);
    }

}
